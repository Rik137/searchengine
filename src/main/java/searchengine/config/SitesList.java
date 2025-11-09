package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Configuration bean containing a list of websites to be indexed.
 * <p>
 * The list is loaded from application.yml / application.properties
 * with the prefix {@code indexing-settings}.
 */

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {

    /**
     * List of websites to be indexed.
     * Each website is represented by a {@link Site} object.
     */
    private List<Site> sites;
}
