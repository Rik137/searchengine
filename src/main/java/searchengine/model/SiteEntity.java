package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sites")
public class SiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    private Status status;

    @Column(name = "status_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "url", nullable = false, columnDefinition = "VARCHAR(255) ")
    private String url;

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

//    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<PageEntity> pageEntityList;
@OneToMany(mappedBy = "siteEntity", cascade = CascadeType.ALL, orphanRemoval = true)
private final List<PageEntity> pageEntityList = new ArrayList<>();
//    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<LemmaEntity> lemmaEntityList;
@OneToMany(mappedBy = "siteEntity", cascade = CascadeType.ALL, orphanRemoval = true)
private final List<LemmaEntity> lemmaEntityList = new ArrayList<>();
}
