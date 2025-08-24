package searchengine.services;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.services.serviceinterfaces.IndexingService;

@Slf4j
@Setter
@Service
public class IndexingServiceImpl implements IndexingService {
    //-------------------------------------------------------------------поля
    private  boolean statusIndexing = false;
    private final ManagerTasks managerTasks;
    private final SitesList sitesList;

    //------------------------------------------------------------------конструктор
    @Autowired
    public IndexingServiceImpl(ManagerTasks managerTasks, SitesList sitesList) {
        this.managerTasks = managerTasks;
        this.sitesList = sitesList;
    }

    //-------------------------------------------------------------------методы
    public boolean isIndexing(){
        return statusIndexing;
    }

   //---------------------------------------------------------------------

    @Override
    public void startIndexing(){
        setStatusIndexing(true);
        animationPrepareIndexing();
        managerTasks.startIndexTask(sitesList);



        setStatusIndexing(false);
    }
    //-------------------------------------------------------------------
    @Override
    public void stopIndexing(){

        //TODO реализовать метод
    }
    //-------------------------------------------------------------------
    private void animationPrepareIndexing() {
        log.info("НАЧАЛО ИНДЕКСАЦИИ");

        String[] dots = {" ", ".  ", ".. ", "...", "....","....."};
        int times = 20;
        try {
            while (times >= 0) {
                for (String dot : dots) {
                    System.out.print("\r🔄 ПОДГОТОВКА К ИНДЕКСАЦИИ" + dot);
                    Thread.sleep(300);
                    times--;
                }
            }
            System.out.println();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Восстанавливаем прерывание
            throw new RuntimeException("АНММАЦИЯ ПРЕРАВАНА", e);
        }
    }
}
