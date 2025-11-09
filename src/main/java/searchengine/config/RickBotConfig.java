package searchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * Configuration for RickBot, loaded from application.yml / application.properties
 * with the prefix {@code rickbot}.
 * <p>
 * Contains parameters for the bot’s operation, including user-agent, request delays, and referer.
 */

@Configuration
@ConfigurationProperties(prefix = "rickbot")
@Data
public class RickBotConfig {

    /**
     * A list of User-Agent strings that the bot can use when making requests.
     * Allows simulating different browsers to bypass restrictions.
     */
    private List<String> userAgents;

     /**
     * Minimum delay between requests in milliseconds.
     * Used to prevent blocking by websites.
     */
    private int minDelayMs;

    /**
     * Maximum delay between requests in milliseconds.
     * Used to introduce random pauses between requests.
     */
    private int maxDelayMs;

    /**
     * HTTP referer that the bot will send with requests.
     * Allows emulating a visit from a specific page.
     */
    private String referer;
}
