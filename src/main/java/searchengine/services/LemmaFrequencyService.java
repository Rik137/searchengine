package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.search.SearchResult;
import searchengine.logs.LogTag;
import searchengine.model.*;
import searchengine.services.util.EntityFactory;
import searchengine.services.util.SearchBuilder;
import java.util.*;
import java.util.stream.Collectors;

 /**
 * Service for managing lemmas and page indexes.
 *
 * <p>Main responsibilities:
 * <ul>
 *   <li>Saves lemmas and indexes for a page.</li>
 *   <li>Decreases lemma frequency when a page is removed or updated.</li>
 *   <li>Recalculates the relative weight (rank) of lemmas for search relevance.</li>
 *   <li>Searches for pages based on a query, considering lemma frequencies and intersections.</li>
 * </ul>
 *
 * <p>Uses {@link LemmaProcessor} for lemma generation,
 * {@link DataManager} for database operations, and {@link EntityFactory} for entity creation.
 */

@Service
@Slf4j
@RequiredArgsConstructor

public class LemmaFrequencyService {

    private static final LogTag TAG = LogTag.LEMMA_FREQUENCY_SERVER;
    private final DataManager dataManager;
    private final LemmaProcessor lemmaProcessor;
    private final EntityFactory entityFactory;
    private static final double PERCENT = 30.0f;

