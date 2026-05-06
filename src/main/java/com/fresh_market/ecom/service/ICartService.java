package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.Cart;
import com.fresh_market.ecom.model.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ICartService {
    Cart getOrCreateCartForUser(UUID userId);
    Cart getCartById(UUID cartId);
    List<CartItem> getCartItems(UUID cartId);
    CartItem getCartItemById(UUID cartItemId);
    CartItem addOrIncrementItem(UUID cartId, UUID productId, int quantity);
    CartItem updateCartItemQuantity(UUID cartItemId, int quantity); // quantity <= 0 = remove
    void removeItem(UUID cartItemId);
    void clearCart(UUID cartId);
    BigDecimal getCartTotal(UUID cartId);
}
