package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {
    private String site;       // базовый адрес сайта, например "https://nikoartgallery.com"
    private String siteName;   // имя сайта, например "Галерея искусств Niko"
    private String uri;        // относительный путь страницы, например "/about" или "/page1"
    private String title;      // заголовок найденной страницы
    private String snippet;    // HTML-фрагмент с выделенными совпадениями
    private float relevance;   // степень релевантности
}
