package com.sanosysalvos.petservice.repository;

import com.sanosysalvos.petservice.model.PetEventHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetEventHistoryRepository extends JpaRepository<PetEventHistory, Long> {

    boolean existsByEventId(String eventId);

    List<PetEventHistory> findByPetIdOrderByIdDesc(Long petId);
}
