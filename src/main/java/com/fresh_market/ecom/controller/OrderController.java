package com.fresh_market.ecom.controller;

import com.fresh_market.ecom.model.Order;
import com.fresh_market.ecom.model.OrderItem;
import com.fresh_market.ecom.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Orders", description = "Manages order placement, retrieval, status updates and cancellations")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Place an order from a cart")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/checkout")
    public Order checkOut(@RequestParam UUID cartId, @RequestParam UUID userId) {
        return orderService.placeOrder(cartId, userId);
    }

    @Operation(summary = "Get an order by ID")
    @GetMapping("/{orderId}")
    public Order getOrderById(@PathVariable UUID orderId) {
        return orderService.getOrderById(orderId);
    }

    @Operation(summary = "Get all orders for a user")
    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUserId(@PathVariable UUID userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @Operation(summary = "Get all items in an order")
    @GetMapping("/{orderId}/items")
    public List<OrderItem> getOrderItems(@PathVariable UUID orderId) {
        return orderService.getOrderItems(orderId);
    }

    @Operation(summary = "Update the status of an order")
    @PatchMapping("/{orderId}/status")
    public Order updateOrderStatus(@PathVariable UUID orderId, @RequestParam Order.OrderStatus status) {
        return  orderService.updateOrderStatus(orderId, status);
    }

    @Operation(summary = "Cancel an order")
    @PatchMapping("/{orderId}/cancel")
    public Order cancelOrder(@PathVariable UUID orderId) {
        return orderService.cancelOrder(orderId);
    }

}
