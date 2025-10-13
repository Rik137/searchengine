package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.search.SearchResult;
import searchengine.model.*;
import searchengine.repositories.LemmaRepository;
import searchengine.services.util.EntityFactory;
import searchengine.services.util.SearchBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LemmaFrequencyService {
    private final LemmaRepository lemmaRepository;

    private final DataManager dataManager;
    private final LemmaProcessor lemmaProcessor;
    private final EntityFactory entityFactory;
    private static final double PERCENT = 30.0f;

    @Transactional
    public void decreaseLemmaFrequencies(PageEntity page) {
        String content = page.getContent();
        SiteEntity site = page.getSiteEntity();
        if (content == null || content.isBlank()) {
            log.warn("Пустой контент для страницы id={}", page.getId());
            return;
        }

        Map<String, Integer> lemmas = lemmaProcessor.getLemmas(content);
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaName = entry.getKey();
            int countToRemove = entry.getValue();

            dataManager.findLemma(lemmaName, site).ifPresentOrElse(
                    lemmaEntity -> {
                        int newFrequency = lemmaEntity.getFrequency() - countToRemove;
                        lemmaEntity.setFrequency(Math.max(newFrequency, 0));

                        if (lemmaEntity.getFrequency() == 0) {
                            dataManager.deleteLemma(lemmaEntity.getId());
                            log.debug("Удалена лемма '{}'", lemmaName);
                        } else {
                            dataManager.saveLemma(lemmaEntity);
                        }
                    },
                    () -> log.debug("Лемма '{}' не найдена в БД", lemmaName)
            );
        }
    }

    @Transactional
    public void savePageLemmasAndIndexes(PageEntity page, String content) {
        if (content == null || content.isBlank()) {
            log.warn("Пустой контент, сохранение лемм пропущено для страницы id={}", page.getId());
            return;
        }
        Map<String, Integer> lemmas = lemmaProcessor.getLemmas(content);
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaName = entry.getKey();
            int frequencyToAdd = entry.getValue();

            Optional<LemmaEntity> lemmaOpt = dataManager.findLemma(
                    lemmaName,
                    page.getSiteEntity().getId()
            );

            LemmaEntity lemmaEntity;

            if (lemmaOpt.isEmpty()) {
                lemmaEntity = entityFactory.createLemmaEntity(page.getSiteEntity(), lemmaName, frequencyToAdd);
                dataManager.saveLemma(lemmaEntity);
                log.debug("Создана новая лемма '{}'", lemmaName);
            } else {
                lemmaEntity = lemmaOpt.get();
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + frequencyToAdd);
                dataManager.saveLemma(lemmaEntity);
            }
            IndexEntity index = entityFactory.createIndexEntity(page, lemmaEntity, frequencyToAdd);
            dataManager.saveIndex(index);
        }
    }

    public synchronized void savePageLemmasAndIndexesThreadSafe(PageEntity page, String content) {
        savePageLemmasAndIndexes(page, content);
    }

    @Transactional
    public void recalculateRankForAllSites(SiteEntity site) {
        int totalPages = dataManager.getCountPagesBySite(site);
        List<LemmaEntity> lemmas = dataManager.findAllLemmasBySite(site);

        for (LemmaEntity lemma : lemmas) {
            int df = dataManager.getCountPagesWhereLemma(lemma, site);
            List<IndexEntity> indexes = dataManager.getAllIndexesBySite(lemma, site);

            for (IndexEntity index : indexes) {
                float tf = index.getRank();
                float newRank = (float) (tf * Math.log((double) totalPages / (df + 1)));
                index.setRank(newRank);
            }
            dataManager.saveIndex(indexes);
        }
    }


    private List<LemmaEntity> getLemmaFromDataBase(List<String> lemmas, String url) {
        return (url == null || url.isBlank())
                ? dataManager.findLemmas(lemmas)
                : dataManager.findLemmas(lemmas, url);
    }

    public List<SearchResult> searchResult(String query, String url, int offset, int limit) {
        log.info("Поиск запроса '{}' по сайту '{}'", query, url);

        // 1️⃣ Получаем леммы для поиска
        List<String> lemmas = lemmaProcessor.getLemmasForSearch(query);
        System.out.println("леммы для запроса " + lemmas);
        if (lemmas.isEmpty()) {
            log.warn("Не найдено лемм для запроса '{}'", query);
            return List.of();
        }

        // 2️⃣ Извлекаем леммы из БД (фильтруем по сайту, если указан)
        List<LemmaEntity> lemmasEntity = getLemmaFromDataBase(lemmas, url);
        if (lemmasEntity.isEmpty()) {
            log.warn("Не найдено лемм в БД для запроса '{}'", query);
            return List.of();
        }

        // 3️⃣ Фильтруем по частоте и сортируем по возрастанию
        List<LemmaEntity> filtered = lemmasEntity.stream()
                .filter(lemma -> {
                    float totalPages = dataManager.getCountPagesBySite(lemma.getSiteEntity());
                    float onePercent = totalPages / 100.0f;
                    float lemmaPercent = lemma.getIndexEntityList().size() / onePercent;
                    return lemmaPercent <= PERCENT;
                })
                .sorted(Comparator.comparingInt(LemmaEntity::getFrequency))
                .toList();

        if (filtered.isEmpty()) {
            log.warn("После фильтрации не осталось релевантных лемм");
            return List.of();
        }

        // 4️⃣ Находим индексы страниц для лемм
        List<IndexEntity> indexes = findIndexesForAllLemmas(filtered, url);
        if (indexes.isEmpty()) {
            log.info("Поиск не дал результатов — пересечение пусто");
            return List.of();
        }

        // 5️⃣ Считаем абсолютный и относительный ранг
        Map<PageEntity, Float> absolute = calcAbsoluteRank(indexes);
        Map<PageEntity, Float> relative = calcRelativeRank(absolute, indexes, lemmas);

        // 6️⃣ Собираем результаты
        SearchBuilder builder = new SearchBuilder();
        List<SearchResult> results = builder.build(relative, offset, limit, query);

        log.info("По запросу '{}' найдено {} результатов", query, results.size());
        return results;
    }




    private List<IndexEntity> findIndexesForAllLemmas(List<LemmaEntity> lemmas, String url) {
        if (lemmas.isEmpty()) return List.of();

        if (url != null && !url.isBlank()) {
            // Пересечение страниц для конкретного сайта
            List<IndexEntity> baseIndexes = new ArrayList<>(lemmas.get(0).getIndexEntityList());
            for (int i = 1; i < lemmas.size(); i++) {
                Set<Integer> pagesWithCurrentLemma = lemmas.get(i).getIndexEntityList().stream()
                        .map(idx -> idx.getPageEntity().getId())
                        .collect(Collectors.toSet());
                baseIndexes = baseIndexes.stream()
                        .filter(idx -> pagesWithCurrentLemma.contains(idx.getPageEntity().getId()))
                        .toList();
            }
            return baseIndexes;
        } else {
            // Объединение всех страниц по леммам для поиска по всем сайтам
            return lemmas.stream()
                    .flatMap(l -> l.getIndexEntityList().stream())
                    .distinct() // убираем дубликаты
                    .toList();
        }
    }

    private Map<PageEntity, Float> calcAbsoluteRank(List<IndexEntity> indexes) {
        Map<PageEntity, Float> pageRanks = new HashMap<>();
        for (IndexEntity idx : indexes) {
            pageRanks.merge(idx.getPageEntity(), idx.getRank(), Float::sum);
        }
        log.info("Вычислена абсолютная релевантность для {} страниц", pageRanks.size());
        return pageRanks;
    }
    private Map<PageEntity, Float> calcRelativeRank(Map<PageEntity, Float> absoluteRanks, List<IndexEntity> indexes, List<String> queryLemmas) {
        if (absoluteRanks.isEmpty()) return Map.of();

        // Количество совпадений лемм на странице
        Map<PageEntity, Integer> lemmaMatches = new HashMap<>();
        for (IndexEntity idx : indexes) {
            lemmaMatches.merge(idx.getPageEntity(), 1, Integer::sum);
        }

        float maxRank = absoluteRanks.values().stream().max(Float::compare).orElse(1.0f);
        Map<PageEntity, Float> relativeRanks = new HashMap<>();

        for (Map.Entry<PageEntity, Float> entry : absoluteRanks.entrySet()) {
            PageEntity page = entry.getKey();
            float base = entry.getValue() / maxRank;

            // Вес увеличивается пропорционально совпадению лемм
            int matchCount = lemmaMatches.getOrDefault(page, 0);
            float weight = 1.0f + (matchCount / (float) queryLemmas.size());
            relativeRanks.put(page, base * weight);
        }

        // Сортировка по убыванию
        return relativeRanks.entrySet().stream()
                .sorted(Map.Entry.<PageEntity, Float>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }


}

