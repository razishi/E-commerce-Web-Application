package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 🔍 Search by name (used in filtering)
    List<Product> findByNameContainingIgnoreCase(String name);

    // 🔁 Get distinct category names for dropdown filter
    @Query("select distinct p.category.name from Product p")
    List<String> findDistinctCategories();

    // 📂 Find all products by category name
    List<Product> findByCategoryName(String category);

    // ✅ Used to check for duplicates when adding a product
    List<Product> findAllByNameIgnoreCase(String name);

    @Query("SELECT p, COALESCE(AVG(r.rating), 0) as avgRating " +
            "FROM Product p LEFT JOIN Review r ON r.product = p " +
            "GROUP BY p " +
            "ORDER BY avgRating DESC")
    List<Object[]> findAllOrderByRatingDesc();

    @Query("SELECT p, COALESCE(AVG(r.rating), 0) as avgRating " +
            "FROM Product p LEFT JOIN Review r ON r.product = p " +
            "GROUP BY p " +
            "ORDER BY avgRating ASC")
    List<Object[]> findAllOrderByRatingAsc();



}
