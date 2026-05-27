package com.sparta.paymentsystem.domain.cart.repository;

import com.sparta.paymentsystem.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.member.id = :memberId")
    List<CartItem> findByMemberId(@Param("memberId") Long memberId);

    Optional<CartItem> findByMember_IdAndProduct_Id(Long memberId, Long productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.id = :id AND ci.member.id = :memberId")
    int deleteByIdAndMember_Id(@Param("id") Long id, @Param("memberId") Long memberId);

    // 주문서에 담을 선택된 장바구니 아이템을 상품 정보와 함께 조회
    // - memberId 조건 : 다른 회원의 cartItemId를 넘겨도 조회되지 않도록 소유권 검증
    // - JOIN FETCH : 주문서에서 상품명/가격을 써야 하므로 n+1 방지
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.id IN :ids AND ci.member.id = :memberId")
    List<CartItem> findByIdInAndMember_IdWithProduct(@Param("ids") List<Long> ids, @Param("memberId") Long memberId);
}

