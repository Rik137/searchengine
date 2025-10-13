package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.tasks.SitesTask;
import searchengine.services.util.IndexingContext;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class ManagerTasks {

    private ForkJoinPool pool;  //поле для потоков;
    private final IndexingContext context; // все необходимые компоненты водном

    //метод старта запуска пулов
    public void startIndexTask(){
        context.clearStopRequest();
        pool = new ForkJoinPool();
        //очищение коллекции посещений при повторной индексации
        context.getVisitedUrlStore().resetAll();

        log.info("ИНДЕКСАЦИЯ УСПЕШНО ЗАПУЩЕНА...");
      try {
          // запускаем одну корневую задачу, которая разрулит все сайты
          pool.invoke(new SitesTask(context));
      }finally {
          log.info("ИНДЕКСАЦИЯ ЗАВЕРШИНА...");
      }
    }
    //метод остановки
    public void stopIndexingTask() {
        log.info("Останавливаем индексацию: ставим флаг и шлём shutdownNow() пулу");
        context.requestStop();         // ставим флаг
        pool.shutdown();// отправляем interrupt в воркеры
        try {
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // если через 30 сек не остановился, тогда уже жёстко
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        updateSitesAfterStop();
    }
    private void updateSitesAfterStop() {
        Collection<SiteEntity> activeSites = context.getVisitedUrlStore().getActiveSites();
        for (SiteEntity site : activeSites) {
            site.setStatus(Status.FAILED);
            site.setLastError("Пользователь остановил индексацию");
            site.setStatusTime(LocalDateTime.now());
            context.getDataManager().saveSite(site);
        }
    }
}
