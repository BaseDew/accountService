package com.basedew.messaging;

import com.basedew.dto.CreateAccountRequest;
import com.basedew.dto.FundsDTO;
import jakarta.validation.constraints.NotNull;

public record AccountCommandMessage(
        @NotNull CommandType type,
        Long userId,
        Long accountId,
        CreateAccountRequest createRequest,
        FundsDTO fundsDto
) {
    public enum CommandType {
        CREATE_ACCOUNT,
        ADD_FUNDS,
        DEDUCT_FUNDS,
        BLOCK_FUNDS,
        UNBLOCK_FUNDS,
        DELETE_ACCOUNT
    }
}
