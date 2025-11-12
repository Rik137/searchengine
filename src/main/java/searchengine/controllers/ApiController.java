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

@Tag(name = "API", description = "Endpoints for indexing, searching, and statistics")
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
    @Operation(summary = "Start indexing all websites")
    @GetMapping("/startIndexing")
    public ResponseEntity<ApiResponse> startIndexing() {
        log.info("{} endpoint called: GET /startIndexing", TAG);
        if (indexingService.isIndexing()) {
            log.warn("{} Attempt to start indexing while it is already in progress", TAG);
            return error("Indexing is already in progress", HttpStatus.BAD_REQUEST);
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
    @Operation(summary = "Stop the indexing process")
    @GetMapping("/stopIndexing")
    public ResponseEntity<ApiResponse> stopIndexing() {
        log.info("Endpoint called: GET /stopIndexing");
        if (!indexingService.isIndexing()) {
            log.warn("{} Attempt to stop indexing when no process is running", TAG);
           return error("Indexing has not been started", HttpStatus.BAD_REQUEST);
        }
            log.warn("{} An attempt to stop indexing was made", TAG);
            indexingService.stopIndexing();
            return ResponseEntity.ok(new ApiResponse(true, null));
    }

    /**
    * Returns statistics about site indexing.
    *
    * @return {@link ResponseEntity} containing a {@link StatisticsResponse} object.
    */
    @Operation(summary = "Retrieve indexing statistics")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        log.info("{} Endpoint called: GET /statistics", TAG);
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
    @Operation(summary = "Index a single page")
    @PostMapping("/indexPage")
    public ResponseEntity<ApiResponse> indexPage(@RequestParam("url") @NotBlank String url) {
        log.info("{} Endpoint called: POST /indexPage", TAG);
        log.info("{} Indexing page started: {}", TAG, url);
        boolean indexed =  pageIndexingService.indexPage(url);
        if (!indexed) {
            log.warn("{} Indexing of page {} was not performed", TAG, url);
            return error("The page is outside the configured sites in the configuration file",
            HttpStatus.BAD_REQUEST);
        }
        log.info("{} Successfully indexed page {}", TAG, url);
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
    @Operation(summary = "Search within a site or by query")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("{} Endpoint /api/search called with parameters: query={}, site={}, offset={}, limit={}", TAG,
          query, site, offset, limit);
        if (query == null || query.trim().isEmpty()) {
            return error("Search query cannot be empty", HttpStatus.BAD_REQUEST);
        }
        try {
           List<SearchResult> results = searchService.search(query, site, offset, limit);
            if (results.isEmpty()) {
               return error("Nothing found for the given query", HttpStatus.NOT_FOUND);
            }
            log.info("{} Search finished: {} results returned", TAG, results.size());
            return ResponseEntity.ok(new SearchResponse(true, results.size(), results));
        }catch (IllegalStateException e) {
            log.warn("{} An error occurred during search: {}", TAG, e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("{} An internal error occurred during search", TAG, e);
            return error("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
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
