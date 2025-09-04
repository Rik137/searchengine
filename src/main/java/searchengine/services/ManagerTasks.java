package searchengine.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import searchengine.config.SitesList;
import searchengine.services.tasts.SitesTask;
import searchengine.services.util.EntityFactory;
import searchengine.services.util.IndexingContext;
import searchengine.services.util.ManagerJSOUP;
import searchengine.services.util.VisitedUrlStore;

import java.util.concurrent.ForkJoinPool;

@Service
@Slf4j
@Getter
public class ManagerTasks {

    private final ForkJoinPool pool = new ForkJoinPool();
    private final IndexingContext context;

    public ManagerTasks(EntityFactory entityFactory,
                        ManagerRepository managerRepository,
                        ManagerJSOUP managerJSOUP,
                        LemmaProcessor luceneLemmaMake,
                        VisitedUrlStore visitedUrlStore) {
        this.context = new IndexingContext(entityFactory, managerRepository, managerJSOUP, luceneLemmaMake, visitedUrlStore);
    }

    public void startIndexTask(SitesList sitesList){
        log.info("ИНДЕКСАЦИЯ УСПЕШНО ЗАПУЩЕНА...");
      try {
          // запускаем одну корневую задачу, которая разрулит все сайты
          pool.invoke(new SitesTask(sitesList.getSites(), context));
      }finally {
          log.info("ИНДЕКСАЦИЯ ЗАВЕРШИНА...");
      }
    }
}
