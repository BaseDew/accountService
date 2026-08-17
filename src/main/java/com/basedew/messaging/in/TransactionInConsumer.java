package com.basedew.messaging.in;


import com.basedew.dto.TransactionDto;
import com.basedew.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.io.IOException;
import java.util.Objects;

@ApplicationScoped
public class TransactionInConsumer {

    ObjectMapper objectMapper;
    private final AccountService accountService;

    public TransactionInConsumer(AccountService accountService, ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }

    @Incoming("transactions-in")
    @Blocking
    @Transactional
    public void process(Buffer buffer) throws IOException {
        TransactionDto msg = objectMapper.readValue(buffer.getBytes(), TransactionDto.class);
        accountService.processTransactionIn(msg);
    }
}