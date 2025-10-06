package searchengine.services.util;

import lombok.Getter;
import org.springframework.stereotype.Component;
import searchengine.model.SiteEntity;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class VisitedUrlStore {
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Map<String, SiteEntity> activeSites = new ConcurrentHashMap<>();

    public boolean markAsVisited(String url) {
        return visited.add(url);
    }

    public void reset() {
        visited.clear();
    }


    public int size() {
        return visited.size();
    }
    public void markSiteActive(SiteEntity site) {
        activeSites.put(site.getUrl(), site);
    }

    public void markSiteFinished(String url) {
        activeSites.remove(url);
    }

    public Collection<SiteEntity> getActiveSites() {
        return activeSites.values();
    }

    public void resetAll() {
        visited.clear();
        activeSites.clear();
    }
}
