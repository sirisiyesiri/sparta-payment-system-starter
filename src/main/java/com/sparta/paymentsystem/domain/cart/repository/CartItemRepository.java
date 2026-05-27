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

    // 주문 생성 완료 직후 "주문한 장바구니 아이템만" 일괄 삭제
    // - member.id 조건 : 남의 cartItemId를 섞어 보내도 삭제되지 않게 하는 소유권 검증
    // - IN절 일괄 삭제 : 개별 deleteByIdAndMember_Id를 주문한 아이템 수만큼 반복 호출하는 대신 한 번의 쿼리로 처리(N번 쿼리 -> 1번)
    // - 반환 int : 실제로 삭제된 행 수
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.id IN :ids AND c.member.id = :memberId")
    int deleteAllByIdInAndMemberId(@Param("ids") List<Long> ids, @Param("memberId") Long memberId);

    /**
     * [ 벌크 삭제(Bulk Delete) 및 1차 캐시 동기화 매커니즘 ]
     * * * 1. @Modifying(clearAutomatically = true, flushAutomatically = true)
     * - 벌크 연산(한 번에 여러 데이터를 수정/삭제)은 JPA의 '1차 캐시'를 거치지 않고 DB로 곧장 쿼리를 날립니다.
     * - 이 때문에 DB에서는 데이터가 지워졌지만, 자바 메모리(1차 캐시)에는 여전히 지워진 객체들이 남아있는 '데이터 불일치'가 발생합니다.
     * - 이 상태로 동일한 트랜잭션 내에서 재조회를 하면, DB가 아닌 1차 캐시의 유령 데이터를 읽어오는 치명적인 버그가 생깁니다.
     * * * 2. 옵션별 역할
     * - flushAutomatically = true : 벌크 쿼리를 실행하기 전, 쓰기 지연 저장소에 남아있던 변경 사항들을 DB에 먼저 반영하여 순서를 보장합니다.
     * - clearAutomatically = true : 벌크 쿼리 실행 직후, 영속성 컨텍스트(1차 캐시)를 완전히 비워버립니다(🧹).
     * 따라서 이후에 재조회를 수행할 때, 메모리가 비어있으므로 DB로부터 진짜 최신 데이터를 강제로 새로 읽어오게 만듭니다.
     */
}

