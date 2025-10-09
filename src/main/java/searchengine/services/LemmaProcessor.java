package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.WrongCharaterException;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;
import searchengine.services.util.LemmaFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class LemmaProcessor {

    private final LemmaFilter lemmaFilter;

    /**
     * Генерирует список всех лемм из текста.
     */
    private List<String> generateLemmas(String text) {
        return lemmaFilter.tokenizeText(text).stream()
                .map(lemmaFilter::normalizeRussianWord)   // очистка
                .filter(word -> !word.isEmpty())           // убираем пустые
                .flatMap(word -> {
                    try {
                        return lemmaFilter.getLuceneMorphology().getNormalForms(word).stream();
                    } catch (WrongCharaterException e) {
                        log.debug("Пропущено некорректное слово для морфологии: {}", word);
                        return Stream.empty();
                    }
                })
                .toList();
    }

    /**
     * Считает количество каждой леммы в списке слов.
     */
    private Map<String, Integer> countLemmas(List<String> lemmas) {
        Map<String, Integer> countMap = new HashMap<>();
        for (String lemma : lemmas) {
            countMap.put(lemma, countMap.getOrDefault(lemma, 0) + 1);
        }
        return countMap;
    }

    /**
     * Основной метод для получения всех лемм с их количеством из текста.
     */
    public Map<String, Integer> getLemmas(String text) {
        List<String> lemmas = generateLemmas(text);
        return countLemmas(lemmas);
    }
    public List<String> getLemmasForSearch(String text) {
        return generateLemmas(text).stream()
                .distinct() // убирает дубликаты
                .toList();  // требует Java 16+
    }
}

