package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.model.Category;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.ProductService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/admin/products")
public class ProductAdminController {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public ProductAdminController(ProductRepository productRepository,
                                  CategoryRepository categoryRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    
        this.productService = productService;}

    /**
     * Displays the manage products page with a list of all products.
     */
    @GetMapping("/manage")
    public String showManage(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "admin_manage_products";
    }

    /**
     * Shows the add new product form.
     */
    @GetMapping("/add")
    public String showAdd(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin_add_product";
    }

    /**
     * Handles submission of new product form, validates uniqueness, handles image upload, saves to database.
     */
    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product,
                             @RequestParam("category.id") Long categoryId,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             RedirectAttributes redirectAttributes,
                             Model model) throws IOException {

        // Check if product name already exists (case-insensitive)
        if (!productRepository.findAllByNameIgnoreCase(product.getName()).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product name is already used. Choose another name.");
            return "redirect:/admin/products/add";
        }

        // Set category by ID
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
        product.setCategory(category);

        // Handle image file upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = StringUtils.cleanPath(imageFile.getOriginalFilename());
            Path uploadDir = Path.of(System.getProperty("user.dir"), "uploads");
            Files.createDirectories(uploadDir);
            Files.copy(imageFile.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            product.setImageUrl("uploads/" + filename);
        }

        // Save product
        productRepository.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "✅ Product added successfully!");
        return "redirect:/admin/products/add";
    }


    /**
     * Shows the edit form for an existing product.
     */
    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin_edit_product";
    }

    /**
     * Updates an existing product, handles optional image upload and category reassignment.
     */
    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute Product product,
                                @RequestParam("category.id") Long categoryId,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                Model model) throws IOException {

        // Set the product ID and category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
        product.setId(id);
        product.setCategory(category);

        // Handle image replacement or reuse existing one
        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = StringUtils.cleanPath(imageFile.getOriginalFilename());
            Path uploadDir = Path.of(System.getProperty("user.dir"), "uploads");
            Files.createDirectories(uploadDir);
            Files.copy(imageFile.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            product.setImageUrl("uploads/" + filename);
        } else {
            // Retain previous image if none is uploaded
            Product existing = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product ID"));
            product.setImageUrl(existing.getImageUrl());
        }

        // Save updated product
        productRepository.save(product);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("successMessage", "✅ Product updated successfully!");
        return "admin_edit_product";
    }

    /**
     * Deletes a product by ID and redirects to the manage page.
     */
    @GetMapping("/delete/{id}")
    
public String deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return "redirect:/admin/products/manage";
}

}
