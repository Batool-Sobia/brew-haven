package src;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ApiHandler processes RESTful API requests for Brew Haven:
 * - GET  /api/products : Fetch menu catalog
 * - POST /api/sales    : Process order, calculate bill with BigDecimal, save to file
 * - GET  /api/sales    : Fetch all completed sales or lookup by ID (?id=...)
 * - GET  /api/summary  : Fetch today's sales performance summary
 */
public class ApiHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(ApiHandler.class.getName());
    private final SalesService salesService;

    public ApiHandler(SalesService salesService) {
        this.salesService = salesService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers for browser compatibility
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        URI requestUri = exchange.getRequestURI();
        String path = requestUri.getPath();

        // Normalize trailing slashes
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        try {
            if (path.equals("/api/products")) {
                handleProducts(exchange, method);
            } else if (path.equals("/api/sales")) {
                handleSales(exchange, method, requestUri);
            } else if (path.equals("/api/summary")) {
                handleSummary(exchange, method);
            } else {
                sendJson(exchange, 404, "{\"success\":false,\"error\":\"Endpoint not found: " + path + "\"}");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled API error for " + path, e);
            sendJson(exchange, 500, "{\"success\":false,\"error\":\"Internal server error: " + JsonUtil.escape(e.getMessage()) + "\"}");
        }
    }

    private void handleProducts(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equals(method)) {
            sendJson(exchange, 405, "{\"success\":false,\"error\":\"Method Not Allowed\"}");
            return;
        }

        List<Product> products = salesService.getAllProducts();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true,\"products\":[");
        for (int i = 0; i < products.size(); i++) {
            sb.append(products.get(i).toJson());
            if (i < products.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");

        sendJson(exchange, 200, sb.toString());
    }

    private void handleSales(HttpExchange exchange, String method, URI uri) throws IOException {
        if ("GET".equals(method)) {
            Map<String, String> params = parseQueryParams(uri.getQuery());
            String receiptId = params.get("id");

            if (receiptId != null && !receiptId.trim().isEmpty()) {
                Sale sale = salesService.getSaleById(receiptId.trim());
                if (sale == null) {
                    sendJson(exchange, 404, "{\"success\":false,\"error\":\"Receipt not found: " + JsonUtil.escape(receiptId) + "\"}");
                } else {
                    sendJson(exchange, 200, "{\"success\":true,\"sale\":" + sale.toJson() + "}");
                }
            } else {
                List<Sale> allSales = salesService.getSalesHistory();
                StringBuilder sb = new StringBuilder();
                sb.append("{\"success\":true,\"count\":").append(allSales.size()).append(",\"sales\":[");
                for (int i = 0; i < allSales.size(); i++) {
                    sb.append(allSales.get(i).toJson());
                    if (i < allSales.size() - 1) {
                        sb.append(",");
                    }
                }
                sb.append("]}");
                sendJson(exchange, 200, sb.toString());
            }
        } else if ("POST".equals(method)) {
            String requestBody = readRequestBody(exchange);
            try {
                Sale completedSale = salesService.processSale(requestBody);
                sendJson(exchange, 201, "{\"success\":true,\"message\":\"Sale completed successfully\",\"sale\":" + completedSale.toJson() + "}");
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
            } catch (IllegalStateException e) {
                sendJson(exchange, 500, "{\"success\":false,\"error\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
            }
        } else {
            sendJson(exchange, 405, "{\"success\":false,\"error\":\"Method Not Allowed\"}");
        }
    }

    private void handleSummary(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equals(method)) {
            sendJson(exchange, 405, "{\"success\":false,\"error\":\"Method Not Allowed\"}");
            return;
        }

        SalesService.DailySummary summary = salesService.getDailySummary();
        sendJson(exchange, 200, "{\"success\":true,\"summary\":" + summary.toJson() + "}");
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.trim().isEmpty()) {
            return params;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf("=");
            if (idx > 0 && idx < pair.length() - 1) {
                String key = pair.substring(0, idx);
                String val = pair.substring(idx + 1);
                params.put(key, val);
            } else if (idx > 0) {
                params.put(pair.substring(0, idx), "");
            }
        }
        return params;
    }

    private void sendJson(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
