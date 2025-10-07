package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
    Optional<SiteEntity> findByUrl(String url);
    void deleteByUrl(String url);
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SiteEntity s")
    boolean hasAnySites();
}