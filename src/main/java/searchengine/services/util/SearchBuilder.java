package searchengine.services.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.dto.search.SearchResult;
import searchengine.logs.LogTag;
import searchengine.model.PageEntity;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 /**
 * A class responsible for building search results.
 * <p>Creates {@link SearchResult} objects from pages, taking relevance into account,
 * highlights query keywords in snippets, and generates text previews.
 * <p>Main responsibilities:
 * <ul>
 *   <li>Building a paginated list of search results.</li>
 *   <li>Creating snippets with highlighted query terms.</li>
 *   <li>Extracting page titles and relative paths.</li>
 * </ul>
 */

@Slf4j
@NoArgsConstructor

public class SearchBuilder {

    private static final LogTag TAG = LogTag.SEARCH_BUILDER;

     /**
     * The number of characters taken on each side of a matched query term
     * when generating a snippet.
     */
     private static final int SNIPPET_RADIUS = 100;

     /**
     * The maximum allowed length of the generated snippet.
     */
     private static final int SNIPPET_MAX_LENGTH = 250;

     /**
     * The average line length used as a heuristic when formatting snippet output.
     */
     private static final int AVG_LINE_LENGTH = 80;

     /**
     * The number of lines to include in the final formatted snippet.
     */
     private static final int SNIPPET_LINES = 3;

    /**
    * Builds a list of {@link SearchResult} objects based on a map of pages with their relevance.
    *
    * @param rankedPages a map of pages and their relevance scores
    * @param offset the pagination offset
    * @param limit the maximum number of results
    * @param query the search query used for highlighting terms in the snippet
    * @return a list of {@link SearchResult} with populated snippets and titles
    */
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
    /**
    * Removes HTML tags from the given text and normalizes excess whitespace.
    *
    * @param content the page's HTML content
    * @return plain text with tags removed and excess whitespace collapsed/trimmed
    */
    private String cleanHtmlTags(String content) {
        return content.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    /**
    * Builds a text snippet for display in search results.
    * <p>Highlights query keywords in bold (<b>) and limits the snippet length.
    *
    * @param text the page text without HTML
    * @param queryWords the list of words from the search query
    * @return the generated snippet for display
    */
    private String buildSnippet(String text, List<String> queryWords) {
        if (text.isBlank()) return "";

        String snippet = getString(text, queryWords);

        if (!queryWords.isEmpty()) {
            Pattern pattern = Pattern.compile("\\b(" + String.join("|", queryWords) + ")\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(snippet);
            snippet = matcher.replaceAll("<b>$1</b>");
        }

        int maxLen = AVG_LINE_LENGTH * SNIPPET_LINES;
        snippet = snippet.length() > maxLen ? snippet.substring(0, maxLen) + "..." : snippet;
        return snippet;
    }

     /**
     * Finds a text fragment around the first occurrence of any query word.
     * <p>Used internally by {@link #buildSnippet(String, List)}.
     *
     * @param text the page text
     * @param queryWords the query words
     * @return a segment of text containing the match, or the beginning of the text if no match is found
     */
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

     /**
     * Creates a {@link SearchResult} object for a single page.
     * <p>Extracts the title, relative path, snippet, and other related data.
     *
     * @param page the page
     * @param relevance the page's relevance score
     * @param queryWords the list of query words
     * @return a {@link SearchResult} representing this page
     */
     private SearchResult createSearchResult(PageEntity page, float relevance, List<String> queryWords) {
        String siteUrl = Optional.ofNullable(page.getSiteEntity())
                .map(s -> s.getUrl())
                .orElse("");
        String siteName = Optional.ofNullable(page.getSiteEntity())
                .map(s -> s.getName())
                .orElse("(no name)");
        String pagePath = Optional.ofNullable(page.getPath()).orElse("");
        String uri = pagePath.startsWith("http") ? extractRelativePath(pagePath, siteUrl) : pagePath;
        String content = Optional.ofNullable(page.getContent()).orElse("");
        String title = extractTitleFromHtml(content);
        String text = cleanHtmlTags(content);
        String snippet = buildSnippet(text, queryWords);

        return new SearchResult(siteUrl, siteName, uri, title, snippet, relevance);
    }

     /**
     * Extracts the page title from its HTML content.
     *
     * @param html the page's HTML content
     * @return the page title, or "(no title)" if not found
     */
     private String extractTitleFromHtml(String html) {
         if (html == null || html.isBlank()) return "(no title)";
        Pattern pattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String title = matcher.group(1).replaceAll("\\s+", " ").trim();
            return title.isEmpty() ? "(no title)" : title;
        }
        return "(no title)";
    }

     /**
     * Computes the relative path of a page with respect to the site's URL.
     *
     * @param fullUrl the full URL of the page
     * @param siteUrl the base URL of the site
     * @return the relative path (e.g., "/index.html") or "/" in case of errors
     */
     private String extractRelativePath(String fullUrl, String siteUrl) {
        try {
            if (fullUrl.startsWith(siteUrl)) {
                String relative = fullUrl.substring(siteUrl.length());
                return relative.isEmpty() ? "/" : (relative.startsWith("/") ? relative : "/" + relative);
            }
            return "/";
        } catch (Exception e) {
            log.warn("{}  Failed to extract relative path from {}", TAG, fullUrl, e);
            return "/";
        }
    }
}

