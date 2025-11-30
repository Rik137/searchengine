package searchengine.services.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * Component for preprocessing text and filtering words.
 *
 * <p>Main responsibilities of the class:
 * <ul>
 *     <li>Removing HTML tags from text via {@link ManagerJSOUP}</li>
 *     <li>Tokenizing text into individual words</li>
 *     <li>Normalizing words: converting to lowercase and removing extraneous characters</li>
 *     <li>Filtering out functional parts of speech (conjunctions, interjections, particles, etc.) using LuceneMorphology</li>
 *     <li>Validating that a word is a proper Russian word</li>
 * </ul>
 *
 * <p>Used for preparing text before lemma extraction and indexing.
 */

@Component
@Getter
@RequiredArgsConstructor

public class LemmaFilter {

    private final ManagerJSOUP managerJSOUP;
    private LuceneMorphology luceneMorphology;
    private static final Set<String> SERVICE_POS = Set.of(
            "СОЮЗ", "МЕЖД", "ЧАСТ", "ПРЕД", "МС", "МС-П", "МС-С", "INTJ"
    );

    @PostConstruct
    private void init() {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException("LuceneMorphology initialization error", e);
        }
    }

    /**
    * Splits the text into individual words and filters out functional parts of speech.
    *
    * @param text the original HTML or plain text
    * @return a list of words suitable for further processing
    */
    public List<String> tokenizeText(String text) {
        List<String> words = prepareText(text);

        return words.stream()
                .filter(this::isNotServiceWord)
                .toList();
    }

    /**
    * Checks whether a word is a functional word (conjunction, interjection, particle, etc.).
    *
    * @param word the word to check
    * @return true if the word is NOT a functional word
    */
    private boolean isNotServiceWord(String word) {
        word = normalizeRussianWord(word);
        if(isValidRussianWord(word)) {
            List<String> morphInfo = luceneMorphology.getMorphInfo(word);
            return morphInfo.stream().noneMatch(info ->
                    SERVICE_POS.stream().anyMatch(info::contains)
            );
        }
        return false;
    }

    /**
    * Removes HTML tags from the text using {@link ManagerJSOUP}.
    *
    * @param text the original HTML
    * @return the text without HTML tags
    */
    private String filterTagHtml(String text) {
        return managerJSOUP.stripHtmlTags(text);
    }
    /**
    * Prepares text for tokenization: removes extraneous characters, converts it to lowercase,
    * filters out Latin characters and empty tokens.
    *
    * @param html the original HTML or text
    * @return a list of "clean" words
    */
    private List<String> prepareText(String html) {
        String text = filterTagHtml(html);

        return Arrays.stream(text.split("\\s+"))
                .map(word -> word.strip()
                        .replaceAll("[^\\p{L}]", "")
                        .toLowerCase())
                .filter(word -> !word.isEmpty())
                .filter(word -> !word.matches("[a-z]+"))
                .toList();
    }

    /**
    * Normalizes a Russian word by converting it to lowercase and removing extraneous characters.
    *
    * @param word the word to normalize
    * @return the normalized form of the word
    */
    public String normalizeRussianWord(String word) {
        return word.toLowerCase()
                .replaceAll("[^а-яё]", "");
    }

    /**
    * Checks whether a word is a valid Russian word.
    *
    * @param word the word to check
    * @return true if the word contains only letters а–я or ё
    */
    public boolean isValidRussianWord(String word) {
        return word.matches("[а-яё]+");
    }
}

