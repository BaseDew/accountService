package com.basedew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransactionDto(
        @NotNull(message = "User ID is required")
        Long userId,
        @NotNull(message = "Transaction ID is required")
        Long transactionId,
        @NotNull(message = "Account ID is required")
        Long accountId,
        @NotNull(message = "Currency amount is required")
        Double funds,
        Double feeFactor,
        String recipient,

        @NotBlank(message = "Currency code is required")
        @Size(min = 3, max = 3, message = "Currency code must consist of 3 characters (e.g. EUR)")
        String currency,
        TransactionStatus transactionStatus,
        String timestamp,
        String message
){
        public enum TransactionStatus {
                CREATED,
                PENDING,
                COMPLETED,
                REJECTED
        }
}
