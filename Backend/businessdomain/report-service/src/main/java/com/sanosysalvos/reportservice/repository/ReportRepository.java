package com.sanosysalvos.reportservice.repository;

import com.sanosysalvos.reportservice.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByTipo(Report.TipoReporte tipo);
    List<Report> findByEstado(Report.EstadoReporte estado);
    List<Report> findByReporterUserId(Long userId);
    List<Report> findByPetId(Long petId);
}
