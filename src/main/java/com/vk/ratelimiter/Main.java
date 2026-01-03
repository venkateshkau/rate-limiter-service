package com.vk.ratelimiter;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(final String[] args) {
        int cores = Runtime.getRuntime().availableProcessors();
        logger.info("Available processors: {}", cores);

        RoutingHandler routingHandler = new RoutingHandler();
        routingHandler.add( new HttpString("GET"), "/health", healthHandler())
                .setFallbackHandler(notFoundHandler());

        Undertow server = Undertow.builder()
                .addHttpListener(Constants.PORT, Constants.HOST)
                .setHandler(routingHandler).build();
        server.start();
    }

    private static HttpHandler notFoundHandler() {
        return new HttpHandler() {

            @Override
            public void handleRequest(HttpServerExchange exchange) throws Exception {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.setStatusCode(StatusCodes.NOT_FOUND);
                exchange.getResponseSender().send("{\"status\":\"Not Found\"}");
            }
        };
    }

    private static HttpHandler healthHandler() {
        return new HttpHandler() {
            @Override
            public void handleRequest(HttpServerExchange exchange) throws Exception {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send("{\"status\":\"ok\"}");
            }
        };
    }
}
