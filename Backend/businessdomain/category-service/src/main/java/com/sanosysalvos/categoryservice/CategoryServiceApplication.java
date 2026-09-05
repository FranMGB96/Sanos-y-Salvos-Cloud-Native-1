package com.sanosysalvos.categoryservice;

import com.sanosysalvos.categoryservice.model.Category;
import com.sanosysalvos.categoryservice.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication @EnableDiscoveryClient
@OpenAPIDefinition(info = @Info(title = "Category Service API", version = "1.0"))
public class CategoryServiceApplication {
    public static void main(String[] args) { SpringApplication.run(CategoryServiceApplication.class, args); }

    // Siembra las categorías (especies) por defecto si la tabla está vacía.
    @Bean
    CommandLineRunner seedCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                List<String> defaults = List.of("Perro", "Gato", "Ave", "Otros");
                defaults.forEach(nombre ->
                        categoryRepository.save(Category.builder().nombre(nombre).build()));
            }
        };
    }
}
