package com.orbitamarket.payments_service.service;

import com.orbitamarket.payments.exception.AccountAlreadyExistsException;
import com.orbitamarket.payments.exception.AccountNotFoundException;
import com.orbitamarket.payments.model.Account;
import com.orbitamarket.payments.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Account createAccount(String userId) {
        if (accountRepository.findByUserId(userId).isPresent()) {
            throw new AccountAlreadyExistsException("Account already exists for user: " + userId);
        }
        Account account = new Account();
        account.setUserId(userId);
        account.setBalance(0);
        return accountRepository.save(account);
    }

    public Account topUp(String userId, long amount) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for user: " + userId));
        account.setBalance(account.getBalance() + amount);
        return accountRepository.save(account);
    }

    public Account getBalance(String userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    public Account findByUserId(String userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }
}