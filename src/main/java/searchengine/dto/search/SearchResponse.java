package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import searchengine.dto.ApiResponse;
import java.util.List;

/**
 * Extends {@link ApiResponse} and represents the result of a search query.
 * <p>
 * Used to deliver search results to the client in JSON format.
 * Fields:
 * <ul>
 *   <li>{@code count} — total number of found results</li>
 *   <li>{@code data} — a list of {@link SearchResult} objects</li>
 * </ul>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)

public class SearchResponse extends ApiResponse {

    /**
    * Number of results found.
    */
    private int count;

    /**
    * List of results found.
    */
    private List<SearchResult> data;
    
    /**
    * Constructor for a successful response.
    *
    * @param result the operation result (usually true)
    * @param count  number of results found
    * @param data   list of results
    */
    public SearchResponse(boolean result, int count, List<SearchResult> data) {
        super(result, null);
        this.count = count;
        this.data = data;
    }
}
