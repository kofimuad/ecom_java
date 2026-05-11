package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryService implements ICategoryService {
    private final JdbcTemplate jdbcTemplate;

    public CategoryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Category> categoryRowMapper = (rs, rowNum) -> {
        Category category = new Category();
        category.setId((UUID) rs.getObject("id"));
        category.setName((String) rs.getObject("name"));
        category.setDescription((String) rs.getObject("description"));
        category.setParentId((UUID) rs.getObject("parent_id"));
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if  (created != null) {
            category.setCreatedAt(created.toLocalDateTime());
        }
        if  (updated != null) {
            category.setUpdatedAt(updated.toLocalDateTime());
        }
        return category;
    };

    @Override
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM categories ORDER BY id DESC";
        return jdbcTemplate.query(sql, categoryRowMapper);
    }

    @Override
    public Category getCategoryById(UUID id) {
        String sql = "SELECT * FROM categories WHERE id = ?::uuid";
        return jdbcTemplate.queryForObject(sql, categoryRowMapper, id);
    }

    @Override
    public Category createCategory(Category category) {
        String sql = "INSERT INTO categories (name, description, parent_id) VALUES (?, ?, ?::uuid) RETURNING *";
        return jdbcTemplate.queryForObject(
                sql,
                categoryRowMapper,
                category.getName(),
                category.getDescription(),
                category.getParentId()
        );
    }

    @Override
    public Category updateCategoryById(UUID id, Category category) {
        String sql = """
                UPDATE categories
                SET name = ?,  description = ?, parent_id = ?::uuid, updated_at = NOW()
                WHERE id = ?::uuid
                RETURNING *
                """;
        return jdbcTemplate.queryForObject(
                sql,
                categoryRowMapper,
                category.getName(),
                category.getDescription(),
                category.getParentId(),
                id
        );
    }

    @Override
    public String deleteCategory(UUID id) {
        String sql = "DELETE FROM categories WHERE id = ?::uuid";
        int rows = jdbcTemplate.update(sql, id);
        if (rows > 0) {
            return "Category with categoryId: " + id + " deleted successfully";
        }
        return "No category found with categoryId: " + id;
    }
}
