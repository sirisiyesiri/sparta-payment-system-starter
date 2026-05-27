package com.sparta.paymentsystem.domain.product.controller;

import com.sparta.paymentsystem.domain.product.dto.ProductResponse;
import com.sparta.paymentsystem.domain.product.service.ProductService;
import com.sparta.paymentsystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> list() {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.findAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.findById(id))
        );
    }
}

