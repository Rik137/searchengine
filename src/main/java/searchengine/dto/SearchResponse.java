package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.naming.directory.SearchResult;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SearchResponse extends ApiResponse{
    private int count;
    private List<SearchResult> data;

    public SearchResponse(boolean result, int count, List<SearchResult> data) {
        super(result, null);
        this.count = count;
        this.data = data;
    }
}
