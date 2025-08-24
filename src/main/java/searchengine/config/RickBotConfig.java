package searchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "rickbot")
@Data
public class RickBotConfig {
    private List<String> userAgents;
    private int minDelayMs;
    private int maxDelayMs;
    private String referer;
}