package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.PageResponse;
import searchengine.logs.LogTag;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.serviceinterfaces.PageIndexingService;
import searchengine.services.util.IndexingContext;
import searchengine.services.util.Stopwatch;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

 /*
 * Service for indexing a single page
 */

@Service
@Slf4j
@Setter
@Getter
@RequiredArgsConstructor

public class PageIndexingServiceImpl implements PageIndexingService {

    private static final LogTag TAG = LogTag.PAGE_INDEXING_SERVER;

    /** 
    * Indexing context providing access to data and lemma managers
    */
    private final IndexingContext context;


    /**
    * Indexes a page by the specified URL.
    *
    * <p>The method performs the following steps:
    * 1. Downloads the page content.
    * 2. Determines which site the URL belongs to.
    * 3. If the page already exists in the database, removes old lemmas and the page itself.
    * 4. Saves the page and recalculates lemmas.
    *
    * @param url the page URL
    * @return true if the page was successfully indexed, false if an error occurred
    */
    public boolean indexPage(String url) {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();

        PageResponse response = context.getManagerJSOUP().fetchPageWithContent(url);

        if (response.getBody() == null) {
            log.warn("{}  Failed to download content for URL {}, status code {}", 
                     TAG,  url, response.getStatusCode());
            return false;
        }

        SiteEntity siteEntity = findSiteForUrl(url);

        if (siteEntity == null) {
            log.warn("{}  Page {} does not belong to any site in the database", TAG, url);
            return false;
        }

        context.getDataManager()
                .findPathPage(url)
                .ifPresent(existingPage -> {
                    context.getLemmaFrequencyService().decreaseLemmaFrequencies(existingPage);
                    context.getDataManager().deletePage(existingPage);
                    log.info("{}  Old page {} was removed before updating", TAG, url);
                });

        savePageAndLemmas(siteEntity, url, response);
        stopwatch.stop();
        log.info("{}  Page {} successfully saved for site {}. Indexing time: {} sec.",
                TAG, url, siteEntity.getUrl(), stopwatch.getSeconds());
        stopwatch.reset();
        return true;
    }

    /**
    * Finds the site in the database to which the specified URL belongs.
    *
    * @param url the page URL
    * @return {@link SiteEntity} of the site, or null if no matching site is found
    */
    private SiteEntity findSiteForUrl(String url) {
        List<SiteEntity> sites = context.getDataManager().getAllSites();

        if (sites.isEmpty()) {
            log.warn("{}  The site database is empty! Add at least one site.", TAG);
            return null;
        }

        try {
            URL inputURL = new URL(url);
            for (SiteEntity site : sites) {
                try {
                    URL siteURL = new URL(site.getUrl());
                    if (matchesHostAndProtocol(inputURL, siteURL)) {
                        return site;
                    }
                } catch (MalformedURLException e) {
                    log.error("{} Invalid URL in the site database: {}", TAG, site.getUrl());
                }
            }
        } catch (MalformedURLException e) {
            log.error("{}  Invalid or unacceptable URL: {}", TAG, url);
        }
        return null;
    }

    /**
    * Checks if the host and protocol of the input URL match those of the site.
    *
    * @param input input URL
    * @param site  site URL
    * @return true if host and protocol match
    */
    private boolean matchesHostAndProtocol(URL input, URL site) {
        String inputHost = input.getHost().replaceFirst("^www\\.", "").toLowerCase();
        String siteHost = site.getHost().replaceFirst("^www\\.", "").toLowerCase();
        return inputHost.equals(siteHost) && input.getProtocol().equals(site.getProtocol());
    }

    /**
    * Saves the page and recalculates lemmas.
    *
    * @param site     the site to which the page belongs
    * @param path     the page URL
    * @param response the response containing the page content
    */
    private void savePageAndLemmas(SiteEntity site, String path, PageResponse response) {
        PageEntity page = context.getEntityFactory().createPageEntity(
                site,
                path,
                response.getStatusCode(),
                response.getBody()
        );
        context.getDataManager().savePage(page);
        context.getLemmaFrequencyService().savePageLemmasAndIndexes(page, response.getBody());
    }
}
