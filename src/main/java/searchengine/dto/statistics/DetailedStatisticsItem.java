package searchengine.dto.statistics;

import lombok.Data;

/**
 * Detailed statistics for each site.
 */

@Data
public class DetailedStatisticsItem {

    /**
    * URL site.
    */
    private String url;

    /**
    * name site.
    */
    private String name;

    /**
    * Current status (e.g., INDEXING, INDEXED, FAILED).
    */
    private String status;

    /**
    * Timestamp of the last status update (ms).
    */
    private long statusTime;

    /**
    * message about error
    */
    private String error;

    /**
    * Number of indexed pages.
    */
    private int pages;

    /**
    * Number of discovered lemmas.
    */
    private int lemmas;
}
