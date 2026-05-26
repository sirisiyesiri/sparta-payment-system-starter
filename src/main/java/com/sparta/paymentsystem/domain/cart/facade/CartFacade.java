package com.sparta.paymentsystem.domain.cart.facade;

import com.sparta.paymentsystem.domain.cart.dto.AddCartRequest;
import com.sparta.paymentsystem.domain.cart.entity.CartItem;
import com.sparta.paymentsystem.domain.cart.service.CartService;
import com.sparta.paymentsystem.domain.member.entity.Member;
import com.sparta.paymentsystem.domain.member.service.MemberService;
import com.sparta.paymentsystem.domain.product.entity.Product;
import com.sparta.paymentsystem.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CartFacade {

    private final CartService cartService;
    private final MemberService memberService;
    private final ProductService productService;

    @Transactional
    public Long addItem(Long memberId, AddCartRequest request) {
        Member member = memberService.findById(memberId);
        Product product = productService.findProductEntity(request.productId());
        CartItem cartItem = new CartItem(member, product, request.quantity());
        return cartService.addItem(cartItem);
    }

}
