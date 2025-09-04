package searchengine.services.tasts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.services.ManagerRepository;
import searchengine.services.util.IndexingContext;
import searchengine.services.util.ManagerJSOUP;
import searchengine.services.util.VisitedUrlStore;

import java.util.List;
import java.util.concurrent.RecursiveAction;
@Slf4j
@RequiredArgsConstructor
public class PageTask extends RecursiveAction {

    private final String url;
    private final String siteDomain;
    private final IndexingContext context;

    @Override
    protected void compute() {
        int statusCode = context.getManagerJSOUP().getResponseCode(url);
        log.info("Парсинг страницы: {} | Код ответа: {}", url, statusCode);
        System.out.println("URL: " + url + " | HTTP: " + statusCode);
        if (statusCode == 200) {
            List<PageTask> refs = context.getManagerJSOUP().getLinksFromPage(url, siteDomain).stream()
                    .filter(context.getVisitedUrlStore()::markAsVisited) // <--- вот оно!
                    .map(link -> new PageTask(link, siteDomain, context))
                    .toList();

            if (!refs.isEmpty()) {
                invokeAll(refs);
            }
        }
    }
}
