package searchengine.services.util;

import lombok.Getter;

 /**
 * The {@code Stopwatch} class is a lightweight tool
 * for precise measurement of operation execution time
 */

@Getter
public class Stopwatch {

     /**
     * The start time in nanoseconds
     */
     private long startTime;

     /**
     * Measured time in nanoseconds
     */
     private long elapsedNanos;

    /**
    * Starts the time measurement
    */
    public void start() {
        startTime = System.nanoTime();
    }

     /**
     * Stops the measurement and returns the elapsed time in seconds
     *
     * @return the elapsed time in seconds
     */
     public double stop() {
        elapsedNanos = System.nanoTime() - startTime;
        return getSeconds();
    }

     /**
     * Returns the measured time in seconds
     */
     public double getSeconds() {
        return elapsedNanos / 1_000_000_000.0;
    }

    /**
    * Returns the measured time in milliseconds.
    */
    public double getMillis() {
        return elapsedNanos / 1_000_000.0;
    }

    /**
    * Resets the measured data, allowing the timer to be reused
    */
    public void reset() {
        startTime = 0L;
        elapsedNanos = 0L;
    }
}