    /**
    * Decreases the frequencies of all lemmas present on the page.
    *
    * <p>Used when a page is deleted or updated to keep lemma frequencies
    * in the database consistent.
    *
    * @param page the page for which lemma frequencies should be decreased
    */
    @Transactional
    public void decreaseLemmaFrequencies(PageEntity page) {
        String content = page.getContent();
        SiteEntity site = page.getSiteEntity();
        if (content == null || content.isBlank()) {
            log.warn("{}  Пустой контент для страницы id={}", TAG, page.getId());
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
                            log.debug("{}  Удалена лемма '{}'", TAG, lemmaName);
                        } else {
                            dataManager.saveLemma(lemmaEntity);
                        }
                    },
                    () -> log.debug("{}  Лемма '{}' не найдена в БД", TAG, lemmaName)
            );
        }
    }

    /**
    * Thread-safe version of {@link #savePageLemmasAndIndexes(PageEntity, String)}.
    *
    * @param page the page to be indexed
    * @param content the page content
    */
    @Transactional
    public void savePageLemmasAndIndexes(PageEntity page, String content) {
        if (content == null || content.isBlank()) {
            log.warn("{}  Пустой контент, сохранение лемм пропущено для страницы id={}", TAG, page.getId());
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
                log.debug("{}  Создана новая лемма '{}'", TAG, lemmaName);
            } else {
                lemmaEntity = lemmaOpt.get();
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + frequencyToAdd);
                dataManager.saveLemma(lemmaEntity);
            }
            IndexEntity index = entityFactory.createIndexEntity(page, lemmaEntity, frequencyToAdd);
            dataManager.saveIndex(index);
        }
    }

    /**
    * Thread-safe version of {@link #savePageLemmasAndIndexes(PageEntity, String)}.
    *
    * @param page the page to be indexed
    * @param content the page content
    */
    public synchronized void savePageLemmasAndIndexesThreadSafe(PageEntity page, String content) {
        savePageLemmasAndIndexes(page, content);
    }

    /**
    * Recalculates the weight (rank) of lemmas for all pages of the site.
    *
    * <p>Used after full site indexing or when recalculating relevance.
    *
    * @param site the site for which lemma ranks should be recalculated
    */
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


    /**
    * Retrieves lemmas from the database.
    * <p>If a URL is provided, filters by site/page; otherwise returns all matching lemmas.
    *
    * @param lemmas list of lemmas to search for
    * @param url the site or specific page (can be null)
    * @return list of lemma entities from the database
    */
    private List<LemmaEntity> getLemmaFromDataBase(List<String> lemmas, String url) {
        return (url == null || url.isBlank())
                ? dataManager.findLemmas(lemmas)
                : dataManager.findLemmas(lemmas, url);
    }

    /**
    * Performs a search for pages by query.
    *
    * @param query the search query
    * @param url the site (if null — search across all sites)
    * @param offset pagination offset
    * @param limit maximum number of results
    * @return list of {@link SearchResult} with relevant pages
    */
    public List<SearchResult> searchResult(String query, String url, int offset, int limit) {
        log.info("{}  Поиск запроса '{}' по сайту '{}'", TAG, query, url);

        List<String> lemmas = lemmaProcessor.getLemmasForSearch(query);
        System.out.println("леммы для запроса " + lemmas);
        if (lemmas.isEmpty()) {
            log.warn("{}  Не найдено лемм для запроса '{}'", TAG, query);
            return List.of();
        }

        List<LemmaEntity> lemmasEntity = getLemmaFromDataBase(lemmas, url);
        if (lemmasEntity.isEmpty()) {
            log.warn("{}  Не найдено лемм в БД для запроса '{}'", TAG, query);
            return List.of();
        }


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
            log.warn("{}  После фильтрации не осталось релевантных лемм", TAG);
            return List.of();
        }

        List<IndexEntity> indexes = findIndexesForAllLemmas(filtered, url);
        if (indexes.isEmpty()) {
            log.info("{}  Поиск не дал результатов — пересечение пусто", TAG);
            return List.of();
        }

        Map<PageEntity, Float> absolute = calcAbsoluteRank(indexes);
        Map<PageEntity, Float> relative = calcRelativeRank(absolute, indexes, lemmas);
        SearchBuilder builder = new SearchBuilder();
        List<SearchResult> results = builder.build(relative, offset, limit, query);

        log.info("{}  По запросу '{}' найдено {} результатов", TAG, query, results.size());
        return results;
    }

    /**
    * Finds page indexes for all lemmas.
    * <p>If a URL is provided, intersects pages by lemmas for a single site; otherwise merges all pages by lemmas.
    *
    * @param lemmas list of lemmas
    * @param url the site or null
    * @return list of page indexes
    */
    private List<IndexEntity> findIndexesForAllLemmas(List<LemmaEntity> lemmas, String url) {
        if (lemmas.isEmpty()) return List.of();

        if (url != null && !url.isBlank()) {
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
            return lemmas.stream()
                    .flatMap(l -> l.getIndexEntityList().stream())
                    .distinct() // убираем дубликаты
                    .toList();
        }
    }

    /**
    * Calculates the absolute rank of pages.
    * <p>Sums the weights of all page indexes.
    *
    * @param indexes list of indexes
    * @return map of pages and their absolute ranks
    */
    private Map<PageEntity, Float> calcAbsoluteRank(List<IndexEntity> indexes) {
        Map<PageEntity, Float> pageRanks = new HashMap<>();
        for (IndexEntity idx : indexes) {
            pageRanks.merge(idx.getPageEntity(), idx.getRank(), Float::sum);
        }
        log.info("{}  Вычислена абсолютная релевантность для {} страниц", TAG, pageRanks.size());
        return pageRanks;
    }

    /**
    * Calculates the relative rank of pages.
    * <p>The adjusted rank considers the frequency of matching lemmas in the query and is normalized by the maximum value.
    *
    * @param absoluteRanks map of pages and their absolute ranks
    * @param indexes list of indexes
    * @param queryLemmas list of lemmas from the search query
    * @return map of pages and their relative ranks, sorted in descending order
    */
    private Map<PageEntity, Float> calcRelativeRank(Map<PageEntity, Float> absoluteRanks, List<IndexEntity> indexes, List<String> queryLemmas) {
        if (absoluteRanks.isEmpty()) return Map.of();
        Map<PageEntity, Integer> lemmaMatches = new HashMap<>();
        for (IndexEntity idx : indexes) {
            lemmaMatches.merge(idx.getPageEntity(), 1, Integer::sum);
        }

        float maxRank = absoluteRanks.values().stream().max(Float::compare).orElse(1.0f);
        Map<PageEntity, Float> relativeRanks = new HashMap<>();

        for (Map.Entry<PageEntity, Float> entry : absoluteRanks.entrySet()) {
            PageEntity page = entry.getKey();
            float base = entry.getValue() / maxRank;
            int matchCount = lemmaMatches.getOrDefault(page, 0);
            float weight = 1.0f + (matchCount / (float) queryLemmas.size());
            relativeRanks.put(page, base * weight);
        }

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

