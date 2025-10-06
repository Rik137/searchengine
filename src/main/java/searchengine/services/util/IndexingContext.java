package searchengine.services.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.services.LemmaFrequencyService;
import searchengine.services.LemmaProcessor;
import searchengine.services.ManagerRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
public class IndexingContext {
    private final SitesList sites;
    private final EntityFactory entityFactory;
    private final ManagerRepository managerRepository;
    private final ManagerJSOUP managerJSOUP;
    private final LemmaProcessor lemmaProcessor;
    private final VisitedUrlStore visitedUrlStore;
    private final @Lazy LemmaFrequencyService lemmaFrequencyService;
    private volatile boolean stopRequested = false;

    public void requestStop() {
        this.stopRequested = true;
    }

    public void clearStopRequest() {
        this.stopRequested = false;
    }
    /**
     * Проверяет, нужно ли остановить текущую задачу.
     * Если да — возвращает true, иначе false.
     */
    public boolean shouldStop(String taskName) {
        boolean stop = stopRequested || Thread.currentThread().isInterrupted();
        if (stop) {
            // можно логировать только один раз на задачу
            log.info("Остановка задачи {}: выполнение прервано", taskName);
        }
        return stop;
    }

}
