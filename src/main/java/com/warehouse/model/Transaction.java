package com.warehouse.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private Type type;
    private int productId;
    private String productName;
    private String productSku;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String note;
    private int userId;
    private String userName;
    private LocalDateTime createdAt;
    private String supplier;
    private String customer;

    public enum Type {
        INCOMING("Приход"),
        OUTGOING("Расход"),
        ADJUSTMENT("Корректировка");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Transaction() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
}
