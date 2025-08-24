package searchengine.services;

import ch.qos.logback.classic.filter.LevelFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.services.util.LemmaFilter;

@Service
@Slf4j
@RequiredArgsConstructor
public class LuceneLemmaMake {
    private final LemmaFilter lemmaFilter;
}
