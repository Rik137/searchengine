package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.List;

/**
 * Entity representing a lemma (the base form of a word) used in website indexing.
 * <p>Table: {@code lemmas}</p>
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "lemmas")
public class LemmaEntity {

    /** 
    * Unique identifier of the entry
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    /** 
    * The site to which the lemma belongs
    */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity siteEntity;

    /** 
    * The text of the lemma (e.g., "search") 
    */
    @Column(name = "lemma", nullable = false, columnDefinition = "VARCHAR(255)")
    private String lemma;

    /** 
    * Frequency of the lemma on the site 
    */
    @Column(name = "frequency", nullable = false)
    private int frequency;

    /** 
    * List of indexes where this lemma is used
    */
    @OneToMany(mappedBy = "lemmaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IndexEntity> indexEntityList;
}
