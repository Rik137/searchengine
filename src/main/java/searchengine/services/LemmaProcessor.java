package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.WrongCharaterException;
import org.springframework.stereotype.Service;
import searchengine.logs.LogTag;
import searchengine.services.util.LemmaFilter;
import java.util.*;
import java.util.stream.Stream;

 /**
 * Service for text processing and lemma extraction.
 *
 * <p>Main functions of the class:
 * <ul>
 *     <li>Tokenizing text into individual words</li>
 *     <li>Cleaning and normalizing words</li>
 *     <li>Obtaining the base forms of words via LuceneMorphology</li>
 *     <li>Counting lemma occurrence frequencies</li>
 *     <li>Generating a unique list of lemmas for search queries</li>
 * </ul>
 *
 * <p>Used for page indexing, lemma frequency counting, and preparing data
 * for site search.
 *
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class LemmaProcessor {

    private static final LogTag TAG = LogTag.LEMMA;

    /** 
    * Component for working with lemmas: tokenization, normalization, morphology
    */
    private final LemmaFilter lemmaFilter;


    /**
    * Generates a list of lemmas from text.
    *
    * @param text the source text
    * @return list of lemmas (may contain duplicates)
    */
    private List<String> generateLemmas(String text) {
        return lemmaFilter.tokenizeText(text).stream()
                .map(lemmaFilter::normalizeRussianWord)
                .filter(word -> !word.isEmpty())
                .flatMap(word -> {
                    try {
                        return lemmaFilter.getLuceneMorphology().getNormalForms(word).stream();
                    } catch (WrongCharaterException e) {
                        log.debug("{}  Skipped invalid word for morphology: {}", TAG, word);
                        return Stream.empty();
                    }
                })
                .toList();
    }

    /**
    * Counts the occurrences of each lemma.
    *
    * @param lemmas list of lemmas
    * @return mapping of "lemma -> count"
    */
    private Map<String, Integer> countLemmas(List<String> lemmas) {
        Map<String, Integer> countMap = new HashMap<>();
        for (String lemma : lemmas) {
            countMap.put(lemma, countMap.getOrDefault(lemma, 0) + 1);
        }
        return countMap;
    }

    /**
    * Retrieves a mapping of all lemmas with their frequencies for the given text.
    *
    * @param text the source text
    * @return map of lemma -> count
    */
    public Map<String, Integer> getLemmas(String text) {
        List<String> lemmas = generateLemmas(text);
        return countLemmas(lemmas);
    }

    /**
    * Retrieves a list of unique lemmas suitable for search.
    *
    * @param text the source text
    * @return list of unique lemmas
    */
    public List<String> getLemmasForSearch(String text) {
        return new ArrayList<>(new LinkedHashSet<>(generateLemmas(text)));
    }
}

