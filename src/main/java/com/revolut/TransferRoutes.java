package com.revolut;

import akka.actor.ActorRef;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.RouteAdapter;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.revolut.AccountPersistentActor.CommandAccountResponse;
import com.revolut.TransferActor.CommandDeposit;
import com.revolut.TransferActor.CommandGetAccount;
import com.revolut.TransferActor.CommandWithdrawal;
import scala.concurrent.duration.Duration;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class TransferRoutes extends AllDirectives {
    final private ActorRef transferActor;

    final private Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));

    public TransferRoutes(ActorRef transferActor) {
        this.transferActor = transferActor;
    }

    public Route routes() {
        return route(pathPrefix("transfer",
                () -> route(
                        pathPrefix("receive",
                                () -> route(receiveTransfer())),
                        pathPrefix("send",
                                () -> route(sendTransfer())),
                        pathPrefix("accounts",
                                () -> route(path(PathMatchers.segment(), name -> route(getAccount(name)))))
                ))
        );

    }


    private Route sendTransfer() {
        return
                pathEnd(() ->
                        route(
                                post(() -> entity(
                                        Jackson.unmarshaller(CommandWithdrawal.class),
                                        commandWithdrawal -> {
                                            CompletionStage<Optional<CommandAccountResponse>> account = PatternsCS
                                                    .ask(transferActor, commandWithdrawal, timeout)
                                                    .thenApply(obj -> (Optional<CommandAccountResponse>) obj);
                                            return onSuccess(account);
                                        }))
                        ));
    }

    private Route transferSend(String name) {
        return get(() -> {
            CompletionStage<Optional<CommandAccountResponse>> account = PatternsCS
                    .ask(transferActor, new CommandWithdrawal("200", new BigDecimal(1)), timeout)
                    .thenApply(obj -> (Optional<CommandAccountResponse>) obj);

            return onSuccess(account);
        });
    }

    private Route receiveTransfer() {
        return
                pathEnd(() ->
                        route(
                                post(() -> entity(
                                        Jackson.unmarshaller(CommandDeposit.class),
                                        commandDeposit -> {
                                            CompletionStage<Optional<CommandAccountResponse>> account = PatternsCS
                                                    .ask(transferActor, commandDeposit, timeout)
                                                    .thenApply(obj -> (Optional<CommandAccountResponse>) obj);
                                            return onSuccess(account);
                                        }))
                        ));
    }


    private Route getAccount(String accountId) {
        return
                get(() -> {
                    CompletionStage<Optional<CommandAccountResponse>> account = PatternsCS
                            .ask(transferActor, new CommandGetAccount(accountId), timeout)
                            .thenApply(obj -> (Optional<CommandAccountResponse>) obj);
//
                    return onSuccess(account);
                });
    }

    private RouteAdapter onSuccess(CompletionStage<Optional<CommandAccountResponse>> account) {
        return onSuccess(() -> account,
                performed -> {
                    if (performed.isPresent())
                        return complete(StatusCodes.OK, performed.get(), Jackson.marshaller());
                    else
                        return complete(StatusCodes.NOT_FOUND);
                }
        );
    }

}
