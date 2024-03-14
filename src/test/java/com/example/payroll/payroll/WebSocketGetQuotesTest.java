package com.example.payroll.payroll;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class WebSocketGetQuotesTest {

    private MainVerticle service;

    private int port;

    @Before
    public void setup(final TestContext tc) {
        this.port = 8085;

        this.service = new MainVerticle();
        Async async = tc.async();
        this.service.startHttpServer(this.port, h -> {
            async.complete();
            if (h.failed()) {
                tc.assertTrue(false, "Error -> "
                    + h.cause().getMessage());
            }
        });
    }

    @After
    public void close(final TestContext tc) { }

    @Test
    public void getUsers(final TestContext tc) {
        HttpClient client = Vertx.vertx().createHttpClient();

        WebSocketConnectOptions opts = new WebSocketConnectOptions()
            .setPort(this.port)
            .setHost("localhost")
            .setURI("/getuser");
            // .addHeader("apiKey", UUID.randomUUID().toString());

        Async async = tc.async();
        client.webSocket(opts, h -> {
            if (h.succeeded()) {
                WebSocket ws = h.result();
                JsonObject body = new JsonObject()
                    .put("count", 6);

                ws.writeTextMessage(body.encode(), wh -> {
                    if (wh.failed()) {
                        async.complete();
                        tc.assertTrue(false, "Send message error -> "
                            + wh.cause().getMessage());
                    } else {
                        ws.textMessageHandler(msg -> {
                            System.out.println("RESPONSE ===> " + msg);
                            JsonObject resp = new JsonObject(msg);
                            int status = resp.getInteger("Status", 0);
                            if (status == 0) {
                                async.complete();
                            }
                        });
                    }
                });
            } else {
                async.complete();
                tc.assertTrue(false, "Error -> "
                    + h.cause().getMessage());
            }
        });
    }
}
