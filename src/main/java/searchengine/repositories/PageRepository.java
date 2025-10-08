package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;

public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    Optional<PageEntity> findByPath(String path);
    // Все страницы
    @Query("SELECT p FROM PageEntity p WHERE p.siteEntity.id = :siteId")
    List<PageEntity> findAllBySiteId(@Param("siteId") int siteId);
    List<PageEntity> findAllBySiteEntity(SiteEntity siteEntity);
    @Query("SELECT COUNT(p) FROM PageEntity p WHERE p.siteEntity.id = :siteId")
    int countBySiteId(@Param("siteId") int siteId);
}
