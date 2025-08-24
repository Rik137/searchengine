package searchengine.services.tasts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.services.util.ManagerJSOUP;
import searchengine.services.util.VisitedUrlStore;

import java.util.List;
import java.util.concurrent.RecursiveAction;

@Slf4j
@RequiredArgsConstructor
public class SitesTask extends RecursiveAction {
    private final List<Site> sites;
    private final ManagerJSOUP managerJSOUP;
    private final VisitedUrlStore visitedUrlStore;


    @Override
    protected void compute() {
        if (sites == null || sites.isEmpty()) return;

        // создаём задачи для каждого сайта
        List<SiteTask> siteTasks = sites.stream()
                .map(site -> new SiteTask(site, managerJSOUP, visitedUrlStore))
                .toList();

        // запускаем все сайты параллельно
        invokeAll(siteTasks);
    }

}
