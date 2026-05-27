package com.sparta.paymentsystem.domain.order.repository;

import com.sparta.paymentsystem.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * [ 내 주문 목록 조회 (최신순) ]
     * * 1. LEFT JOIN FETCH o.orderItems
     * - 주문(Order)을 가져올 때 주문 상품(OrderItem)들을 N+1 문제 없이 한 번에 가져옵니다(Fetch Join).
     * - 그냥 JOIN을 쓰면 '주문 상품이 없는 주문'이 있을 때 그 주문 자체가 목록에서 누락되지만, → JOIN 은 교집합
     * LEFT JOIN을 사용하면 주문 상품의 유무와 상관없이 내 주문(Order) 데이터를 안전하게 모두 살려서 가져옵니다. → LEFT JOIN은 왼쪽테이블 전체 + 오른쪽의 교집합(오른쪽에 없으면 null)
     * (비즈니스적으로 주문 상품이 없는 게 오류일지라도, 시스템이 통째로 멈추는 것을 막는 방어적 코드입니다.)
     * * 2. DISTINCT
     * - 데이터베이스에서 1:N 관계를 조인(JOIN)하면, 주문 상품의 개수만큼 주문(Order) 데이터 행(Row)이 중복되어 늘어납니다.
     * - 이로 인해 JPA가 똑같은 주문 객체를 리스트에 여러 개 담아버리는 현상이 발생하는데,
     * DISTINCT를 적어주면 JPA가 메모리 상에서 중복된 주문(Order) 객체들을 하나로 깔끔하게 합쳐줍니다.
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.member.id = :memberId ORDER BY o.createdAt DESC")
    List<Order> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);

    // 주문 단건 상세 조회 : orderId만으로 조회
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithOrderItems(@Param("orderId") Long orderId);
}
