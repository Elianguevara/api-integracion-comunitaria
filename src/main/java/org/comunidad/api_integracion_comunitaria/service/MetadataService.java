package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.model.City;
import org.comunidad.api_integracion_comunitaria.model.Profession;
import org.comunidad.api_integracion_comunitaria.model.TypePetition;
import org.comunidad.api_integracion_comunitaria.repository.CityRepository;
import org.comunidad.api_integracion_comunitaria.repository.ProfessionRepository;
import org.comunidad.api_integracion_comunitaria.repository.TypePetitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Gestión de Metadatos (Datos Maestros).
 * <p>
 * Este servicio es responsable de proveer toda la información "estática" o de catálogo
 * necesaria para alimentar los selectores (dropdowns) del Frontend.
 * <p>
 * Incluye:
 * <ul>
 * <li>Listado de Profesiones (Plomero, Gasista, etc.)</li>
 * <li>Listado de Ciudades disponibles.</li>
 * <li>Tipos de Petición (Urgencia, Presupuesto, etc.)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class MetadataService {

    private final ProfessionRepository professionRepository;
    private final CityRepository cityRepository;
    // Inyectamos el repositorio que faltaba para los tipos de urgencia
    private final TypePetitionRepository typePetitionRepository;

    /**
     * Obtiene el catálogo completo de profesiones registradas.
     * <p>
     * Se utiliza en:
     * <ul>
     * <li>Formulario de Registro de Proveedores (qué oficio realizan).</li>
     * <li>Formulario de Creación de Solicitud (qué profesional necesito).</li>
     * </ul>
     *
     * @return Lista de objetos {@link Profession}.
     */
    @Transactional(readOnly = true)
    public List<Profession> getAllProfessions() {
        return professionRepository.findAll();
    }

    /**
     * Obtiene el listado de ciudades donde opera la plataforma.
     * <p>
     * Fundamental para filtrar proveedores por zona y asignar ubicación a las solicitudes.
     *
     * @return Lista de objetos {@link City}.
     */
    @Transactional(readOnly = true)
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    /**
     * Obtiene los tipos o categorías de urgencia para una solicitud.
     * <p>
     * Ejemplo: "Urgencia Inmediata", "Solicitud de Presupuesto", "Consulta Técnica".
     * Este dato ayuda a los proveedores a priorizar sus respuestas.
     *
     * @return Lista de objetos {@link TypePetition}.
     */
    @Transactional(readOnly = true)
    public List<TypePetition> getAllTypePetitions() {
        return typePetitionRepository.findAll();
    }
}