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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/petitions")
@RequiredArgsConstructor
public class PetitionController {

    private final PetitionService petitionService;

    /**
     * Crea una nueva petición.
     * Requiere autenticación y rol de CLIENTE.
     */
    @PostMapping
    public ResponseEntity<PetitionResponse> createPetition(@Valid @RequestBody PetitionRequest request) {
        return ResponseEntity.ok(petitionService.createPetition(request));
    }

    /**
     * Endpoint paginado para el feed de peticiones públicas.
     * <p>
     * Ejemplo de uso desde React:
     * <code>GET /api/petitions/feed?page=0&size=10&sort=dateSince,desc</code>
     * </p>
     * * @param pageable Configuración automática de paginación.
     * Si no se envían parámetros, usa por defecto: página 0, 10 elementos, orden
     * descendente por fecha.
     * 
     * @return Objeto Page con la lista de peticiones y metadatos (total de páginas,
     *         elementos, etc.).
     */
    @GetMapping("/feed")
    public ResponseEntity<Page<PetitionResponse>> getFeed(
            @PageableDefault(page = 0, size = 10, sort = "dateSince", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(petitionService.getAllPublishedPetitions(pageable));
    }
}