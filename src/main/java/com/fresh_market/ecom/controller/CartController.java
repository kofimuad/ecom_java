package com.fresh_market.ecom.controller;

import com.fresh_market.ecom.model.Cart;
import com.fresh_market.ecom.model.CartItem;
import com.fresh_market.ecom.service.ICartService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/user/{userId}") // Get cart for a user, creates one if it doesn't exist
    public Cart getOrCreateCart(@PathVariable UUID userId) {
        return cartService.getOrCreateCartForUser(userId);
    }

    @GetMapping("/{cardId}") // Get a cart by its own id
    public Cart getCartById(@PathVariable UUID cardId) {
        return cartService.getCartById(cardId);
    }

    @GetMapping("/{cardId}/items") // Get all items in the cart
    public List<CartItem> getCartItems(@PathVariable UUID cardId) {
        return cartService.getCartItems(cardId);
    }

    @GetMapping("/{cartId}/total") // Calculate total price for a particular cart
    public BigDecimal getCartTotal(@PathVariable UUID cartId) {
        return cartService.getCartTotal(cartId);
    }

    @PostMapping("/{cartId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItem addItemToCart(@PathVariable UUID cartId, @RequestBody AddItemRequest request) {
        return cartService.addOrIncrementItem(cartId, request.getProductId(), request.getQuantity());
    }

    @PatchMapping("/items/{cartItemId}")
    public CartItem updateCartItemQuantity(@PathVariable UUID cartItemId, @RequestBody UpdateQuantityRequest request) {
        return cartService.updateCartItemQuantity(cartItemId, request.getQuantity());
    }

    @DeleteMapping("/items/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable UUID cartItemId) {
        cartService.removeItem(cartItemId);
    }

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
