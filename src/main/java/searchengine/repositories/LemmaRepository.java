package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import java.util.List;
import java.util.Optional;

 /**
 * Repository for working with the {@link LemmaEntity} entity.
 * <p>Contains methods for searching, counting, and retrieving lemmas by website.</p>
 */

public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {

   /**
   * Finds a lemma by its text and the website ID.
   *
   * @param lemma the text of the lemma
   * @param siteId the ID of the website
   * @return an Optional containing the found lemma
   */
    @Query("SELECT l FROM LemmaEntity l WHERE l.lemma = :lemma AND l.siteEntity.id = :siteId")
    Optional<LemmaEntity> findByLemmaAndSiteId(@Param("lemma") String lemma, @Param("siteId") Integer siteId);

   /**
   * Finds a lemma by its text and the website object.
   *
   * @param lemma the text of the lemma
   * @param site the website object
   * @return an Optional containing the found lemma
   */
    @Query("SELECT l FROM LemmaEntity l WHERE l.lemma = :lemma AND l.siteEntity = :site")
    Optional<LemmaEntity> findByLemmaAndSite(@Param("lemma") String lemma, @Param("site") SiteEntity site);


    /**
    * Checks whether any lemmas exist in the database.
    *
    * @return true if at least one lemma exists
    */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LemmaEntity l")
    boolean hasAnyLemmas();

    /**
    * Retrieves all lemmas for the specified website.
    *
    * @param site the website object
    * @return a list of lemmas
    */
    @Query("SELECT l FROM LemmaEntity l WHERE l.siteEntity = :site")
    List<LemmaEntity> findAllBySite(@Param("site") SiteEntity site);

    /**
    * Retrieves all lemmas from a list of texts.
    *
    * @param names the list of lemma texts
    * @return a list of lemmas
    */
    List<LemmaEntity> findByLemmaIn(List<String> names);

    /**
    * Retrieves lemmas from a list of texts and a website URL.
    *
    * @param names the list of lemma texts
    * @param siteUrl the URL of the website
    * @return a list of lemmas
    */
    List<LemmaEntity> findByLemmaInAndSiteEntity_Url(List<String> names, String siteUrl);

    /**
    * Counts the number of lemmas for a website by its ID.
    *
    * @param siteId the ID of the website
    * @return the number of lemmas
    */
    @Query("SELECT COUNT(l) FROM LemmaEntity l WHERE l.siteEntity.id = :siteId")
    int countLemmasBySite(@Param("siteId") Integer siteId);
}

