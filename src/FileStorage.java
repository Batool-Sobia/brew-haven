package src;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileStorage handles permanent persistence of sales records on the local filesystem.
 * Implements atomic writes to prevent data corruption and handles missing files gracefully.
 */
public class FileStorage {
    private static final Logger LOGGER = Logger.getLogger(FileStorage.class.getName());
    private final Path salesDir;

    public FileStorage(String dataDirPath) {
        this.salesDir = Paths.get(dataDirPath, "sales");
        initDirectory();
    }

    private void initDirectory() {
        try {
            if (!Files.exists(salesDir)) {
                Files.createDirectories(salesDir);
                LOGGER.info("Created sales storage directory at: " + salesDir.toAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize sales directory", e);
        }
    }

    /**
     * Saves a completed Sale to disk.
     * Uses a temporary file and atomic rename to guarantee file integrity.
     *
     * @param sale The sale transaction to record
     * @return true if successfully saved, false otherwise
     */
    public synchronized boolean saveSale(Sale sale) {
        if (sale == null || sale.getReceiptId() == null || sale.getReceiptId().trim().isEmpty()) {
            LOGGER.warning("Attempted to save an invalid or null sale record.");
            return false;
        }

        String safeFileName = sanitizeFileName(sale.getReceiptId()) + ".json";
        Path targetFile = salesDir.resolve(safeFileName);

        // Prevent duplicate transaction IDs
        if (Files.exists(targetFile)) {
            LOGGER.warning("Duplicate transaction ID detected: " + sale.getReceiptId());
            return false;
        }

        Path tempFile = salesDir.resolve(safeFileName + ".tmp");

        try {
            // Write to temporary file first
            String jsonContent = sale.toJson();
            Files.writeString(tempFile, jsonContent, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            // Atomically replace target file
            try {
                Files.move(tempFile, targetFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                // Fallback for file systems that do not support atomic moves
                Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            LOGGER.info("Sale successfully saved to: " + targetFile.getFileName());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error writing sale file for receipt: " + sale.getReceiptId(), e);
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {}
            return false;
        }
    }

    /**
     * Loads a single sale by its receipt ID.
     */
    public synchronized Sale loadSale(String receiptId) {
        if (receiptId == null || receiptId.trim().isEmpty()) {
            return null;
        }

        String safeFileName = sanitizeFileName(receiptId) + ".json";
        Path targetFile = salesDir.resolve(safeFileName);

        if (!Files.exists(targetFile) || !Files.isRegularFile(targetFile)) {
            return null;
        }

        try {
            String json = Files.readString(targetFile, StandardCharsets.UTF_8);
            return Sale.fromJson(json);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error reading sale file: " + targetFile.getFileName(), e);
            return null;
        }
    }

    /**
     * Loads all completed sales from local storage, sorted newest to oldest.
     * Never crashes if directory is empty or if a single file has an issue.
     */
    public synchronized List<Sale> loadAllSales() {
        List<Sale> salesList = new ArrayList<>();
        if (!Files.exists(salesDir)) {
            return salesList;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(salesDir, "*.json")) {
            for (Path path : stream) {
                try {
                    String json = Files.readString(path, StandardCharsets.UTF_8);
                    Sale sale = Sale.fromJson(json);
                    if (sale != null && sale.getReceiptId() != null) {
                        salesList.add(sale);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Skipping corrupt or unreadable sale file: " + path.getFileName(), e);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read sales directory", e);
        }

        // Sort descending by timestamp (newest transactions first)
        salesList.sort(Comparator.comparing(Sale::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())));
        return salesList;
    }

    /**
     * Sanitizes file names to prevent path traversal or invalid characters.
     */
    private String sanitizeFileName(String input) {
        return input.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
