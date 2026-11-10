package com.sanosysalvos.analyticsservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Cuántos reportes de mascotas perdidas hay en una zona dentro de la ventana de tiempo. */
@Data
@NoArgsConstructor
public class AcumuladoZona {

    private int cantidad;
    private List<Long> reportIds = new ArrayList<>();

    public AcumuladoZona agregar(Long reportId) {
        if (reportId != null && !reportIds.contains(reportId)) {
            reportIds.add(reportId);
        }
        cantidad = reportIds.size();
        return this;
    }
}
