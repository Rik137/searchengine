package searchengine.services.tasts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.services.ManagerRepository;
import searchengine.services.util.IndexingContext;
import searchengine.services.util.ManagerJSOUP;
import searchengine.services.util.VisitedUrlStore;

import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SiteTask extends RecursiveAction {
    private final Site site;
    private final IndexingContext context;
    @Override
    protected void compute() {
        if (site == null || site.getUrl() == null) return;

        log.info("Обработка сайта: {}", site.getUrl());

        // Получаем страницы только внутри сайта
        List<String> pages = context.getManagerJSOUP().getLinksFromPage(site.getUrl(), site.getUrl());
        log.info("Найдено {} внутренних ссылок на {}", pages.size(), site.getUrl());
        // Создаём задачи PageTask для каждой страницы
        List<PageTask> pageTasks = pages.stream()
                .filter(context.getVisitedUrlStore()::markAsVisited) // оставляем только новые URL
                .map(url -> new PageTask(url, site.getUrl(), context))
                .collect(Collectors.toList());
        if (!pageTasks.isEmpty()) {
            invokeAll(pageTasks);
        }
    }
}
