package searchengine.services.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.dto.PageResponse;
import searchengine.logs.LogTag;
import java.io.IOException;
import java.util.*;

 /**
 * Component for working with HTML pages using Jsoup.
 *
 * <p>Main responsibilities:
 * <ul>
 *   <li>Retrieving all links from a given page with domain-based filtering.</li>
 *   <li>Fetching a page and returning its response code, HTML content, and a flag indicating whether it is HTML.</li>
 *   <li>Cleaning HTML by removing tags and extracting text.</li>
 * </ul>
 *
 * <p>Uses {@link RickBotClient} to fetch pages by URL.
 */
@Slf4j
@RequiredArgsConstructor
public class ManagerJSOUP {

    private static final LogTag TAG = LogTag.JSOUP_MANAGER;
    private final RickBotClient rickBotClient;

     /**
     * Extracts links from a page, keeping only those that belong to the specified domain.
     *
     * @param url the URL of the page to scan
     * @param siteDomain the site domain; links outside this domain will be ignored
     * @return a list of links starting with the specified domain; an empty list if no links are found or an error occurs
     */
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
            log.warn("{}  Failed to fetch page {}: {}", TAG, url, e.getMessage());
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

     /**
     * Loads a page and returns a {@link PageResponse} containing:
     * <ul>
     *   <li>HTTP status code</li>
     *   <li>HTML body of the page, if available</li>
     *   <li>isHtml flag — whether the content is HTML</li>
     * </ul>
     *
     * @param url URL of the page
     * @return {@link PageResponse} with the page data; on errors, status=-1, body=null, isHtml=false
     */
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
            log.warn("{}  Error loading {}: {}", TAG, url, e.getMessage());
            return new PageResponse(-1, null, false);
        }
    }

     /**
     * Removes all HTML tags from a string and returns plain text.
     *
     * @param html HTML content
     * @return text without tags; empty string if input is null
     */
     public String stripHtmlTags(String html) {
        return html == null ? "" : Jsoup.parse(html).text();
    }

    /**
    * Extracts text from {@link PageResponse}.
    * <p>Returns null if:
    * <ul>
    *   <li>response is null</li>
    *   <li>HTTP status is not 200</li>
    *   <li>response body is missing</li>
    * </ul>
    *
    * @param response {@link PageResponse} object
    * @return page text or null in case of errors
    */
    public String extractText(PageResponse response) {
        if (response == null || response.getStatusCode() != 200 || response.getBody() == null) {
            return null;
        }
        try {
            return Jsoup.parse(response.getBody()).text();
        } catch (Exception e) {
            log.warn("{}  Failed to extract text from response (status {}): {}", TAG,
                    response.getStatusCode(), e.getMessage());
            return null;
        }
    }
}
