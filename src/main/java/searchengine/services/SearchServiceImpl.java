package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResult;
import searchengine.logs.LogTag;
import searchengine.model.Status;
import searchengine.services.serviceinterfaces.SearchService;
import searchengine.services.util.IndexingContext;
import searchengine.services.util.Stopwatch;
import java.util.List;

 /*
 * Service for searching websites by query
 */

@Service
@RequiredArgsConstructor
@Slf4j

public class SearchServiceImpl implements SearchService {

    private static final LogTag TAG = LogTag.SEARCH_SERVER;

    /**
    * Timer used to measure search execution time
    */
    private final Stopwatch stopwatch = new Stopwatch();

    /**
    * Indexing context providing access to data and the lemma service
    */
    private final IndexingContext context;

    /**
    * Checks whether the index is ready for search.
    *
    * <p>If a specific site is provided, its indexing status is verified.
    * If no site is specified, the method checks whether at least one site has been indexed.
    *
    * @param site the site URL, or null to check all sites
    * @return true if the index is ready for search; false otherwise
    */
    public boolean isIndexReady(String site){
        if (site != null) {
            if (!isSiteIndexed(site)) return false;
        } else {
            if (!hasAnySites()) return false;
        }
        log.info("{}  service is ready for search", TAG);
        return  context.getDataManager().hasLemmas();
    }

    /**
    * Performs a search in the index using the provided query.
    *
    * @param query  the search query
    * @param url    the website URL to limit the search scope (may be null)
    * @param offset the pagination offset
    * @param limit  the number of results to return
    * @return a list of search results {@link SearchResult}
    * @throws IllegalStateException if the index is not yet ready
    */
    @Override
    public List<SearchResult> search(String query, String url, int offset, int limit) throws IllegalStateException{
        if (!isIndexReady(url)) {
            throw new IllegalStateException("The index is not ready yet. Please try again later.");
        }
        log.info("{}  Search started for query '{}' on site '{}'", TAG, query, url);
        stopwatch.start();
          List<SearchResult> searchResul = context.getLemmaFrequencyService().searchResult(query, url, offset, limit);
          stopwatch.stop();
          log.info("{}  Search completed in {} seconds.", TAG, stopwatch.getSeconds());
          stopwatch.reset();
          return searchResul;
    }

    /**
    * Checks whether a specific site has been indexed.
    *
    * @param url the site URL
    * @return true if the site is indexed; false otherwise
    */
    private boolean isSiteIndexed(String url) {
        return context.getDataManager()
                .findSite(url)
                .map(site -> site.getStatus() == Status.INDEXED)
                .orElse(false);
    }

    /**
    * Checks if there is at least one indexed site.
    *
    * @return true if there is at least one site, false otherwise
    */
    private boolean hasAnySites(){
        return context.getDataManager().hasSites();
    }
}
