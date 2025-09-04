package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;
import searchengine.services.util.LemmaFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LemmaProcessor {

    private final LemmaFilter lemmaFilter;

    /**
     * Генерирует список всех лемм из текста.
     */
    private List<String> generateLemmas(String text) {
        List<String> words = lemmaFilter.tokenizeText(text);
        return words.stream()
                .flatMap(word -> lemmaFilter.getLuceneMorphology()
                        .getNormalForms(word).stream())
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
}

