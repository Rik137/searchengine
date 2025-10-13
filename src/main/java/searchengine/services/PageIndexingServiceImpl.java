package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.PageResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.serviceinterfaces.PageIndexingService;
import searchengine.services.util.IndexingContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Service
@Slf4j
@Setter
@Getter
@RequiredArgsConstructor
public class PageIndexingServiceImpl implements PageIndexingService {
    private final IndexingContext context;

    public boolean indexPage(String url) {
        PageResponse response = context.getManagerJSOUP().fetchPageWithContent(url);

        if (response.getBody() == null) {
            log.warn("Не удалось скачать контент для URL {}, код {}", url, response.getStatusCode());
            return false;
        }
        SiteEntity siteEntity = findSiteForUrl(url);

        if (siteEntity == null) {
            log.warn("Страница {} не принадлежит ни одному сайту из БД", url);
            return false;
        }
        // Если страница уже есть — удаляем её и связанные леммы/индексы
        context.getDataManager()
                .findPathPage(url)
                .ifPresent(existingPage -> {
                    context.getLemmaFrequencyService().decreaseLemmaFrequencies(existingPage);
                    context.getDataManager().deletePage(existingPage);
                    //вызов подсчет и сохранение лемм
                    log.info("Старая страница {} удалена перед обновлением", url);
                });
        savePageAndLemmas(siteEntity, url, response);
        log.info("Страница {} успешно сохранена для сайта {}", url, siteEntity.getUrl());
        return true;
    }

    private SiteEntity findSiteForUrl(String url) {
        List<SiteEntity> sites = context.getDataManager().getAllSites();
        if (sites.isEmpty()) {
            log.warn("База сайтов пуста! Добавьте хотя бы один сайт.");
            return null;
        }
        try {
            URL inputURL = new URL(url);
            for (SiteEntity site : sites) {
                try {
                    URL siteURL = new URL(site.getUrl());
                    if (matchesHostAndProtocol(inputURL, siteURL)) {
                        return site;
                    }
                } catch (MalformedURLException e) {
                    log.error("Неверный URL в базе сайтов: {}", site.getUrl());
                }
            }
        } catch (MalformedURLException e) {
            log.error("Неверный или недопустимый URL: {}", url);
        }
        return null;
    }
    private boolean matchesHostAndProtocol(URL input, URL site) {
        String inputHost = input.getHost().replaceFirst("^www\\.", "").toLowerCase();
        String siteHost = site.getHost().replaceFirst("^www\\.", "").toLowerCase();
        return inputHost.equals(siteHost) && input.getProtocol().equals(site.getProtocol());
    }

    private void savePageAndLemmas(SiteEntity site, String path, PageResponse response) {
        PageEntity page = context.getEntityFactory().createPageEntity(
                site,
                path,
                response.getStatusCode(),
                response.getBody()
        );
        context.getDataManager().savePage(page);
        context.getLemmaFrequencyService().savePageLemmasAndIndexes(page, response.getBody());
    }
}
