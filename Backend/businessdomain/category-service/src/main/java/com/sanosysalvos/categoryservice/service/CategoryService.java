package com.sanosysalvos.categoryservice.service;

import com.sanosysalvos.categoryservice.dto.CategoryDto;
import com.sanosysalvos.categoryservice.exception.ResourceNotFoundException;
import com.sanosysalvos.categoryservice.model.Category;
import com.sanosysalvos.categoryservice.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public CategoryDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    public CategoryDto create(CategoryDto dto) {
        Category category = Category.builder().nombre(dto.getNombre().trim()).build();
        return toDto(categoryRepository.save(category));
    }

    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = findOrThrow(id);
        if (dto.getNombre() != null) category.setNombre(dto.getNombre().trim());
        return toDto(categoryRepository.save(category));
    }

    public void delete(Long id) {
        categoryRepository.delete(findOrThrow(id));
    }

    // ── Métodos privados ──────────────────────────────────────────────

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + id));
    }

    private CategoryDto toDto(Category c) {
        return CategoryDto.builder().id(c.getId()).nombre(c.getNombre()).build();
    }
}
