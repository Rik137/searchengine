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
import searchengine.logs.LogTag;
import java.io.IOException;
import java.util.Random;

 /**
 * A class for interacting with web pages over HTTP while emulating a real browser.
 *
 * <p>Main features:
 * <ul>
 *     <li>Selects a random User-Agent from the configuration</li>
 *     <li>Adds a random delay between requests to reduce the risk of blocking</li>
 *     <li>Fetches pages via Jsoup with proper handling of HTTP and MIME errors</li>
 * </ul>
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class RickBotClient {

    private static final LogTag TAG = LogTag.RICK_BOT_CLIENT;

    /**
    * RickBot client configuration (User-Agent list, delays, referrer)
    */
    private final RickBotConfig config;

    /**
    * Random generator used for selecting User-Agent strings and request delays
    */
    private final Random random = new Random();

    /**
    * Retrieves a web page by URL using a random User-Agent and a randomized delay.
    * <p>
    * This method emulates real user behavior to reduce the risk of being blocked.
    *
    * @param url the page URL
    * @return a {@link Document} containing the page content, or null if the page is unavailable or the MIME type is unsupported
    * @throws IOException if a connection error occurs
    * @throws InterruptedException if the thread is interrupted during the delay
    */
    public Document fetchPage(String url) throws IOException, InterruptedException {
        String ua = config.getUserAgents()
                .get(random.nextInt(config.getUserAgents().size()));
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
            log.warn("{}  HTTP error {} for {}", TAG, e.getStatusCode(), url);
            return null;
        } catch (UnsupportedMimeTypeException e) {
            return null;
        } catch (IOException e) {
            log.error("{}  Connection error with {}: {}", TAG, url, e.getMessage());
            throw e;
        }
    }
}
