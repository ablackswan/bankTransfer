package com.revolut;

import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.journal.Tagged;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

public class AccountPersistentActor extends AbstractPersistentActor {
    private final LoggingAdapter log = Logging.getLogger(context().system(), this);
    public static final String ACCOUNT_TAG = "account";
    private Account account;

    public AccountPersistentActor(String accountId) {
        this.account = new Account(accountId);
    }

    public static Props props(String accountId) {
        return Props.create(AccountPersistentActor.class, accountId);
    }

    @Override
    public String persistenceId() {
        return account.getAccountId();
    }

    private Tagged asTagged(Object event, String... tags) {
        return new Tagged(event, new HashSet<>(Arrays.asList(tags)));
    }

    @Override
    public Receive createReceiveRecover() {
        return ReceiveBuilder.create()
                .match(EventDeposit.class, this::recoverEventDeposit)
                .match(EventWithdraw.class, this::recoverEventWithdrawal)
                .build();
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(TransferActor.CommandDeposit.class, this::receiveCommandDeposit)
                .match(TransferActor.CommandWithdrawal.class, this::receiveCommandWithdrawal)
                .match(TransferActor.CommandGetAccount.class, this::receiveCommandGetAccount)
                .build();
    }

    private void recoverEventDeposit(EventDeposit eventDeposit) {
        account.deposit(eventDeposit.getAmount());
    }

    private void recoverEventWithdrawal(EventWithdraw eventWithdraw) {
        account.withdraw(eventWithdraw.getAmount());
    }

    private void receiveCommandDeposit(TransferActor.CommandDeposit commandDeposit) {
        EventDeposit eventDeposit = new EventDeposit(account.getAccountId(), commandDeposit.getAmount());
        persist(asTagged(eventDeposit, ACCOUNT_TAG), this::depositPersisted);
    }

    private void receiveCommandWithdrawal(TransferActor.CommandWithdrawal commandWithdrawal) {
        EventWithdraw eventWithdraw = new EventWithdraw(account.getAccountId(), commandWithdrawal.getAmount());
        persist(asTagged(eventWithdraw, "account"), this::withdrawPersisted);
    }

    private void receiveCommandGetAccount(TransferActor.CommandGetAccount commandGetAccount) {
        getSender().tell(Optional.of(new CommandAccountResponse(account)), self());
    }

    private void depositPersisted(Tagged tagged) {
        EventDeposit eventDeposit = (EventDeposit) tagged.payload();
        account.deposit(eventDeposit.getAmount());
        getSender().tell(Optional.of(new CommandAccountResponse(account)), self());
        log.info("deposit {}  {} ",eventDeposit.getAmount(), account);
    }

    private void withdrawPersisted(Tagged tagged) {
        EventWithdraw eventWithdraw = (EventWithdraw) tagged.payload();
        account.withdraw(eventWithdraw.getAmount());
        getSender().tell(Optional.of(new CommandAccountResponse(account)), self());
        log.info("withdraw {}  {}",eventWithdraw.getAmount(), account);
    }

    public static class EventDeposit implements Serializable {
        private static final long serialVersionUID = 8679724848171721423L;
        private final String accountId;
        private final BigDecimal amount;

        public EventDeposit(String accountId, BigDecimal amount) {
            this.accountId = accountId;
            this.amount = amount;
        }

        public String getAccountId() {
            return accountId;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }

    public static class EventWithdraw implements Serializable {
        private static final long serialVersionUID = -3167348943370220426L;
        private final String accountId;
        private final BigDecimal amount;

        public EventWithdraw(String accountId, BigDecimal amount) {
            this.accountId = accountId;
            this.amount = amount;
        }

        public String getAccountId() {
            return accountId;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }

    public static class CommandAccountResponse implements Serializable {
        private static final long serialVersionUID = -3040738273342611395L;
        private final Account account;

        public CommandAccountResponse(Account account) {
            this.account = account;
        }

        public Account getAccount() {
            return account;
        }
    }

}




