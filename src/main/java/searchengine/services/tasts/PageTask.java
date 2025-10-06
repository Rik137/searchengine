package searchengine.services.tasts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.dto.PageResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.util.IndexingContext;
import java.util.List;
import java.util.concurrent.RecursiveAction;

@Slf4j
@RequiredArgsConstructor
public class PageTask extends RecursiveAction {

    private final String url;
    private final String siteDomain;
    private final IndexingContext context;
    private final SiteEntity siteEntity;

    @Override
    protected void compute() {

        if (context.shouldStop("PageTask-" + url)) return;

        try {
            PageResponse resp = context.getManagerJSOUP().fetchPageWithContent(url);
            String htmlBody = (resp.getBody() != null) ? resp.getBody() : "<html><body></body></html>";
            // сохраняем весь HTML в базе
            PageEntity page = context.getEntityFactory().createPageEntity(
                    siteEntity,
                    url,
                    resp.getStatusCode(),
                    htmlBody
            );
            context.getManagerRepository().savePage(page);


                context.getLemmaFrequencyService().savePageLemmasAndIndexesThreadSafe(page, page.getContent());

            if (resp.isHtml() && resp.getBody() != null) {
                List<PageTask> refs = context.getManagerJSOUP()
                        .getLinksFromPage(url, siteDomain)
                        .stream()
                        .filter(context.getVisitedUrlStore()::markAsVisited)
                        .map(link -> new PageTask(link, siteDomain, context, siteEntity))
                        .toList();

                if (!refs.isEmpty()) invokeAll(refs);
            }

        } catch (Exception e) {
            log.error("Ошибка при обработке страницы {} возможно это изображение", url, e);
        }

    }
}
