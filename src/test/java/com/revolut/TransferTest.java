package com.revolut;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import org.junit.Before;
import org.junit.Test;


public class TransferTest extends JUnitRouteTest {

    private TestRoute testRoute;

    @Before
    public void initClass() {
        ActorSystem system = ActorSystem.create("transferHttpServer");
        ActorRef transferActor = system.actorOf(TransferActor.props(), "transferActor");
        QuickStartServer server = new QuickStartServer(transferActor);
        testRoute = testRoute(server.createRoute());
    }

    @Test
    public void testGetAccount() {
        testRoute.run(HttpRequest.GET("/transfer/accounts/IBAN1"))
                .assertStatusCode(StatusCodes.OK)
                .assertMediaType("application/json")
                .assertEntity("{\"account\":{\"accountId\":\"IBAN1\",\"balance\":0}}");
    }

    @Test
    public void testReceiveTransfer() {
        testRoute.run(HttpRequest.POST("/transfer/receive")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(),
                        "{\"accountId\": \"IBAN1\", \"amount\": 200}"))
                .assertStatusCode(StatusCodes.OK)
                .assertMediaType("application/json")
                .assertEntity("{\"account\":{\"accountId\":\"IBAN1\",\"balance\":200}}");
    }


    @Test
    public void testReceiveMultipleTransfer() {
        testRoute.run(HttpRequest.POST("/transfer/receive")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(),
                        "{\"accountId\": \"IBAN1\", \"amount\": 200}"))
                .assertStatusCode(StatusCodes.OK);

        testRoute.run(HttpRequest.POST("/transfer/receive")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(),
                        "{\"accountId\": \"IBAN1\", \"amount\": 200}"))
                .assertStatusCode(StatusCodes.OK);


        testRoute.run(HttpRequest.POST("/transfer/receive")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(),
                        "{\"accountId\": \"IBAN1\", \"amount\": 200}"))
                .assertStatusCode(StatusCodes.OK)
                .assertMediaType("application/json")
                .assertEntity("{\"account\":{\"accountId\":\"IBAN1\",\"balance\":600}}");
    }

    @Test
    public void testSendTransfer() {
        testRoute.run(HttpRequest.POST("/transfer/send")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(),
                        "{\"accountId\": \"IBAN1\", \"amount\": 200}"))
                .assertStatusCode(StatusCodes.OK)
                .assertMediaType("application/json")
                .assertEntity("{\"account\":{\"accountId\":\"IBAN1\",\"balance\":-200}}");
    }




}
