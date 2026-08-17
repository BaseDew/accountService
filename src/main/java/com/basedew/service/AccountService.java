package com.basedew.service;

import com.basedew.Account;
import com.basedew.dto.CreateAccountRequest;
import com.basedew.dto.TransactionDto;
import com.basedew.messaging.out.RmqProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;

import java.util.List;

@ApplicationScoped
public class AccountService {

    private final RmqProducer rmqProducer;

    public AccountService(RmqProducer rmqProducer) {
        this.rmqProducer = rmqProducer;
    }

    public Account create(@Valid CreateAccountRequest request) {
            Account account = new Account();
            account.userId = request.userId();
            account.currency = request.currency();
            account.persist();
            return account;
    }

    public boolean deleteAccount(Long userId, Long accountId) {
        Account account = Account.findById(accountId);
        if (account == null || !account.userId.equals(userId)) {
            return false;
        }
        if (account.funds != 0) {
            throw new IllegalStateException("Can not delete account with funds on it");
        }
        account.delete();
        return true;
    }

    public List<Account> getUserAccounts(Long userId) {
        return Account.list("userId", userId);
    }

    public Account getDetails(Long accountId) {
        return Account.findById(accountId);
    }


    public void processTransactionIn(TransactionDto transactionDto) {
        try {
            if (TransactionDto.TransactionStatus.COMPLETED.equals(transactionDto.transactionStatus())) {
                addFunds(transactionDto);
            } else {
                rmqProducer.send(changeTransactionStatus(transactionDto, TransactionDto.TransactionStatus.REJECTED));
            }
        } catch (IllegalStateException e) { //TODO logging
            rmqProducer.send(changeTransactionStatus(transactionDto, TransactionDto.TransactionStatus.REJECTED, e.getMessage()));
        } catch (Exception e) {
            rmqProducer.send(changeTransactionStatus(transactionDto, TransactionDto.TransactionStatus.REJECTED, "Unknown error occurred"));
        }
    }

    public void processTransactionOut(TransactionDto transactionDto) {
        try {
            switch (transactionDto.transactionStatus()) {
                case CREATED -> blockFunds(transactionDto);
                case COMPLETED -> deductFunds(transactionDto);
                case REJECTED -> unblockFunds(transactionDto);
                default -> rmqProducer.send(changeTransactionStatus(transactionDto, TransactionDto.TransactionStatus.REJECTED));
            }
        } catch (IllegalStateException e) { //TODO logging
            rmqProducer.send(changeTransactionStatus(transactionDto, TransactionDto.TransactionStatus.REJECTED, e.getMessage()));
        } catch (Exception e) {
            rmqProducer.send(changeTransactionStatus(transactionDto, TransactionDto.TransactionStatus.REJECTED, "Unknown error occurred"));
        }
    }


    public void addFunds(@Valid TransactionDto transactionDto) {
        Account account = Account.findById(transactionDto.accountId());
        validateGeneral(transactionDto, account);
        account.funds += transactionDto.funds();
        account.persist();
    }

    public void deductFunds(@Valid TransactionDto transactionDto) {
        Account account = Account.findById(transactionDto.accountId());
        validateGeneral(transactionDto, account);

        Double fundsToDeduct = calcGross(transactionDto.funds(), transactionDto.feeFactor());
        if (account.blockedFunds < fundsToDeduct) {
            throw new IllegalStateException("Account balance too low");
        }

        account.funds -= fundsToDeduct;
        account.blockedFunds -= fundsToDeduct;
        account.persist();
    }

    public void unblockFunds(@Valid TransactionDto transactionDto) {
        Account account = Account.findById(transactionDto.accountId());
        validateGeneral(transactionDto, account);

        Double fundsToUnblock = calcGross(transactionDto.funds(), transactionDto.feeFactor()); //TODO Fee non-refundable/partially refundable?
        if (account.blockedFunds < fundsToUnblock) {
            throw new IllegalStateException("No funds to unblock");
        }
        account.blockedFunds -= fundsToUnblock;
        account.persist();
    }

    public void blockFunds(@Valid TransactionDto transactionDto) {
        Account account = Account.findById(transactionDto.accountId());
        validateGeneral(transactionDto, account);

        Double fundsToBeBlocked = calcGross(transactionDto.funds(), transactionDto.feeFactor());
        if (account.funds - account.blockedFunds < fundsToBeBlocked) {
            throw new IllegalStateException("Account balance too low");
        }

        account.blockedFunds += fundsToBeBlocked;
        account.persist();

        rmqProducer.send(changeTransactionStatus(transactionDto, TransactionDto.TransactionStatus.PENDING));
    }


    private static void validateGeneral(TransactionDto transactionDto, Account account) {
        if (account == null || !account.userId.equals(transactionDto.userId())) {
            throw new IllegalStateException("Specified account not found for user");
        }
        if (!account.currency.equals(transactionDto.currency())) {
            throw new IllegalStateException("Currency type not match account");
        }
    }

    private TransactionDto changeTransactionStatus(TransactionDto transaction, TransactionDto.TransactionStatus status) {
        return changeTransactionStatus(transaction, status, transaction.message());
    }

    private TransactionDto changeTransactionStatus(TransactionDto transaction, TransactionDto.TransactionStatus status, String message) {
        return new TransactionDto(
                transaction.userId(),
                transaction.transactionId(),
                transaction.accountId(),
                transaction.funds(),
                transaction.feeFactor(),
                transaction.recipient(),
                transaction.currency(),
                status,
                transaction.timestamp(),
                message
        );
    }

    private Double calcGross(Double amount, Double feeFactor) {
        if (feeFactor == null || feeFactor <= 0) {
            return amount;
        }
        return amount + (amount * feeFactor);
    }
}
