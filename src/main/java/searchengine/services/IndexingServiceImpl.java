package searchengine.services;

import lombok.extern.slf4j.Slf4j;;
import org.springframework.stereotype.Service;
import searchengine.services.serviceinterfaces.IndexingService;

@Slf4j
@Service
public class IndexingServiceImpl implements IndexingService {

    private  boolean statusIndexing = false;

    public boolean isIndexing(){
        return statusIndexing;
    }

    public void setStatusIndexing(boolean statusIndexing) {
        this.statusIndexing = statusIndexing;
    }

    @Override
    public String startIndexing(){
        setStatusIndexing(true);
        String message = "";
        //TODO реализовать метод


        setStatusIndexing(false);
        return message;//возвращает сообщение лога
    }

    @Override
    public String stopIndexing(){
        String message = "";
        //TODO реализовать метод
        return message; // /возвращает сообщение лога
    }
}
