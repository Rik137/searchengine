package searchengine.logs;

/**
 * Enum для тегов логов. Используется для унифицированного префикса в логах,
 * чтобы быстро определять подсистему или компонент, откуда пришло сообщение.
 */

public enum LogTag {

    // Контроллер API
    API("API"),

    // Задачи индексации страниц
    PAGE_TASK("PAGE-TASK"),

    // Задачи для сайтов (например, обход и обработка)
    SITE_TASK("SITE-TASK"),

    // Менеджеры задач
    MANAGER_TASKS("MANAGER_TASKS"),

    // Обработка лемм
    LEMMA("LEMMA"),

    // Основная индексация
    INDEXING("INDEXING"),

    // Статистика
    STATISTICS("STATISTICS"),

    // Клиент RickBot
    RICK_BOT_CLIENT("RBC"),

    // Контекст индексации
    INDEXING_CONTEXT("IDX_CTX"),

    // Менеджер JSoup
    JSOUP_MANAGER("JSOUP"),

    // Сервер индексации
    INDEXING_SERVER("INDEX-SERVER"),

    // Сервер лемм и частот
    LEMMA_FREQUENCY_SERVER("LEMMA-SERVER"),

    // Менеджер данных / DB
    DATA_MANAGER("DATA-MANAGER"),

    // Сервер индексации конкретных страниц
    PAGE_INDEXING_SERVER("PAGE-SERVER"),

    // Сервис поиска
    SEARCH_SERVER("SEARCH-SERVER");

    private final String tag;

    LogTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "[" + tag + "]";
    }
}
