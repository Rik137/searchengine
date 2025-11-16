package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.List;

/**
 * Entity representing a website page.
 * <p>Table: {@code pages}</p>
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "pages", indexes = {
        @Index(name = "idx_path", columnList = "path")
})

public class PageEntity {

    /** 
    * Unique identifier of the page record
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** 
    * The site to which the page belongs
    */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity siteEntity;

    /** 
    * Path of the page (e.g., "/about") 
    */
    @Column(name = "path", nullable = false, columnDefinition = "TEXT")
    private String path;

    /** 
    * HTTP response code of the page 
    */
    @Column(name = "code", nullable = false)
    private int code;

    /** 
    * HTML content of the page
    */
    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    /** 
    * Indexes (lemmas and their weights) associated with this page 
    */
    @OneToMany(mappedBy = "pageEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IndexEntity> indexEntityList;
}
