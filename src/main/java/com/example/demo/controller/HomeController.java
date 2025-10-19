
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles the routing to the home page.
 */
@Controller
public class HomeController {

    /**
     * Maps the "/home" URL to the "home.html" Thymeleaf template.
     *
     * @return the name of the view to render (home.html in templates folder)
     */
    @GetMapping("/home")
    public String home() {
        return "home";
    }
}
