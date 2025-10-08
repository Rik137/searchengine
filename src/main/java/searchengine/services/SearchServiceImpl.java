package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SortComparator;
import org.springframework.stereotype.Service;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
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


    public boolean isIndexReady(String site){
        if (site != null) {
            if (!isSiteIndexed(site)) return false; // конкретный сайт не готов
        } else {
            if (!hasAnySites()) return false; // глобальная проверка
        }
        return  context.getManagerRepository().hasLemmas();
    }

    @Override
    public List<SearchResult> search(String query, String url, int offset, int limit) throws IllegalStateException{
        if (!isIndexReady(url)) {
            throw new IllegalStateException("Индекс ещё не готов. Попробуйте позже.");
        }
          return context.getLemmaFrequencyService().findSearchResult(query, url, offset, limit);
    }

    private boolean isSiteIndexed(String url) {
        return context.getManagerRepository()
                .findSite(url)
                .map(site -> site.getStatus() == Status.INDEXED)
                .orElse(false);
    }

    private boolean hasAnySites(){
        return context.getManagerRepository().hasSites();
    }
}
