package searchengine.dto.statistics;

import lombok.Data;

import java.util.List;

/**
* Core data structure for the statistics response.
*/

@Data
public class StatisticsData {

    /**
    * Summary statistics for all sites
    */
    private TotalStatistics total;

    /**
    * Detailed data for each site.
    */
    private List<DetailedStatisticsItem> detailed;
}
