package org.comunidad.api_integracion_comunitaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.PostulationRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PostulationResponse;
import org.comunidad.api_integracion_comunitaria.service.PostulationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar el ciclo de vida de las Postulaciones.
 * Permite a los proveedores postularse y a los clientes aceptar propuestas.
 */
@RestController
@RequestMapping("/api/postulations")
@RequiredArgsConstructor
public class PostulationController {

    private final PostulationService postulationService;

    /**
     * Crea una nueva postulación para una petición existente.
     * Solo accesible para usuarios con rol PROVEEDOR.
     *
     * @param request DTO con los datos de la postulación (presupuesto, descripción).
     * @return La postulación creada con estado PENDIENTE.
     */
    @PostMapping
    public ResponseEntity<PostulationResponse> create(@Valid @RequestBody PostulationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(postulationService.createPostulation(auth.getName(), request));
    }

    /**
     * Lista los candidatos (postulaciones) de una petición específica.
     * Solo accesible para el CLIENTE dueño de la petición.
     *
     * @param idPetition ID de la petición a consultar.
     * @return Lista de postulaciones.
     */
    @GetMapping("/petition/{idPetition}")
    public ResponseEntity<List<PostulationResponse>> getByPetition(@PathVariable Integer idPetition) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(postulationService.getPostulationsByPetition(idPetition, auth.getName()));
    }

    /**
     * Lista el historial de postulaciones del proveedor autenticado.
     * Permite ver el estado de sus ofertas (PENDIENTE, ACEPTADA, RECHAZADA).
     *
     * @param pageable Configuración de paginación.
     * @return Página de mis postulaciones.
     */
    @GetMapping("/my")
    public ResponseEntity<Page<PostulationResponse>> getMyPostulations(
            @PageableDefault(page = 0, size = 10, sort = "datePostulation", direction = Sort.Direction.DESC) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(postulationService.getMyPostulations(auth.getName(), pageable));
    }

    /**
     * Acepta (Adjudica) una postulación ganadora.
     * Esta acción cierra la petición y rechaza automáticamente al resto de candidatos.
     * Solo accesible para el CLIENTE dueño.
     *
     * @param idPostulation ID de la postulación a aceptar.
     * @return Mensaje de éxito.
     */
    @PutMapping("/{idPostulation}/accept")
    public ResponseEntity<String> acceptPostulation(@PathVariable Integer idPostulation) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        postulationService.acceptPostulation(idPostulation, auth.getName());
        return ResponseEntity.ok("Postulación aceptada y petición adjudicada correctamente.");
    }
    @GetMapping("/check/{idPetition}")
    public ResponseEntity<Boolean> checkIfApplied(@PathVariable Integer idPetition) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(postulationService.checkIfApplied(idPetition, auth.getName()));
    }
}