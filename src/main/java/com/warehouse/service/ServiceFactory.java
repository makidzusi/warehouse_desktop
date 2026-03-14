package com.warehouse.service;

import com.warehouse.repository.*;

public class ServiceFactory {
    private static ServiceFactory instance;

    private final UserRepository userRepository = new UserRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();

    private final AuthService authService = new AuthService(userRepository);
    private final CategoryService categoryService = new CategoryService(categoryRepository);
    private final ProductService productService = new ProductService(productRepository);
    private final TransactionService transactionService = new TransactionService(transactionRepository, productRepository);
    private final AnalyticsService analyticsService = new AnalyticsService(transactionRepository, productRepository);

    private ServiceFactory() {}

    public static ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    public AuthService getAuthService() { return authService; }
    public CategoryService getCategoryService() { return categoryService; }
    public ProductService getProductService() { return productService; }
    public TransactionService getTransactionService() { return transactionService; }
    public AnalyticsService getAnalyticsService() { return analyticsService; }
}
