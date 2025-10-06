package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageResponse {
    private final int statusCode;
    private final String body;
    private final boolean isHtml;

}
