package com.sanosysalvos.bff.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioConMascotasDto {
    private Long id;
    private String nombre;
    private String email;
    private String rol;
    private List<MascotaResumenDto> mascotas;
}
