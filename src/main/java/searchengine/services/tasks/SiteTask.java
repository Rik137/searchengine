package searchengine.services.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.util.IndexingContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SiteTask extends RecursiveAction {
    private final Site site;
    private SiteEntity siteEntity;
    private final IndexingContext context;

    @Override
    protected void compute() {

        if (site == null || site.getUrl() == null) return;
        if (context.shouldStop("SiteTask-" + site.getUrl())) return;
        // Сохранение дефолтного варианта сайта
         siteEntity = context.getEntityFactory().createSiteEntity(site.getName(), site.getUrl());
         context.getDataManager().saveSite(siteEntity);
         context.getVisitedUrlStore().activateSite(siteEntity);

        try {
            log.info("Обработка сайта: {}", site.getUrl());
            // Получаем страницы только внутри сайта
            List<String> pages = context.getManagerJSOUP().getLinksFromPage(site.getUrl(), site.getUrl());

            if (context.shouldStop("SiteTask-pages-" + site.getUrl())) return;

            log.info("Найдено {} внутренних ссылок на {}", pages.size(), site.getUrl());
            // Создаём задачи PageTask для каждой страницы
            List<PageTask> pageTasks = pages.stream()
                    .filter(context.getVisitedUrlStore()::visitUrl)
                    .map(url -> new PageTask(url, site.getUrl(), context, siteEntity))
                    .collect(Collectors.toList());

            if (!pageTasks.isEmpty()) {
                invokeAll(pageTasks);
            }
           boolean hasFailedPages = pageTasks.stream().anyMatch(PageTask::isCompletedAbnormally);

            if (hasFailedPages) {
                failSite("Одна или несколько страниц завершились с ошибкой");
           } else {
                siteEntity.setStatus(Status.INDEXED);
                siteEntity.setLastError(null);
                siteEntity.setStatusTime(LocalDateTime.now());
                context.getDataManager().saveSite(siteEntity);
                log.info("идет подсчет веса лемм");
                context.getLemmaFrequencyService().recalculateRankForAllSites(siteEntity);
                log.info("подсчет веса лемм завершен");
            }

        }catch (Exception e) {
            log.error("Ошибка при обработке сайта {}: {}", site.getUrl(), e.getMessage(), e);
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
