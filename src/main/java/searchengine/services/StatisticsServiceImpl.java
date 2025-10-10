package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.services.serviceinterfaces.StatisticsService;
import searchengine.services.util.IndexingContext;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final IndexingContext context;
    private final IndexingServiceImpl indexingServiceImp;

    @Override
    public StatisticsResponse getStatistics() {
        // Получаем все сайты из репозитория
        List<SiteEntity> sites = context.getManagerRepository().getAllSites();

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.size());
        total.setIndexing(indexingServiceImp.isIndexing());

        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        for (SiteEntity site : sites) {
            // Подсчёт страниц и лемм
            int pages = site.getPageEntityList().size();
            int lemmas = context.getManagerRepository().getCountLemmasBySite(site);

            // Суммируем в total
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);

            // Преобразуем LocalDateTime в миллисекунды
            long statusTimeMillis = site.getStatusTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            // Формируем детальную статистику
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            item.setStatus(site.getStatus().name()); // enum -> String
            item.setError(site.getLastError());
            item.setStatusTime(statusTimeMillis);
            item.setPages(pages);
            item.setLemmas(lemmas);

            detailed.add(item);
        }

        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);

        StatisticsResponse response = new StatisticsResponse();
        response.setStatistics(data);
        response.setResult(true);

        return response;
    }
}