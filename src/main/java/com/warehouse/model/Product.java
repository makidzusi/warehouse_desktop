package com.warehouse.model;

import java.math.BigDecimal;

public class Product {
    private int id;
    private String name;
    private String sku;
    private String description;
    private int categoryId;
    private String categoryName;
    private int quantity;
    private int minQuantity;
    private BigDecimal price;
    private String unit;

    public Product() {}

    public Product(String name, String sku, String description, int categoryId,
                   int quantity, int minQuantity, BigDecimal price, String unit) {
        this.name = name;
        this.sku = sku;
        this.description = description;
        this.categoryId = categoryId;
        this.quantity = quantity;
        this.minQuantity = minQuantity;
        this.price = price;
        this.unit = unit;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getMinQuantity() { return minQuantity; }
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public boolean isLowStock() {
        return quantity <= minQuantity;
    }

    @Override
    public String toString() {
        return name + " [" + sku + "]";
    }
}
