package com.warehouse.service;

import com.warehouse.model.Category;
import com.warehouse.repository.CategoryRepository;

import java.util.List;

public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category save(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }
        return categoryRepository.save(category);
    }

    public void delete(int id) {
        categoryRepository.deleteById(id);
    }
}
