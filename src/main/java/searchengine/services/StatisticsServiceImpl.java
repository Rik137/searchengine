package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.logs.LogTag;
import searchengine.model.SiteEntity;
import searchengine.services.serviceinterfaces.StatisticsService;
import searchengine.services.util.IndexingContext;
import searchengine.services.util.Stopwatch;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

 /**
 * Service for generating statistics on website indexing.
 *
 * <p>Provides both aggregated information across all sites and detailed data for each site.
 */

@Service
@RequiredArgsConstructor
@Slf4j

public class StatisticsServiceImpl implements StatisticsService {

    private static final LogTag TAG = LogTag.STATISTICS;
    private final Stopwatch stopwatch = new Stopwatch();

    /**
    * Indexing context with access to data managers and task status.
    */
    private final IndexingContext context;

    /**
    * Indexing service for checking the current state of the process
    */
    private final IndexingServiceImpl indexingServiceImp;

    /**
    * Retrieves the current indexing statistics.
    *
    * @return {@link StatisticsResponse} containing both overall and detailed information about the sites
    */
    @Override
    public StatisticsResponse getStatistics() {
        List<SiteEntity> sites = context.getDataManager().getAllSites();
        log.info("{}  Generating statistics for {} sites", TAG, sites.size());
        stopwatch.start();
        TotalStatistics total = calculateTotal(sites);
        List<DetailedStatisticsItem> detailed = buildDetailedStatistics(sites);

        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);

        StatisticsResponse response = new StatisticsResponse();
        response.setStatistics(data);
        response.setResult(true);
        stopwatch.stop();
        log.info("{}  Statistics calculation completed in {} sec.", TAG, stopwatch.getSeconds());
        stopwatch.reset();
        return response;
    }
    /**
    * Calculates aggregated statistics across all sites.
    *
    * @param sites list of sites
    * @return {@link TotalStatistics} containing the total number of pages, lemmas, and indexing status
    */
    private TotalStatistics calculateTotal(List<SiteEntity> sites) {
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.size());
        total.setIndexing(indexingServiceImp.isIndexing());

        for (SiteEntity site : sites) {
            int pages = site.getPageEntityList().size();
            int lemmas = context.getDataManager().getCountLemmasBySite(site);

            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
        }
        return total;
    }

    /**
    * Generates detailed statistics for each site.
    *
    * @param sites list of sites
    * @return list of {@link DetailedStatisticsItem} containing information for each site
    */
    private List<DetailedStatisticsItem> buildDetailedStatistics(List<SiteEntity> sites) {
        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        for (SiteEntity site : sites) {
            DetailedStatisticsItem item = mapSiteToStatisticsItem(site);
            detailed.add(item);
        }
        return detailed;
    }

    /**
    * Converts a SiteEntity object into a DetailedStatisticsItem.
    *
    * @param site the site
    * @return {@link DetailedStatisticsItem} with detailed information
    */
    private DetailedStatisticsItem mapSiteToStatisticsItem(SiteEntity site) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();

        item.setName(site.getName());
        item.setUrl(site.getUrl());
        item.setStatus(site.getStatus().name());
        item.setError(site.getLastError());
        item.setStatusTime(site.getStatusTime()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());
        item.setPages(site.getPageEntityList().size());
        item.setLemmas(context.getDataManager().getCountLemmasBySite(site));
        return item;
    }
}