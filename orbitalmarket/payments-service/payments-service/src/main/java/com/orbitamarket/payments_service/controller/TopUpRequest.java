package com.orbitamarket.payments_service.controller;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopUpRequest {
    @NotNull
    private Long amount;
}