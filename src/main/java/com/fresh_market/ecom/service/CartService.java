package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.Cart;
import com.fresh_market.ecom.model.CartItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CartService implements ICartService {

    private final JdbcTemplate jdbcTemplate;

    public CartService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static class CartRowMapper implements RowMapper<Cart> {

        @Override
        public Cart mapRow(ResultSet rs, int rowNum) throws SQLException {
            Cart c = new Cart();
            c.setId((UUID) rs.getObject("id"));
            c.setUserId((UUID) rs.getObject("user_id"));
            c.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            return c;
        }
    }

    private static class CartItemRowMapper implements RowMapper<CartItem> {
        @Override
        public CartItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            CartItem ci = new CartItem();
            ci.setId((UUID) rs.getObject("id"));
            ci.setCartId((UUID) rs.getObject("cart_id"));
            ci.setProductId((UUID) rs.getObject("product_id"));
            ci.setQuantity(rs.getInt("quantity"));
            ci.setUnitPrice(rs.getBigDecimal("unit_price")); // snapshot price
            ci.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            return ci;
        }
    }

    @Override
    public Cart getOrCreateCartForUser(UUID userId) {
        String sql = "SELECT * FROM carts WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new CartRowMapper(), userId);
        } catch (EmptyResultDataAccessException e) {
            Cart cart = new Cart();
            cart.setId((UUID) UUID.randomUUID());
            cart.setUserId(userId);
            cart.setCreatedAt(LocalDateTime.now());

            String insertSql = "INSERT INTO carts (id, user_id, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, cart.getId(), cart.getUserId(), cart.getCreatedAt());
            return cart;
        }
    }

    @Override
    public Cart getCartById(UUID cartId) {
        String sql = "SELECT  * FROM carts WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new CartRowMapper(), cartId);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Cart Not Found: " + cartId);
        }
    }

    @Override
    public List<CartItem> getCartItems(UUID cartId) {
        String sql = "SELECT * FROM cart_items WHERE cart_id = ? ORDER BY created_at";
        return jdbcTemplate.query(sql, new CartItemRowMapper(), cartId);
    }

    @Override
    public CartItem getCartItemById(UUID cartItemId) {
        String sql = "SELECT * FROM cart_items WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new CartItemRowMapper(), cartItemId);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Cart Not Found: " + cartItemId);
        }
    }

    @Override
    public CartItem addOrIncrementItem(UUID cartId, UUID productId, int quantity) {
        getCartById(cartId); // ensures that the cart exists

        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Quantity must be greater than 0");
        }

        String productSql = "SELECT price, stock_quantity FROM products WHERE id = ?";
        Map<String, Object> productData;
        try {
            productData = jdbcTemplate.queryForMap(
                    productSql, productId
            );
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Product Not Found: " + productId);
        }

        BigDecimal unitPrice = (BigDecimal) productData.get("price");
        int stockQuantity = (Integer) productData.get("stock_quantity");

        // Check if the product is already in the cart
        String checkSql = "SELECT * FROM cart_items WHERE cart_id = ? AND product_id = ?";
        List<CartItem> existing = jdbcTemplate.query(checkSql, new CartItemRowMapper(), cartId, productId);

        if (!existing.isEmpty()) { // if it already exists
            CartItem item = existing.get(0);
            int  newQuantity = item.getQuantity() + quantity;

            // Stock check for existing item before incrementing
            if (newQuantity > stockQuantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Not enough stock. Requested: " + newQuantity + ", Available: " + stockQuantity);
            }

            String updateSql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
            jdbcTemplate.update(updateSql, newQuantity, item.getId());
            item.setQuantity(newQuantity);
            return item;
        }

        // Stock check for new item before adding to cart.

        if (quantity > stockQuantity) {
            throw new  ResponseStatusException(HttpStatus.BAD_REQUEST,"Not enough stock. Requested: " + quantity + ", Available: " + stockQuantity);
        }

        // new item -> if it doesn't exist.
        CartItem ci = new CartItem();
        ci.setId(UUID.randomUUID());
        ci.setCartId(cartId);
        ci.setProductId(productId);
        ci.setQuantity(quantity);
        ci.setUnitPrice(unitPrice);
        ci.setCreatedAt(LocalDateTime.now());

        String insertSql = """
                INSERT INTO cart_items (id, cart_id, product_id, quantity, unit_price, created_at) VALUES (?, ?, ?, ?, ?, ?);
                """;
        jdbcTemplate.update(
                insertSql,
                ci.getId(),
                ci.getCartId(),
                ci.getProductId(),
                ci.getQuantity(),
                ci.getUnitPrice(),
                ci.getCreatedAt()
        );
        return ci;
    }

    @Override
    public CartItem updateCartItemQuantity(UUID cartItemId, int quantity) {
        if (quantity <= 0) {
            removeItem(cartItemId);
            return null;
        }

        CartItem existing = getCartItemById(cartItemId);
        // Stock check before updating quantity

        String stockSql = "SELECT stock_quantity FROM products WHERE id = ?";
        Integer stockQuantity = jdbcTemplate.queryForObject(stockSql, Integer.class, existing.getProductId());
        if (stockQuantity == null || quantity > stockQuantity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Not enough stock. Requested: " + quantity + ", Available: " + stockQuantity
            );
        }
        String updateSql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        jdbcTemplate.update(updateSql, quantity, cartItemId);
        existing.setQuantity(quantity);
        return existing;
    }

    @Override
    public void removeItem(UUID cartItemId) {
        getCartItemById(cartItemId); // Throws 404 if not found
        String sql = "DELETE FROM cart_items WHERE id = ?";
        jdbcTemplate.update(sql, cartItemId);
    }

    @Override
    public void clearCart(UUID cartId) {
        getCartById(cartId);
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        jdbcTemplate.update(sql, cartId);
    }

    @Override
    public BigDecimal getCartTotal(UUID cartId) {
        getCartById(cartId);
        String sql = """
                    SELECT COALESCE(SUM(unit_price * quantity), 0) FROM cart_items WHERE cart_id = ?
                    """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, cartId);
    }
}
