package src;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Product represents a coffee or bakery item available in the Brew Haven menu.
 * Designed for clarity and university presentation explanations.
 */
public class Product {
    private final int id;
    private final String name;
    private final String description;
    private final String category;
    private final BigDecimal price; // Using BigDecimal for precise financial values
    private final String image;

    public Product(int id, String name, String description, String category, BigDecimal price, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    /**
     * Converts the product to a clean JSON string representation.
     */
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"category\":\"%s\",\"price\":%.2f,\"image\":\"%s\"}",
            id,
            JsonUtil.escape(name),
            JsonUtil.escape(description),
            JsonUtil.escape(category),
            price.doubleValue(),
            JsonUtil.escape(image)
        );
    }
}
