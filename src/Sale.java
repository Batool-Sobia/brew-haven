package src;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sale represents a complete, finalized sales transaction at Brew Haven.
 * Financial totals are calculated and verified using BigDecimal for absolute precision.
 */
public class Sale {
    private final String receiptId;
    private final String timestamp;
    private final String customerName;
    private final String orderType;
    private final List<SaleItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal discountPercent;
    private final BigDecimal discountAmount;
    private final BigDecimal finalTotal;
    private final int totalItemsCount;
    private final String status;

    public Sale(String receiptId, String timestamp, String customerName, String orderType,
                List<SaleItem> items, BigDecimal subtotal, BigDecimal discountPercent,
                BigDecimal discountAmount, BigDecimal finalTotal, int totalItemsCount, String status) {
        this.receiptId = receiptId;
        this.timestamp = timestamp;
        this.customerName = (customerName == null || customerName.trim().isEmpty()) ? "Valued Guest" : customerName.trim();
        this.orderType = (orderType == null || orderType.trim().isEmpty()) ? "Dine-in" : orderType.trim();
        this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
        this.subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        this.discountPercent = discountPercent.setScale(2, RoundingMode.HALF_UP);
        this.discountAmount = discountAmount.setScale(2, RoundingMode.HALF_UP);
        this.finalTotal = finalTotal.setScale(2, RoundingMode.HALF_UP);
        this.totalItemsCount = totalItemsCount;
        this.status = (status == null || status.trim().isEmpty()) ? "COMPLETED" : status.trim();
    }

    public String getReceiptId() {
        return receiptId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getOrderType() {
        return orderType;
    }

    public List<SaleItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public int getTotalItemsCount() {
        return totalItemsCount;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Converts this Sale into a formatted JSON string for API responses and file storage.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"receiptId\":\"").append(JsonUtil.escape(receiptId)).append("\",");
        sb.append("\"timestamp\":\"").append(JsonUtil.escape(timestamp)).append("\",");
        sb.append("\"customerName\":\"").append(JsonUtil.escape(customerName)).append("\",");
        sb.append("\"orderType\":\"").append(JsonUtil.escape(orderType)).append("\",");
        sb.append("\"subtotal\":").append(subtotal.toPlainString()).append(",");
        sb.append("\"discountPercent\":").append(discountPercent.toPlainString()).append(",");
        sb.append("\"discountAmount\":").append(discountAmount.toPlainString()).append(",");
        sb.append("\"finalTotal\":").append(finalTotal.toPlainString()).append(",");
        sb.append("\"totalItemsCount\":").append(totalItemsCount).append(",");
        sb.append("\"status\":\"").append(JsonUtil.escape(status)).append("\",");

        sb.append("\"items\":[");
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i).toJson());
            if (i < items.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Reconstructs a Sale instance from a saved JSON string.
     */
    public static Sale fromJson(String json) {
        String receiptId = JsonUtil.extractString(json, "receiptId");
        String timestamp = JsonUtil.extractString(json, "timestamp");
        String customerName = JsonUtil.extractString(json, "customerName");
        String orderType = JsonUtil.extractString(json, "orderType");
        BigDecimal subtotal = JsonUtil.extractBigDecimal(json, "subtotal", BigDecimal.ZERO);
        BigDecimal discountPercent = JsonUtil.extractBigDecimal(json, "discountPercent", BigDecimal.ZERO);
        BigDecimal discountAmount = JsonUtil.extractBigDecimal(json, "discountAmount", BigDecimal.ZERO);
        BigDecimal finalTotal = JsonUtil.extractBigDecimal(json, "finalTotal", BigDecimal.ZERO);
        int totalItemsCount = JsonUtil.extractInt(json, "totalItemsCount", 0);
        String status = JsonUtil.extractString(json, "status");
        if (status.isEmpty()) status = "COMPLETED";

        List<SaleItem> items = JsonUtil.parseSaleItems(json);
        if (totalItemsCount == 0 && items != null) {
            for (SaleItem it : items) {
                totalItemsCount += it.getQuantity();
            }
        }

        return new Sale(receiptId, timestamp, customerName, orderType, items,
                subtotal, discountPercent, discountAmount, finalTotal, totalItemsCount, status);
    }
}
