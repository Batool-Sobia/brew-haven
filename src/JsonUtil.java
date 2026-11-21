package src;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonUtil provides simple, dependency-free JSON parsing and serialization.
 * Perfectly suited for student college assignments where external libraries like
 * Jackson or Gson are prohibited or complicate compilation.
 */
public class JsonUtil {

    /**
     * Escapes characters for safe inclusion in JSON strings.
     */
    public static String escape(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Extracts a string value by key from a simple JSON object string.
     */
    public static String extractString(String json, String key) {
        if (json == null || key == null) return "";
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Extracts an integer value by key from a JSON string.
     */
    public static int extractInt(String json, String key, int defaultValue) {
        if (json == null || key == null) return defaultValue;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Extracts a BigDecimal value by key from a JSON string.
     */
    public static BigDecimal extractBigDecimal(String json, String key, BigDecimal defaultValue) {
        if (json == null || key == null) return defaultValue;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1)).setScale(2, RoundingMode.HALF_UP);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Helper to extract order items from checkout payload:
     * [{"productId": 1, "quantity": 2}, {"productId": 2, "quantity": 1}]
     */
    public static List<SimpleCartItem> parseOrderItems(String json) {
        List<SimpleCartItem> items = new ArrayList<>();
        if (json == null) return items;

        // Locate the "items" array in the json string
        int itemsIndex = json.indexOf("\"items\"");
        if (itemsIndex == -1) return items;

        int startBracket = json.indexOf('[', itemsIndex);
        if (startBracket == -1) return items;

        int endBracket = json.indexOf(']', startBracket);
        if (endBracket == -1) return items;

        String itemsArrayContent = json.substring(startBracket + 1, endBracket);

        // Match each individual object { ... }
        Pattern objPattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = objPattern.matcher(itemsArrayContent);

        while (matcher.find()) {
            String itemObj = matcher.group(1);
            int productId = extractInt("{" + itemObj + "}", "productId", -1);
            if (productId == -1) {
                productId = extractInt("{" + itemObj + "}", "id", -1);
            }
            int quantity = extractInt("{" + itemObj + "}", "quantity", 1);
            if (productId > 0 && quantity > 0) {
                items.add(new SimpleCartItem(productId, quantity));
            }
        }
        return items;
    }

    /**
     * Parses the list of SaleItems from a saved Sale JSON file.
     */
    public static List<SaleItem> parseSaleItems(String json) {
        List<SaleItem> items = new ArrayList<>();
        if (json == null) return items;

        int itemsIndex = json.indexOf("\"items\"");
        if (itemsIndex == -1) return items;

        int startBracket = json.indexOf('[', itemsIndex);
        if (startBracket == -1) return items;

        int endBracket = json.indexOf(']', startBracket);
        if (endBracket == -1) return items;

        String itemsArrayContent = json.substring(startBracket + 1, endBracket);
        Pattern objPattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = objPattern.matcher(itemsArrayContent);

        while (matcher.find()) {
            String itemObj = "{" + matcher.group(1) + "}";
            int productId = extractInt(itemObj, "productId", 0);
            String productName = extractString(itemObj, "productName");
            BigDecimal unitPrice = extractBigDecimal(itemObj, "unitPrice", BigDecimal.ZERO);
            int quantity = extractInt(itemObj, "quantity", 1);
            BigDecimal subtotal = extractBigDecimal(itemObj, "subtotal", unitPrice.multiply(BigDecimal.valueOf(quantity)));

            items.add(new SaleItem(productId, productName, unitPrice, quantity, subtotal));
        }
        return items;
    }

    /**
     * DTO for simple parsed cart items from request.
     */
    public static class SimpleCartItem {
        public final int productId;
        public final int quantity;

        public SimpleCartItem(int productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
