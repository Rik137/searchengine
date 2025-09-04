package searchengine.services.util;

import org.springframework.stereotype.Component;
import searchengine.model.*;

import java.time.LocalDateTime;

@Component
public class EntityFactory {
    public SiteEntity createSiteEntity(String name, String url){
        SiteEntity site = new SiteEntity();
        site.setStatus(Status.INDEXED);
        site.setName(name);
        site.setUrl(url);
        site.setStatusTime(LocalDateTime.now());
        return site;
    }
    public PageEntity createPageEntity(SiteEntity siteEntity,
                                    String path, int code, String content){
        PageEntity page = new PageEntity();
        page.setSiteEntity(siteEntity);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        return page;
    }
    public LemmaEntity createLemmaEntity(SiteEntity siteEntity, String lemma){
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setSiteEntity(siteEntity);
        lemmaEntity.setLemma(lemma);
        return lemmaEntity;
    }
    public IndexEntity createIndexEntity(PageEntity pageEntity, LemmaEntity lemmaEntity){
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setPageEntity(pageEntity);
        indexEntity.setLemmaEntity(lemmaEntity);
        return indexEntity;
    }
}
