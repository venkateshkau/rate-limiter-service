package com.vk.ratelimiter;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(final String[] args) {
        int cores = Runtime.getRuntime().availableProcessors();
        LOGGER.info("Available processors: {}", cores);

        RoutingHandler routingHandler = new RoutingHandler();
        routingHandler.add(new HttpString("GET"), "/health", healthHandler())
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

                exchange.getRequestReceiver().receiveFullString((ex, body) -> {
                    Map<String, Object> payload = null;
                    try {
                        if(body == null || body.isBlank() ) {
                            sendJson(ex, 400, "{\"error\": \"Empty request body.\"}");
                            return;
                        }
                        payload = MAPPER.readValue(body, new TypeReference<>() {
                        });

                        String key = (String) payload.get("key");
                        if(key == null || key.isBlank()) {
                            sendJson(ex, 400, "{\"error\": \"Missing key in request?\"}");
                            return;
                        }
                        RateLimitResult response = TokenBucket.check(key);
                        String json = MAPPER.writeValueAsString(response);
                        ex.getResponseHeaders()
                                .put(Headers.CONTENT_TYPE, "application/json");
                        ex.setStatusCode(200);
                        ex.getResponseSender().send(json);
                    } catch (JsonProcessingException e) {
                        sendJson(ex, 400, "{\"error\": \"Invalid JSON, Good try Hacker! \" }");
                    } catch (Exception e) {
                        sendJson(ex, 500, "{\"error\": \""+ e.getMessage() + "\" }");
                    }
                });

            }
        };
    }

    private static void sendJson(HttpServerExchange ex, int status, String json) {
        ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json; charset=utf-8");
        ex.setStatusCode(status);
        ex.getResponseSender().send(json);
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
