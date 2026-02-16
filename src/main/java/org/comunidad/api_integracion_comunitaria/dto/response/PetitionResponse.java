package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) que representa la información pública de una
 * petición.
 * Se utiliza para listar los trabajos disponibles en el feed de los
 * proveedores.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PetitionResponse {

    /** Identificador único de la petición */
    private Integer idPetition;

    /** Descripción detallada del trabajo a realizar */
    private String description;

    /** Nombre de la categoría o tipo de petición (ej: "Urgencia", "Presupuesto") */
    private String typePetitionName;

    /** Nombre de la profesión requerida (ej: "Plomero", "Electricista") */
    private String professionName;

    /** Estado actual de la petición (ej: "PUBLICADA", "ADJUDICADA") */
    private String stateName;

    /** Fecha de publicación */
    private LocalDate dateSince;

    /** Fecha límite para postularse */
    private LocalDate dateUntil;

    /** Nombre completo del cliente que solicita el servicio */
    private String customerName;

    /**
     * Nombre de la ciudad donde se requiere el servicio.
     * <p>
     * Fundamental para el filtrado geográfico en el Frontend.
     * </p>
     */
    private String cityName;

    // --- NUEVO CAMPO AGREGADO ---
    /** * URL de la foto adjunta del problema (proveniente de Cloudinary).
     * Puede ser nulo si el cliente no subió ninguna imagen.
     */
    private String imageUrl;
}