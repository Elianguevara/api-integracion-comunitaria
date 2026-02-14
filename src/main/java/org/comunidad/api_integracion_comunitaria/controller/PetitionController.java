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
 * Controlador REST para gestionar las Peticiones de trabajo en el sistema.
 * <p>
 * Proporciona endpoints para que los Clientes creen y administren sus solicitudes,
 * y para que los Proveedores visualicen las oportunidades disponibles en el feed.
 * </p>
 * * @author Elian Guevara
 * @version 1.1
 */
@RestController
@RequestMapping("/api/petitions")
@RequiredArgsConstructor
public class PetitionController {

    private final PetitionService petitionService;

    /**
     * Crea una nueva solicitud de trabajo asociada al Cliente autenticado.
     *
     * @param request Objeto DTO que contiene los detalles de la petición (profesión, ciudad, descripción, etc.).
     * @return {@link ResponseEntity} con la {@link PetitionResponse} creada y estado 200 OK.
     */
    @PostMapping
    public ResponseEntity<PetitionResponse> createPetition(@Valid @RequestBody PetitionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(petitionService.createPetition(auth.getName(), request));
    }

    /**
     * Obtiene el feed público de solicitudes de trabajo disponibles para los Proveedores.
     * <p>
     * Filtra automáticamente las peticiones para que el usuario autenticado no vea sus propias solicitudes.
     * </p>
     *
     * @param pageable Configuración de paginación y ordenamiento (por defecto: página 0, tamaño 10, ordenado por fecha descendente).
     * @return {@link ResponseEntity} con una página de {@link PetitionResponse}.
     */
    @GetMapping("/feed")
    public ResponseEntity<Page<PetitionResponse>> getFeed(
            @PageableDefault(page = 0, size = 10, sort = "dateSince", direction = Sort.Direction.DESC) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(petitionService.getFeed(auth.getName(), pageable));
    }

    /**
     * Obtiene el historial de solicitudes creadas exclusivamente por el Cliente autenticado.
     *
     * @param pageable Configuración de paginación.
     * @return {@link ResponseEntity} con la página de solicitudes pertenecientes al cliente.
     */
    @GetMapping("/my")
    public ResponseEntity<Page<PetitionResponse>> getMyPetitions(
            @PageableDefault(page = 0, size = 10, sort = "dateSince", direction = Sort.Direction.DESC) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(petitionService.getMyPetitions(auth.getName(), pageable));
    }

    /**
     * Recupera la información detallada de una solicitud específica por su identificador.
     *
     * @param id Identificador único de la petición.
     * @return {@link ResponseEntity} con los datos de la petición si se encuentra disponible.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PetitionResponse> getPetitionById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(petitionService.getPetitionById(id, auth.getName()));
    }

    /**
     * Elimina o cancela una solicitud de trabajo del sistema.
     * <p>
     * Esta operación solo puede ser realizada por el Cliente que creó originalmente la solicitud.
     * </p>
     *
     * @param id Identificador de la petición a eliminar.
     * @return {@link ResponseEntity} con estado 204 No Content tras la eliminación exitosa.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePetition(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        petitionService.deletePetition(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Finaliza oficialmente una solicitud de trabajo una vez completado el servicio.
     * <p>
     * Cambia el estado de la petición a 'FINALIZADA'. Esta acción es crucial para el flujo
     * de estados y permite habilitar el sistema de calificaciones posterior.
     * </p>
     *
     * @param id Identificador de la petición a finalizar.
     * @return {@link ResponseEntity} con la {@link PetitionResponse} actualizada y estado 200 OK.
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<PetitionResponse> completePetition(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(petitionService.completePetition(id, auth.getName()));
    }

    /**
     * Reactiva una solicitud de trabajo específica.
     *
     * @param id ID de la petición.
     * @return 200 OK con la petición reactivada.
     */
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<PetitionResponse> reactivatePetition(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(petitionService.reactivatePetition(id, auth.getName()));
    }
}