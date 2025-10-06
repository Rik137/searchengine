package searchengine.services;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.services.serviceinterfaces.IndexingService;
import searchengine.services.util.Stopwatch;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private  boolean statusIndexing = false;
    private final ManagerTasks managerTasks;
    private Stopwatch stopwatch = new Stopwatch();

    public boolean isIndexing(){
        return statusIndexing;
    }

    @Override
    public void startIndexing(){
        setStatusIndexing(true);
        animationPrepareIndexing();
        stopwatch.start();
        managerTasks.startIndexTask();
        stopwatch.stop();
        setStatusIndexing(false);
        log.info("Индексация прошла за {} cek.", stopwatch.getTime());
    }

    @Override
    public void stopIndexing() {
    managerTasks.stopIndexingTask();
    setStatusIndexing(false);
    }
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
