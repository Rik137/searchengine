package searchengine.dto.statistics;

import lombok.Data;

/**
* API response containing indexing statistics
*/

@Data
public class StatisticsResponse {

    /**
    * Result of the request execution (true means success)
    */
    private boolean result;

    /**
    * Statistical data
    */
    private StatisticsData statistics;
}
