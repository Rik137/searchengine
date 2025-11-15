package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single search result.
 * <p>
 * Used as part of {@link SearchResponse}.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchResult {

    /**
    * URL of the website where the result was found
    */
    private String site;

    /**
    * Name of the website (from configuration or meta tags)
    */
    private String siteName;

    /**
    * Relative URI of the page (e.g., "/news/123")
    */
    private String uri;

    /**
    * Page title (HTML document title)
    */
    private String title;

    /**
    * Short text fragment where the query appears (snippet)
    */
    private String snippet;

    /**
    * Relevance of the result (used for sorting)
    */
    private float relevance;
}
