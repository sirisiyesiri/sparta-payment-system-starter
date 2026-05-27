package com.sparta.paymentsystem.domain.order.facade;

import com.sparta.paymentsystem.domain.cart.entity.CartItem;
import com.sparta.paymentsystem.domain.cart.facade.CartFacade;
import com.sparta.paymentsystem.domain.cart.service.CartService;
import com.sparta.paymentsystem.domain.member.entity.Member;
import com.sparta.paymentsystem.domain.member.service.MemberService;
import com.sparta.paymentsystem.domain.order.dto.CheckoutResponse;
import com.sparta.paymentsystem.domain.order.dto.OrderCheckoutRequest;
import com.sparta.paymentsystem.domain.order.dto.OrderCheckoutResponse;
import com.sparta.paymentsystem.domain.order.dto.OrderResponse;
import com.sparta.paymentsystem.domain.order.entity.Order;
import com.sparta.paymentsystem.domain.order.entity.OrderItem;
import com.sparta.paymentsystem.domain.order.service.OrderService;
import com.sparta.paymentsystem.domain.payment.entity.Payment;
import com.sparta.paymentsystem.domain.payment.entity.PaymentStatus;
import com.sparta.paymentsystem.domain.payment.service.PaymentService;
import com.sparta.paymentsystem.domain.product.entity.Product;
import com.sparta.paymentsystem.global.error.BusinessException;
import com.sparta.paymentsystem.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderFacade {

    private final CartService cartService;
    private final MemberService memberService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public CheckoutResponse getCheckout(Long memberId, List<Long> cartItemIds) {
        // 주문서 미리보기: 재고 차감/주문 생성 없는 읽기 전용
        // cartItemIds가 null/비어있으면 "전체 장바구니", 값이 있으면 "선택된 아이템만" 주문서에 담는다.
        List<CartItem> cartItems = getValidateCartItems(
                memberId, cartItemIds != null ? cartItemIds : List.of()
        );

        // 장바구니 아이템에서 상품 가격과 장바구니 수량을 곱해서 각 아이템의 총액을 구한다.
        // CartItem을 CheckoutResponse.CheckoutItemResponse로 변환
        List<CheckoutResponse.CheckoutItemResponse> items = cartItems.stream()
                .map(cartItem -> {
                    int price = cartItem.getProduct().getPrice();
                    int subtotal = price * cartItem.getQuantity();
                    return new CheckoutResponse.CheckoutItemResponse(
                            cartItem.getProductId(),
                            cartItem.getProduct().getName(),
                            price,
                            cartItem.getQuantity(),
                            subtotal
                    );
                })
                .toList();

        // 장바구니 주문 총액을 구한다. CheckoutResponse.CheckoutItemResponse의 subtotal을 모두 더한다.
        int totalPrice = items.stream()
                .mapToInt(CheckoutResponse.CheckoutItemResponse::subtotal)
                .sum();

        return new CheckoutResponse(items, totalPrice);
    }

    @Transactional
    public OrderCheckoutResponse createOrder(Long memberId, OrderCheckoutRequest request) {
        List<Long> cartItemIds = (request != null) ? request.cartItemIds() : List.of();

        // 0. 회원 조회
        Member member = memberService.findById(memberId);

        // 1. 장바구니 조회 (선택된 아이템만)
        List<CartItem> cartItems = getValidateCartItems(memberId, cartItemIds);

        // 2~3. 재고 차감 + 스냅샷 OrderItem 생성
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            product.deductStock(cartItem.getQuantity());

            OrderItem orderItem = new OrderItem(
                    product,
                    product.getPrice(),
                    cartItem.getQuantity()
            );
            orderItems.add(orderItem);
        }
        int totalPrice = orderItems.stream().mapToInt(OrderItem::getSubtotal).sum();

        // 4. 주문 저장
        Order order = orderService.createOrder(member, orderItems, totalPrice);

        // 5. 결제 정보 생성 (IN_PROGRESS 상태)
        Payment payment = paymentService.createPayment(order, order.getTotalPrice());

        // 6. 주문한 장바구니 아이템만 삭제
        List<Long> orderedItemIds = cartItems.stream().map(CartItem::getId).toList();
        cartService.clearCartItems(orderedItemIds, memberId);

        // 7. 응답
        return new OrderCheckoutResponse(
                order.getId(),
                payment.getPortonePaymentId(),
                totalPrice,
                order.getOrderName(),
                order.getStatus().name()
        );
    }

    public List<OrderResponse> getOrders(Long memberId) {
        List<Order> orders = orderService.findOrderEntities(memberId);
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, Long> paymentMap = paymentService.findPaymentIdMapByOrderIds(orderIds);

        return orders.stream()
                .map(order -> orderService.toResponse(order, paymentMap.get(order.getId())))
                .toList();
    }

    public OrderResponse getOrder(Long memberId, Long orderId) {
        Order order = orderService.findOrderEntity(orderId);
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        Long paymentId = paymentService.findPaymentIdByOrderId(orderId).orElseThrow(
                () -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND)
        );
        return orderService.toResponse(order, paymentId);
    }

    private List<CartItem> getValidateCartItems(Long memberId, List<Long> cartItemIds) {
        // cartItemIds가 비어있으면 "전체 장바구니", 아니면 "선택된 아이템만" 조회
        List<CartItem> cartItems = cartItemIds.isEmpty()
                ? cartService.findCartEntities(memberId)
                : cartService.findCartEntitiesByIds(memberId, cartItemIds);

        // 1차 검증: 주문할 아이템이 하나도 없으면 주문서 자체가 성립하지 않는다.
        // (전체 조회: 빈 장바구니 / 선택 조회: 넘긴 ID가 전부 남의 것/없는 것일 때도 여기로 떨어짐)
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        // 2차 검증: 요청한 ID 개수와 조회된 개수가 다르다. -> 일부가 "남의 것" 또는 "존재하지 않는 ID"다.
        // -> 일부만 주문되는 상황을 막고, 명시적으로 에러를 던진다.
        if (!cartItemIds.isEmpty() && cartItems.size() != cartItemIds.size()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        return cartItems;
    }

}


