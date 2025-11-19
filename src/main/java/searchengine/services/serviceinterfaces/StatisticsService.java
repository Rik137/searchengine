package searchengine.services.serviceinterfaces;

import searchengine.dto.statistics.StatisticsResponse;

 /**
 * Service for retrieving statistics about the site indexing process.
 */

public interface StatisticsService {

    /**
    * Returns the current indexing statistics.
    *
    * @return a {@link StatisticsResponse} object containing the statistics data
    */
    StatisticsResponse getStatistics();
}
