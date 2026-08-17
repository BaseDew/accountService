package com.basedew.messaging.in;


import com.basedew.dto.CreateAccountRequest;
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

    @Incoming("create-account")
    @Blocking
    @Transactional
    public void process(Buffer buffer) throws IOException {
        CreateAccountRequest msg = objectMapper.readValue(buffer.getBytes(), CreateAccountRequest.class);
        accountService.create(msg);
    }
}
