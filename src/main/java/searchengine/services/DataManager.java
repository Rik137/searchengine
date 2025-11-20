package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.logs.LogTag;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Manager for handling entity data: Sites, Pages, Lemmas, and Indexes.
 * <p>
 * Provides methods for searching, saving, and deleting entities, as well as checking the presence of data.
 * Error and operation logging is performed via {@link LogTag#DATA_MANAGER}.
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class DataManager {

    private static final LogTag TAG = LogTag.DATA_MANAGER;
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    
    /**
    * Unified error handler 
    */
    private <T> T wrapOperation(Supplier<T> operation, String errorMsg, T defaultValue) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("{} {}", TAG, errorMsg, e);
            return defaultValue;
        }
    }

        // ==== SITE METHODS ====

    /**
    * Retrieve all sites.
    *
    * @return list of all SiteEntity objects
    */
    @Transactional(readOnly = true)
    public List<SiteEntity> getAllSites() {
        return wrapOperation(siteRepository::findAll, "Ошибка при получении всех сайтов", Collections.emptyList());
    }

    /**
    * Check whether any sites exist in the database.
    *
    * @return true if at least one site is present
    */
    @Transactional(readOnly = true)
    public boolean hasSites() {
        return wrapOperation(siteRepository::hasAnySites, "Ошибка при проверке наличия сайтов", false);
    }

    /**
    * Find a site by its URL.
    *
    * @param url the site's URL
    * @return an Optional containing the SiteEntity
    */
    @Transactional(readOnly = true)
    public Optional<SiteEntity> findSite(String url) {
        return wrapOperation(() -> siteRepository.findByUrl(url), "Ошибка при поиске сайта с url=" + url, Optional.empty());
    }

    /**
    * Find a site by its ID.
    *
    * @param id the site's identifier
    * @return an Optional containing the SiteEntity
    */
    @Transactional(readOnly = true)
    public Optional<SiteEntity> findSite(int id) {
        return wrapOperation(() -> siteRepository.findById(id), "Ошибка при поиске сайта с id=" + id, Optional.empty());
    }

    /**
    * Save a site.
    *
    * @param site the SiteEntity object
    * @return true if the site was successfully saved
    */
    @Transactional
    public boolean saveSite(SiteEntity site) {
        return wrapOperation(() -> {
            siteRepository.save(site);
            log.debug("{} Сайт {} сохранён", TAG, site);
            return true;
        }, "Ошибка при сохранении сайта " + site, false);
    }

    /**
    * Delete a site by its object.
    *
    * @param site the SiteEntity object
    * @return true if the site was successfully deleted
    */
    @Transactional
    public boolean deleteSite(SiteEntity site) {
        return wrapOperation(() -> {
            siteRepository.delete(site);
            log.debug("{} Сайт с id={} удалён", TAG, site.getId());
            return true;
        }, "Ошибка при удалении сайта с id=" + site.getId(), false);
    }

    /**
    * Delete a site by its URL.
    *
    * @param url the site's URL
    * @return true if the site was successfully deleted
    */
    @Transactional
    public boolean deleteSite(String url) {
        return wrapOperation(() -> {
            siteRepository.deleteByUrl(url);
            log.debug("{} Сайт с url={} удалён", TAG, url);
            return true;
        }, "Ошибка при удалении сайта с url=" + url, false);
    }

    /**
    * Delete a site by its ID.
    *
    * @param id the site's identifier
    * @return true if the site was successfully deleted
    */
    @Transactional
    public boolean deleteSiteById(int id) {
        return wrapOperation(() -> {
            siteRepository.deleteById(id);
            log.debug("{} Сайт с id={} удалён", TAG, id);
            return true;
        }, "Ошибка при удалении сайта с id=" + id, false);
    }
        // ==== PAGE METHODS ====

    /**
    * Find a page by ID.
    *
    * @param id page identifier
    * @return Optional PageEntity
    */
    @Transactional(readOnly = true)
    public Optional<PageEntity> findPage(int id) {
        return wrapOperation(() -> pageRepository.findById(id), "Ошибка при поиске страницы с id=" + id, Optional.empty());
    }

    /**
    * Find a page by path.
    *
    * @param path page path
    * @return Optional PageEntity
    */
    @Transactional(readOnly = true)
    public Optional<PageEntity> findPathPage(String path) {
        return wrapOperation(() -> pageRepository.findByPath(path), "Ошибка при поиске страницы по пути " + path, Optional.empty());
    }

    /**
    * Get all pages of a site.
    *
    * @param site SiteEntity object
    * @return list of PageEntity
    */
    @Transactional(readOnly = true)
    public List<PageEntity> getAllPagesBySite(SiteEntity site) {
        return wrapOperation(() -> pageRepository.findAllBySiteEntity(site),
                "Ошибка при поиске страниц на сайте " + site.getId(), Collections.emptyList());
    }

    /**
    * Count the pages of a site.
    *
    * @param site SiteEntity object
    * @return number of pages
    */
    @Transactional(readOnly = true)
    public int getCountPagesBySite(SiteEntity site) {
        return wrapOperation(() -> pageRepository.countBySiteId(site.getId()),
                "Ошибка при подсчёте страниц сайта " + site.getId(), 0);
    }

    /**
    * Save a page.
    *
    * @param page PageEntity object
    * @return true if saved successfully
    */
    @Transactional
    public boolean savePage(PageEntity page) {
        return wrapOperation(() -> {
            pageRepository.save(page);
            log.debug("{} Страница {} сохранена", TAG, page);
            return true;
        }, "Ошибка при сохранении страницы " + page, false);
    }

    /**
    * Delete a page by object.
    *
    * @param page PageEntity object
    * @return true if deleted successfully
    */
    @Transactional
    public boolean deletePage(PageEntity page) {
        return wrapOperation(() -> {
            pageRepository.delete(page);
            log.debug("{} Страница с id={} удалена", TAG, page.getId());
            return true;
        }, "Ошибка при удалении страницы с id=" + page.getId(), false);
    }

    /**
    * Delete a page by ID.
    *
    * @param id page identifier
    * @return true if deleted successfully
    */
    @Transactional
    public boolean deletePage(int id) {
        return wrapOperation(() -> {
            pageRepository.deleteById(id);
            log.debug("{} Страница с id={} удалена", TAG, id);
            return true;
        }, "Ошибка при удалении страницы с id=" + id, false);
    }

        // ==== LEMMA METHODS ====

    /**
    * Check if there are any lemmas in the database.
    *
    * @return true if at least one lemma exists
    */
    @Transactional(readOnly = true)
    public boolean hasLemmas() {
        return wrapOperation(lemmaRepository::hasAnyLemmas, "Ошибка при проверке наличия лемм", false);
    }
    /**
    * Find a lemma by ID.
    *
    * @param id lemma identifier
    * @return Optional LemmaEntity
    */
    @Transactional(readOnly = true)
    public Optional<LemmaEntity> findLemma(int id) {
        return wrapOperation(() -> lemmaRepository.findById(id), "Ошибка при поиске леммы с id=" + id, Optional.empty());
    }

    /**
    * Find a lemma by name and site ID.
    *
    * @param lemma lemma name
    * @param siteId site ID
    * @return Optional LemmaEntity
    */
    @Transactional(readOnly = true)
    public Optional<LemmaEntity> findLemma(String lemma, int siteId) {
        return wrapOperation(() -> lemmaRepository.findByLemmaAndSiteId(lemma, siteId),
                "Ошибка при поиске леммы " + lemma + " на сайте " + siteId, Optional.empty());
    }

    /**
    * Find a lemma by name and site object.
    *
    * @param lemma lemma name
    * @param site SiteEntity object
    * @return Optional LemmaEntity
    */
    @Transactional(readOnly = true)
    public Optional<LemmaEntity> findLemma(String lemma, SiteEntity site) {
        return wrapOperation(() -> lemmaRepository.findByLemmaAndSite(lemma, site),
                "Ошибка при поиске леммы " + lemma + " на сайте " + site.getId(), Optional.empty());
    }

    /**
    * Get all lemmas of a site.
    *
    * @param site SiteEntity object
    * @return list of LemmaEntity
    */
    @Transactional(readOnly = true)
    public List<LemmaEntity> findAllLemmasBySite(SiteEntity site) {
        return wrapOperation(() -> lemmaRepository.findAllBySite(site),
                "Ошибка при поиске лемм на сайте " + site.getId(), Collections.emptyList());
    }

    /**
    * Get a list of lemmas by their names.
    *
    * @param names list of names
    * @return list of LemmaEntity
    */
    @Transactional(readOnly = true)
    public List<LemmaEntity> findLemmas(List<String> names) {
        return wrapOperation(() -> lemmaRepository.findByLemmaIn(names),
                "Ошибка при поиске лемм " + names, Collections.emptyList());
    }

    /**
    * Get a list of lemmas by names for a specific site.
    *
    * @param names list of names
    * @param siteUrl site URL
    * @return list of LemmaEntity
    */
    @Transactional(readOnly = true)
    public List<LemmaEntity> findLemmas(List<String> names, String siteUrl) {
        return wrapOperation(() -> lemmaRepository.findByLemmaInAndSiteEntity_Url(names, siteUrl),
                "Ошибка при поиске лемм " + names + " на сайте " + siteUrl, Collections.emptyList());
    }

    /**
    * Count lemmas on a site.
    *
    * @param site SiteEntity object
    * @return number of lemmas
    */
    @Transactional(readOnly = true)
    public int getCountLemmasBySite(SiteEntity site) {
        return wrapOperation(() -> lemmaRepository.countLemmasBySite(site.getId()),
                "Ошибка при подсчёте лемм на сайте " + site.getId(), 0);
    }

    /**
    * Save a lemma.
    *
    * @param lemma LemmaEntity object
    * @return true if saved successfully
    */
    @Transactional
    public boolean saveLemma(LemmaEntity lemma) {
        return wrapOperation(() -> {
            lemmaRepository.save(lemma);
            log.debug("{} Лемма {} сохранена", TAG, lemma);
            return true;
        }, "Ошибка при сохранении леммы " + lemma, false);
    }

    /**
    * Delete a lemma by ID.
    *
    * @param id lemma identifier
    * @return true if deleted successfully
    */
    @Transactional
    public boolean deleteLemma(int id) {
        return wrapOperation(() -> {
            lemmaRepository.deleteById(id);
            log.debug("{} Лемма с id={} удалена", TAG, id);
            return true;
        }, "Ошибка при удалении леммы с id=" + id, false);
    }

        // ==== INDEX METHODS ====

    /**
    * Find an index by ID.
    *
    * @param id index identifier
    * @return Optional IndexEntity
    */
    @Transactional(readOnly = true)
    public Optional<IndexEntity> findIndex(int id) {
        return wrapOperation(() -> indexRepository.findById(id),
                "Ошибка при поиске индекса с id=" + id, Optional.empty());
    }

    /**
    * Get all indexes of a lemma on a specific site.
    *
    * @param lemma LemmaEntity object
    * @param site SiteEntity object
    * @return list of IndexEntity
    */
    @Transactional(readOnly = true)
    public List<IndexEntity> getAllIndexesBySite(LemmaEntity lemma, SiteEntity site) {
        return wrapOperation(() -> indexRepository.findIndexesByLemmaAndSite(lemma, site),
                "Ошибка при поиске индексов на сайте c id=" + site.getId(),
                Collections.emptyList());
    }

    /**
    * Count the pages where a lemma appears on a site.
    *
    * @param lemma LemmaEntity object
    * @param site SiteEntity object
    * @return number of pages
    */
    @Transactional(readOnly = true)
    public int getCountPagesWhereLemma(LemmaEntity lemma, SiteEntity site) {
        return wrapOperation(() -> indexRepository.countPagesContainingLemma(lemma, site),
                "Ошибка при подсчёте страниц на сайте " + site.getId(), 0);
    }

    /**
    * Save a single index.
    *
    * @param index IndexEntity object
    * @return true if saved successfully
    */
    @Transactional
    public boolean saveIndex(IndexEntity index) {
        return wrapOperation(() -> {
            indexRepository.save(index);
            log.debug("{} Индекс {} сохранён", TAG, index);
            return true;
        }, "Ошибка при сохранении индекса " + index, false);
    }

    /**
    * Save a list of indexes.
    *
    * @param indexes list of IndexEntity
    * @return true if saved successfully
    */
    @Transactional
    public boolean saveIndex(List<IndexEntity> indexes) {
        return wrapOperation(() -> {
            indexRepository.saveAll(indexes);
            log.debug("{} Список индексов сохранён, count={}", TAG, indexes.size());
            return true;
        }, "Ошибка при сохранении списка индексов", false);
    }

    /**
    * Delete an index by ID.
    *
    * @param id index identifier
    * @return true if deleted successfully
    */
    @Transactional
    public boolean deleteIndex(int id) {
        return wrapOperation(() -> {
            indexRepository.deleteById(id);
            log.debug("{} Индекс с id={} удалён", TAG, id);
            return true;
        }, "Ошибка при удалении индекса с id=" + id, false);
    }
}
