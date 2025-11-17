package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import java.util.List;
import java.util.Optional;

/**
 * Repository for working with the {@link PageEntity} entity.
 * <p>Contains methods for searching, retrieving, and counting pages by website.</p>
 */

public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    /**
    * Finds a page by its path.
    *
    * @param path the path of the page
    * @return an Optional containing the found page
    */
    Optional<PageEntity> findByPath(String path);

    /**
    * Retrieves all pages for a website by its ID.
    *
    * @param siteId the ID of the website
    * @return a list of pages
    */
    @Query("SELECT p FROM PageEntity p WHERE p.siteEntity.id = :siteId")
    List<PageEntity> findAllBySiteId(@Param("siteId") int siteId);

    /**
    * Retrieves all pages for a website by the website object.
    *
    * @param siteEntity the website object
    * @return a list of pages
    */
    List<PageEntity> findAllBySiteEntity(SiteEntity siteEntity);

    /**
    * Counts the number of pages for a website by its ID.
    *
    * @param siteId the ID of the website
    * @return the number of pages
    */
    @Query("SELECT COUNT(p) FROM PageEntity p WHERE p.siteEntity.id = :siteId")
    int countBySiteId(@Param("siteId") int siteId);
}
