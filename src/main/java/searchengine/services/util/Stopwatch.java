package searchengine.services.util;

import lombok.Getter;

@Getter
public class Stopwatch {
    private long time;

    public void start() {
        time = System.nanoTime();
    }

    public void stop() {
        time = (System.nanoTime() - time) / 1000_000_000;
    }
}
