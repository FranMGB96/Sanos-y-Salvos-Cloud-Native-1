package com.sanosysalvos.categoryservice;

import com.sanosysalvos.categoryservice.dto.CategoryDto;
import com.sanosysalvos.categoryservice.exception.ResourceNotFoundException;
import com.sanosysalvos.categoryservice.model.Category;
import com.sanosysalvos.categoryservice.repository.CategoryRepository;
import com.sanosysalvos.categoryservice.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryService categoryService;

    @Test void createCategory_datosValidos_retornaCategory() {
        CategoryDto dto = CategoryDto.builder().nombre("Perro").build();
        Category saved = Category.builder().id(1L).nombre("Perro").build();
        when(categoryRepository.save(any())).thenReturn(saved);
        CategoryDto result = categoryService.create(dto);
        assertNotNull(result); assertEquals("Perro", result.getNombre());
    }

    @Test void getById_noExiste_lanzaExcepcion() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getById(99L));
    }

    @Test void getAll_retornaTodasLasCategorias() {
        when(categoryRepository.findAll()).thenReturn(List.of(
            Category.builder().id(1L).nombre("Perro").build(),
            Category.builder().id(2L).nombre("Gato").build(),
            Category.builder().id(3L).nombre("Ave").build(),
            Category.builder().id(4L).nombre("Otros").build()
        ));
        assertEquals(4, categoryService.getAll().size());
    }

    @Test void update_actualizaNombre() {
        Category category = Category.builder().id(1L).nombre("Perro").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenReturn(category);
        CategoryDto result = categoryService.update(1L, CategoryDto.builder().nombre("Canino").build());
        assertEquals("Canino", result.getNombre());
    }

    @Test void delete_categoriaExistente_eliminaSinError() {
        Category category = Category.builder().id(1L).nombre("Perro").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        categoryService.delete(1L);
        verify(categoryRepository).delete(category);
    }
}
