package src;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

/**
 * Main application entry point for Brew Haven.
 * Starts Java's built-in HttpServer with zero external dependencies.
 */
public class Main {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        // Ensure directories exist
        String dataDir = "data";
        String publicDir = "public";

        try {
            Files.createDirectories(Paths.get(dataDir, "sales"));
            Files.createDirectories(Paths.get(publicDir));
        } catch (IOException e) {
            System.err.println("Warning: Could not create base directories: " + e.getMessage());
        }

        // Initialize Services
        FileStorage fileStorage = new FileStorage(dataDir);
        SalesService salesService = new SalesService(fileStorage);

        HttpServer server = null;
        int activePort = port;

        // Try primary port, or fall back if already in use
        for (int p = port; p <= port + 5; p++) {
            try {
                server = HttpServer.create(new InetSocketAddress(p), 0);
                activePort = p;
                break;
            } catch (IOException e) {
                System.out.println("Port " + p + " is occupied, trying next port...");
            }
        }

        if (server == null) {
            System.err.println("ERROR: Could not bind server to ports " + port + "-" + (port + 5));
            System.exit(1);
        }

        // Register Handlers
        server.createContext("/api/", new ApiHandler(salesService));
        server.createContext("/", new StaticFileHandler(publicDir));

        // Use multithreaded executor for smooth request handling
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        final HttpServer finalServer = server;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down Brew Haven server cleanly...");
            finalServer.stop(1);
            System.out.println("Server stopped.");
        }));

        printWelcomeBanner(activePort);
    }

    private static void printWelcomeBanner(int port) {
        System.out.println("=============================================================");
        System.out.println("            ☕ BREW HAVEN COFFEE SHOP SERVER ☕             ");
        System.out.println("      Sales and Receipt Management System (University Lab)   ");
        System.out.println("=============================================================");
        System.out.println(" [✔] Status: Server is RUNNING");
        System.out.println(" [✔] Local URL: http://localhost:" + port);
        System.out.println(" [✔] Static Assets: public/");
        System.out.println(" [✔] Permanent File Storage: data/sales/");
        System.out.println(" [✔] Engine: Java Built-in com.sun.net.httpserver.HttpServer");
        System.out.println(" [✔] Dependencies: 100% Standard Java (Zero External JARs)");
        System.out.println("-------------------------------------------------------------");
        System.out.println(" Open your browser and navigate to: http://localhost:" + port);
        System.out.println(" Press Ctrl + C in the terminal to stop the server anytime.");
        System.out.println("=============================================================");
    }
}
