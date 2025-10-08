package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.*;
import searchengine.services.util.EntityFactory;

import javax.naming.directory.SearchResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LemmaFrequencyService {

    private final ManagerRepository managerRepository;
    private final LemmaProcessor lemmaProcessor;
    private final EntityFactory entityFactory;

    @Transactional
    public void decreaseLemmaFrequencies(PageEntity page){
        String content = page.getContent();
        SiteEntity site = page.getSiteEntity();
        if (content == null || content.isBlank()) {
            log.warn("Пустой контент для страницы id={}", page.getId());
            return;
        }

        Map<String, Integer> lemmas = lemmaProcessor.getLemmas(content);
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaName = entry.getKey();
            int countToRemove = entry.getValue();

            managerRepository.findLemma(lemmaName, site).ifPresentOrElse(
                    lemmaEntity -> {
                        int newFrequency = lemmaEntity.getFrequency() - countToRemove;
                        lemmaEntity.setFrequency(Math.max(newFrequency, 0));

                        if (lemmaEntity.getFrequency() == 0) {
                            managerRepository.deleteLemma(lemmaEntity.getId());
                            log.debug("Удалена лемма '{}'", lemmaName);
                        } else {
                            managerRepository.saveLemma(lemmaEntity);
                        }
                    },
                    () -> log.debug("Лемма '{}' не найдена в БД", lemmaName)
            );
        }
    }
    @Transactional
    public void savePageLemmasAndIndexes(PageEntity page, String content){
        if (content == null || content.isBlank()) {
            log.warn("Пустой контент, сохранение лемм пропущено для страницы id={}", page.getId());
            return;
        }
       Map<String, Integer> lemmas = lemmaProcessor.getLemmas(content);
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaName = entry.getKey();
            int frequencyToAdd = entry.getValue();

            Optional<LemmaEntity> lemmaOpt = managerRepository.findLemma(
                    lemmaName,
                    page.getSiteEntity().getId()
            );

            LemmaEntity lemmaEntity;

            if (lemmaOpt.isEmpty()) {
                lemmaEntity = entityFactory.createLemmaEntity(page.getSiteEntity(), lemmaName, frequencyToAdd);
                managerRepository.saveLemma(lemmaEntity);
                log.debug("Создана новая лемма '{}'", lemmaName);
            } else {
                lemmaEntity = lemmaOpt.get();
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + frequencyToAdd);
                managerRepository.saveLemma(lemmaEntity);
            }

            IndexEntity index = entityFactory.createIndexEntity(page, lemmaEntity, frequencyToAdd);
            managerRepository.saveIndex(index);
        }
    }
    public synchronized void savePageLemmasAndIndexesThreadSafe(PageEntity page, String content){
        savePageLemmasAndIndexes(page, content);
    }

    @Transactional
    public void recalculateRankForAllSites(SiteEntity site){
            int totalPages = managerRepository.getCountPagesBySite(site);
            List<LemmaEntity> lemmas = managerRepository.findAllLemmasBySite(site);

            for (LemmaEntity lemma : lemmas) {
                int df = managerRepository.getCountPagesWhereLemma(lemma, site);
                List<IndexEntity> indexes = managerRepository.getAllIndexesBySite(lemma, site);

                for (IndexEntity index : indexes) {
                    float tf = index.getRank();
                    float newRank = (float) (tf * Math.log((double) totalPages / (df + 1)));
                    index.setRank(newRank);
                }
                managerRepository.saveIndex(indexes);
        }
    }
    public List<SearchResult> findSearchResult(String query, String url, int offset, int limit){
        Map<String, Integer> lemmas = lemmaProcessor.getLemmas(query);
        List<LemmaEntity> listLemmasEntity = getLemmaFromDataBase(lemmas, url);
        if(listLemmasEntity.isEmpty()){
            log.error("нет подходящих лемм в БД");
            return List.of();
        }
        return List.of();
    }
    private List<LemmaEntity> getLemmaFromDataBase( Map<String, Integer> lemmas, String url){
        List<String> list = lemmas.keySet().stream().toList();
       if(url != null){
           return managerRepository.findLemmas(list, url);
       }
       return managerRepository.findLemmas(list);
    }
}
