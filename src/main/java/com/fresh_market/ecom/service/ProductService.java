package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService implements IProductService {

    private final JdbcTemplate jdbcTemplate;

    public ProductService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productRowMapper = (rs, numRow) -> {
        Product product = new Product();
        product.setId((UUID) rs.getObject("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setSku(rs.getString("sku"));
        product.setCategoryId((UUID) rs.getObject("category_id"));
        product.setActive(rs.getBoolean("is_active"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setProductType(rs.getString("product_type"));
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if  (created != null) {
            product.setCreatedAt(created.toLocalDateTime());
        }
        if  (updated != null) {
            product.setUpdatedAt(updated.toLocalDateTime());
        }
        return product;
    };

    @Override
    public List<Product> getAllProducts() {
        String sql = "select * from products";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    @Override
    public Product getProductById(UUID id) {
        String sql = "select * from products where id = ?::uuid";
        return jdbcTemplate.queryForObject(sql, productRowMapper, id);
    }

    @Override
    public Product createProduct(Product product) {
        product.setId(UUID.randomUUID());
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        String sql = """
                INSERT into products
                (id, name, description, price, stock_quantity, product_type, sku, category_id, is_active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getProductType(),
                product.getSku(),
                product.getCategoryId(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
        return product;
    }

    @Override
    public Product updateProduct(UUID id, Product product) {
        product.setUpdatedAt(LocalDateTime.now());

        String sql = """
                UPDATE products
                SET name = ?, description  = ?, price = ?, stock_quantity = ?, product_type = ?, sku = ?, category_id = ?::uuid, is_active = ?, updated_at = ?
                WHERE id = ?::uuid
                """;
        jdbcTemplate.update(
                sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getProductType(),
                product.getSku(),
                product.getCategoryId(),
                product.isActive(),
                product.getUpdatedAt(),
                id
        );

        return product;
    }

    @Override
    public void deleteProduct(UUID id) {
        getProductById(id); // Returns 404 if not found
        String sql = """
                DELETE FROM products WHERE id = ?::uuid
        """;
        jdbcTemplate.update(sql, id);
    }
}