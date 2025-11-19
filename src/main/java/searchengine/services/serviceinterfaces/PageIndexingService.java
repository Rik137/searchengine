package searchengine.services.serviceinterfaces;

/**
 * Service for indexing individual pages by URL.
 */

public interface PageIndexingService {

    /**
    * Indexes a page by the specified URL.
    * @param url the URL of the page to index
    * @return true if the page was successfully indexed, false otherwise
    */
    boolean indexPage(String url);
}
