package searchengine.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {

   /**
   * This method generates a page from the HTML file `index.html`,
   * located in the `resources/templates` directory.
   * The Thymeleaf library handles this process.
   */
    @RequestMapping("/")
    public String index() {
        return "index";
    }
}
