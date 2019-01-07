package com.revolut;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.Option;

import java.io.Serializable;
import java.math.BigDecimal;


public class TransferActor extends AbstractActor {
    static Props props() {
        return Props.create(TransferActor.class);
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(CommandDeposit.class, commandDeposit -> sendCommandToAccount(commandDeposit.getAccountId(), commandDeposit))
                .match(CommandWithdrawal.class, commandWithdrawal -> sendCommandToAccount(commandWithdrawal.getAccountId(), commandWithdrawal))
                .match(CommandGetAccount.class, commandGetAccount -> sendCommandToAccount(commandGetAccount.getAccountId(), commandGetAccount))
                .build();
    }

    private void sendCommandToAccount(String accountId, Object message) {
        Option<ActorRef> accountRefOption = context().child(accountId);
        if (accountRefOption.isDefined()) {
            accountRefOption.get().forward(message, context());
        } else {
            ActorRef accountRef = context().actorOf(AccountPersistentActor.props(accountId), accountId);
            accountRef.forward(message, context());
        }
    }


    public static class CommandGetAccount implements Serializable {
        private static final long serialVersionUID = -6282954257859026069L;
        private final String accountId;

        public CommandGetAccount(String accountId) {
            this.accountId = accountId;
        }

        public String getAccountId() {
            return accountId;
        }
    }


    public static class CommandDeposit implements Serializable {
        private static final long serialVersionUID = -6317803722230154367L;
        private final String accountId;
        private final BigDecimal amount;

        public CommandDeposit() {
            this.accountId = "";
            this.amount = BigDecimal.ZERO;
        }

        public CommandDeposit(String accountId, BigDecimal amount) {
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

    public static class CommandWithdrawal implements Serializable {
        private static final long serialVersionUID = -8078540626349119736L;
        private final String accountId;
        private final BigDecimal amount;

        public CommandWithdrawal() {
            this.accountId = "";
            this.amount = BigDecimal.ZERO;
        }

        public CommandWithdrawal(String accountId, BigDecimal amount) {
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


}
