package org.comunidad.api_integracion_comunitaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.PetitionRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PetitionResponse;
import org.comunidad.api_integracion_comunitaria.service.PetitionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar las Peticiones de trabajo.
 * Expone endpoints para crear, listar, ver detalle y eliminar solicitudes.
 */
@RestController
@RequestMapping("/api/petitions")
@RequiredArgsConstructor
public class PetitionController {

    private final PetitionService petitionService;

    /**
     * Crea una nueva solicitud de trabajo.
     *
     * @param request Datos del formulario validados.
     * @return 200 OK con la petición creada.
     */
    @PostMapping
    public ResponseEntity<PetitionResponse> createPetition(@Valid @RequestBody PetitionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(petitionService.createPetition(auth.getName(), request));
    }

    /**
     * Obtiene el Feed de trabajos disponibles para Proveedores.
     * Excluye las peticiones creadas por el propio usuario.
     *
     * @param pageable Configuración de paginación (por defecto ordena por fecha descendente).
     * @return Página de solicitudes.
     */
    @GetMapping("/feed")
    public ResponseEntity<Page<PetitionResponse>> getFeed(
            @PageableDefault(page = 0, size = 10, sort = "dateSince", direction = Sort.Direction.DESC) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(petitionService.getFeed(auth.getName(), pageable));
    }

    /**
     * Obtiene el historial de solicitudes creadas por el Cliente autenticado.
     *
     * @param pageable Configuración de paginación.
     * @return Página de mis solicitudes.
     */
    @GetMapping("/my")
    public ResponseEntity<Page<PetitionResponse>> getMyPetitions(
            @PageableDefault(page = 0, size = 10, sort = "dateSince", direction = Sort.Direction.DESC) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(petitionService.getMyPetitions(auth.getName(), pageable));
    }

    /**
     * Obtiene el detalle de una solicitud específica por su ID.
     *
     * @param id ID de la petición.
     * @return 200 OK con el detalle de la petición.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PetitionResponse> getPetitionById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(petitionService.getPetitionById(id, auth.getName()));
    }

    /**
     * Elimina (Cancela) una solicitud específica.
     * Requiere que el usuario autenticado sea el creador de la solicitud.
     *
     * @param id ID de la petición a eliminar.
     * @return 204 No Content si la operación fue exitosa.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePetition(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        petitionService.deletePetition(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}