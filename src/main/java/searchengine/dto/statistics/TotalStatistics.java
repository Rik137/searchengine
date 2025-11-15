package searchengine.dto.statistics;

import lombok.Data;

/**
* Aggregate statistics across all sites
*/

@Data
public class TotalStatistics {

    /**
    * Total number of sites added to the system
    */
    private int sites;

    /**
    * Total number of indexed pages.
    */
    private int pages;

    /**
    * Total number of lemmas (unique word forms) in the index.
    */
    private int lemmas;

    /**
    * Flag indicating whether indexing is currently in progress.
    */
    private boolean indexing;
}
