package com.sanosysalvos.userservice.repository;

import com.sanosysalvos.userservice.model.AlertaRecibida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRecibidaRepository extends JpaRepository<AlertaRecibida, Long> {

    boolean existsByAlertaId(String alertaId);

    List<AlertaRecibida> findTop20ByOrderByIdDesc();
}
