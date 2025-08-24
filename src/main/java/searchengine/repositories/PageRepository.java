package searchengine.repositories;

import org.springframework.data.repository.CrudRepository;
import searchengine.model.PageEntity;

import java.util.Optional;

public interface PageRepository extends CrudRepository<PageEntity, Integer> {
    Optional<PageEntity> findByPath(String path);
}
