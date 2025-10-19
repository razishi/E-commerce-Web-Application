package com.example.demo.controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * This controller handles file uploads (images or documents) for admin users.
 * It displays a form and processes the uploaded file into the static directory.
 */
@Controller
public class UploadController {

    //  Defines the target path where files will be saved on disk.
    // This location is inside the project and gets bundled in the JAR during build.
    private static final Path TARGET = Path.of(System.getProperty("user.dir"), "uploads");

    /**
     * Displays the upload form (upload.html).
     *
     * @return the name of the Thymeleaf template to render.
     */
    @GetMapping("/upload")
    public String showForm() {
        return "upload";            // renders upload.html
    }

    /**
     * Handles the file upload after form submission.
     *
     * @param file the uploaded file
     * @param ra redirect attributes to store feedback messages
     * @return redirect to the upload page with a flash message
     * @throws IOException if there's an error saving the file
     */
    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file,
                               RedirectAttributes ra) throws IOException {

        //  Check if no file was selected

        if (file.isEmpty()) {
            ra.addFlashAttribute("message", "Please choose a file!");
            return "redirect:/upload";
        }

        //  Clean and extract the file name safely (avoids path injection issues)
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        //  Copy the uploaded file to the target location
        Files.copy(file.getInputStream(), TARGET.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);

        // Show success message with file path reference
        ra.addFlashAttribute("message",
                "Saved! Use /images/uploads/" + filename + " in the product record.");
        return "redirect:/upload";
    }
}
