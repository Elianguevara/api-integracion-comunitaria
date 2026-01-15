package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Recomendado para logs reales en lugar de System.err
import org.comunidad.api_integracion_comunitaria.dto.request.PetitionRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PetitionResponse;
import org.comunidad.api_integracion_comunitaria.model.City;
import org.comunidad.api_integracion_comunitaria.model.Customer;
import org.comunidad.api_integracion_comunitaria.model.Petition;
import org.comunidad.api_integracion_comunitaria.model.User;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la gestión del ciclo de vida de las Peticiones
 * (Trabajos solicitados).
 * <p>
 * Maneja la creación, publicación y recuperación de peticiones, así como
 * la orquestación de notificaciones a proveedores relevantes.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j // Agrega soporte para logging (opcional, pero buena práctica)
public class PetitionService {

        private final PetitionRepository petitionRepository;
        private final CustomerRepository customerRepository;
        private final UserRepository userRepository;
        private final ProfessionRepository professionRepository;
        private final TypePetitionRepository typePetitionRepository;
        private final PetitionStateRepository petitionStateRepository;
        private final NotificationService notificationService;
        private final CityRepository cityRepository; // <--- NUEVO: Inyección del repositorio de Ciudades

        /**
         * Crea una nueva petición de servicio en el sistema.
         * <p>
         * Este método realiza las siguientes acciones:
         * 1. Identifica al usuario autenticado y valida su perfil de Cliente.
         * 2. Valida la existencia de entidades relacionadas (Profesión, Ciudad, Tipo).
         * 3. Guarda la petición con estado inicial "PUBLICADA".
         * 4. Dispara notificaciones asíncronas a proveedores que coincidan con la
         * Profesión y la Ciudad.
         * </p>
         *
         * @param request DTO con los datos del formulario de creación.
         * @return PetitionResponse DTO con los datos de la petición creada.
         * @throws RuntimeException si el usuario no existe, no es cliente o faltan
         *                          datos de configuración.
         */
        @Transactional
        public PetitionResponse createPetition(PetitionRequest request) {
                // 1. Obtener usuario autenticado desde el Contexto de Seguridad
                String userEmail = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                                .getUsername();
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException(
                                                "Usuario autenticado no encontrado en base de datos."));

                // 2. Obtener perfil de cliente
                Customer customer = customerRepository.findByUser_IdUser(user.getIdUser())
                                .orElseThrow(() -> new RuntimeException(
                                                "El usuario actual no tiene un perfil de Cliente activo."));

                // 3. Instanciar y poblar la Entidad Petition
                Petition petition = new Petition();
                petition.setCustomer(customer);
                petition.setDescription(request.getDescription());
                petition.setDateSince(LocalDate.now());
                petition.setDateUntil(request.getDateUntil());
                petition.setIsDeleted(false);

                // 4. Asignar relaciones y validar existencia (Fail-fast)
                petition.setProfession(professionRepository.findById(request.getIdProfession())
                                .orElseThrow(() -> new RuntimeException("La profesión seleccionada (ID: "
                                                + request.getIdProfession() + ") no existe.")));

                petition.setTypePetition(typePetitionRepository.findById(request.getIdTypePetition())
                                .orElseThrow(() -> new RuntimeException(
                                                "El tipo de petición seleccionado no existe.")));

                // --- NUEVO: Asignación de Ciudad para geolocalización ---
                City city = cityRepository.findById(request.getIdCity())
                                .orElseThrow(() -> new RuntimeException(
                                                "La ciudad seleccionada (ID: " + request.getIdCity() + ") no existe."));
                petition.setCity(city);
                // --------------------------------------------------------

                // 5. Asignar estado inicial 'PUBLICADA'
                petition.setState(petitionStateRepository.findByName("PUBLICADA")
                                .orElseThrow(() -> new RuntimeException(
                                                "Error crítico: Estado 'PUBLICADA' no configurado en BD.")));

                // 6. Guardar en Base de Datos
                Petition savedPetition = petitionRepository.save(petition);

                // 7. Notificar a los proveedores (Lógica de Negocio Inteligente)
                // Usamos un bloque try-catch para asegurar que una falla en notificaciones
                // no revierta la transacción de la creación de la petición.
                try {
                        notificationService.notifyProvidersByProfessionAndCity(
                                        savedPetition.getProfession().getIdProfession(),
                                        savedPetition.getCity().getIdCity(), // Pasamos la ciudad para el filtro
                                        savedPetition);
                } catch (Exception e) {
                        // En producción, usa log.error()
                        System.err.println("Advertencia: No se pudieron enviar las notificaciones. Error: "
                                        + e.getMessage());
                }

                return mapToResponse(savedPetition);
        }

        /**
         * Recupera todas las peticiones que se encuentran en estado 'PUBLICADA'.
         * Útil para el listado general que ven los proveedores.
         *
         * @return Lista de DTOs de peticiones.
         */
        public List<PetitionResponse> getAllPublishedPetitions() {
                return petitionRepository.findByState_Name("PUBLICADA").stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Convierte una entidad {@link Petition} del modelo de dominio a un DTO
         * {@link PetitionResponse}.
         * <p>
         * Este método se encarga de aplanar la estructura de objetos para que el
         * Frontend
         * reciba cadenas de texto simples (nombres) en lugar de objetos anidados
         * complejos.
         * </p>
         *
         * @param petition La entidad persistente recuperada de la base de datos.
         * @return Un objeto de respuesta optimizado para la vista del
         *         cliente/proveedor.
         */
        private PetitionResponse mapToResponse(Petition petition) {
                // Manejo seguro de nulos: Si por error la ciudad no se guardó, mostramos un
                // texto por defecto
                // para evitar que la aplicación móvil se cierre inesperadamente
                // (NullPointerException).
                String cityName = (petition.getCity() != null)
                                ? petition.getCity().getName()
                                : "Ubicación no especificada";

                return PetitionResponse.builder()
                                .idPetition(petition.getIdPetition())
                                .description(petition.getDescription())
                                // Usamos operadores ternarios para proteger contra nulos en relaciones clave
                                .typePetitionName(petition.getTypePetition() != null
                                                ? petition.getTypePetition().getTypePetitionName()
                                                : "Sin categoría")
                                .professionName(petition.getProfession() != null ? petition.getProfession().getName()
                                                : "Profesión no especificada")
                                .stateName(petition.getState() != null ? petition.getState().getName()
                                                : "ESTADO_DESCONOCIDO")
                                .dateSince(petition.getDateSince())
                                .dateUntil(petition.getDateUntil())
                                .customerName(petition.getCustomer().getUser().getName() + " "
                                                + petition.getCustomer().getUser().getLastname())
                                // --- ASIGNACIÓN DEL CAMPO NUEVO ---
                                .cityName(cityName)
                                .build();
        }
}