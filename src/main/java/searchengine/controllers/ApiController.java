package searchengine.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.ApiResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.logs.LogTag;
import searchengine.services.IndexingServiceImpl;
import searchengine.services.PageIndexingServiceImpl;
import searchengine.services.SearchServiceImpl;
import searchengine.services.serviceinterfaces.StatisticsService;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * API controller for managing site indexing, search, and statistics.
 * <p>
 * Main features:
 * <ul>
 *     <li>Start and stop indexing of all configured sites</li>
 *     <li>Index a specific page by its URL</li>
 *     <li>Retrieve site indexing statistics</li>
 *     <li>Perform searches across sites and queries</li>
 * </ul>
 * <p>
 * The {@link Tag} annotation is used for grouping methods in Swagger/OpenAPI UI.
 */

@Tag(name = "API", description = "Методы индексации, поиска и статистики")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {
    
    private static final LogTag TAG = LogTag.API;
    private final StatisticsService statisticsService;
    private final IndexingServiceImpl indexingService;
    private final SearchServiceImpl searchService;
    private final PageIndexingServiceImpl pageIndexingService;

    /**
    * Starts indexing of all sites specified in the configuration file.
    * <p>
    * Checks if indexing is already running. If it is, returns {@link HttpStatus#BAD_REQUEST}.
    *
    * @return {@link ResponseEntity} with {@link ApiResponse} indicating whether the operation was successful.
    */
    @Operation(summary = "Запуск индексации всех сайтов")
    @GetMapping("/startIndexing")
    public ResponseEntity<ApiResponse> startIndexing() {
        log.info("{}  запущен метод: GET /startIndexing", TAG);
        if (indexingService.isIndexing()) {
            log.warn("{}  Попытка запустить индексацию при уже запущенном процессе", TAG);
            return error("Индексация уже запущена", HttpStatus.BAD_REQUEST);
        }
        indexingService.startIndexing();
        return ResponseEntity.ok(new ApiResponse(true, null));
    }

    /**
    * Stops the current indexing process of all sites.
    * <p>
    * If indexing is not running, returns {@link HttpStatus#BAD_REQUEST}.
    *
    * @return {@link ResponseEntity} with {@link ApiResponse} indicating whether the operation was successful.
    */
    @Operation(summary = "Остановка индексации")
    @GetMapping("/stopIndexing")
    public ResponseEntity<ApiResponse> stopIndexing() {
        log.info("запущен метод: GET /stopIndexing");
        if (!indexingService.isIndexing()) {
            log.warn("{}  Попытка остановить индексацию, которая не запущена", TAG);
            return error("Индексация не запущена", HttpStatus.BAD_REQUEST);
        }
        log.warn("{}  попытка остановить индексацию", TAG);
            indexingService.stopIndexing();
            return ResponseEntity.ok(new ApiResponse(true, null));
    }

    /**
    * Returns statistics about site indexing.
    *
    * @return {@link ResponseEntity} containing a {@link StatisticsResponse} object.
    */
    @Operation(summary = "Получение статистики по индексированию")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        log.info("{}  Вызов метода GET /statistics", TAG);
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    /**
    * Indexes a specific page by the given URL.
    * <p>
    * If the page is outside the configured sites, returns {@link HttpStatus#BAD_REQUEST}.
    *
    * @param url the URL of the page to index; must not be blank
    * @return {@link ResponseEntity} with {@link ApiResponse} indicating whether the operation was successful.
    */
    @Operation(summary = "Индексация конкретной страницы")
    @PostMapping("/indexPage")
    public ResponseEntity<ApiResponse> indexPage(@RequestParam("url") @NotBlank String url) {
        log.info("{}  запущен метод: Post /indexPage", TAG);
        log.info("{}  Запуск индексации страницы {}", TAG, url);
        boolean indexed =  pageIndexingService.indexPage(url);
        if (!indexed) {
            log.warn("{}  Индексация страницы {} не выполнена", TAG, url);
            return error("Данная страница находится за пределами сайтов, указанных в конфигурационном файле",
                    HttpStatus.BAD_REQUEST);
        }
        log.info("{} Индексация страницы {} прошла успешно", TAG, url);
        return ResponseEntity.ok(new ApiResponse(true, null));
    }

    /**
    * Performs a search across a specific site or the entire index.
    * <p>
    * If the query is empty, returns {@link HttpStatus#BAD_REQUEST}.
    * If no results are found, returns {@link HttpStatus#NOT_FOUND}.
    *
    * @param query  the search query (optional but must not be empty)
    * @param site   the site to search within (optional)
    * @param offset result offset (default is 0)
    * @param limit  number of results (default is 20)
    * @return {@link ResponseEntity} containing a {@link SearchResponse} or an {@link ApiResponse} with an error.
    */
    @Operation(summary = "Поиск по сайту/запросу")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("{} Запущен метод /api/search с параметрами: query={}, site={}, offset={}, limit={}", TAG,
                query, site, offset, limit);
        if (query == null || query.trim().isEmpty()) {
            return error("Задан пустой поисковый запрос", HttpStatus.BAD_REQUEST);
        }
        try {
           List<SearchResult> results = searchService.search(query, site, offset, limit);
            if (results.isEmpty()) {
                return error("По запросу ничего не найдено", HttpStatus.NOT_FOUND);
            }
            log.info("{} Поиск завершён: найдено {} результатов", TAG, results.size());
            return ResponseEntity.ok(new SearchResponse(true, results.size(), results));
        }catch (IllegalStateException e) {
            log.warn("{}  Ошибка поиска: {}", TAG, e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("{}  Внутренняя ошибка поиска", TAG, e);
            return error("Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
    * Helper method for building an error response.
    *
    * @param message the error message
    * @param status  the HTTP status
    * @return {@link ResponseEntity} containing an {@link ApiResponse}
    */
    private ResponseEntity<ApiResponse> error(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ApiResponse(false, message));
    }
}
