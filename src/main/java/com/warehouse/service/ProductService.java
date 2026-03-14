package com.warehouse.service;

import com.warehouse.model.Product;
import com.warehouse.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public List<Product> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        return productRepository.search(query);
    }

    public List<Product> getLowStock() {
        return productRepository.findLowStock();
    }

    public Optional<Product> getById(int id) {
        return productRepository.findById(id);
    }

    public Product save(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    public void delete(int id) {
        productRepository.deleteById(id);
    }

    public int getTotalCount() {
        return productRepository.countAll();
    }

    public int getLowStockCount() {
        return productRepository.countLowStock();
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Название товара не может быть пустым");
        }
        if (product.getSku() == null || product.getSku().isBlank()) {
            throw new IllegalArgumentException("Артикул не может быть пустым");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Количество не может быть отрицательным");
        }
    }
}
