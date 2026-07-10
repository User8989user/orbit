package com.orbitamarket.orders.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class CreateOrderRequest {
    @NotBlank
    private String productType;
    @NotNull
    private Long price;
    private Map<String, Object> payload;   
}