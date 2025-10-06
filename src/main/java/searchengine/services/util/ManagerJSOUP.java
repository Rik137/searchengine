package searchengine.services.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.dto.PageResponse;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ManagerJSOUP {
    private final RickBotClient rickBotClient;

    // Получение всех ссылок с страницы с retry
    public List<String> getLinksFromPage(String url, String siteDomain) {
        List<String> links = new ArrayList<>();
        Document doc = null;

        String cleanUrl = url.split("#")[0].toLowerCase();
        if (cleanUrl.matches(".*\\.(jpg|jpeg|png|gif|webp|css|js|svg|ico)$")) {
            return Collections.emptyList();
        }

        try {
            doc = rickBotClient.fetchPage(url);
        } catch (IOException | InterruptedException e) {
            log.warn("Не удалось получить страницу {}: {}", url, e.getMessage());
        }

        if (doc == null) return Collections.emptyList();

        Elements elements = doc.select("a[href]");
        for (Element el : elements) {
            String absUrl = el.absUrl("href").split("#")[0];
            if (!absUrl.isBlank() && absUrl.startsWith(siteDomain)) {
                links.add(absUrl);
            }
        }
        return links;
    }


    public PageResponse fetchPageWithContent(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .ignoreHttpErrors(true)
                    .execute();

            int status = response.statusCode();
            String body = null;
            boolean isHtml = false;

            String type = response.contentType();
            if (type != null && type.contains("text/html") && status == 200) {
                body = response.body();
                isHtml = true;
            }

            return new PageResponse(status, body, isHtml);

        } catch (IOException e) {
            log.warn("Ошибка загрузки {}: {}", url, e.getMessage());
            return new PageResponse(-1, null, false);
        }
    }




    public String stripHtmlTags(String html) {
        return html == null ? "" : Jsoup.parse(html).text();
    }


    public String extractText(PageResponse response) {
        if (response == null || response.getStatusCode() != 200 || response.getBody() == null) {
            return null;
        }
        try {
            return Jsoup.parse(response.getBody()).text();
        } catch (Exception e) {
            log.warn("Не удалось извлечь текст из ответа (статус {}): {}",
                    response.getStatusCode(), e.getMessage());
            return null;
        }
    }
//    public String getTextFromPage(String url) {
//        try {
//            Document doc = rickBotClient.fetchPage(url); // используем уже существующий метод с retry
//            return doc.text(); // извлекаем чистый текст
//        } catch (IOException | InterruptedException e) {
//            log.error("Не удалось получить текст страницы {}: {}", url, e.getMessage());
//            return "";
//        }
//    }
}
