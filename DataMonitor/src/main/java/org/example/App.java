package org.example;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.api.RecentBlocksJsonWriter;
import org.example.api.RecentBlocksResponse;
import org.example.client.BlockchainClient;
import org.example.service.BlockService;
import org.web3j.protocol.Web3j;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class App {

    private static final String DEFAULT_RPC_URL = "https://eth-mainnet.g.alchemy.com/v2/3AM57GszO0MV8S8e6WVyQ";

    public static void main(String[] args) throws IOException {
        String rpcUrl = System.getenv().getOrDefault("ETH_RPC_URL", DEFAULT_RPC_URL);
        int port = Integer.parseInt(System.getenv().getOrDefault("API_PORT", "8080"));

        BlockchainClient client = new BlockchainClient(rpcUrl);
        Web3j web3j = client.getWeb3j();
        BlockService blockService = new BlockService(web3j);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", new HealthHandler());
        server.createContext("/api/blocks/recent", new RecentBlocksHandler(blockService));
        server.setExecutor(null);
        server.start();

        System.out.println("Blockchain API running on http://localhost:" + port);
        System.out.println("RPC source: " + rpcUrl);
    }

    private static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptions(exchange);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            sendJson(exchange, 200, "{\"status\":\"ok\"}");
        }
    }

    private static class RecentBlocksHandler implements HttpHandler {
        private final BlockService blockService;

        private RecentBlocksHandler(BlockService blockService) {
            this.blockService = blockService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptions(exchange);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getQuery());
            int basicCount = parseOrDefault(queryParams.get("basic"), 100);
            int detailedCount = parseOrDefault(queryParams.get("detailed"), 10);

            try {
                RecentBlocksResponse response = blockService.getRecentBlocks(basicCount, detailedCount);
                sendJson(exchange, 200, RecentBlocksJsonWriter.toJson(response));
            } catch (Exception e) {
                String errorJson = "{\"error\":\"Failed to fetch blocks\",\"details\":\"" + escape(e.getMessage()) + "\"}";
                sendJson(exchange, 500, errorJson);
            }
        }
    }

    private static void sendOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        byte[] bytes = responseBody.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        } finally {
            exchange.close();
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<String, String>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                params.put(parts[0], parts[1]);
            }
        }
        return params;
    }

    private static int parseOrDefault(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String escape(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}