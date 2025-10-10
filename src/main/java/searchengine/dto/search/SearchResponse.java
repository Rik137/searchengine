package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import searchengine.dto.ApiResponse;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SearchResponse extends ApiResponse {
    private int count;
    private List<SearchResult> data;

    public SearchResponse(boolean result, int count, List<SearchResult> data) {
        super(result, null);
        this.count = count;
        this.data = data;
    }
}
