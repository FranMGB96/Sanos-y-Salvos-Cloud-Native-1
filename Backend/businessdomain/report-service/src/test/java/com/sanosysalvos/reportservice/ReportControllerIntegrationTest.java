package com.sanosysalvos.reportservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanosysalvos.reportservice.dto.ReportDto;
import com.sanosysalvos.reportservice.model.Report;
import com.sanosysalvos.reportservice.repository.ReportRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de INTEGRACIÓN — ReportController
 *
 * Levanta el contexto completo de Spring Boot con H2 en memoria.
 * Prueba los endpoints críticos del proceso de negocio de reportes:
 * crear reporte, listar, filtrar por tipo y cambiar estado.
 */
@SpringBootTest(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.cloud.config.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReportControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ReportRepository reportRepository;

    private static Long reporteIdCreado;

    @BeforeEach
    void limpiarBD() {
        reportRepository.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────
    // CREAR REPORTE
    // ─────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Crear reporte PERDIDO → retorna 201 con datos correctos")
    void crearReporte_perdido_retorna201() throws Exception {
        ReportDto dto = ReportDto.builder()
                .tipo("PERDIDO")
                .descripcion("Mi perro Firulais se perdió en el Parque O'Higgins")
                .latitud(-33.4569)
                .longitud(-70.6483)
                .ubicacionDescripcion("Parque O'Higgins, Santiago")
                .reporterUserId(1L)
                .build();

        String response = mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("PERDIDO"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.descripcion").value("Mi perro Firulais se perdió en el Parque O'Higgins"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        // Guardar el ID para otros tests
        reporteIdCreado = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("Crear reporte ENCONTRADO → retorna 201 con estado ACTIVO")
    void crearReporte_encontrado_retorna201() throws Exception {
        ReportDto dto = ReportDto.builder()
                .tipo("ENCONTRADO")
                .descripcion("Encontré un gato blanco cerca del metro Baquedano")
                .latitud(-33.4383)
                .longitud(-70.6346)
                .ubicacionDescripcion("Metro Baquedano, Santiago")
                .reporterUserId(2L)
                .build();

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ENCONTRADO"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @Order(3)
    @DisplayName("Crear reporte sin tipo → retorna error 400 por validación")
    void crearReporte_sinTipo_retornaError400() throws Exception {
        ReportDto dto = ReportDto.builder()
                .descripcion("Descripcion sin tipo")
                .reporterUserId(1L)
                .build();

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("Crear reporte sin descripción → retorna error 400 por validación")
    void crearReporte_sinDescripcion_retornaError400() throws Exception {
        ReportDto dto = ReportDto.builder()
                .tipo("PERDIDO")
                .reporterUserId(1L)
                .build();

        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────────────────────
    // LISTAR Y FILTRAR REPORTES
    // ─────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("Listar todos los reportes → retorna lista no vacía")
    void listarReportes_retornaLista() throws Exception {
        // Crear un reporte primero
        ReportDto dto = ReportDto.builder()
                .tipo("PERDIDO")
                .descripcion("Mascota perdida para listar")
                .reporterUserId(1L)
                .build();

        mockMvc.perform(post("/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    @Order(6)
    @DisplayName("Filtrar reportes por tipo PERDIDO → solo retorna PERDIDOS")
    void filtrarPorTipo_perdido_soloRetornaPerdidos() throws Exception {
        // Crear reporte PERDIDO
        mockMvc.perform(post("/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        ReportDto.builder().tipo("PERDIDO").descripcion("Perdido test").reporterUserId(1L).build())));

        // Crear reporte ENCONTRADO
        mockMvc.perform(post("/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        ReportDto.builder().tipo("ENCONTRADO").descripcion("Encontrado test").reporterUserId(1L).build())));

        // Filtrar por PERDIDO
        mockMvc.perform(get("/reports/tipo/PERDIDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("PERDIDO"));
    }

    // ─────────────────────────────────────────────────────────────
    // CAMBIO DE ESTADO
    // ─────────────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("Cambiar estado a RESUELTO → el reporte queda RESUELTO")
    void cambiarEstado_aResuelto_exitoso() throws Exception {
        // Crear reporte
        String response = mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ReportDto.builder().tipo("PERDIDO").descripcion("Mascota para resolver").reporterUserId(1L).build())))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // Cambiar estado
        mockMvc.perform(patch("/reports/" + id + "/estado")
                        .param("estado", "RESUELTO")
                        .header("X-User-Id",   "1")
                        .header("X-User-Role", "OWNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTO"));
    }

    @Test
    @Order(8)
    @DisplayName("Obtener reporte por ID inexistente → retorna 404")
    void getReporteById_noExiste_retorna404() throws Exception {
        mockMvc.perform(get("/reports/9999"))
                .andExpect(status().isNotFound());
    }
}