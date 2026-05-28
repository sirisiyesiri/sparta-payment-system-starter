package com.sparta.paymentsystem.domain.payment.repository;

import com.sparta.paymentsystem.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 주문 단건 조회 화면에서 결제 ID 한 건만 필요하므로 Payment 엔티티 전체를 로딩하지 않고 ID만 프로젝션함
    // Order <- Payment가 단방향 관계(Order가 Payment를 참조하지 않음)라서,
    // 주문에 결제 ID를 붙이려면 이렇게 Payment 쪽에서 역으로 찾아야 함.
    @Query("SELECT p.id FROM Payment p WHERE p.order.id = :orderId")
    Optional<Long> findIdByOrderId(@Param("orderId") Long orderId);

    // 주문 "목록" 조회용 N+1 방지 일괄 조회
    // 각 주문마다 findByOrderId를 돌리면 N번 쿼리가 나가는데, IN 절로 한 번에 가져와서
    // 서비스 레이어에서 Map<OrderId, PaymentId>로 재구성
    // 반환 List<Object[]>인 이유 : [OrderId, PaymentId] 쌍을 내려받기 위한 JPA 튜플 프로젝션
    // 타입 안정성은 떨어지므로, 실무에선 인터페이스/레코드 프로젝션으로 개선 가능
    @Query("SELECT p.order.id, p.id FROM Payment p WHERE p.order.id IN :orderIds")
    List<Object[]> findIdsByOrderIds(@Param("orderIds") List<Long> orderIds);

    // 주문 단건 상세 조회 : orderId만으로 조회
    @Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.order.id = :orderId")
    Optional<Payment> findByOrderIdWithOrder(@Param("orderId") Long orderId);

    // Payment 조회 시 연관된 Order를 fetch join 으로 함께 로딩 (N+1 방지)
    @Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.id = :paymentId")
    Optional<Payment> findByIdWithOrder(@Param("paymentId") Long paymentId);
}
