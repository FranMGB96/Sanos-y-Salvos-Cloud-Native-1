package com.sanosysalvos.categoryservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoryDto {
    private Long id;
    @NotBlank(message = "El nombre es obligatorio") private String nombre;
}
