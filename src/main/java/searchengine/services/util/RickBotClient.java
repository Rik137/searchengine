package searchengine.services.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.config.RickBotConfig;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class RickBotClient {
    private final RickBotConfig config;
    private final Random random = new Random();

    /**
     * Получение страницы с рандомным User-Agent
     */
    public Document fetchPage(String url) throws IOException, InterruptedException {
        // Рандомный User-Agent
        String ua = config.getUserAgents()
                .get(random.nextInt(config.getUserAgents().size()));
        // Рандомная задержка между 0.5x и 1x maxDelay
        int delay = config.getMinDelayMs() + random.nextInt(Math.max(1, config.getMaxDelayMs() - config.getMinDelayMs() + 1));
        Thread.sleep(delay);

        Connection connection = Jsoup.connect(url)
                .userAgent(ua)
                .referrer(config.getReferer())
                .ignoreHttpErrors(true)
                .timeout(10000);

        try {
            return connection.get();
        } catch (HttpStatusException e) {
            log.warn("HTTP ошибка {} для {}", e.getStatusCode(), url);
            return null;
        } catch (UnsupportedMimeTypeException e) {
            // Не логируем, просто пропускаем не-HTML ресурсы
            return null;
        } catch (IOException e) {
            log.error("Ошибка соединения с {}: {}", url, e.getMessage());
            throw e;
        }
    }

}
