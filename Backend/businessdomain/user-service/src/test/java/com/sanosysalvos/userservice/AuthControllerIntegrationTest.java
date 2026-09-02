package com.sanosysalvos.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanosysalvos.userservice.dto.AuthDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String EMAIL_TEST = "integracion@test.cl";
    private static final String PASSWORD   = "password123";

    @Test @Order(1)
    @DisplayName("Registro exitoso → retorna 201 con token y datos del usuario")
    void registro_datosValidos_retornaTokenYDatos() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setNombre("Usuario Test");
        request.setEmail(EMAIL_TEST);
        request.setPassword(PASSWORD);
        request.setTelefono("912345678");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL_TEST))
                .andExpect(jsonPath("$.rol").value("OWNER"));
    }

    @Test @Order(2)
    @DisplayName("Registro con email duplicado → retorna error 400")
    void registro_emailDuplicado_retornaError400() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setNombre("Usuario Duplicado");
        request.setEmail(EMAIL_TEST);
        request.setPassword(PASSWORD);
        request.setTelefono("912345678");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(3)
    @DisplayName("Registro sin nombre → retorna error 400 por validación")
    void registro_sinNombre_retornaError400() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setNombre("");
        request.setEmail("sinNombre@test.cl");
        request.setPassword(PASSWORD);
        request.setTelefono("912345678");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(4)
    @DisplayName("Login exitoso → retorna token JWT válido")
    void login_credencialesCorrectas_retornaToken() throws Exception {
        AuthDto.RegisterRequest registro = new AuthDto.RegisterRequest();
        registro.setNombre("Login Test");
        registro.setEmail("login@test.cl");
        registro.setPassword(PASSWORD);
        registro.setTelefono("912345678");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registro)));

        AuthDto.LoginRequest login = new AuthDto.LoginRequest();
        login.setEmail("login@test.cl");
        login.setPassword(PASSWORD);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.email").value("login@test.cl"));
    }

    @Test @Order(5)
    @DisplayName("Login con contraseña incorrecta → retorna error 401")
    void login_passwordIncorrecta_retornaError401() throws Exception {
        AuthDto.LoginRequest login = new AuthDto.LoginRequest();
        login.setEmail("login@test.cl");
        login.setPassword("passwordMala");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(6)
    @DisplayName("Login con usuario inexistente → retorna error 401")
    void login_usuarioNoExiste_retornaError() throws Exception {
        AuthDto.LoginRequest login = new AuthDto.LoginRequest();
        login.setEmail("noexiste@test.cl");
        login.setPassword(PASSWORD);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}