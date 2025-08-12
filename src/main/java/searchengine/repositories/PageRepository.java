package searchengine.repositories;

import org.springframework.data.repository.CrudRepository;
import searchengine.model.PageEntity;

public interface PageRepository extends CrudRepository<PageEntity, Integer> {
    //TODO возможно нужно будет реализовать метод findByUrl(Url url)
}
