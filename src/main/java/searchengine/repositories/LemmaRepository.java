package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;

public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    // Optional<LemmaEntity> findByLemma(String lemma);
    @Query("SELECT l FROM LemmaEntity l WHERE l.lemma = :lemma AND l.siteEntity.id = :siteId")
    Optional<LemmaEntity> findByLemmaAndSiteId(@Param("lemma") String lemma, @Param("siteId") Integer siteId);

    @Query("SELECT l FROM LemmaEntity l WHERE l.lemma = :lemma AND l.siteEntity = :site")
    Optional<LemmaEntity> findByLemmaAndSite(@Param("lemma") String lemma, @Param("site") SiteEntity site);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LemmaEntity l")
    boolean hasAnyLemmas();

    // Получить все леммы для конкретного сайта по объекту SiteEntity
    @Query("SELECT l FROM LemmaEntity l WHERE l.siteEntity = :site")
    List<LemmaEntity> findAllBySite(@Param("site") SiteEntity site);

    List<LemmaEntity> findByLemmaIn(List<String> names);

    List<LemmaEntity> findByLemmaInAndSiteEntity_Url(List<String> names, String siteUrl);

    @Query("SELECT COUNT(l) FROM LemmaEntity l WHERE l.siteEntity.id = :siteId")
    int countLemmasBySite(@Param("siteId") Integer siteId);
}

