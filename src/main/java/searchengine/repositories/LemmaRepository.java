package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.Optional;

public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    Optional<LemmaEntity> findByLemma(String lemma);
    @Query("SELECT l FROM LemmaEntity l WHERE l.lemma = :lemma AND l.siteEntity.id = :siteId")
    Optional<LemmaEntity> findByLemmaAndSiteId(@Param("lemma") String lemma, @Param("siteId") Integer siteId);
    @Query("SELECT l FROM LemmaEntity l WHERE l.lemma = :lemma AND l.siteEntity = :site")
    Optional<LemmaEntity> findByLemmaAndSite(@Param("lemma") String lemma, @Param("site") SiteEntity site);
}

