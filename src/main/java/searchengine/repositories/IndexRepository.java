package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import java.util.List;

 /**
 * Repository for working with the {@link IndexEntity} entity.
 * <p>Contains methods for retrieving lemma indexes and counting their occurrences on websites.</p>
 */

public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {

   /**
   * Counts the number of unique pages on a website where the specified lemma appears.
   *
   * @param lemma the lemma to search for
   * @param site the website to search the lemma on
   * @return the number of pages containing the given lemma
   */
    @Query("SELECT COUNT(DISTINCT i.pageEntity.id) " +
            "FROM IndexEntity i " +
            "WHERE i.lemmaEntity = :lemma " +
            "AND i.pageEntity.siteEntity = :site")
    int countPagesContainingLemma(@Param("lemma") LemmaEntity lemma,
                                  @Param("site") SiteEntity site);

   /**
   * Retrieves all indexes of the specified lemma on a particular website.
   *
   * @param lemma the lemma to search for
   * @param site the website to search the indexes on
   * @return a list of {@link IndexEntity} for the given lemma on the website
   */
    @Query("SELECT i FROM IndexEntity i " +
            "WHERE i.lemmaEntity = :lemma " +
            "AND i.pageEntity.siteEntity = :site")
    List<IndexEntity> findIndexesByLemmaAndSite(@Param("lemma") LemmaEntity lemma,
                                                @Param("site") SiteEntity site);

}
