package com.fresh_market.ecom.controller;

import com.fresh_market.ecom.model.Cart;
import com.fresh_market.ecom.model.CartItem;
import com.fresh_market.ecom.service.ICartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Tag(name = "Cart", description = "Manages shopping cart operations")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Get or create a cart for a user")
    @GetMapping("/user/{userId}")
    public Cart getOrCreateCart(@PathVariable UUID userId) {
        return cartService.getOrCreateCartForUser(userId);
    }

    @Operation(summary = "Get a cart by ID")
    @GetMapping("/{cardId}")
    public Cart getCartById(@PathVariable UUID cardId) {
        return cartService.getCartById(cardId);
    }

    @Operation(summary = "Get all items in a cart")
    @GetMapping("/{cardId}/items")
    public List<CartItem> getCartItems(@PathVariable UUID cardId) {
        return cartService.getCartItems(cardId);
    }

    @Operation(summary = "Get the total price of a cart")
    @GetMapping("/{cartId}/total")
    public BigDecimal getCartTotal(@PathVariable UUID cartId) {
        return cartService.getCartTotal(cartId);
    }

    @Operation(summary = "Add or increment an item in the cart")
    @PostMapping("/{cartId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItem addItemToCart(@PathVariable UUID cartId, @RequestBody AddItemRequest request) {
        return cartService.addOrIncrementItem(cartId, request.getProductId(), request.getQuantity());
    }

    @Operation(summary = "Update the quantity of a cart item")
    @PatchMapping("/items/{cartItemId}")
    public CartItem updateCartItemQuantity(@PathVariable UUID cartItemId, @RequestBody UpdateQuantityRequest request) {
        return cartService.updateCartItemQuantity(cartItemId, request.getQuantity());
    }

    @Operation(summary = "Remove an item from the cart")
    @DeleteMapping("/items/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable UUID cartItemId) {
        cartService.removeItem(cartItemId);
    }

    @Operation(summary = "Clear all items from a cart")
    @DeleteMapping("/{cartId}/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable UUID cartId) {
        cartService.clearCart(cartId);
    }

    // -- Request body class ---

    public static class AddItemRequest{
        private UUID productId;
        private int quantity;

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    // -- Update class --

    public static class UpdateQuantityRequest {
        private int quantity;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
