package searchengine.services.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import searchengine.model.SiteEntity;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

 /**
 * Storage for visited URLs and active sites during indexing.
 * <p>
 * Uses thread-safe collections to operate correctly in a multithreaded environment.
 */

@Component
@Getter
@Setter

public class VisitedUrlStore {

     /**
     * A set of visited URLs. Uses a thread-safe Set implementation.
     * Keys are added when pages are visited.
     */
     private final Set<String> visited = ConcurrentHashMap.newKeySet();

     /**
     * A map of active sites: the key is the site's URL, the value is a SiteEntity.
     * Used to track sites that are currently being indexed.
     */
     private final Map<String, SiteEntity> activeSites = new ConcurrentHashMap<>();


     /**
     * Marks the given URL as visited.
     *
     * @param url the page URL
     * @return true if the URL was added for the first time; false if it was already present
     */
     public boolean visitUrl(String url) {
        return visited.add(url);
    }

     /**
     * Returns the number of visited URLs.
     *
     * @return the count of unique visited URLs
     */
     public int size() {
        return visited.size();
    }


    /**
    * Marks a site as active.
    *
    * @param site the SiteEntity representing the site
    */
    public void activateSite(SiteEntity site) {
        activeSites.put(site.getUrl(), site);
    }

    /**
    * Removes a site from the list of active sites by its URL.
    *
    * @param url the URL of the site that has finished indexing
    */
    public void markSiteFinished(String url) {
        activeSites.remove(url);
    }

     /**
     * Returns the collection of active sites.
     *
     * @return a collection of SiteEntity objects representing active sites
     */
     public Collection<SiteEntity> getActiveSites() {
        return activeSites.values();
    }

     /**
     * Clears all stored data: visited URLs and active sites.
     */
     public void resetAll() {
        visited.clear();
        activeSites.clear();
    }
}
