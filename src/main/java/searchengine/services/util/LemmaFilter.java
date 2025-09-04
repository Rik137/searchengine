package searchengine.services.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.text.html.HTMLDocument;
import java.io.IOException;
import java.util.*;

@Component
@Getter
@RequiredArgsConstructor
public class LemmaFilter {

    private final ManagerJSOUP managerJSOUP;

    private LuceneMorphology luceneMorphology;

    // Служебные части речи, которые нужно фильтровать
    private static final Set<String> SERVICE_POS = Set.of(
            "СОЮЗ", "МЕЖД", "ЧАСТ", "ПРЕД", "МС", "МС-П", "МС-С", "INTJ"
    );

    @PostConstruct
    private void init() {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка инициализации LuceneMorphology", e);
        }
    }

    /**
     * Основной метод для получения чистых лемм из текста.
     * Очищает HTML, убирает английские слова и служебные части речи.
     */
    public List<String> tokenizeText(String text) {
        List<String> words = prepareText(text);

        return words.stream()
                .filter(this::isNotServiceWord)
                .toList();
    }

    /** Проверка, является ли слово служебной частью речи */
    private boolean isNotServiceWord(String word) {
        List<String> morphInfo = luceneMorphology.getMorphInfo(word);
        return morphInfo.stream().noneMatch(info ->
                SERVICE_POS.stream().anyMatch(info::contains)
        );
    }

    /** Убирает HTML-теги из текста */
    private String filterTagHtml(String text) {
        return managerJSOUP.stripHtmlTags(text);
    }

    /** Подготавливает текст: убирает HTML, небуквенные символы, английские слова и переводит в нижний регистр */
    private List<String> prepareText(String html) {
        String text = filterTagHtml(html);

        return Arrays.stream(text.split("\\s+"))
                .map(word -> word.strip()
                        .replaceAll("[^\\p{L}]", "")  // оставляем только буквы
                        .toLowerCase())
                .filter(word -> !word.isEmpty())       // убираем пустые строки
                .filter(word -> !word.matches("[a-z]+")) // убираем английские слова
                .toList();
    }
}

