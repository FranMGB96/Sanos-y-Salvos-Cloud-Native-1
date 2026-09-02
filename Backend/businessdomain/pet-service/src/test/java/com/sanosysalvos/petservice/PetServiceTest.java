package com.sanosysalvos.petservice;

import com.sanosysalvos.petservice.dto.PetDto;
import com.sanosysalvos.petservice.exception.ResourceNotFoundException;
import com.sanosysalvos.petservice.model.Pet;
import com.sanosysalvos.petservice.repository.PetRepository;
import com.sanosysalvos.petservice.service.PetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {
    @Mock private PetRepository petRepository;
    @InjectMocks private PetService petService;

    @Test void createPet_datosValidos_retornaPet() {
        PetDto dto = PetDto.builder().nombre("Firulais").especie("perro").ownerId(1L).build();
        Pet saved = Pet.builder().id(1L).nombre("Firulais").especie("perro").ownerId(1L).active(true).build();
        when(petRepository.save(any())).thenReturn(saved);
        PetDto result = petService.createPet(dto);
        assertNotNull(result); assertEquals("Firulais", result.getNombre());
    }

    @Test void getPetById_noExiste_lanzaExcepcion() {
        when(petRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> petService.getPetById(99L));
    }

    @Test void getAllPets_retornaActivos() {
        when(petRepository.findByActiveTrue()).thenReturn(List.of(
            Pet.builder().id(1L).nombre("Rex").especie("perro").ownerId(1L).active(true).build()
        ));
        assertEquals(1, petService.getAllPets().size());
    }

    @Test void deletePet_desactiva() {
        Pet pet = Pet.builder().id(1L).nombre("Rex").especie("perro").ownerId(1L).active(true).build();
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any())).thenReturn(pet);
        petService.deletePet(1L, 1L, "OWNER");
        assertFalse(pet.getActive());
    }
}
