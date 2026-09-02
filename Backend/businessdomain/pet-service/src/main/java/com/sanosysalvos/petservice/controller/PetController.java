package com.sanosysalvos.petservice.controller;

import com.sanosysalvos.petservice.dto.PetDto;
import com.sanosysalvos.petservice.service.PetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/pets")
@Tag(name = "Mascotas")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping
    public ResponseEntity<List<PetDto>> getAll() {
        return ResponseEntity.ok(petService.getAllPets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.getPetById(id));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<PetDto>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(petService.getPetsByOwner(ownerId));
    }

    @GetMapping("/especie/{especie}")
    public ResponseEntity<List<PetDto>> getByEspecie(@PathVariable String especie) {
        return ResponseEntity.ok(petService.getPetsByEspecie(especie));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetDto> create(
            @RequestParam("nombre") String nombre,
            @RequestParam("especie") String especie,
            @RequestParam(value = "raza", required = false) String raza,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "tamanio", required = false) String tamanio,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("ownerId") Long ownerId,
            @RequestParam(value = "foto", required = false) MultipartFile foto
    ) throws IOException {

        String fotoUrl = procesarFoto(foto);

        PetDto dto = new PetDto();
        dto.setNombre(nombre);
        dto.setEspecie(especie);
        dto.setRaza(raza);
        dto.setColor(color);
        dto.setTamanio(tamanio);
        dto.setDescripcion(descripcion);
        dto.setOwnerId(ownerId);
        dto.setFotoUrl(fotoUrl);

        return ResponseEntity.status(HttpStatus.CREATED).body(petService.createPet(dto));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetDto> update(
            @PathVariable Long id,
            @RequestParam("nombre") String nombre,
            @RequestParam("especie") String especie,
            @RequestParam(value = "raza", required = false) String raza,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "tamanio", required = false) String tamanio,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") String requestingUserId,
            // rol con default
            @RequestHeader(value = "X-User-Role", defaultValue = "ADMIN") String requestingUserRole
    ) throws IOException {

        String fotoUrl = procesarFoto(foto);

        PetDto dto = new PetDto();
        dto.setNombre(nombre);
        dto.setEspecie(especie);
        dto.setRaza(raza);
        dto.setColor(color);
        dto.setTamanio(tamanio);
        dto.setDescripcion(descripcion);
        dto.setFotoUrl(fotoUrl);

        return ResponseEntity.ok(
                petService.updatePet(id, dto, Long.parseLong(requestingUserId), requestingUserRole)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") String requestingUserId,
            // rol con default
            @RequestHeader(value = "X-User-Role", defaultValue = "ADMIN") String requestingUserRole
    ) {
        petService.deletePet(id, Long.parseLong(requestingUserId), requestingUserRole);
        return ResponseEntity.noContent().build();
    }

    // ── Método privado para procesar foto ────────────────────────────

    private String procesarFoto(MultipartFile foto) throws IOException {
        if (foto == null || foto.isEmpty()) return null;

        String carpetaUploads = System.getProperty("user.dir") + "/uploads/";
        File carpeta = new File(carpetaUploads);
        if (!carpeta.exists()) carpeta.mkdirs();

        String nombreArchivo = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
        Path rutaArchivo = Paths.get(carpetaUploads + nombreArchivo);
        Files.write(rutaArchivo, foto.getBytes());

        String baseUrl = System.getenv("PET_SERVICE_BASE_URL") != null
                ? System.getenv("PET_SERVICE_BASE_URL")
                : "http://localhost:8082";

        return baseUrl + "/uploads/" + nombreArchivo;
    }
}