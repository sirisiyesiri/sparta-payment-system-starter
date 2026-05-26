package com.sparta.paymentsystem.domain.cart.controller;

import com.sparta.paymentsystem.domain.cart.dto.AddCartRequest;
import com.sparta.paymentsystem.domain.cart.dto.AddCartResponse;
import com.sparta.paymentsystem.domain.cart.dto.CartItemResponse;
import com.sparta.paymentsystem.domain.cart.dto.UpdateCartRequest;
import com.sparta.paymentsystem.domain.cart.facade.CartFacade;
import com.sparta.paymentsystem.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartFacade cartFacade;
    private final CartService cartService;

    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponse>> getItems(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(cartService.getCartItems(memberId));
    }

    @PostMapping("/items")
    public ResponseEntity<AddCartResponse> addItem(@AuthenticationPrincipal Long memberId,
                                                   @Valid @RequestBody AddCartRequest request) {
        Long cartItemId = cartFacade.addItem(memberId, request);
        return ResponseEntity.ok(new AddCartResponse(cartItemId));
    }

    @PatchMapping("/items/{id}")
    public ResponseEntity<Void> updateQuantity(@AuthenticationPrincipal Long memberId,
                                               @PathVariable Long id,
                                               @Valid @RequestBody UpdateCartRequest request) {
        cartService.updateQuantity(memberId, id, request.quantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal Long memberId,
                                           @PathVariable Long itemId) {
        cartService.removeItem(memberId, itemId);
        return ResponseEntity.ok().build();
    }
}
