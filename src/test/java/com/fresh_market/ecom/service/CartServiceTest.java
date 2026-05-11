package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.Cart;
import com.fresh_market.ecom.model.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked") // What does this even do?
public class CartServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate; // Fake DB

    @InjectMocks
    private CartService cartService; // Real service with fake DB injected

    // Shared IDs reused across tests. @BeforeEach resets them before every test.
    private UUID cartId;
    private UUID userId;
    private UUID productId;
    private UUID cartItemId;

    @BeforeEach
    void setUp() {
        cartId     = UUID.randomUUID();
        userId     = UUID.randomUUID();
        productId  = UUID.randomUUID();
        cartItemId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // Helpers — build objects used repeatedly across tests
    // -------------------------------------------------------------------------

    private Cart buildCart() {
        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUserId(userId);
        cart.setCreatedAt(LocalDateTime.now());
        return cart;
    }

    private CartItem buildCartItem(int quantity) {
        CartItem item = new CartItem();
        item.setId(cartItemId);
        item.setCartId(cartId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setCreatedAt(LocalDateTime.now());
        return item;
    }

    // Shortcut: make the mock return a valid Cart when getCartById is called.
    // getCartById calls jdbcTemplate.queryForObject(sql, new CartRowMapper(), cartId).
    // We use any(RowMapper.class) because the RowMapper is created with `new` inside
    // the service — we can't get a reference to it from here, so we match "any RowMapper".
    private void mockCartExists() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(cartId)))
                .thenReturn(buildCart());
    }

    // Shortcut: make the mock return a CartItem when getCartItemById is called.
    private void mockCartItemExists(int quantity) {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(cartItemId)))
                .thenReturn(buildCartItem(quantity));
    }

    // Shortcut: make the product lookup return a given stock level.
    // queryForMap is the method CartService uses to fetch price + stock in one call.
    private void mockProductExists(int stock) {
        when(jdbcTemplate.queryForMap(anyString(), eq(productId)))
                .thenReturn(Map.of("price", new BigDecimal("10.00"), "stock_quantity", stock));
    }

    // =========================================================================
    // getOrCreateCartForUser
    // =========================================================================

    @Test
    void getOrCreateCartForUser_returnsExistingCart_whenCartAlreadyExists() {
        // The DB finds a cart for this user — queryForObject returns it normally.
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(userId)))
                .thenReturn(buildCart());

        Cart result = cartService.getOrCreateCartForUser(userId);

        assertEquals(cartId, result.getId());
        assertEquals(userId, result.getUserId());
    }

    @Test
    void getOrCreateCartForUser_createsAndReturnsNewCart_whenNoneExists() {
        // The DB finds nothing — queryForObject throws EmptyResultDataAccessException.
        // CartService catches this and runs an INSERT instead.
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(userId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        // The INSERT passes 3 args: id, user_id, created_at.
        when(jdbcTemplate.update(anyString(), any(), any(), any()))
                .thenReturn(1);

        Cart result = cartService.getOrCreateCartForUser(userId);

        // The service builds a new Cart object in-memory and returns it.
        assertNotNull(result.getId());
        assertEquals(userId, result.getUserId());
        assertNotNull(result.getCreatedAt());
    }

    // =========================================================================
    // getCartById  (your existing tests — kept exactly as you wrote them)
    // =========================================================================

    @Test
    void getCardById_returnsCart_whenCartExists() {
        UUID cartId = UUID.randomUUID();

        Cart fakeCart = new Cart();

        fakeCart.setId(cartId);
        fakeCart.setUserId(UUID.randomUUID());
        fakeCart.setCreatedAt(LocalDateTime.now());

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(cartId))).thenReturn(fakeCart);

        // ACT
        Cart result = cartService.getCartById(cartId);

        // ASSERT
        assertEquals(cartId, result.getId());
    }

    @Test
    void getCardById_throwsNotFound_whenCartDoesNotExist() {
        UUID cartId = UUID.randomUUID();

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(cartId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        // The "1" means "expected 1 result, got 0." It's just how Spring constructs
        // this exception. You don't have to match what the real DB would pass — you're
        // simulating the exception itself, not the conditions that cause it.

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> cartService.getCartById(cartId)
        );
        assertEquals(404, ex.getStatusCode().value());
    }

    // =========================================================================
    // getCartItems
    // =========================================================================

    @Test
    void getCartItems_returnsListOfItems_forGivenCart() {
        // jdbcTemplate.query (not queryForObject) is used here — it always returns a List.
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(cartId)))
                .thenReturn(List.of(buildCartItem(2), buildCartItem(5)));

        List<CartItem> result = cartService.getCartItems(cartId);

        assertEquals(2, result.size());
    }

    @Test
    void getCartItems_returnsEmptyList_whenCartHasNoItems() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(cartId)))
                .thenReturn(List.of());

        List<CartItem> result = cartService.getCartItems(cartId);

        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // getCartItemById
    // =========================================================================

    @Test
    void getCartItemById_returnsItem_whenItemExists() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(cartItemId)))
                .thenReturn(buildCartItem(3));

        CartItem result = cartService.getCartItemById(cartItemId);

        assertEquals(cartItemId, result.getId());
        assertEquals(3, result.getQuantity());
    }

    @Test
    void getCartItemById_throwsNotFound_whenItemDoesNotExist() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(cartItemId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> cartService.getCartItemById(cartItemId)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    // =========================================================================
    // addOrIncrementItem  (your two existing tests + the remaining cases)
    // =========================================================================

    @Test
    void addOrIncrementItem_throwsBadRequest_whenQuantityIsZero() {
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addOrIncrementItem(cartId, productId, 0)
        );
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void addOrIncrementItem_throwsBadRequest_whenQuantityIsNegative() {
        UUID cardId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () ->  cartService.addOrIncrementItem(cardId, productId, -1)
        );
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void addOrIncrementItem_throwsNotFound_whenProductDoesNotExist() {
        // Cart must exist first so we get past the getCartById check.
        mockCartExists();

        // The product lookup throws — product not in DB.
        when(jdbcTemplate.queryForMap(anyString(), eq(productId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addOrIncrementItem(cartId, productId, 1)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void addOrIncrementItem_throwsBadRequest_whenNewItemQuantityExceedsStock() {
        mockCartExists();
        mockProductExists(3); // only 3 in stock

        // Empty list means this product is not yet in the cart — new item path.
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(cartId), eq(productId)))
                .thenReturn(List.of());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addOrIncrementItem(cartId, productId, 5) // requesting 5, only 3 available
        );

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void addOrIncrementItem_insertsNewItem_whenProductIsNotAlreadyInCart() {
        mockCartExists();
        mockProductExists(10);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(cartId), eq(productId)))
                .thenReturn(List.of()); // product not in cart yet

        // The INSERT passes 6 args: id, cartId, productId, quantity, unitPrice, createdAt.
        // We don't stub the update — the mock returns 0 (default for int) which is fine,
        // the service doesn't use the return value.

        CartItem result = cartService.addOrIncrementItem(cartId, productId, 3);

        assertEquals(3, result.getQuantity());
        assertEquals(new BigDecimal("10.00"), result.getUnitPrice());
        assertEquals(cartId, result.getCartId());
        assertEquals(productId, result.getProductId());
        assertNotNull(result.getId());
    }

    @Test
    void addOrIncrementItem_incrementsQuantity_whenItemIsAlreadyInCart() {
        mockCartExists();
        mockProductExists(10);

        CartItem existing = buildCartItem(2); // item is already in the cart with quantity 2

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(cartId), eq(productId)))
                .thenReturn(List.of(existing));

        // The UPDATE passes: newQuantity (4) and the item's id.
        when(jdbcTemplate.update(anyString(), eq(4), eq(cartItemId)))
                .thenReturn(1);

        CartItem result = cartService.addOrIncrementItem(cartId, productId, 2); // add 2 more

        assertEquals(4, result.getQuantity()); // 2 existing + 2 new = 4
    }

    @Test
    void addOrIncrementItem_throwsBadRequest_whenIncrementWouldExceedStock() {
        mockCartExists();
        mockProductExists(3); // only 3 in stock

        CartItem existing = buildCartItem(2); // already have 2 in cart

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(cartId), eq(productId)))
                .thenReturn(List.of(existing));

        // 2 (existing) + 2 (new request) = 4, but only 3 in stock.
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addOrIncrementItem(cartId, productId, 2)
        );

        assertEquals(400, ex.getStatusCode().value());
    }

    // =========================================================================
    // updateCartItemQuantity
    // =========================================================================

    @Test
    void updateCartItemQuantity_removesItemAndReturnsNull_whenQuantityIsZero() {
        // quantity=0 triggers the early `removeItem` path inside updateCartItemQuantity.
        // removeItem itself calls getCartItemById first, so that needs to be mocked.
        mockCartItemExists(3);

        CartItem result = cartService.updateCartItemQuantity(cartItemId, 0);

        assertNull(result); // the method explicitly returns null in the remove path

        // Confirm a DELETE was actually executed.
        verify(jdbcTemplate).update(anyString(), eq(cartItemId));
    }

    @Test
    void updateCartItemQuantity_throwsBadRequest_whenQuantityExceedsStock() {
        mockCartItemExists(1);

        // This overload — queryForObject(sql, Class, args) — is different from the RowMapper
        // overload above. Mockito treats them as separate methods so both mocks can coexist.
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(productId)))
                .thenReturn(5); // only 5 in stock

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> cartService.updateCartItemQuantity(cartItemId, 10) // requesting 10
        );

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void updateCartItemQuantity_updatesAndReturnsItem_whenStockIsSufficient() {
        mockCartItemExists(1);

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(productId)))
                .thenReturn(10); // plenty of stock

        CartItem result = cartService.updateCartItemQuantity(cartItemId, 7);

        assertEquals(7, result.getQuantity());
    }

    // =========================================================================
    // removeItem
    // =========================================================================

    @Test
    void removeItem_throwsNotFound_whenItemDoesNotExist() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(cartItemId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> cartService.removeItem(cartItemId)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void removeItem_executesDelete_whenItemExists() {
        mockCartItemExists(2);

        assertDoesNotThrow(() -> cartService.removeItem(cartItemId));

        // verify(mock).method() asserts the method was called exactly once with these args.
        verify(jdbcTemplate).update(anyString(), eq(cartItemId));
    }

    // =========================================================================
    // clearCart
    // =========================================================================

    @Test
    void clearCart_throwsNotFound_whenCartDoesNotExist() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(cartId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> cartService.clearCart(cartId)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void clearCart_deletesAllItems_whenCartExists() {
        mockCartExists();

        assertDoesNotThrow(() -> cartService.clearCart(cartId));

        verify(jdbcTemplate).update(anyString(), eq(cartId));
    }

    // =========================================================================
    // getCartTotal
    // =========================================================================

    @Test
    void getCartTotal_returnsZero_whenCartIsEmpty() {
        mockCartExists();

        // getCartTotal calls queryForObject twice:
        //   1. getCartById     → queryForObject(sql, RowMapper, cartId)  ← handled by mockCartExists()
        //   2. the SUM query   → queryForObject(sql, BigDecimal.class, cartId)
        // Because the second call uses BigDecimal.class instead of a RowMapper,
        // Mockito treats it as a completely different method overload.
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), eq(cartId)))
                .thenReturn(BigDecimal.ZERO);

        BigDecimal total = cartService.getCartTotal(cartId);

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void getCartTotal_returnsCorrectSum_whenCartHasItems() {
        mockCartExists();

        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), eq(cartId)))
                .thenReturn(new BigDecimal("45.00"));

        BigDecimal total = cartService.getCartTotal(cartId);

        assertEquals(new BigDecimal("45.00"), total);
    }
}