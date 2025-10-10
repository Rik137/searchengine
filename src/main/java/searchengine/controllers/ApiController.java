package searchengine.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.ApiResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingServiceImpl;
import searchengine.services.PageIndexingServiceImpl;
import searchengine.services.SearchServiceImpl;
import searchengine.services.serviceinterfaces.StatisticsService;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingServiceImpl indexingService;
    private final SearchServiceImpl searchService;
    private final PageIndexingServiceImpl pageIndexingService;

    @Autowired
    public ApiController(StatisticsService statisticsService,
                         IndexingServiceImpl indexingService,
                         SearchServiceImpl searchService,
                         PageIndexingServiceImpl pageIndexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchService = searchService;
        this.pageIndexingService = pageIndexingService;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<ApiResponse> startIndexing() {

        log.info("запущен метод: GET /startIndexing");

        if (indexingService.isIndexing()) {
            log.warn("Попытка запустить индексацию при уже запущенном процессе");
            return error("Индексация уже запущена", HttpStatus.BAD_REQUEST);
        }

        indexingService.startIndexing();
        return ResponseEntity.ok(new ApiResponse(true, null));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<ApiResponse> stopIndexing() {
        log.info("запущен метод: GET /stopIndexing");

        if (!indexingService.isIndexing()) {
            log.warn("Попытка остановить индексацию, которая не запущена");
            return error("Индексация не запущена", HttpStatus.BAD_REQUEST);
        }
            log.warn("попытка остановить индексацию");
            indexingService.stopIndexing();
            return ResponseEntity.ok(new ApiResponse(true, null));
        }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<ApiResponse> indexPage(@RequestParam("url") @NotBlank String url) {
        log.info("запущен метод: Post /indexPage");
        log.info("Запуск индексации страницы {}", url);

        boolean indexed =  pageIndexingService.indexPage(url);
        if (!indexed) {
            log.warn("Индексация страницы {} не выполнена", url);
            return error("Данная страница находится за пределами сайтов, указанных в конфигурационном файле",
                    HttpStatus.BAD_REQUEST);
        }

        log.info("Индексация страницы {} прошла успешно", url);
        return ResponseEntity.ok(new ApiResponse(true, null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Запущен метод /api/search с параметрами: query={}, site={}, offset={}, limit={}",
                query, site, offset, limit);
        // 1. Проверяем вход
        if (query == null || query.trim().isEmpty()) {
            return error("Задан пустой поисковый запрос", HttpStatus.BAD_REQUEST);
        }

        try {
           List<SearchResult> results = searchService.search(query, site, offset, limit);
            if (results .isEmpty()) {
                return error("По запросу ничего не найдено", HttpStatus.NOT_FOUND);
            }
            log.info("Поиск завершён: найдено {} результатов", results.size());

            return ResponseEntity.ok(new SearchResponse(true, results.size(), results));

        }catch (IllegalStateException e) {
            log.warn("Ошибка поиска: {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Внутренняя ошибка поиска", e);
            return error("Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<ApiResponse> error(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ApiResponse(false, message));
    }
}