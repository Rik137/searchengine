package searchengine.config;

import lombok.Data;

/**
 * The Site class represents information about a website.
 * <p>
 * Contains the website’s URL and its display name.
 */

@Data
public class Site {

    /**
     * Website URL, for example: "https://example.com"
     */
    private String url;
    
    /**
     * Website name for display in the interface or logs
     */
    private String name;
}
