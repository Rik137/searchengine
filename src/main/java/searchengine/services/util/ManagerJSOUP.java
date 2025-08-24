package searchengine.services.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

        // до 3 попыток с экспоненциальной задержкой + небольшой рандом
        for (int i = 0; i < 3; i++) {
            try {
                if (i > 0) {
                    long delay = 200L * (long) Math.pow(2, i - 1); // 200, 400, 800 ms
                    Thread.sleep(delay);
                }

                doc = rickBotClient.fetchPage(url);
                break;

            } catch (IOException e) {
                log.warn("Попытка {} не удалась для {}: {}", i + 1, url, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Задержка прервана для {}", url);
            }
        }

        if (doc == null) {
            log.error("Не удалось получить страницу после 3 попыток: {}", url);
            return Collections.emptyList();
        }

        Elements elements = doc.select("a[href]");
        for (Element el : elements) {
            String absUrl = el.absUrl("href").split("#")[0];
            if (!absUrl.isBlank()) {
                links.add(absUrl);
            }
        }
        return links.stream()
                .filter(link -> link.startsWith(siteDomain))
                .toList();
    }



    public int getResponseCode(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .ignoreHttpErrors(true)
                    .execute();
            return response.statusCode();
        } catch (UnsupportedMimeTypeException e) {
            log.debug("Пропускаем не-HTML ресурс: {}", url);
            return -1;
        } catch (IOException e) {
            log.warn("Ошибка при получении кода ответа {}: {}", url, e.getMessage());
            return -1;
        }
    }
}
