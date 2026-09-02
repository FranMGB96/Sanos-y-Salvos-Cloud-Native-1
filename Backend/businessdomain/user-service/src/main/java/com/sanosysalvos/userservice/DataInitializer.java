package com.sanosysalvos.userservice;

import com.sanosysalvos.userservice.model.User;
import com.sanosysalvos.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@sanosysalvos.cl";

        User admin = userRepository.findByEmail(adminEmail).orElse(
                User.builder()
                        .email(adminEmail)
                        .active(true)
                        .build()
        );

        admin.setNombre("Administrador");
        admin.setPassword(passwordEncoder.encode("admin1"));
        admin.setRol(User.Role.ADMIN);
        admin.setTelefono("000000000");  // ✅ campo requerido

        userRepository.save(admin);
        System.out.println("✅ Admin listo: " + adminEmail);
    }
}
