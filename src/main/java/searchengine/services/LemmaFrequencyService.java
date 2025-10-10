package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.SearchResult;
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

    private final ManagerRepository managerRepository;
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

            managerRepository.findLemma(lemmaName, site).ifPresentOrElse(
                    lemmaEntity -> {
                        int newFrequency = lemmaEntity.getFrequency() - countToRemove;
                        lemmaEntity.setFrequency(Math.max(newFrequency, 0));

                        if (lemmaEntity.getFrequency() == 0) {
                            managerRepository.deleteLemma(lemmaEntity.getId());
                            log.debug("Удалена лемма '{}'", lemmaName);
                        } else {
                            managerRepository.saveLemma(lemmaEntity);
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

            Optional<LemmaEntity> lemmaOpt = managerRepository.findLemma(
                    lemmaName,
                    page.getSiteEntity().getId()
            );

            LemmaEntity lemmaEntity;

            if (lemmaOpt.isEmpty()) {
                lemmaEntity = entityFactory.createLemmaEntity(page.getSiteEntity(), lemmaName, frequencyToAdd);
                managerRepository.saveLemma(lemmaEntity);
                log.debug("Создана новая лемма '{}'", lemmaName);
            } else {
                lemmaEntity = lemmaOpt.get();
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + frequencyToAdd);
                managerRepository.saveLemma(lemmaEntity);
            }
            IndexEntity index = entityFactory.createIndexEntity(page, lemmaEntity, frequencyToAdd);
            managerRepository.saveIndex(index);
        }
    }

    public synchronized void savePageLemmasAndIndexesThreadSafe(PageEntity page, String content) {
        savePageLemmasAndIndexes(page, content);
    }

    @Transactional
    public void recalculateRankForAllSites(SiteEntity site) {
        int totalPages = managerRepository.getCountPagesBySite(site);
        List<LemmaEntity> lemmas = managerRepository.findAllLemmasBySite(site);

        for (LemmaEntity lemma : lemmas) {
            int df = managerRepository.getCountPagesWhereLemma(lemma, site);
            List<IndexEntity> indexes = managerRepository.getAllIndexesBySite(lemma, site);

            for (IndexEntity index : indexes) {
                float tf = index.getRank();
                float newRank = (float) (tf * Math.log((double) totalPages / (df + 1)));
                index.setRank(newRank);
            }
            managerRepository.saveIndex(indexes);
        }
    }


    private List<LemmaEntity> getLemmaFromDataBase(List<String> lemmas, String url) {
        return (url == null || url.isBlank())
                ? managerRepository.findLemmas(lemmas)
                : managerRepository.findLemmas(lemmas, url);
    }

    public List<SearchResult> searchResult(String query, String url, int offset, int limit) {
        log.info("Поиск запроса '{}' по сайту '{}'", query, url);

        // 1️⃣ Получаем леммы для поиска
        List<String> lemmas = lemmaProcessor.getLemmasForSearch(query);
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

        // 3️⃣ Отфильтровываем “шумные” леммы (частые)
        List<LemmaEntity> filtered = calculateSiteRank(lemmasEntity);
        if (filtered.isEmpty()) {
            log.warn("После фильтрации не осталось релевантных лемм");
            return List.of();
        }

        // 4️⃣ Находим пересечение страниц, где встречаются все леммы
        List<IndexEntity> indexes = findIndexesForAllLemmas(filtered);
        if (indexes.isEmpty()) {
            log.info("Поиск не дал результатов — пересечение пусто");
            return List.of();
        }

        // 5️⃣ Считаем абсолютный и относительный ранг
        Map<PageEntity, Float> absolute = calcAbsoluteRank(indexes);
        Map<PageEntity, Float> relative = calcRelativeRank(absolute);

        // 6️⃣ Собираем результаты
        SearchBuilder builder = new SearchBuilder();
        List<SearchResult> results = builder.build(relative, offset, limit, query);

        log.info("По запросу '{}' найдено {} результатов", query, results.size());
        return results;
    }

    public List<LemmaEntity> calculateSiteRank(List<LemmaEntity> lemmas) {
        return lemmas.stream()
                .filter(lemma -> {
                    float totalPages = managerRepository.getCountPagesBySite(lemma.getSiteEntity());
                    float onePercent = totalPages / 100.0f;
                    float lemmaPercent = lemma.getIndexEntityList().size() / onePercent;
                    return lemmaPercent <= PERCENT; // фильтр по весу
                })
                .sorted(Comparator.comparingInt(LemmaEntity::getFrequency)) // от меньшего к большему
                .toList(); // Java 16+; если ниже — .collect(Collectors.toList())
    }

    private List<LemmaEntity> filterAndSortLemmas(List<LemmaEntity> lemmas) {
        return lemmas.stream().sorted(Comparator.comparingInt(LemmaEntity::getFrequency)).collect(Collectors.toList());
    }

    private List<IndexEntity> findIndexesForAllLemmas(List<LemmaEntity> lemmas) {
        if (lemmas.isEmpty()) return List.of();
        // 1️⃣ Начинаем с индексов первой леммы
        List<IndexEntity> baseIndexes = new ArrayList<>(lemmas.get(0).getIndexEntityList());

        // 2️⃣ Пересекаем по страницам для всех последующих лемм
        for (int i = 1; i < lemmas.size(); i++) {
            Set<Integer> pagesWithCurrentLemma = lemmas.get(i).getIndexEntityList().stream()
                    .map(idx -> idx.getPageEntity().getId())
                    .collect(Collectors.toSet());
            // оставляем только те индексы, где страница присутствует и в текущей лемме
            baseIndexes = baseIndexes.stream()
                    .filter(idx -> pagesWithCurrentLemma.contains(idx.getPageEntity().getId()))
                    .toList();
        }
        return baseIndexes;
    }

    private Map<PageEntity, Float> calcAbsoluteRank(List<IndexEntity> indexes) {
        Map<PageEntity, Float> pageRanks = new HashMap<>();
        for (IndexEntity index : indexes) {
            PageEntity page = index.getPageEntity();
            pageRanks.merge(page, index.getRank(), Float::sum);
        }

        log.debug("Вычислена абсолютная релевантность для {} страниц", pageRanks.size());
        return pageRanks;
    }

    private Map<PageEntity, Float> calcRelativeRank(Map<PageEntity, Float> absoluteRanks) {
        if (absoluteRanks.isEmpty()) {
            return Map.of();
        }

        float maxRank = absoluteRanks.values().stream()
                .max(Float::compare)
                .orElse(1.0f);

        Map<PageEntity, Float> relativeRanks = absoluteRanks.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue() / maxRank));

        log.debug("Рассчитана относительная релевантность. Максимальный ранг = {}", maxRank);
        return relativeRanks;
    }
}

