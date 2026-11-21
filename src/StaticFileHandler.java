package src;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * StaticFileHandler safely serves HTML, CSS, JavaScript, and image assets
 * from the public/ directory. Prevents directory traversal attacks.
 */
public class StaticFileHandler implements HttpHandler {
    private final Path publicRoot;
    private final Map<String, String> mimeTypes = new HashMap<>();

    public StaticFileHandler(String publicDir) {
        this.publicRoot = Paths.get(publicDir).toAbsolutePath().normalize();
        initMimeTypes();
    }

    private void initMimeTypes() {
        mimeTypes.put(".html", "text/html; charset=UTF-8");
        mimeTypes.put(".htm", "text/html; charset=UTF-8");
        mimeTypes.put(".css", "text/css; charset=UTF-8");
        mimeTypes.put(".js", "application/javascript; charset=UTF-8");
        mimeTypes.put(".json", "application/json; charset=UTF-8");
        mimeTypes.put(".svg", "image/svg+xml");
        mimeTypes.put(".png", "image/png");
        mimeTypes.put(".jpg", "image/jpeg");
        mimeTypes.put(".jpeg", "image/jpeg");
        mimeTypes.put(".ico", "image/x-icon");
        mimeTypes.put(".txt", "text/plain; charset=UTF-8");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }

        URI requestUri = exchange.getRequestURI();
        String pathStr = requestUri.getPath();

        // Default root to index.html
        if (pathStr == null || pathStr.equals("/") || pathStr.isEmpty()) {
            pathStr = "/index.html";
        }

        // Clean relative path (remove leading slash)
        if (pathStr.startsWith("/")) {
            pathStr = pathStr.substring(1);
        }

        Path requestedFile = publicRoot.resolve(pathStr).normalize();

        // Security check: Directory Traversal Prevention
        if (!requestedFile.startsWith(publicRoot)) {
            byte[] forbidden = "403 Forbidden: Access Denied".getBytes(StandardCharsets.UTF_8);
            sendResponse(exchange, 403, "text/plain", forbidden);
            return;
        }

        // If path doesn't have an extension, try appending .html (e.g. /menu -> /menu.html)
        if (!Files.exists(requestedFile) && !pathStr.contains(".")) {
            Path htmlFallback = publicRoot.resolve(pathStr + ".html").normalize();
            if (Files.exists(htmlFallback) && Files.isRegularFile(htmlFallback)) {
                requestedFile = htmlFallback;
            }
        }

        // If directory, look for index.html inside
        if (Files.isDirectory(requestedFile)) {
            requestedFile = requestedFile.resolve("index.html");
        }

        if (!Files.exists(requestedFile) || !Files.isRegularFile(requestedFile)) {
            byte[] notFound = ("404 Not Found: The requested page " + requestUri.getPath() + " was not found.")
                    .getBytes(StandardCharsets.UTF_8);
            sendResponse(exchange, 404, "text/plain; charset=UTF-8", notFound);
            return;
        }

        String mimeType = getMimeType(requestedFile.getFileName().toString());
        byte[] fileBytes = Files.readAllBytes(requestedFile);

        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.getResponseHeaders().set("Pragma", "no-cache");
        exchange.getResponseHeaders().set("Expires", "0");

        if ("HEAD".equalsIgnoreCase(method)) {
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
            return;
        }

        sendResponse(exchange, 200, mimeType, fileBytes);
    }

    private String getMimeType(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex != -1) {
            String ext = filename.substring(dotIndex).toLowerCase();
            return mimeTypes.getOrDefault(ext, "application/octet-stream");
        }
        return "application/octet-stream";
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String contentType, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }
}
