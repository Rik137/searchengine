package searchengine.services.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import searchengine.services.util.IndexingContext;
import java.util.List;
import java.util.concurrent.RecursiveAction;

@Slf4j
@RequiredArgsConstructor
@Transactional
public class SitesTask extends RecursiveAction {

    private final IndexingContext context;

    @Override
    protected void compute() {
        //проверка на null и пустую коллекцию сайтов
        if (context.getSites() == null || context.getSites().getSites().isEmpty()) return;
        //поверка на то что был ли использован метод stopIndexing
        if (context.shouldStop("SitesTask")) return;
        //производим удаление всех старых сайтов которые необходимо индексировать повторно
        context.getSites().getSites().stream()
                .map(site -> site.getUrl())
                .forEach(url -> context.getDataManager().deleteSite(url));
        // создаём задачи для каждого сайта
        List<SiteTask> siteTasks = context.getSites().getSites().stream()
                .map(site -> new SiteTask(site, context))
                .toList();
        // запускаем все сайты параллельно
        invokeAll(siteTasks);
    }
}
