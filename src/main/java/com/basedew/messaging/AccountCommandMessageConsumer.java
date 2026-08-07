package com.basedew.messaging;


import com.basedew.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.io.IOException;

@ApplicationScoped
public class AccountCommandMessageConsumer {

    ObjectMapper objectMapper;
    private final AccountService accountService;

    public AccountCommandMessageConsumer(AccountService accountService, ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }

    @Incoming("accounts")
    @Blocking
    @Transactional
    public void process(Buffer buffer) throws IOException {
        AccountCommandMessage msg = objectMapper.readValue(buffer.getBytes(), AccountCommandMessage.class);
        switch (msg.type()) {
            case ADD_FUNDS -> accountService.addFunds(msg.userId(), msg.accountId(), msg.fundsDto());
            case BLOCK_FUNDS -> accountService.blockFunds(msg.userId(), msg.accountId(), msg.fundsDto());
            case DEDUCT_FUNDS -> accountService.deductFunds(msg.userId(), msg.accountId(), msg.fundsDto());
            case UNBLOCK_FUNDS -> accountService.unblockFunds(msg.userId(), msg.accountId(), msg.fundsDto());
            case CREATE_ACCOUNT -> accountService.create(msg.createRequest());
            case DELETE_ACCOUNT -> accountService.deleteAccount(msg.userId(), msg.accountId());
            default -> System.out.println("Unknown command received");
        }
    }
}
