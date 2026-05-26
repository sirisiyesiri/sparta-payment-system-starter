package com.sparta.paymentsystem.domain.cart.service;

import com.sparta.paymentsystem.domain.cart.entity.CartItem;
import com.sparta.paymentsystem.domain.cart.dto.CartItemResponse;
import com.sparta.paymentsystem.domain.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;

    public List<CartItemResponse> getCartItems(Long memberId) {
        return cartItemRepository.findByMemberId(memberId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public Long addItem(CartItem cartItem) {
        Optional<CartItem> existing = cartItemRepository.findByMember_IdAndProduct_Id(
                cartItem.getMemberId(), cartItem.getProductId()
        );
        if (existing.isPresent()) {
            CartItem found = existing.get();
            found.addQuantity(cartItem.getQuantity());
            return found.getId();
        } else {
            return cartItemRepository.save(cartItem).getId();
        }
    }

    @Transactional
    public void updateQuantity(Long memberId, Long itemId, int quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .filter(ci -> ci.getMemberId().equals(memberId))
                .orElseThrow(() -> new RuntimeException("장바구니 항목을 찾을 수 없습니다."));
        item.changeQuantity(quantity);
    }

    @Transactional
    public void removeItem(Long memberId, Long itemId) {
        int deleted = cartItemRepository.deleteByIdAndMember_Id(itemId, memberId);
        if (deleted == 0) {
            throw new RuntimeException("장바구니 항목을 찾을 수 없습니다.");
        }
    }

    private CartItemResponse toResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.getProduct().getStock()
        );
    }
}
