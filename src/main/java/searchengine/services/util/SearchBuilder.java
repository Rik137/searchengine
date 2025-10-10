package searchengine.services.util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.dto.SearchResult;
import searchengine.model.PageEntity;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor
public class SearchBuilder {

    private static final int SNIPPET_RADIUS = 100; // длина до и после совпадения
    private static final int SNIPPET_MAX_LENGTH = 250;

    public List<SearchResult> build(Map<PageEntity, Float> rankedPages, int offset, int limit, String query) {
        if (rankedPages.isEmpty()) {
            return List.of();
        }

        List<String> queryWords = Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(s -> s.length() > 1)
                .toList();

        return rankedPages.entrySet().stream()
                .skip(offset)
                .limit(limit)
                .map(entry -> {
                    PageEntity page = entry.getKey();
                    float relevance = entry.getValue();

                    String title = extractTitle(page);
                    String text = extractText(page);
                    String snippet = buildSnippet(text, queryWords);
                   // String fullUrl = page.getSiteEntity().getUrl() + page.getPath();
                 //   String rawPath = page.getPath();
                  //  String siteUrl = page.getSiteEntity().getUrl();
                    String siteUrl = page.getSiteEntity() != null && page.getSiteEntity().getUrl() != null
                            ? page.getSiteEntity().getUrl()
                            : "";

                    String path = page.getPath() != null ? page.getPath() : "";
// Если path уже полный URL, используем его напрямую
                   String fullUrl;
                    if (siteUrl.isEmpty() && path.isEmpty()) {
                        fullUrl = "#"; // ссылка-заглушка
                    } else if (!siteUrl.isEmpty() && !path.isEmpty()) {
                        // если path уже абсолютный URL, не добавляем siteUrl
                        if (path.startsWith("http://") || path.startsWith("https://")) {
                            fullUrl = path;
                        } else {
                            fullUrl = siteUrl.endsWith("/") ? siteUrl + path : siteUrl + "/" + path;
                        }
                    } else {
                        fullUrl = siteUrl + path; // один из них непустой
                    }

                    System.out.println(fullUrl);
                    SearchResult result = new SearchResult(
                          fullUrl,
                            title,
                            snippet,
                            relevance
                    );
                    log.info("Страница {}: siteUrl='{}', path='{}', fullUrl='{}'",
                            page.getId(), siteUrl, path, fullUrl);

                    return result;
                })
                .toList();
    }

    private String extractTitle(PageEntity page) {
        try {
            Document doc = Jsoup.parse(page.getContent());
            return doc.title().isBlank() ? "(без заголовка)" : doc.title();
        } catch (Exception e) {
            log.warn("Ошибка извлечения заголовка для страницы {}", page.getPath(), e);
            return "(ошибка извлечения заголовка)";
        }
    }

    private String extractText(PageEntity page) {
        try {
            Document doc = Jsoup.parse(page.getContent());
            return doc.text();
        } catch (Exception e) {
            log.warn("Ошибка очистки HTML для страницы {}", page.getPath(), e);
            return "";
        }
    }

    private String buildSnippet(String text, List<String> queryWords) {
        if (text == null || text.isBlank()) return "";

        String lower = text.toLowerCase();
        int matchIndex = -1;
        String matchedWord = null;

        for (String word : queryWords) {
            int index = lower.indexOf(word);
            if (index >= 0) {
                matchIndex = index;
                matchedWord = word;
                break;
            }
        }

        if (matchIndex == -1) {
            // ничего не найдено — берём начало текста
            String snippet = text.substring(0, Math.min(SNIPPET_MAX_LENGTH, text.length()));
            return escapeAndTrim(snippet);
        }

        int start = Math.max(0, matchIndex - SNIPPET_RADIUS);
        int end = Math.min(text.length(), matchIndex + SNIPPET_RADIUS);
        String snippet = text.substring(start, end);

        // выделяем <b> совпадения
        for (String word : queryWords) {
            snippet = snippet.replaceAll("(?i)" + Pattern.quote(word), "<b>$0</b>");
        }

        return escapeAndTrim(snippet);
    }

    private String escapeAndTrim(String snippet) {
        snippet = snippet.replaceAll("\\s+", " ").trim();
        if (snippet.length() > SNIPPET_MAX_LENGTH) {
            snippet = snippet.substring(0, SNIPPET_MAX_LENGTH) + "...";
        }
        return snippet;
    }
}