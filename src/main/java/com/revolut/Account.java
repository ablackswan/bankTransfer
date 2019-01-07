package com.revolut;

import java.io.Serializable;
import java.math.BigDecimal;

public class Account implements Serializable {
    private static final long serialVersionUID = -55191161464652486L;
    private final String accountId;
    private BigDecimal balance = BigDecimal.ZERO;

    public Account(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal deposit(BigDecimal amount) {
        balance = balance.add(amount);
        return balance;
    }

    public BigDecimal withdraw(BigDecimal amount) {
        balance = balance.subtract(amount);
        return balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId='" + accountId + '\'' +
                ", balance=" + balance +
                '}';
    }
}
