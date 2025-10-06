package hris.hris.service;

import hris.hris.dto.CityDto;
import hris.hris.dto.CityDropdownDto;
import hris.hris.model.City;
import hris.hris.repository.CityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    @Transactional(readOnly = true)
    public List<CityDropdownDto> getAllActiveCitiesForDropdown() {
        log.debug("Fetching all active cities for dropdown");
        List<City> cities = cityRepository.findAllActiveCities();

        return cities.stream()
                .map(city -> CityDropdownDto.builder()
                        .id(city.getId())
                        .cityCode(city.getCityCode())
                        .cityName(city.getCityName())
                        .provinceName(city.getProvinceName())
                        .displayName(city.getCityName() + ", " + city.getProvinceName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CityDropdownDto> searchCities(String query) {
        log.debug("Searching cities with query: {}", query);
        List<City> cities = cityRepository.findActiveCitiesBySearch(query);

        return cities.stream()
                .map(city -> CityDropdownDto.builder()
                        .id(city.getId())
                        .cityCode(city.getCityCode())
                        .cityName(city.getCityName())
                        .provinceName(city.getProvinceName())
                        .displayName(city.getCityName() + ", " + city.getProvinceName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CityDropdownDto> getAllActiveCitiesForDropdown(Pageable pageable) {
        log.debug("Fetching all active cities for dropdown with pagination");
        Page<City> citiesPage = cityRepository.findAllActiveCities(pageable);

        return citiesPage.map(city -> CityDropdownDto.builder()
                        .id(city.getId())
                        .cityCode(city.getCityCode())
                        .cityName(city.getCityName())
                        .provinceName(city.getProvinceName())
                        .displayName(city.getCityName() + ", " + city.getProvinceName())
                        .build());
    }

    @Transactional(readOnly = true)
    public Page<CityDropdownDto> searchCities(String query, Pageable pageable) {
        log.debug("Searching cities with query: {} and pagination", query);
        Page<City> citiesPage = cityRepository.findActiveCitiesBySearch(query, pageable);

        return citiesPage.map(city -> CityDropdownDto.builder()
                        .id(city.getId())
                        .cityCode(city.getCityCode())
                        .cityName(city.getCityName())
                        .provinceName(city.getProvinceName())
                        .displayName(city.getCityName() + ", " + city.getProvinceName())
                        .build());
    }

    @Transactional(readOnly = true)
    public List<CityDropdownDto> getCitiesByProvince(String province) {
        log.debug("Fetching cities for province: {}", province);
        List<City> cities = cityRepository.findActiveCitiesByProvinceContaining(province);

        return cities.stream()
                .map(city -> CityDropdownDto.builder()
                        .id(city.getId())
                        .cityCode(city.getCityCode())
                        .cityName(city.getCityName())
                        .provinceName(city.getProvinceName())
                        .displayName(city.getCityName() + ", " + city.getProvinceName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CityDto> getCityById(Long id) {
        return cityRepository.findById(id)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Optional<CityDto> getCityByCode(String cityCode) {
        return cityRepository.findByCityCodeAndIsActive(cityCode, true)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Optional<CityDto> getCityByName(String cityName) {
        return cityRepository.findByCityNameAndIsActive(cityName, true)
                .map(this::convertToDto);
    }

    @Transactional
    public City createCity(CityDto cityDto) {
        log.info("Creating new city: {}", cityDto.getCityName());

        if (cityRepository.existsByCityCodeAndIsActive(cityDto.getCityCode(), true)) {
            throw new RuntimeException("City with code " + cityDto.getCityCode() + " already exists");
        }

        if (cityRepository.existsByCityNameAndIsActive(cityDto.getCityName(), true)) {
            throw new RuntimeException("City with name " + cityDto.getCityName() + " already exists");
        }

        City city = new City();
        city.setCityCode(cityDto.getCityCode());
        city.setCityName(cityDto.getCityName());
        city.setProvinceName(cityDto.getProvinceName());
        city.setIsActive(true);

        return cityRepository.save(city);
    }

    private CityDto convertToDto(City city) {
        return CityDto.builder()
                .id(city.getId())
                .cityCode(city.getCityCode())
                .cityName(city.getCityName())
                .provinceName(city.getProvinceName())
                .isActive(city.getIsActive())
                .createdAt(city.getCreatedAt())
                .updatedAt(city.getUpdatedAt())
                .build();
    }
}