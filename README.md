# searchengine

Поисковый движок на Java + Spring Boot.

## Технологии
- Java 17
- Spring Boot 3
- Jsoup
- MySQL

## Конфигурация RickBot
```yaml
rickbot:
  user-agents:
    - "RickBot/1.0"
  min-delay-ms: 500
  max-delay-ms: 2000
  referer: "https://github.com/Rick/MySearchEngine"

---

### 2. **Структура проекта**
- `src/main/java/...` — код проекта.
- `src/main/resources/application.yml` — конфиги.
- `.gitignore` — чтобы не заливать IDE-файлы, `.class` и т.п.
- README.md — как выше.

---

### 3. **При желании**
- Добавить LICENSE (MIT или Apache 2.0) — чтобы не было юридических вопросов.
- Добавить пример `.env` или `application-example.yml` без паролей для конфигураций.
- Мелкие инструкции по сборке через Maven/Gradle.
