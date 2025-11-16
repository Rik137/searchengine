package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

 /**
 * Entity representing a website.
 * <p>Table: {@code sites}</p>
 */

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sites")

public class SiteEntity {

    /** 
    * Unique identifier of the site record
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    /** 
    * Indexing status of the site 
    */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    private Status status;

    /** 
    * Time of the last status update 
    */
    @Column(name = "status_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime statusTime;

    /** 
    * Error message from the last indexing 
    */
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    /** 
    * URL site
    */
    @Column(name = "url", nullable = false, columnDefinition = "VARCHAR(255) ")
    private String url;

    /** 
    * name site
    */
    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    /**
    * list pages of site
    */
    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PageEntity> pageEntityList = new ArrayList<>();

    /**
    * list lemmas of site
    */
    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<LemmaEntity> lemmaEntityList = new ArrayList<>();
}
