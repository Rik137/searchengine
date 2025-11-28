package searchengine.services;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.logs.LogTag;
import searchengine.services.serviceinterfaces.IndexingService;
import searchengine.services.util.Stopwatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of the site indexing service.
 *
 * <p>Responsible for starting and stopping the indexing process.
 * Uses {@link ManagerTasks} to manage crawling and page-processing tasks.
 * {@link Stopwatch} is used to measure the execution time of the full indexing run.
 */

@Slf4j
@Setter
@Service
@RequiredArgsConstructor

public class IndexingServiceImpl implements IndexingService {

    private static final LogTag TAG = LogTag.INDEXING_SERVER;

    /** 
    * Flag indicating the indexing state (true — indexing is in progress)
    */
    private volatile boolean statusIndexing = false;

    /** 
    * Task manager responsible for starting and stopping indexing processes
    */
    private final ManagerTasks managerTasks;

    /** 
    * Timer used to measure indexing duration
    */
    private Stopwatch stopwatch = new Stopwatch();

    /** 
    * Thread pool for asynchronous task execution
    */
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
    * Checks whether indexing is currently running.
    *
    * @return true if indexing is active, otherwise false
    */
    public boolean isIndexing(){
        return statusIndexing;
    }

   /*
   * Starts the indexing process for all sites.
   */
   @Override
   public void startIndexing() {
        if (isIndexing()) {
            log.warn("{} Indexing is already running", TAG);
            return;
        }
        if (executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newSingleThreadExecutor();
        }
        setStatusIndexing(true);
        executor.submit(() -> {
            try {
                stopwatch.start();
                managerTasks.startIndexTask();
            } catch (Exception e) {
                log.error("{} Error during indexing", TAG, e);
            } finally {
                stopwatch.stop();
                stopwatch.reset();
                setStatusIndexing(false);
            }
        });
    }
    
   /**
   * Stops the current indexing process.
   */
   @Override
    public void stopIndexing() {
        try {
            managerTasks.stopIndexingTask();
        } finally {
            setStatusIndexing(false);
            stopwatch.stop();
            stopwatch.reset();
        }
    }
}
