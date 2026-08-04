package com.basedew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest (
        @NotNull(message = "User identifier is required")
        Long userId,

        @NotBlank(message = "Currency code is required")
        @Size(min = 3, max = 3, message = "Currency code must consist of 3 characters (e.g. EUR)")
        String currency
){}
