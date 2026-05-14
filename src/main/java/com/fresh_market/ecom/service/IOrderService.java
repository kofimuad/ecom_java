package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.Order;
import com.fresh_market.ecom.model.OrderItem;

import java.util.List;
import java.util.UUID;

public interface IOrderService {

    Order placeOrder(UUID cartId, UUID userId); // This is to place an order from the current cart, checkout operation
    Order getOrderById(UUID orderId);
    List<Order> getOrdersByUserId(UUID userId);
    List<OrderItem> getOrderItems(UUID orderId);
    Order updateOrderStatus(UUID orderId, Order.OrderStatus status);
    Order cancelOrder(UUID orderId);
}
