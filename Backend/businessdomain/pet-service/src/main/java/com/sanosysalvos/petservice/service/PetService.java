package com.sanosysalvos.petservice.service;

import com.sanosysalvos.petservice.dto.PetDto;
import com.sanosysalvos.petservice.exception.ResourceNotFoundException;
import com.sanosysalvos.petservice.exception.UnauthorizedException;
import com.sanosysalvos.petservice.model.Pet;
import com.sanosysalvos.petservice.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.sanosysalvos.petservice.exception.UnauthorizedException;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    public List<PetDto> getAllPets() {
        return petRepository.findByActiveTrue().stream().map(this::toDto).collect(Collectors.toList());
    }

    public PetDto getPetById(Long id) {
        return toDto(findOrThrow(id));
    }

    public List<PetDto> getPetsByOwner(Long ownerId) {
        return petRepository.findByOwnerId(ownerId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<PetDto> getPetsByEspecie(String especie) {
        return petRepository.findByEspecieIgnoreCase(especie).stream().map(this::toDto).collect(Collectors.toList());
    }

    public PetDto createPet(PetDto dto) {
        Pet pet = Pet.builder()
                .nombre(dto.getNombre())
                .especie(dto.getEspecie())
                .raza(dto.getRaza())
                .color(dto.getColor())
                .tamanio(parseTamanio(dto.getTamanio()))
                .fotoUrl(dto.getFotoUrl())
                .descripcion(dto.getDescripcion())
                .ownerId(dto.getOwnerId())
                .build();
        return toDto(petRepository.save(pet));
    }

    public PetDto updatePet(Long id, PetDto dto, Long requestingUserId, String requestingUserRole) {
        Pet pet = findOrThrow(id);

        boolean esAdmin = "ADMIN".equals(requestingUserRole);
        boolean esDuenio = pet.getOwnerId().equals(requestingUserId);

        if (!esAdmin && !esDuenio) {
            throw new UnauthorizedException("No tienes permiso para modificar esta mascota");
        }

        if (dto.getNombre()      != null) pet.setNombre(dto.getNombre());
        if (dto.getEspecie()     != null) pet.setEspecie(dto.getEspecie());
        if (dto.getRaza()        != null) pet.setRaza(dto.getRaza());
        if (dto.getColor()       != null) pet.setColor(dto.getColor());
        if (dto.getTamanio()     != null) pet.setTamanio(parseTamanio(dto.getTamanio()));
        if (dto.getFotoUrl() != null && !dto.getFotoUrl().isEmpty()) pet.setFotoUrl(dto.getFotoUrl());
        if (dto.getDescripcion() != null) pet.setDescripcion(dto.getDescripcion());

        return toDto(petRepository.save(pet));
    }

    // deletePet — cambia la firma y la validación
    public void deletePet(Long id, Long requestingUserId, String requestingUserRole) {
        Pet p = findOrThrow(id);

        boolean esAdmin = "ADMIN".equals(requestingUserRole);
        boolean esDuenio = p.getOwnerId().equals(requestingUserId);

        if (!esAdmin && !esDuenio) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta mascota");
        }

        p.setActive(false);
        petRepository.save(p);
    }

    // ── Métodos privados ──────────────────────────────────────────────

    private Pet findOrThrow(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada: " + id));
    }

    private Pet.Tamanio parseTamanio(String t) {
        if (t == null) return null;
        try { return Pet.Tamanio.valueOf(t.toUpperCase()); }
        catch (Exception e) { return null; }
    }

    private PetDto toDto(Pet p) {
        return PetDto.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .especie(p.getEspecie())
                .raza(p.getRaza())
                .color(p.getColor())
                .tamanio(p.getTamanio() != null ? p.getTamanio().name() : null)
                .fotoUrl(p.getFotoUrl())
                .descripcion(p.getDescripcion())
                .ownerId(p.getOwnerId())
                .active(p.getActive())
                .createdAt(p.getCreatedAt())
                .build();
    }
}