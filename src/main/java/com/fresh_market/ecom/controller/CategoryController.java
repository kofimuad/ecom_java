package com.fresh_market.ecom.controller;

import com.fresh_market.ecom.model.Category;
import com.fresh_market.ecom.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Categories", description = "Manages product categories")
@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @Operation(summary = "Get all categories")
    @GetMapping("/public/categories")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @Operation(summary = "Create a new category")
    @PostMapping("/public/categories")
    public String createCategory(@RequestBody Category category) {
        categoryService.createCategory(category);
        return "Category created successfully";
    }

    @Operation(summary = "Update a category")
    @PutMapping("/admin/categories/{id}")
    public String updateCategory(@PathVariable UUID id, @RequestBody Category category) {
        categoryService.updateCategoryById(id, category);
        return "Category updated successfully";
    }

    @Operation(summary = "Delete a category")
    @DeleteMapping("/admin/categories/{id}")
    public String deleteCategory(@PathVariable UUID id) {
        return categoryService.deleteCategory(id);
    }
}
