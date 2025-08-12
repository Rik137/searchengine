package searchengine.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.ApiResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingServiceImpl;
import searchengine.services.PageIndexingServiceImpl;
import searchengine.services.SearchServiceImpl;
import searchengine.services.serviceinterfaces.StatisticsService;

import javax.validation.constraints.NotBlank;

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

        log.info("запущен метод: GET /api/startIndexing");

        if (indexingService.isIndexing()) {
            log.warn("Попытка запустить индексацию при уже запущенном процессе");
            return error("Индексация уже запущена", HttpStatus.BAD_REQUEST);
        }
        String message = indexingService.startIndexing();
        log.info("Индексация успешно запущена");
        log.info(message);
        return ResponseEntity.ok(new ApiResponse(true, null));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<ApiResponse> stopIndexing() {
        log.info("запущен метод: GET /api/stopIndexing");

        if (!indexingService.isIndexing()) {
            log.warn("Попытка остановить индексацию, которая не запущена");
            return error("Индексация не запущена", HttpStatus.BAD_REQUEST);
        }else {
            log.warn("попытка остановить индексацию");
            String message = indexingService.stopIndexing();
            log.info(message);
            return ResponseEntity.ok(new ApiResponse(true, null));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<ApiResponse> indexPage(@RequestParam("url") @NotBlank String url) {
        //TODO реализовать метод добавления в индекс и обновляет страницу которой передан параметр
    return null;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchPage(){//дописать сигнатуру метода
        //TODO реализовать метод поиска страницы по параметру запроса
        return null;
    }

    private ResponseEntity<ApiResponse> error(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ApiResponse(false, message));
    }
}
