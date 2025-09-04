package searchengine.services.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import searchengine.services.LemmaProcessor;
import searchengine.services.ManagerRepository;

@RequiredArgsConstructor
@Getter
@Setter
public class IndexingContext {
    private final EntityFactory entityFactory;
    private final ManagerRepository managerRepository;
    private final ManagerJSOUP managerJSOUP;
    private final LemmaProcessor lemmaProcessor;
    private final VisitedUrlStore visitedUrlStore;
}
