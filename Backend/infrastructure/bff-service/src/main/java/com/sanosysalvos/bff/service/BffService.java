package com.sanosysalvos.bff.service;

import com.sanosysalvos.bff.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BffService {

    @Autowired private RestTemplate restTemplate;

    @Value("${services.user-url}")   private String userUrl;
    @Value("${services.pet-url}")    private String petUrl;
    @Value("${services.report-url}") private String reportUrl;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @CircuitBreaker(name = "bff-dashboard", fallbackMethod = "dashboardFallback")
    public DashboardDto getDashboard() {
        List<Object>               usuarios = fetchList(userUrl + "/users",   Object.class);
        List<MascotaResumenDto>    mascotas = fetchList(petUrl  + "/pets",    MascotaResumenDto.class);
        List<ReporteConDetalleDto> reportes = getReportesConDetalle();

        long activos     = reportes.stream().filter(r -> "ACTIVO".equals(r.getEstado())).count();
        long perdidos    = reportes.stream().filter(r -> "PERDIDO".equals(r.getTipo())).count();
        long encontrados = reportes.stream().filter(r -> "ENCONTRADO".equals(r.getTipo())).count();

        List<ReporteConDetalleDto> ultimos = reportes.stream()
                .sorted((a, b) -> b.getCreatedAt() != null && a.getCreatedAt() != null
                        ? b.getCreatedAt().compareTo(a.getCreatedAt()) : 0)
                .limit(5)
                .collect(Collectors.toList());

        return DashboardDto.builder()
                .totalUsuarios(usuarios.size()).totalMascotas(mascotas.size()).totalReportes(reportes.size())
                .reportesActivos(activos).reportesPerdidos(perdidos).reportesEncontrados(encontrados)
                .ultimosReportes(ultimos).build();
    }

    public DashboardDto dashboardFallback(Exception ex) {
        return DashboardDto.builder()
                .totalUsuarios(0).totalMascotas(0).totalReportes(0)
                .reportesActivos(0).reportesPerdidos(0).reportesEncontrados(0)
                .ultimosReportes(Collections.emptyList())
                .build();
    }

    // ── Usuario con mascotas ──────────────────────────────────────────────────

    @CircuitBreaker(name = "bff-usuario", fallbackMethod = "usuarioFallback")
    public UsuarioConMascotasDto getUsuarioConMascotas(Long userId) {
        var usuario = restTemplate.getForObject(userUrl + "/users/" + userId, java.util.Map.class);
        List<MascotaResumenDto> mascotas = fetchList(petUrl + "/pets/owner/" + userId, MascotaResumenDto.class);
        return UsuarioConMascotasDto.builder()
                .id(userId)
                .nombre(usuario != null ? (String) usuario.get("nombre") : null)
                .email(usuario  != null ? (String) usuario.get("email")  : null)
                .rol(usuario    != null ? (String) usuario.get("rol")    : null)
                .mascotas(mascotas).build();
    }

    public UsuarioConMascotasDto usuarioFallback(Long userId, Exception ex) {
        return UsuarioConMascotasDto.builder()
                .id(userId)
                .nombre("No disponible")
                .email("No disponible")
                .rol("DESCONOCIDO")
                .mascotas(Collections.emptyList())
                .build();
    }

    // ── Reportes con detalle ──────────────────────────────────────────────────

    @CircuitBreaker(name = "bff-reportes", fallbackMethod = "reportesFallback")
    public List<ReporteConDetalleDto> getReportesConDetalle() {
        return enriquecer(fetchList(reportUrl + "/reports", java.util.Map.class));
    }

    @CircuitBreaker(name = "bff-reportes", fallbackMethod = "reportesFallback")
    public List<ReporteConDetalleDto> getReportesPorTipo(String tipo) {
        return enriquecer(fetchList(reportUrl + "/reports/tipo/" + tipo, java.util.Map.class));
    }

    @CircuitBreaker(name = "bff-reportes", fallbackMethod = "reportesFallback")
    public List<ReporteConDetalleDto> getReportesPorUsuario(Long userId) {
        return enriquecer(fetchList(reportUrl + "/reports/usuario/" + userId, java.util.Map.class));
    }

    public List<ReporteConDetalleDto> reportesFallback(Exception ex) {
        return Collections.emptyList();
    }

    public List<ReporteConDetalleDto> reportesFallback(String param, Exception ex) {
        return Collections.emptyList();
    }

    public List<ReporteConDetalleDto> reportesFallback(Long param, Exception ex) {
        return Collections.emptyList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<ReporteConDetalleDto> enriquecer(List<java.util.Map> rawList) {
        return rawList.stream().map(r -> {

            // Mascota
            Long petId = r.get("petId") != null ? Long.valueOf(r.get("petId").toString()) : null;
            MascotaResumenDto mascota = null;
            if (petId != null) {
                try { mascota = restTemplate.getForObject(petUrl + "/pets/" + petId, MascotaResumenDto.class); }
                catch (Exception ignored) {}
            }

            // ✅ Dueño — nombre y teléfono
            Long reporterUserId = r.get("reporterUserId") != null
                    ? Long.valueOf(r.get("reporterUserId").toString()) : null;

            String nombreReporter   = null;
            String telefonoReporter = null;

            if (reporterUserId != null) {
                try {
                    Map<?, ?> usuario = restTemplate.getForObject(
                            userUrl + "/users/" + reporterUserId, Map.class);
                    if (usuario != null) {
                        nombreReporter   = (String) usuario.get("nombre");
                        telefonoReporter = (String) usuario.get("telefono");
                    }
                } catch (Exception ignored) {}
            }

            return ReporteConDetalleDto.builder()
                    .id(Long.valueOf(r.get("id").toString()))
                    .tipo((String) r.get("tipo"))
                    .descripcion((String) r.get("descripcion"))
                    .latitud(r.get("latitud")   != null ? Double.valueOf(r.get("latitud").toString())  : null)
                    .longitud(r.get("longitud")  != null ? Double.valueOf(r.get("longitud").toString()) : null)
                    .ubicacionDescripcion((String) r.get("ubicacionDescripcion"))
                    .estado((String) r.get("estado"))
                    .reporterUserId(reporterUserId)
                    .mascota(mascota)
                    .nombreReporter(nombreReporter)
                    .telefonoReporter(telefonoReporter)
                    .createdAt(r.get("createdAt") != null
                            ? java.time.LocalDateTime.parse(
                            r.get("createdAt").toString())
                            : null)
                    .build();
        }).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> fetchList(String url, Class<T> clazz) {
        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<T>>() {});
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) { return Collections.emptyList(); }
    }
}