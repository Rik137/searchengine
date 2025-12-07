package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

 /**
 * The main class that launches the Spring Boot application.
 * <p>
 * Main functions:
 * <ul>
 *     <li>Entry point of the application</li>
 *     <li>Automatic Spring Boot configuration via {@link SpringBootApplication}</li>
 * </ul>
 */

@SpringBootApplication
public class Application {
    public static void main(String[] args) {

        /**
        * Entry point of the Spring Boot application.
        * <p>
        * This method calls {@link SpringApplication#run(Class, String[])} to launch the Spring context.
        *
        * @param args command-line arguments (optional)
        */
        SpringApplication.run(Application.class, args);
    }
}
