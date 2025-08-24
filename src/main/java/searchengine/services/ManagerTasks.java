package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import searchengine.config.SitesList;
import searchengine.services.tasts.SitesTask;
import searchengine.services.util.ManagerJSOUP;
import searchengine.services.util.VisitedUrlStore;

import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerTasks {

    private final ForkJoinPool pool = new ForkJoinPool();
    private final ManagerJSOUP managerJSOUP;
    private final VisitedUrlStore visitedUrlStore;

    public void startIndexTask(SitesList sitesList){
        log.info("ИНДЕКСАЦИЯ УСПЕШНО ЗАПУЩЕНА...");
      try {
          // запускаем одну корневую задачу, которая разрулит все сайты
          pool.invoke(new SitesTask(sitesList.getSites(), managerJSOUP, visitedUrlStore));
      }finally {
          log.info("ИНДЕКСАЦИЯ ЗАВЕРШИНА...");
      }
    }
}
