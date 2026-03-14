package com.warehouse.service;

import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.Map;

public class AnalyticsService {
    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    public AnalyticsService(TransactionRepository transactionRepository,
                             ProductRepository productRepository) {
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
    }

    public Map<String, Double> getDailyIncoming(int days) {
        return transactionRepository.getDailyIncoming(days);
    }

    public Map<String, Double> getDailyOutgoing(int days) {
        return transactionRepository.getDailyOutgoing(days);
    }

    public Map<String, Integer> getTopProducts(int limit) {
        return transactionRepository.getTopProductsByMovement(limit);
    }

    public BigDecimal getTotalIncomingValue() {
        return transactionRepository.getTotalValueIncoming();
    }

    public BigDecimal getTotalOutgoingValue() {
        return transactionRepository.getTotalValueOutgoing();
    }

    public int getTotalProducts() {
        return productRepository.countAll();
    }

    public int getLowStockCount() {
        return productRepository.countLowStock();
    }

    public long getTodayTransactions() {
        return transactionRepository.countToday();
    }
}
