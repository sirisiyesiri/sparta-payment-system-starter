package com.sparta.paymentsystem.domain.product.entity;

import com.sparta.paymentsystem.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "int UNSIGNED")
    private int price;

    @Column(nullable = false, columnDefinition = "int UNSIGNED DEFAULT 0")
    private int stock = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Product(String name, int price, int stock, String description) {
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다");
        }
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
    }

}

