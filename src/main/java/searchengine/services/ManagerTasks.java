package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.logs.LogTag;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.tasks.SitesTask;
import searchengine.services.util.IndexingContext;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@Getter
@RequiredArgsConstructor

 /**
 * Indexing Task Manager
 * <p>Responsible for starting and stopping multithreaded indexing of all sites,
 * using {@link ForkJoinPool} and the root task {@link SitesTask}.
 * All necessary components for indexing are stored in {@link IndexingContext}.
 */

public class ManagerTasks {

    private static final LogTag TAG = LogTag.MANAGER_TASKS;

    /**
    * Thread pool for executing indexing tasks
    */
    private ForkJoinPool pool;

    /**
    * Indexing context containing required services and data
    */
    private final IndexingContext context;


    /**
    * Starts indexing tasks for all sites.
    */
    public void startIndexTask(){
        context.clearStopRequest();
        pool = new ForkJoinPool();
        context.getVisitedUrlStore().resetAll();

        log.info("{}  INDEXING SUCCESSFULLY STARTED...", TAG);
        try {

          pool.invoke(new SitesTask(context));
      }finally {
            log.info("{}  INDEXING COMPLETED...", TAG);
        }
    }

    /**
    * Stops the current indexing process.
    */
    public void stopIndexingTask() {
        log.info("{}  Stopping indexing: setting flag and calling shutdownNow() on the pool", TAG);
        context.requestStop();
        pool.shutdown();
        try {
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        updateSitesAfterStop();
    }

    /**
    * Updates the status of sites after a forced stop of indexing.
    * <p>All active sites are marked as FAILED with the error specified.
    */
    private void updateSitesAfterStop() {
        Collection<SiteEntity> activeSites = context.getVisitedUrlStore().getActiveSites();
        for (SiteEntity site : activeSites) {
            site.setStatus(Status.FAILED);
            site.setLastError("User stopped the indexing");
            site.setStatusTime(LocalDateTime.now());
            context.getDataManager().saveSite(site);
        }
    }
}
