package searchengine.services.serviceinterfaces;

import searchengine.dto.search.SearchResult;

import java.util.List;

public interface SearchService {
   List<SearchResult> search (String query, String site, int offset, int limit) throws IllegalStateException;
}
