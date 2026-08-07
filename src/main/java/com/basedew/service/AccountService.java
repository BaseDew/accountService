package com.basedew.service;

import com.basedew.Account;
import com.basedew.dto.CreateAccountRequest;
import com.basedew.dto.FundsDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;

import java.util.List;

@ApplicationScoped
public class AccountService {

    public Account create(@Valid CreateAccountRequest request) {
            Account account = new Account();
            account.userId = request.userId();
            account.currency = request.currency();
            account.persist();
            return account;
    }

    public List<Account> getUserAccounts(Long userId) {
        return Account.list("userId", userId);
    }

    public Account getDetails(Long userId, Long accountId) {
        return Account.findById(accountId);
    }

    public void addFunds(Long userId, Long accountId, @Valid FundsDTO fundsDto) {
        Account account = Account.findById(accountId);
        if (account == null || !account.userId.equals(userId)) {
            throw new IllegalStateException("Specified account not found for user");
        }
        if (!account.currency.equals(fundsDto.currency())) {
            throw new IllegalStateException("Currency type not match account");
        }
        account.funds += fundsDto.funds();
        account.persist();
    }

    public void deductFunds(Long userId, Long accountId, @Valid FundsDTO fundsDto) {
        Account account = Account.findById(accountId);
        if (account == null || !account.userId.equals(userId)) {
            throw new IllegalStateException("Specified account not found for user");
        }
        if (!account.currency.equals(fundsDto.currency())) {
            throw new IllegalStateException("Currency type not match account");
        }
        if (account.funds - account.blockedFunds < fundsDto.funds()) {
            throw new IllegalStateException("Account balance too low");
        }
        account.funds -= fundsDto.funds(); //TODO deduct only if funds blocked?
        account.persist();
    }

    public void unblockFunds(Long userId, Long accountId, @Valid FundsDTO fundsDto) {
        Account account = Account.findById(accountId);
        if (account == null || !account.userId.equals(userId)) {
            throw new IllegalStateException("Specified account not found for user");
        }
        if (!account.currency.equals(fundsDto.currency())) {
            throw new IllegalStateException("Currency type not match account");
        }
        if (account.blockedFunds < fundsDto.funds()) {
            throw new IllegalStateException("No funds to unblock");
        }
        account.blockedFunds -= fundsDto.funds();
        account.persist();
    }

    public void blockFunds(Long userId, Long accountId, @Valid FundsDTO fundsDto) {
        Account account = Account.findById(accountId);
        if (account == null || !account.userId.equals(userId)) {
            throw new IllegalStateException("Specified account not found for user");
        }
        if (!account.currency.equals(fundsDto.currency())) {
            throw new IllegalStateException("Currency type not match account");
        }
        if (account.funds - account.blockedFunds < fundsDto.funds()) {
            throw new IllegalStateException("Account balance too low");
        }
        account.blockedFunds += fundsDto.funds();
        account.persist();
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
}
