package searchengine.services.tasts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.services.ManagerRepository;
import searchengine.services.util.IndexingContext;
import searchengine.services.util.ManagerJSOUP;
import searchengine.services.util.VisitedUrlStore;

import java.util.List;
import java.util.concurrent.RecursiveAction;

@Slf4j
@RequiredArgsConstructor
public class SitesTask extends RecursiveAction {
    private final List<Site> sites;
    private final IndexingContext context;


    @Override
    protected void compute() {
        if (sites == null || sites.isEmpty()) return;

        // создаём задачи для каждого сайта
        List<SiteTask> siteTasks = sites.stream()
                .map(site -> new SiteTask(site, context))
                .toList();

        // запускаем все сайты параллельно
        invokeAll(siteTasks);
    }

}
