package com.warehouse.service;

import com.warehouse.model.Product;
import com.warehouse.model.Transaction;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.TransactionRepository;
import com.warehouse.util.SessionManager;

import java.math.BigDecimal;
import java.util.List;

public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final SessionManager session = SessionManager.getInstance();

    public TransactionService(TransactionRepository transactionRepository,
                              ProductRepository productRepository) {
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
    }

    public List<Transaction> getAll() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getByProductId(int productId) {
        return transactionRepository.findByProductId(productId);
    }

    public Transaction createIncoming(int productId, int quantity, BigDecimal unitPrice,
                                      String supplier, String note) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        if (quantity <= 0) throw new IllegalArgumentException("Количество должно быть > 0");

        Transaction t = new Transaction();
        t.setType(Transaction.Type.INCOMING);
        t.setProductId(productId);
        t.setQuantity(quantity);
        t.setUnitPrice(unitPrice);
        t.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        t.setSupplier(supplier);
        t.setNote(note);
        t.setUserId(session.getCurrentUser().getId());

        productRepository.updateQuantity(productId, product.getQuantity() + quantity);
        return transactionRepository.save(t);
    }

    public Transaction createOutgoing(int productId, int quantity, BigDecimal unitPrice,
                                      String customer, String note) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        if (quantity <= 0) throw new IllegalArgumentException("Количество должно быть > 0");
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                "Недостаточно товара на складе. Доступно: " + product.getQuantity()
            );
        }

        Transaction t = new Transaction();
        t.setType(Transaction.Type.OUTGOING);
        t.setProductId(productId);
        t.setQuantity(quantity);
        t.setUnitPrice(unitPrice);
        t.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        t.setCustomer(customer);
        t.setNote(note);
        t.setUserId(session.getCurrentUser().getId());

        productRepository.updateQuantity(productId, product.getQuantity() - quantity);
        return transactionRepository.save(t);
    }

    public Transaction createAdjustment(int productId, int newQuantity, String note) {
        productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        if (newQuantity < 0) throw new IllegalArgumentException("Количество не может быть отрицательным");

        Transaction t = new Transaction();
        t.setType(Transaction.Type.ADJUSTMENT);
        t.setProductId(productId);
        t.setQuantity(newQuantity);
        t.setUnitPrice(BigDecimal.ZERO);
        t.setTotalPrice(BigDecimal.ZERO);
        t.setNote(note);
        t.setUserId(session.getCurrentUser().getId());

        productRepository.updateQuantity(productId, newQuantity);
        return transactionRepository.save(t);
    }

    public long getTodayCount() {
        return transactionRepository.countToday();
    }
}
