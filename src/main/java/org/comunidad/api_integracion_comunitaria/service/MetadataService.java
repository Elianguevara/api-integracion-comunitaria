package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.model.City;
import org.comunidad.api_integracion_comunitaria.model.Profession;
import org.comunidad.api_integracion_comunitaria.repository.CityRepository;
import org.comunidad.api_integracion_comunitaria.repository.ProfessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetadataService {

    private final ProfessionRepository professionRepository;
    private final CityRepository cityRepository;

    @Transactional(readOnly = true)
    public List<Profession> getAllProfessions() {
        return professionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
}