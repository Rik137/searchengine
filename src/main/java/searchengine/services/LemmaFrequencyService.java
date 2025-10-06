package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.services.util.EntityFactory;
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
        if (content == null || content.isBlank()) {
            log.warn("Пустой контент для страницы id={}", page.getId());
            return;
        }

        Map<String, Integer> lemmas = lemmaProcessor.getLemmas(content);
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaName = entry.getKey();
            int countToRemove = entry.getValue();

            managerRepository.findLemma(lemmaName).ifPresentOrElse(
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

            Optional<LemmaEntity> lemmaOpt = managerRepository.findLemma(lemmaName);
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

            IndexEntity index = entityFactory.createIndexEntity(page, lemmaEntity);
            managerRepository.saveIndex(index);
        }
    }
}
