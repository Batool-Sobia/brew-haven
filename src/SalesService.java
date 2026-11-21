package src;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SalesService handles all business logic, order validation,
 * price verification with BigDecimal, discount rules, and daily sales metrics.
 */
public class SalesService {
    private final FileStorage fileStorage;
    private final Map<Integer, Product> productCatalog = new LinkedHashMap<>();
    private final AtomicInteger dailyCounter = new AtomicInteger(1000);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ID_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public SalesService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        initProducts();
        initCounterFromExistingSales();
    }

    /**
     * Initializes the standard Brew Haven product catalog with prices in Pakistani Rupees (PKR).
     */
    private void initProducts() {
        addProduct(new Product(1, "Espresso", "Rich and concentrated shot made from 100% premium Arabica beans.", "Hot Coffees", new BigDecimal("250.00"), "/images/espresso.svg"));
        addProduct(new Product(2, "Americano", "Double shot of espresso diluted with hot water for a crisp, robust flavor.", "Hot Coffees", new BigDecimal("300.00"), "/images/americano.svg"));
        addProduct(new Product(3, "Cappuccino", "Harmonious blend of equal parts espresso, steamed milk, and velvety foam.", "Hot Coffees", new BigDecimal("400.00"), "/images/cappuccino.svg"));
        addProduct(new Product(4, "Café Latte", "Smooth and creamy espresso combined with gently steamed milk.", "Hot Coffees", new BigDecimal("420.00"), "/images/latte.svg"));
        addProduct(new Product(5, "Mocha", "Decadent espresso fused with rich Dutch dark chocolate and whipped froth.", "Hot Coffees", new BigDecimal("450.00"), "/images/mocha.svg"));
        addProduct(new Product(6, "Caramel Macchiato", "Steamed vanilla-scented milk marked with espresso and drizzled with caramel.", "Hot Coffees", new BigDecimal("480.00"), "/images/caramel_macchiato.svg"));
        addProduct(new Product(7, "Iced Coffee", "Slow-steeped signature cold brew served over crystal ice cubes.", "Cold Brews & Iced", new BigDecimal("350.00"), "/images/iced_coffee.svg"));
        addProduct(new Product(8, "Hot Chocolate", "Thick and comforting Belgian hot cocoa crowned with fluffy marshmallow crema.", "Bakery & Treats", new BigDecimal("380.00"), "/images/hot_chocolate.svg"));
        addProduct(new Product(9, "Chocolate Muffin", "Moist, freshly baked oven muffin packed with melted dark chocolate chips.", "Bakery & Treats", new BigDecimal("220.00"), "/images/chocolate_muffin.svg"));
        addProduct(new Product(10, "Chocolate Brownie", "Fudge-textured artisan brownie baked with pure cocoa and walnut shavings.", "Bakery & Treats", new BigDecimal("250.00"), "/images/chocolate_brownie.svg"));
    }

    private void addProduct(Product p) {
        productCatalog.put(p.getId(), p);
    }

    private void initCounterFromExistingSales() {
        List<Sale> existing = fileStorage.loadAllSales();
        String todayPrefix = "BH-" + LocalDate.now().format(ID_DATE_FORMATTER) + "-";
        int maxSeq = 1000;
        for (Sale s : existing) {
            if (s.getReceiptId() != null && s.getReceiptId().startsWith(todayPrefix)) {
                try {
                    String seqStr = s.getReceiptId().substring(todayPrefix.length());
                    int seq = Integer.parseInt(seqStr);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        dailyCounter.set(maxSeq);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(productCatalog.values());
    }

    public Product getProduct(int id) {
        return productCatalog.get(id);
    }

    /**
     * Authoritatively processes a checkout request:
     * - Validates product existence and quantities.
     * - Uses server-side BigDecimal pricing (never trusts client totals).
     * - Validates discount codes.
     * - Assigns unique receipt ID and current timestamp.
     * - Saves sale atomically to local file storage.
     */
    public synchronized Sale processSale(String orderJson) throws IllegalArgumentException, IllegalStateException {
        if (orderJson == null || orderJson.trim().isEmpty()) {
            throw new IllegalArgumentException("Order payload is empty.");
        }

        List<JsonUtil.SimpleCartItem> requestedItems = JsonUtil.parseOrderItems(orderJson);
        if (requestedItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Please add at least one product.");
        }

        String customerName = JsonUtil.extractString(orderJson, "customerName");
        String orderType = JsonUtil.extractString(orderJson, "orderType");
        String discountCode = JsonUtil.extractString(orderJson, "discountCode").trim().toUpperCase();

        // Calculate discount percentage based on valid promotion codes or percentage
        BigDecimal discountPercent = BigDecimal.ZERO;
        if (!discountCode.isEmpty()) {
            switch (discountCode) {
                case "WELCOME10":
                    discountPercent = new BigDecimal("10.00");
                    break;
                case "STUDENT15":
                    discountPercent = new BigDecimal("15.00");
                    break;
                case "BREW20":
                    discountPercent = new BigDecimal("20.00");
                    break;
                default:
                    // Check if numeric discount rate was passed
                    BigDecimal customPct = JsonUtil.extractBigDecimal(orderJson, "discountPercent", BigDecimal.ZERO);
                    if (customPct.compareTo(BigDecimal.ZERO) > 0 && customPct.compareTo(new BigDecimal("50.00")) <= 0) {
                        discountPercent = customPct;
                    } else {
                        throw new IllegalArgumentException("Invalid discount code: " + discountCode);
                    }
            }
        }

        // Build verified line items and calculate subtotal
        List<SaleItem> verifiedItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItemsCount = 0;

        for (JsonUtil.SimpleCartItem reqItem : requestedItems) {
            Product product = productCatalog.get(reqItem.productId);
            if (product == null) {
                throw new IllegalArgumentException("Product ID " + reqItem.productId + " does not exist in catalog.");
            }
            if (reqItem.quantity <= 0) {
                throw new IllegalArgumentException("Quantity for " + product.getName() + " must be at least 1.");
            }
            if (reqItem.quantity > 99) {
                throw new IllegalArgumentException("Quantity for " + product.getName() + " cannot exceed 99.");
            }

            SaleItem item = new SaleItem(product.getId(), product.getName(), product.getPrice(), reqItem.quantity);
            verifiedItems.add(item);
            subtotal = subtotal.add(item.getSubtotal());
            totalItemsCount += reqItem.quantity;
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        // Calculate discount amount and final total
        BigDecimal discountAmount = subtotal.multiply(discountPercent)
                .divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = subtotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        // Generate unique receipt ID and timestamp
        String receiptId = "BH-" + LocalDate.now().format(ID_DATE_FORMATTER) + "-" + dailyCounter.incrementAndGet();
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        Sale sale = new Sale(
                receiptId,
                timestamp,
                customerName,
                orderType,
                verifiedItems,
                subtotal,
                discountPercent,
                discountAmount,
                finalTotal,
                totalItemsCount,
                "COMPLETED"
        );

        // Save to file storage
        boolean saved = fileStorage.saveSale(sale);
        if (!saved) {
            throw new IllegalStateException("Failed to persist transaction to local file storage.");
        }

        return sale;
    }

    public List<Sale> getSalesHistory() {
        return fileStorage.loadAllSales();
    }

    public Sale getSaleById(String receiptId) {
        return fileStorage.loadSale(receiptId);
    }

    /**
     * Calculates the daily sales summary for today directly from saved sales files.
     */
    public DailySummary getDailySummary() {
        String todayPrefix = LocalDate.now().toString(); // YYYY-MM-DD
        List<Sale> allSales = fileStorage.loadAllSales();

        BigDecimal todaySalesTotal = BigDecimal.ZERO;
        int transactionCount = 0;
        int totalItemsSold = 0;

        for (Sale s : allSales) {
            if (s.getTimestamp() != null && s.getTimestamp().startsWith(todayPrefix)) {
                todaySalesTotal = todaySalesTotal.add(s.getFinalTotal());
                transactionCount++;
                totalItemsSold += s.getTotalItemsCount();
            }
        }

        return new DailySummary(todaySalesTotal.setScale(2, RoundingMode.HALF_UP),
                transactionCount, totalItemsSold, todayPrefix);
    }

    /**
     * Data Transfer Object for Daily Summary metrics.
     */
    public static class DailySummary {
        public final BigDecimal totalSales;
        public final int transactionCount;
        public final int totalItemsSold;
        public final String date;

        public DailySummary(BigDecimal totalSales, int transactionCount, int totalItemsSold, String date) {
            this.totalSales = totalSales;
            this.transactionCount = transactionCount;
            this.totalItemsSold = totalItemsSold;
            this.date = date;
        }

        public String toJson() {
            return String.format(
                "{\"date\":\"%s\",\"totalSales\":%.2f,\"transactionCount\":%d,\"totalItemsSold\":%d}",
                JsonUtil.escape(date),
                totalSales.doubleValue(),
                transactionCount,
                totalItemsSold
            );
        }
    }
}
