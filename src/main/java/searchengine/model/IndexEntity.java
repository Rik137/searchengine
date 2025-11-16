package searchengine.model;

import lombok.*;

import javax.persistence.*;
/**
 * Entity representing the relationship between a page and a lemma.
 * <p>
 * The {@code search_indexes} table stores weight coefficients (rank),
 * indicating the significance of a lemma on a specific page.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="search_indexes")

public class IndexEntity {

    /** 
    *Unique identifier of the record
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
    * Reference to the page where the lemma occurs
    */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private PageEntity pageEntity;

    /** 
    * Reference to the lemma occurring on the page
    */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lemma_id", nullable = false)
    private LemmaEntity lemmaEntity;

    /** 
    * Weight (significance) of the lemma on this page
    */
    @Column(name = "rank_value", nullable = false)
    private float rank;
}
