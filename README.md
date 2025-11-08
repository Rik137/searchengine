# SearchEngine  
---
A search engine built with Java using Spring Boot and MySQL  
SearchEngine is a Spring Boot application that implements full-text search across locally indexed website pages.  
The system supports Russian lemmatization and provides an API for retrieving search results.  
API Documentation:  
Each project method is thoroughly documented. For convenient viewing and testing, the API uses Springdoc OpenAPI UI via the dependency springdoc-openapi-ui.  
Tech Stack  
Language: Java 17  
Framework: Spring Boot 2.7.1  
HTML Parser: Jsoup 1.16.1  
ORM: Spring Data JPA  
Database: MySQL  
Dependency Management: Maven  
Logging & Utilities: Lombok 1.18.32, SLF4J  
DB Migrations: Liquibase  
Templates (if using web interface): Thymeleaf  
Maven and Dependencies (pom.xml)  
The project uses Maven for dependency management and build:  
spring-boot-starter-web — for REST API and web functionality.  
spring-boot-starter-data-jpa — database interaction via JPA/Hibernate.  
spring-boot-starter-validation — request data validation.  
spring-boot-starter-thymeleaf — for page templating (if web UI).  
jsoup — parsing HTML pages and extracting text/tags.  
mysql-connector-java — MySQL driver.  
lombok — reduces boilerplate code (@Getter, @Setter, @Slf4j).  
Apache Lucene Morphology — Russian lemmatization.  
Liquibase — database versioning and schema management.  
Application Configuration (application.yml)  
Example configuration with comments:  
server:
  port: 8080 # Port where the Spring Boot server runs

logging:
  level:
    org.apache.coyote.http11.Http11Processor: ERROR # Reduce Tomcat log noise

## RickBot — the crawler that scans websites and indexes pages  
rickbot:
  user-agents:
    - "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    - "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)"
    - "RickBot/1.0"
  min-delay-ms: 500 # minimum delay between requests
  max-delay-ms: 2000 # maximum delay between requests
  referer: "https://github.com/yourusername/searchengine/blob/main/README.md"

spring:
  liquibase:
    change-log: classpath:db/changelog/changelog-master.xml # Liquibase migration scripts
  datasource:
    username: root
    password: password_here
    url: jdbc:mysql://localhost:3306/search_engine?useSSL=false&allowPublicKeyRetrieval=true
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: none # DB schema managed by Liquibase
    show-sql: false # for debugging SQL output

### Sites to be indexed  
indexing-settings:
  sites:
    - url: https://nikoartgallery.com/
      name: Nikoargallery.com
    - url: https://www.playback.ru
      name: PlayBack.Ru
🔹 Comments help to understand each block and adjust configuration for your sites and database.

---

### Project Structure  
```java
src/  
└─ main/  
├─ java/... # Application source code  
└─ resources/  
├─ application.yml # Spring and DB configurations  
└─ db/changelog/ # Liquibase scripts  
.gitignore # IDE/class/target exclusions  
README.md # Project documentation  
pom.xml # Maven configuration  
```
---

### Build and Run  

