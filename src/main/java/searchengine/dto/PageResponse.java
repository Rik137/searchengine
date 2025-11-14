package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO representing the result of a page fetch.
 */

@Getter
@AllArgsConstructor
public class PageResponse {

    /**
    * HTTP status of the response.
    */
    private final int statusCode;

    /**
    * Page content (HTML or plain text).
    */
    private final String body;

    /**
    * Flag indicating whether the content is HTML.
    */
    private final boolean isHtml;
}
