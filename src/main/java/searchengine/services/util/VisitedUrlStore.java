package searchengine.services.util;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VisitedUrlStore {
    private final Set<String> visited = ConcurrentHashMap.newKeySet();

    public boolean markAsVisited(String url) {
        return visited.add(url);
    }

    public void reset() {
        visited.clear();
    }

    public int size() {
        return visited.size();
    }
}
