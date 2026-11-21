package src;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * SaleItem represents an individual line item inside a completed Sale.
 * Contains product details, quantity, unit price, and the calculated line subtotal.
 */
public class SaleItem {
    private final int productId;
    private final String productName;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final BigDecimal subtotal;

    public SaleItem(int productId, String productName, BigDecimal unitPrice, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.quantity = quantity;
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    // Overload constructor for loading from saved JSON files
    public SaleItem(int productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.quantity = quantity;
        this.subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public String toJson() {
        return String.format(
            "{\"productId\":%d,\"productName\":\"%s\",\"unitPrice\":%.2f,\"quantity\":%d,\"subtotal\":%.2f}",
            productId,
            JsonUtil.escape(productName),
            unitPrice.doubleValue(),
            quantity,
            subtotal.doubleValue()
        );
    }
}
