package searchengine.services.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;
import searchengine.logs.LogTag;
import searchengine.services.LemmaFrequencyService;
import searchengine.services.LemmaProcessor;
import searchengine.services.DataManager;

 /**
 * Context for page indexing processes.
 * <p>Contains all the necessary components for crawling websites, processing pages, and handling lemmas.
 * Provides functionality for managing the process state (stop/check-stop).
 *
 * <p>Components used:
 * <ul>
 *   <li>{@link SitesList} — list of sites to index</li>
 *   <li>{@link EntityFactory} — entity creation for the database</li>
 *   <li>{@link DataManager} — database operations</li>
 *   <li>{@link ManagerJSOUP} — extraction of links and text from pages</li>
 *   <li>{@link LemmaProcessor} — text processing and lemma generation</li>
 *   <li>{@link VisitedUrlStore} — storage of visited URLs</li>
 *   <li>{@link LemmaFrequencyService} — handling lemma frequencies and indexes</li>
 * </ul>
 */

@Component
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j

public class IndexingContext {

    private static final LogTag TAG = LogTag.INDEXING_CONTEXT;

    /**
    * List of sites to crawl
    */
    private final SitesList sites;

    /**
    * Entity factory for saving data to the database
    */
    private final EntityFactory entityFactory;

    /**
    * Database manager
    */
    private final DataManager dataManager;

    /**
    * Component for fetching pages and extracting text
    */
    private final ManagerJSOUP managerJSOUP;

    /**
    * Lemma processor for page text
    */
    private final LemmaProcessor lemmaProcessor;

     /**
     * Store for visited URLs
     */
    private final VisitedUrlStore visitedUrlStore;

     /**
     * Service for handling lemmas and indexes (lazy initialization)
     */
    private final @Lazy LemmaFrequencyService lemmaFrequencyService;

     /**
     * Flag indicating a request to stop the indexing process
     */
    private volatile boolean stopRequested = false;

     /**
     * Requests stopping the current indexing process.
     * <p>Sets {@link #stopRequested} to true.
     */
    public void requestStop() {
        this.stopRequested = true;
    }


    /**
     * Resets the stop request.
     * <p>Used to resume the process after it has been stopped.
     */
     public void clearStopRequest() {
        this.stopRequested = false;
    }


     /**
     * Checks if the process should stop.
     * <p>Returns true if:
     * <ul>
     *   <li>{@link #stopRequested} has been set</li>
     *   <li>The current thread has been interrupted</li>
     * </ul>
     * <p>If stopping is required, logs information about the task interruption.
     *
     * @param taskName the name of the current task (for logging)
     * @return true if execution should be halted, false otherwise
     */
     public boolean shouldStop(String taskName) {
        boolean stop = stopRequested || Thread.currentThread().isInterrupted();
        if (stop) {
            log.info("{}  Stopping task {}: execution interrupted", TAG, taskName);
        }
        return stop;
    }
}
