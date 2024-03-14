package com.example.payroll.payroll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;

public class MainVerticle extends AbstractVerticle {

  /**
     * The logger instance that is used to log.
     */
  private Logger logger = LoggerFactory.getLogger(
    MainVerticle.class.getName());

  /**
   * The default port.
   */
  private static final int DEFAULT_PORT = 8080;


  @Override
  public void start() {

    this.logger.info("Starting MotorService ->");
        try {
            this.startHttpServer(DEFAULT_PORT, event -> {
                if (event.failed()) {
                    logger.error("Server start failed!", event.cause());
                }
            });
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
        this.logger.info("Starting MotorService <-");
  }

  /**
     * Start http server.
     * @param customport A custom server port.
     * @param handler The result handler.
     */
    protected void startHttpServer(final int customport,
        final Handler<AsyncResult<HttpServer>> handler) {

        // For monitoring.
        this.vertx = Vertx.vertx(new VertxOptions()
            .setMetricsOptions(new MicrometerMetricsOptions()
                .setPrometheusOptions(new VertxPrometheusOptions()
                    .setEnabled(true))
                .setEnabled(true)));

        this.createWebSocket(customport, null, handler);
    }

    /**
     * Closes all the resources that were opened.
     * @param completionHandler The complention handler.
     */
    public void close(final Handler<AsyncResult<Void>> completionHandler) {
      this.vertx.close(completionHandler);
    }

    /**
     * Creates a web socket server.
     * @param custowsmport The custom port.
     * @param opts The web server options.
     * @param handler The web socket result handler.
     */
    private void createWebSocket(final int custowsmport,
        final HttpServerOptions opts,
        final Handler<AsyncResult<HttpServer>> handler) {
            if (opts == null) {
                this.vertx.createHttpServer()
                    .webSocketHandler(this::makeWsRequest)
                        .listen(custowsmport, handler);
            } else {
                this.vertx.createHttpServer(opts)
                    .webSocketHandler(this::makeWsRequest)
                        .listen(custowsmport, handler);
            }
    }

    /**
     * Makes web socket requests.
     * @param ws The web socket utils.
     */
    private void makeWsRequest(final ServerWebSocket ws) {
        this.logger.info("makeWsRequest(uri = {"
            + ws.uri()
            + "}, path = {" + ws.path() + "}) ->");
        String path = ws.path() == null
            ? ""
            : ws.path();
        if (path.endsWith("getuser")) {
            this.getWsUser(ws);
        } else {
            ws.writeTextMessage(new JsonObject()
                .put("Status", 500)
                .put("Message", "Error -> Unsuported URL")
                    .encode());
            ws.close();
        }
    }

    /**
     * Gets the user via web socket.
     * @param ws The web socket utils.
     */
    private void getWsUser(final ServerWebSocket ws) {
        ws.textMessageHandler(req -> {
            JsonObject body = new JsonObject(req);
            Integer count = body.getInteger("count");
            if (count == null || count < 1) {
                ws.writeTextMessage(new JsonObject()
                    .put("Status", 501)
                    .put("Message", "Error -> Unsuported count!")
                        .encode());
            } else {
                for (int i = 0; i < count; i++) {
                    ws.writeTextMessage(
                        new JsonObject()
                        .put("Status", 200)
                        .put("Message", "Success")
                        .put("Payload", "Record -> " + i)
                            .encode());
                }

                ws.writeTextMessage(
                        new JsonObject()
                        .put("Status", 0)
                        .put("Message", "Success")
                            .encode());
            }

            ws.close();
        });
    }
}
