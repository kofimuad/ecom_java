package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService implements IOrderService{

    private final JdbcTemplate jdbcTemplate;

    public OrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Order> orderMapper = (rs, rowNum) -> {
        Order order = new Order();
        order.setId((UUID)rs.getObject("id"));
        order.setUserId((UUID)rs.getObject("user_id"));
        order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
        order.setTotalAmount((BigDecimal)rs.getObject("total_amount"));
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null) {
            order.setCreatedAt(created.toLocalDateTime());
        }

        if (updated != null) {
            order.setUpdatedAt(updated.toLocalDateTime());
        }
        return order;
    };

    private final RowMapper<OrderItem> orderItemRowMapper = (rs, rowNum) -> {
        OrderItem orderItem = new OrderItem();
        orderItem.setId((UUID)rs.getObject("id"));
        orderItem.setOrderId((UUID)rs.getObject("order_id"));
        orderItem.setProductId((UUID)rs.getObject("product_id"));
        orderItem.setQuantity((Integer)rs.getObject("quantity"));
        orderItem.setUnitPrice((BigDecimal)rs.getObject("unit_price"));
        Timestamp created = rs.getTimestamp("created_at");

        if (created != null) {
            orderItem.setCreatedAt(created.toLocalDateTime());
        }

        return orderItem;
    };

    private final RowMapper<Cart> cartMapper = (rs, rowNum) -> {
        Cart c = new Cart();
        c.setId((UUID) rs.getObject("id"));
        c.setUserId((UUID) rs.getObject("user_id"));
        c.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return c;
    };

    private final RowMapper<CartItem> cartItemMapper = (rs, rowNum) -> {
        CartItem ci = new CartItem();
        ci.setId((UUID) rs.getObject("id"));
        ci.setCartId((UUID) rs.getObject("cart_id"));
        ci.setProductId((UUID) rs.getObject("product_id"));
        ci.setQuantity(rs.getInt("quantity"));
        ci.setUnitPrice(rs.getBigDecimal("unit_price")); // snapshot price
        ci.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return ci;
    };

    private final RowMapper<Product> productMapper = (rs, numRow) -> {
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
    @Transactional
    public Order placeOrder(UUID cartId, UUID userId) {
        String checkIfCartSql = "SELECT * FROM carts WHERE id = ? AND user_id = ?";
        try {
            jdbcTemplate.queryForObject(checkIfCartSql, cartMapper, cartId, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Cart Not Found For User: " + cartId);
        }

        String checkIfCartItemSql = "SELECT * FROM cart_items WHERE cart_id = ?";
        List<CartItem> cartItems = jdbcTemplate.query(checkIfCartItemSql, cartItemMapper, cartId);
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty " + cartId);
        }

        String checkProductSql = "SELECT * FROM products WHERE id = ?";

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Product product = jdbcTemplate.queryForObject(checkProductSql, productMapper, item.getProductId());
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new  ResponseStatusException(HttpStatus.BAD_REQUEST, "Product Stock Quantity Not Enough");
            }
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal orderTotal = total;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String insertOrderSql = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertOrderSql, new String[]{"id"});
            ps.setObject(1, userId);
            ps.setObject(2, orderTotal);
            ps.setString(3, Order.OrderStatus.PENDING.name());
            return ps;
        }, keyHolder);

        UUID orderId = (UUID) keyHolder.getKeys().get("id");

        String insertIntoOrderItemsSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        for (CartItem cartItem : cartItems) {
            jdbcTemplate.update(insertIntoOrderItemsSql, orderId, cartItem.getProductId(), cartItem.getQuantity(), cartItem.getUnitPrice());
        }

        String stockUpdateSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?";
        for (CartItem cartItem : cartItems) {
            jdbcTemplate.update(stockUpdateSql, cartItem.getQuantity(), cartItem.getProductId());
        }

        String deleteCartItemSql = "DELETE FROM cart_items WHERE cart_id = ?";
        jdbcTemplate.update(deleteCartItemSql, cartId);

        return getOrderById(orderId);
    }

    @Override
    public Order getOrderById(UUID orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try {
            return  jdbcTemplate.queryForObject(sql, orderMapper, orderId);
        }  catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Order Not Found: " + orderId);
        }
    }

    @Override
    public List<Order> getOrdersByUserId(UUID userId) {
        String sql = "SELECT * FROM orders WHERE user_id = ?";
        List<Order> orders = jdbcTemplate.query(sql, orderMapper, userId);
        if (orders.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Order Not Found For User: " + userId);
        }
        return orders;
    }

    @Override
    public List<OrderItem> getOrderItems(UUID orderId) {
        String  sql = "SELECT * FROM order_items WHERE order_id = ?";
        try {
            return jdbcTemplate.query(sql, orderItemRowMapper, orderId);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Order Item(s) Not Found: " + orderId);
        }
    }

    @Override
    public Order updateOrderStatus(UUID orderId, Order.OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        int rows =  jdbcTemplate.update(sql, status.name(), orderId);

        if (rows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Order Not Found: " + orderId);
        }
        return getOrderById(orderId);
    }

    @Override
    public Order cancelOrder(UUID orderId) {
        return updateOrderStatus(orderId, Order.OrderStatus.CANCELED);
    }
}
