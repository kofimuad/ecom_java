package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.Category;

import java.util.List;
import java.util.UUID;

public interface ICategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(UUID id);
    Category updateCategoryById(UUID id, Category category);
    Category createCategory(Category category);
    String deleteCategory(UUID categoryId);
}
