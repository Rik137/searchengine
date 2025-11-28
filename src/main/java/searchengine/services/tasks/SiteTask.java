package searchengine.services.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.logs.LogTag;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.util.IndexingContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

 /**
 * Task for indexing a single site.
 *
 * <p>Saves the default version of the site, retrieves its pages, creates {@link PageTask} instances, 
 * and calculates lemma weights.
 */

@Slf4j
@RequiredArgsConstructor

public class SiteTask extends RecursiveAction {

    private static final LogTag TAG = LogTag.SITE_TASK;
    private final Site site;
    private SiteEntity siteEntity;
    private final IndexingContext context;

    @Override
    protected void compute() {

        if (site == null || site.getUrl() == null) return;

        if (context.shouldStop("SiteTask-" + site.getUrl())) return;

         siteEntity = context.getEntityFactory().createSiteEntity(site.getName(), site.getUrl());
         context.getDataManager().saveSite(siteEntity);
         context.getVisitedUrlStore().activateSite(siteEntity);

        try {
           log.info("{} Processing site: {}", TAG, site.getUrl());

            List<String> pages = context.getManagerJSOUP().getLinksFromPage(site.getUrl(), site.getUrl());

            if (context.shouldStop("SiteTask-pages-" + site.getUrl())) return;

            log.info("{} Found {} internal links on {}", TAG, pages.size(), site.getUrl());

            List<PageTask> pageTasks = pages.stream()
                    .filter(context.getVisitedUrlStore()::visitUrl)
                    .map(url -> new PageTask(url, site.getUrl(), context, siteEntity))
                    .collect(Collectors.toList());

            if (!pageTasks.isEmpty()) {
                invokeAll(pageTasks);
            }

           boolean hasFailedPages = pageTasks.stream().anyMatch(PageTask::isCompletedAbnormally);

            if (hasFailedPages) {
               failSite("One or more pages finished with errors");
           } else {
                siteEntity.setStatus(Status.INDEXED);
                siteEntity.setLastError(null);
                siteEntity.setStatusTime(LocalDateTime.now());
                context.getDataManager().saveSite(siteEntity);
                
                log.info("{} Calculating lemma weights", TAG);

                context.getLemmaFrequencyService().recalculateRankForAllSites(siteEntity);
                log.info("{} Lemma weight calculation completed", TAG);
            }

        }catch (Exception e) {
            log.error("{} Error processing site {}: {}", TAG, site.getUrl(), e.getMessage(), e);
            failSite(e.getMessage());
        }
    }

    private void failSite(String message) {
        siteEntity.setStatus(Status.FAILED);
        siteEntity.setLastError(message);
        siteEntity.setStatusTime(LocalDateTime.now());
        context.getDataManager().saveSite(siteEntity);
    }
}
