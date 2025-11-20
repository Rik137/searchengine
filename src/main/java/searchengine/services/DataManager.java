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
        return wrapOperation(siteRepository::findAll, "Error retrieving all sites", 
                             Collections.emptyList());
    }

    /**
    * Check whether any sites exist in the database.
    *
    * @return true if at least one site is present
    */
    @Transactional(readOnly = true)
    public boolean hasSites() {
        return wrapOperation(siteRepository::hasAnySites, 
                             "Error checking for existing sites", false);
    }

    /**
    * Find a site by its URL.
    *
    * @param url the site's URL
    * @return an Optional containing the SiteEntity
    */
    @Transactional(readOnly = true)
    public Optional<SiteEntity> findSite(String url) {
        return wrapOperation(() -> siteRepository.findByUrl(url), 
                             "Error finding site with url = " + url, Optional.empty());
    }

    /**
    * Find a site by its ID.
    *
    * @param id the site's identifier
    * @return an Optional containing the SiteEntity
    */
    @Transactional(readOnly = true)
    public Optional<SiteEntity> findSite(int id) {
        return wrapOperation(() -> siteRepository.findById(id), 
                             "Error finding site with id = " + id, Optional.empty());
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
            log.debug("{} Site {} saved", TAG, site);
            return true;
        }, "Error saving site " + site, false);
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
            log.debug("{} Site with id = {} deleted", TAG, site.getId());
            return true;
        }, "Error deleting site with id =" + site.getId(), false);
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
            log.debug("{} Site with url = {} deleted", TAG, url);
            return true;
        }, "Error deleting site with url =" + url, false);
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
            log.debug("{} Site with id = {} deleted", TAG, id);
            return true;
        }, "Error deleting site with id = " + id, false);
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
        return wrapOperation(() -> pageRepository.findById(id), 
                             "Error deleting page with id =" + id, Optional.empty());
    }

    /**
    * Find a page by path.
    *
    * @param path page path
    * @return Optional PageEntity
    */
    @Transactional(readOnly = true)
    public Optional<PageEntity> findPathPage(String path) {
        return wrapOperation(() -> pageRepository.findByPath(path), 
                             "Error finding page by path" + path, Optional.empty());
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
                "Error retrieving pages of the site " + site.getId(), Collections.emptyList());
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
                "Error counting pages of the site " + site.getId(), 0);
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
            log.debug("{} pages {} saved", TAG, page);
            return true;
        }, "Error saving page " + page, false);
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
            log.debug("{} page with id = {} deleted", TAG, page.getId());
            return true;
        }, "Error deleting page with id=" + page.getId(), false);
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
            log.debug("{} page с id = {} deleted ", TAG, id);
            return true;
        }, "Error deleting page with id = " + id, false);
    }

        // ==== LEMMA METHODS ====

    /**
    * Check if there are any lemmas in the database.
    *
    * @return true if at least one lemma exists
    */
    @Transactional(readOnly = true)
    public boolean hasLemmas() {
        return wrapOperation(lemmaRepository::hasAnyLemmas, 
                             "Error checking for existing lemmas ", false);
    }
    /**
    * Find a lemma by ID.
    *
    * @param id lemma identifier
    * @return Optional LemmaEntity
    */
    @Transactional(readOnly = true)
    public Optional<LemmaEntity> findLemma(int id) {
        return wrapOperation(() -> lemmaRepository.findById(id), 
                             "Error finding lemma with id=" + id, Optional.empty());
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
                "Error finding lemma " + lemma + " on the site " + siteId, Optional.empty());
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
                "Error finding lemma " + lemma + " on the site " + site.getId(), Optional.empty());
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
                "Error finding lemma " + site.getId(), Collections.emptyList());
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
                "Error finding lemma" + names, Collections.emptyList());
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
                "Error finding lemma " + names + " on the site " + siteUrl, Collections.emptyList());
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
                "Error counting lemmas on the site " + site.getId(), 0);
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
            log.debug("{} lemma {} saved", TAG, lemma);
            return true;
        }, "Error saving lemma" + lemma, false);
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
            log.debug("{} lemma with id = {} удалена", TAG, id);
            return true;
        }, "Error deleting lemma with id = " + id, false);
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
                "Error finding index with id =" + id, Optional.empty());
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
                "Error finding indexes on th site with id =" + site.getId(),
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
            "Error counting pages on the site " + site.getId(), 0);
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
            log.debug("{} index {} saved", TAG, index);
            return true;
        }, "Error saving index " + index, false);
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
            log.debug("{} index list saved, count = {}", TAG, indexes.size());
            return true;
        }, "Error saving index list", false);
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
            log.debug("{} index with id = {} deleted", TAG, id);
            return true;
        }, "Error deleting index with id =" + id, false);
    }
}
