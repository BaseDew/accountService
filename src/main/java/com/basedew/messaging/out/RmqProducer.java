package com.basedew.messaging.out;

import com.basedew.dto.TransactionDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class RmqProducer {
    @Inject
    @Channel("transactions")
    Emitter<TransactionDto> emitter;

    public void send(TransactionDto transactionDto) {
        emitter.send(transactionDto);
    }
}
