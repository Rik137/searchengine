package searchengine.services.serviceinterfaces;

 /**
 * Service for managing the website indexing process.
 * <p>Defines methods for starting and stopping indexing.</p>
 */

public interface IndexingService {

    /**
    * Starts the indexing process for all sites.
    */
    void startIndexing();

    /**
    * Stops the current indexing process.
    */
    void stopIndexing();
}
