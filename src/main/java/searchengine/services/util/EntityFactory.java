package searchengine.services.util;

import org.springframework.stereotype.Component;
import searchengine.model.*;
import java.time.LocalDateTime;

 /**
 * Entity factory for creating {@link SiteEntity}, {@link PageEntity}, {@link LemmaEntity} and {@link IndexEntity} objects.
 *
 * <p>Used to simplify the creation and initialization of entities before saving them to the database.</p>
 */

@Component
public class EntityFactory {

    /**
    * Creates a new {@link SiteEntity} with the initial status {@link Status#INDEXING}.
    *
    * @param name the site name
    * @param url  the site URL
    * @return the {@link SiteEntity} object
    */
    public SiteEntity createSiteEntity(String name, String url){
        SiteEntity site = new SiteEntity();
        site.setStatus(Status.INDEXING);
        site.setName(name);
        site.setUrl(url);
        site.setStatusTime(LocalDateTime.now());
        return site;
    }

    /**
    * Creates a new {@link PageEntity}.
    *
    * @param siteEntity the site to which the page belongs
    * @param path       the page path
    * @param code       the HTTP status code
    * @param content    the HTML content of the page
    * @return the {@link PageEntity} object
    */
    public PageEntity createPageEntity(SiteEntity siteEntity,
                                    String path, int code, String content){
        PageEntity page = new PageEntity();
        page.setSiteEntity(siteEntity);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        return page;
    }

     /**
     * Creates a new {@link LemmaEntity}.
     *
     * @param siteEntity the site to which the lemma belongs
     * @param lemma      the lemma text
     * @param count      the lemma frequency
     * @return the {@link LemmaEntity} object
     */
     public LemmaEntity createLemmaEntity(SiteEntity siteEntity, String lemma, int count){
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setSiteEntity(siteEntity);
        lemmaEntity.setLemma(lemma);
        lemmaEntity.setFrequency(count);
        return lemmaEntity;
    }

     /**
     * Creates a new {@link IndexEntity}.
     *
     * @param pageEntity  the page to which the index belongs
     * @param lemmaEntity the lemma associated with the index
     * @param rank        the lemma weight on the page
     * @return the {@link IndexEntity} object
     */
     public IndexEntity createIndexEntity(PageEntity pageEntity, LemmaEntity lemmaEntity, float rank){
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setPageEntity(pageEntity);
        indexEntity.setLemmaEntity(lemmaEntity);
        indexEntity.setRank(rank);
        return indexEntity;
    }
}
