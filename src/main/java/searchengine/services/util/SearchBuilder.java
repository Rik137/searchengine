package searchengine.services.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.dto.search.SearchResult;
import searchengine.model.PageEntity;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor
public class SearchBuilder {

    private static final int SNIPPET_RADIUS = 100;
    private static final int SNIPPET_MAX_LENGTH = 250;
    private static final int AVG_LINE_LENGTH = 80; // среднее количество символов на строку
    private static final int SNIPPET_LINES = 3;    // ~3 строки

    public List<SearchResult> build(Map<PageEntity, Float> rankedPages, int offset, int limit, String query) {
        if (rankedPages.isEmpty()) return List.of();

        List<String> queryWords = Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(s -> s.length() > 1)
                .toList();

        return rankedPages.entrySet().stream()
                .skip(offset)
                .limit(limit)
                .map(entry -> createSearchResult(entry.getKey(), entry.getValue(), queryWords))
                .toList();
    }



    // простая очистка тегов HTML
    private String cleanHtmlTags(String content) {
        return content.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    private String buildSnippet(String text, List<String> queryWords) {
        if (text.isBlank()) return "";

        String snippet = getString(text, queryWords);

        // Подсветка всех слов одной регуляркой
        if (!queryWords.isEmpty()) {
            Pattern pattern = Pattern.compile("\\b(" + String.join("|", queryWords) + ")\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(snippet);
            snippet = matcher.replaceAll("<b>$1</b>");
        }

        // Ограничение по символам (~3 строки)
        int maxLen = AVG_LINE_LENGTH * SNIPPET_LINES;
        snippet = snippet.length() > maxLen ? snippet.substring(0, maxLen) + "..." : snippet;

        return snippet;
    }

    private static String getString(String text, List<String> queryWords) {
        String lowerText = text.toLowerCase();
        int matchIndex = -1;

        for (String word : queryWords) {
            int idx = lowerText.indexOf(word);
            if (idx >= 0) {
                matchIndex = idx;
                break;
            }
        }

        int start = matchIndex == -1 ? 0 : Math.max(0, matchIndex - SNIPPET_RADIUS);
        int end = matchIndex == -1
                ? Math.min(SNIPPET_MAX_LENGTH, text.length())
                : Math.min(text.length(), matchIndex + SNIPPET_RADIUS);

        return text.substring(start, end);
    }

    private SearchResult createSearchResult(PageEntity page, float relevance, List<String> queryWords) {
        String siteUrl = Optional.ofNullable(page.getSiteEntity())
                .map(s -> s.getUrl())
                .orElse("");

        String siteName = Optional.ofNullable(page.getSiteEntity())
                .map(s -> s.getName())
                .orElse("(без имени)");

        String pagePath = Optional.ofNullable(page.getPath()).orElse("");
        String uri = pagePath.startsWith("http") ? extractRelativePath(pagePath, siteUrl) : pagePath;

        String content = Optional.ofNullable(page.getContent()).orElse("");

        // Сначала извлекаем заголовок
        String title = extractTitleFromHtml(content);

        // Потом чистим текст для сниппета
        String text = cleanHtmlTags(content);

        String snippet = buildSnippet(text, queryWords);

        return new SearchResult(siteUrl, siteName, uri, title, snippet, relevance);
    }

    // Извлечение <title> из HTML без полного парсинга Jsoup
    private String extractTitleFromHtml(String html) {
        if (html == null || html.isBlank()) return "(без заголовка)";
        Pattern pattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String title = matcher.group(1).replaceAll("\\s+", " ").trim();
            return title.isEmpty() ? "(без заголовка)" : title;
        }
        return "(без заголовка)";
    }
    private String extractRelativePath(String fullUrl, String siteUrl) {
        try {
            if (fullUrl.startsWith(siteUrl)) {
                String relative = fullUrl.substring(siteUrl.length());
                // добавляем слэш, если его нет
                return relative.isEmpty() ? "/" : (relative.startsWith("/") ? relative : "/" + relative);
            }
            return "/";
        } catch (Exception e) {
            log.warn("Не удалось извлечь относительный путь из {}", fullUrl, e);
            return "/";
        }
    }


}

