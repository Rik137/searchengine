package searchengine.services.serviceinterfaces;

import searchengine.dto.search.SearchResult;
import java.util.List;

 /**
 * Service for performing searches across sites and queries.
 */

public interface SearchService {

   /**
   * Performs a search by the specified query and site.
   *
   * @param query   the search query
   * @param site    the site URL to limit the search (may be null)
   * @param offset  the result offset (for pagination)
   * @param limit   the maximum number of results
   * @return a list of {@link SearchResult} objects containing the found results
   * @throws IllegalStateException if the search cannot be performed (for example, indexing is not completed)
   */
   List<SearchResult> search (String query, String site, int offset, int limit) throws IllegalStateException;
}
