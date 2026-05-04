package com.fresh_market.ecom.controller;

import com.fresh_market.ecom.model.Category;
import com.fresh_market.ecom.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @GetMapping("/public/categories")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping("/public/categories")
    public String createCategory(@RequestBody Category category) {
        categoryService.createCategory(category);
        return "Category created successfully";
    }

    @PutMapping("/admin/categories/{id}")
    public String updateCategory(@PathVariable UUID id, @RequestBody Category category) {
        categoryService.updateCategoryById(id, category);
        return "Category updated successfully";
    }

    @DeleteMapping("/admin/categories/{id}")
    public String deleteCategory(@PathVariable UUID id) {
        return categoryService.deleteCategory(id);
    }
}