1. Clone the repository:  
```bash
git clone https://github.com/yourusername/searchengine.git
cd searchengine
Configure MySQL database:
CREATE DATABASE search_engine;
Build the project:
mvn clean install
Run:
mvn spring-boot:run
After startup, the API will be available at http://localhost:8080
Database Migrations (Liquibase)
Before running the app, make sure your database is created and accessible.
Example Liquibase config in application.yml:
spring:
  liquibase:
    change-log: classpath:db/changelog/changelog-master.xml
File src/main/resources/db/changelog/changelog-master.xml contains versioned schema changes, for example:
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <!-- 1. Change column type -->
    <changeSet id="1" author="rik">
        <modifyDataType tableName="pages" columnName="path" newDataType="TEXT"/>
    </changeSet>

    <!-- 2. Create index if not exists -->
    <changeSet id="2" author="rik">
        <preConditions onFail="MARK_RAN">
            <not>
                <indexExists indexName="idx_path" tableName="pages"/>
            </not>
        </preConditions>
        <createIndex indexName="idx_path" tableName="pages">
            <column name="path" type="varchar(255)"/>
        </createIndex>
    </changeSet>

    <!-- 3. Alternative via SQL -->
    <changeSet id="3" author="rik">
        <preConditions onFail="MARK_RAN">
            <not>
                <indexExists indexName="idx_path" tableName="pages"/>
            </not>
        </preConditions>
        <modifyDataType tableName="pages" columnName="path" newDataType="TEXT"/>
        <sql>
            CREATE INDEX idx_path ON pages (path(255));
        </sql>
    </changeSet>

</databaseChangeLog>
💡 Notes:
Liquibase automatically applies all pending changes at startup.
If a column or index already exists, the change is skipped (onFail="MARK_RAN").
This ensures safe DB schema versioning and deployment readiness.
Project File Tree
searchengine  
├─ config/    # Application configurations  
│   ├─ RickBotConfig.java  
│   ├─ Site.java  
│   └─ SiteList.java  
├─ controllers/  # REST API and web controllers  
│   ├─ ApiController.java  
│   └─ DefaultController.java  
├─ dto/      # Data Transfer Objects  
│   ├─ ApiResponse.java  
│   ├─ PageResponse.java  
│   ├─ statistics/  
│   │   ├─ DetailedStatisticsItem.java  
│   │   ├─ StatisticsData.java  
│   │   ├─ StatisticsResponse.java  
│   │   └─ TotalResponse.java  
│   └─ search/  
│       ├─ SearchResponse.java  
│       └─ SearchResult.java  
├─ model/          # Database entities  
│   ├─ PageEntity.java  
│   ├─ LemmaEntity.java  
│   ├─ IndexEntity.java  
│   ├─ SiteEntity.java  
│   └─ Status.java  
├─ repositories/  # JPA repositories  
│   ├─ PageRepository.java  
│   ├─ LemmaRepository.java  
│   ├─ IndexRepository.java  
│   └─ SiteRepository.java  
├─ log  
│   └─ LogTag.java  
├─ services/   # Business logic implementation  
│   ├─ IndexingServiceImpl.java  
│   ├─ PageIndexingServiceImpl.java  
│   ├─ SearchServiceImpl.java  
│   ├─ StatisticsServiceImpl.java  
│   ├─ LemmaProcessor.java  
│   ├─ LemmaFrequencyService.java  
│   ├─ DataManager.java  
│   ├─ ManagerTasks.java  
│   ├─ serviceinterface/   # Service interfaces  
│   │  ├─ IndexingService.java  
│   │  ├─ PageIndexingService.java  
│   │  ├─ SearchService.java  
│   │  └─ StatisticsService.java  
│   ├─ tasks/   # Multithreaded indexing tasks  
│   │  ├─ PageTask.java  
│   │  ├─ SiteTask.java  
│   │  └─ SitesTask.java  
│   └─ utils/   # Helper classes and utilities  
│       ├─ EntityFactory.java  
│       ├─ IndexingContext.java  
│       ├─ LemmaFilter.java  
│       ├─ ManagerJSOUP.java  
│       ├─ RickBotClient.java  
│       ├─ SearchBuilder.java  
│       ├─ Stopwatch.java  
│       └─ VisitedUrlStore.java  
├─ Application.java   # Spring Boot entry point  
└─ resources/  
    ├─ db/changelog/   # Liquibase scripts  
    │   └─ changelog-master.xml  
    ├─ static/   # CSS, fonts, frontend assets  
    │   └─ assets  
    │      ├─ css/  
    │      ├─ fonts/  
    │      ├─ img/  
    │      ├─ js/  
    │      └─ plg/  
    ├─ templates/  # Thymeleaf templates  
    │   └─ index.html  
    └─ application.yml  # Application configuration  
Using the API
All endpoints are available under the base path:
http://localhost:8080/api
Start indexing all sites
GET /api/startIndexing
Example:
curl -X GET http://localhost:8080/api/startIndexing
Response:
{
  "result": true,
  "error": null
}
Stop indexing
GET /api/stopIndexing
Example:
curl -X GET http://localhost:8080/api/stopIndexing
Get statistics
GET /api/statistics
Example:
curl -X GET http://localhost:8080/api/statistics
Response:
{
  "totalPages": 120,
  "totalLemmas": 4500,
  "sites": [
    {
      "url": "https://nikoartgallery.com",
      "status": "INDEXED",
      "pages": 70
    },
    {
      "url": "https://www.playback.ru",
      "status": "INDEXED",
      "pages": 50
    }
  ]
}
Index a single page
POST /api/indexPage?url={URL}
Example:
curl -X POST "http://localhost:8080/api/indexPage?url=https://nikoartgallery.com/art1"
Search by query or site
GET /api/search?query={query}&site={site}&offset=0&limit=20
Example:
curl -X GET "http://localhost:8080/api/search?query=картина&site=https://nikoartgallery.com"
Response:
{
  "result": true,
  "count": 5,
  "data": [
    {
      "title": "Painting 'Sunset'",
      "snippet": "The painting 'Sunset' is an oil on canvas — one of the artist’s most well-known works...",
      "url": "https://nikoartgallery.com/art1"
    },
    {
      "title": "Painting 'Morning'",
      "snippet": "The morning landscape captures soft tones of light and sky, evoking a calm atmosphere...",
      "url": "https://nikoartgallery.com/art2"
    }
  ]
}
