package searchengine.services;

import liquibase.pro.packaged.S;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.services.serviceinterfaces.SearchService;
import searchengine.services.util.IndexingContext;

import javax.naming.directory.SearchResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class SearchServiceImpl implements SearchService {
    private final IndexingContext context;


    public boolean isIndexReady(){

        return false;
    }

    @Override
    public List<SearchResult> search(String query, String site, int offset, int limit) throws IllegalStateException{
        if (!isIndexReady()) {
            throw new IllegalStateException("Индекс ещё не готов. Попробуйте позже.");
        }
        Map<String, Integer> lemmas = context.getLemmaProcessor().getLemmas(query);
        //исключить леммы которые встречаются на большом количестве страниц
        //сортировать получившийся список
        return Collections.emptyList();
    }
}
