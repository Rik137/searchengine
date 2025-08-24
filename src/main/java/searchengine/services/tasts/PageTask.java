package searchengine.services.tasts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.services.util.ManagerJSOUP;
import searchengine.services.util.VisitedUrlStore;

import java.util.List;
import java.util.concurrent.RecursiveAction;
@Slf4j
@RequiredArgsConstructor
public class PageTask extends RecursiveAction {

    private final String url;
    private final String siteDomain;
    private final ManagerJSOUP managerJSOUP;
    private final VisitedUrlStore visitedUrlStore;

    @Override
    protected void compute() {
        int statusCode = managerJSOUP.getResponseCode(url);
        log.info("Парсинг страницы: {} | Код ответа: {}", url, statusCode);
        System.out.println("URL: " + url + " | HTTP: " + statusCode);
        if (statusCode == 200) {
            List<PageTask> refs = managerJSOUP.getLinksFromPage(url, siteDomain).stream()
                    .filter(visitedUrlStore::markAsVisited) // <--- вот оно!
                    .map(link -> new PageTask(link, siteDomain, managerJSOUP, visitedUrlStore))
                    .toList();

            if (!refs.isEmpty()) {
                invokeAll(refs);
            }
        }
    }
}
