package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import java.util.Optional;

/**
 * Repository for working with the {@link SiteEntity} entity.
 * <p>Contains methods for searching, deleting, and checking the existence of websites.</p>
 */

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

    /**
    * Finds a website by its URL.
    *
    * @param url the URL of the website
    * @return an Optional containing the found website
    */
    Optional<SiteEntity> findByUrl(String url);

    /**
    * Deletes a website by its URL.
    *
    * @param url the URL of the website
    */
    void deleteByUrl(String url);

    /**
    * Checks whether any websites exist in the database.
    *
    * @return true if at least one website exists
    */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SiteEntity s")
    boolean hasAnySites();
}
