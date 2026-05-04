package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.ProductImage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductImageService implements IProductImage {

    private final JdbcTemplate jdbcTemplate;

    public ProductImageService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static class ProductImageRowMapper implements RowMapper<ProductImage> {

        public ProductImage mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProductImage img = new ProductImage();
            img.setId((UUID) rs.getObject("id"));
            img.setProductId((UUID) rs.getObject("product_id"));
            img.setImageUrl(rs.getString("image_url"));
            img.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            return img;
        }
    }

    @Override
    public List<ProductImage> getProductImages(UUID productId) {
        String sql = "SELECT * FROM product_images WHERE product_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new ProductImageRowMapper(), productId);
    }

    @Override
    public ProductImage getImageById(UUID id) {
        String sql = "SELECT * FROM product_images WHERE id = ? ORDER BY created_at DESC";
        return jdbcTemplate.queryForObject(sql, new ProductImageRowMapper(), id);
    }

    @Override
    public ProductImage addProductImage(UUID productId, String ImageUrl) {
        ProductImage img = new ProductImage();

        img.setId(UUID.randomUUID());
        img.setProductId(productId);
        img.setImageUrl(ImageUrl);
        img.setCreatedAt(LocalDateTime.now());

        String sql = "INSERT INTO product_images (id, product_id, image_url, created_at) VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(sql, img.getId(), productId, img.getImageUrl(), img.getCreatedAt());
        return img;
    }

    @Override
    public void deleteProductImage(UUID id) {
        getImageById(id); // Throws an error if no image
        String sql = "DELETE FROM product_images WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteImagesByProductId(UUID productId) {
        String sql = "DELETE FROM product_images WHERE product_id = ?";
        jdbcTemplate.update(sql, productId);
    }
}
