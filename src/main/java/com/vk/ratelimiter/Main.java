package com.vk.ratelimiter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static  final ObjectMapper MAPPER = new ObjectMapper();
    public static void main(final String[] args) {
        int cores = Runtime.getRuntime().availableProcessors();
        LOGGER.info("Available processors: {}", cores);

        RoutingHandler routingHandler = new RoutingHandler();
        routingHandler.add( new HttpString("GET"), "/health", healthHandler())
                .add(new HttpString("POST"), "/v1/check", checkHandler())
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

    private static HttpHandler checkHandler() {
        return new HttpHandler() {
            @Override
            public void handleRequest(HttpServerExchange exchange) throws Exception {
                exchange.startBlocking();
                try (InputStream in = exchange.getInputStream()) {
                        String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    Map<String, Object> payload =
                            MAPPER.readValue(body, new TypeReference<>() {});
                    String key = (String)payload.get("key");
                    RateLimitResult response = TokenBucket.check(key);
                    String json = MAPPER.writeValueAsString(response);
                    exchange.getResponseHeaders()
                            .put(Headers.CONTENT_TYPE, "application/json");
                    exchange.setStatusCode(200);
                    exchange.getResponseSender().send(json);
                } catch (RuntimeException e) {
                    throw new RuntimeException(e);
                } finally {
                    exchange.endExchange();
                }
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
