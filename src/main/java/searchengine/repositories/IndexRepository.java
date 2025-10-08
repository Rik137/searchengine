package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;

public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {
    // Количество страниц сайта, где встречается лемма
    @Query("SELECT COUNT(DISTINCT i.pageEntity.id) " +
            "FROM IndexEntity i " +
            "WHERE i.lemmaEntity = :lemma " +
            "AND i.pageEntity.siteEntity = :site")
    int countPagesContainingLemma(@Param("lemma") LemmaEntity lemma,
                                  @Param("site") SiteEntity site);
    // Получить все индексы конкретной леммы на конкретном сайте
    @Query("SELECT i FROM IndexEntity i " +
            "WHERE i.lemmaEntity = :lemma " +
            "AND i.pageEntity.siteEntity = :site")
    List<IndexEntity> findIndexesByLemmaAndSite(@Param("lemma") LemmaEntity lemma,
                                                @Param("site") SiteEntity site);

}
