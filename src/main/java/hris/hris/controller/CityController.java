package hris.hris.controller;

import hris.hris.dto.ApiResponse;
import hris.hris.dto.CityDto;
import hris.hris.dto.CityDropdownDto;
import hris.hris.service.CityService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cities")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class CityController {

    @Autowired
    private CityService cityService;

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<CityDropdownDto>>> getCitiesForDropdown(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String province) {
        try {
            List<CityDropdownDto> cities;

            if (search != null && !search.trim().isEmpty()) {
                cities = cityService.searchCities(search.trim());
                log.info("Found {} cities matching search query: {}", cities.size(), search);
            } else if (province != null && !province.trim().isEmpty()) {
                cities = cityService.getCitiesByProvince(province.trim());
                log.info("Found {} cities in province: {}", cities.size(), province);
            } else {
                cities = cityService.getAllActiveCitiesForDropdown();
                log.info("Retrieved {} active cities for dropdown", cities.size());
            }

            return ResponseEntity.ok(ApiResponse.success(cities, "Cities retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving cities for dropdown", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve cities: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CityDropdownDto>>> getAllCities() {
        try {
            List<CityDropdownDto> cities = cityService.getAllActiveCitiesForDropdown();
            log.info("Retrieved {} active cities", cities.size());

            return ResponseEntity.ok(ApiResponse.success(cities, "Cities retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving cities", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve cities: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CityDropdownDto>>> searchCities(
            @RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Search query cannot be empty"));
            }

            List<CityDropdownDto> cities = cityService.searchCities(query.trim());
            log.info("Found {} cities matching query: {}", cities.size(), query);

            return ResponseEntity.ok(ApiResponse.success(cities,
                    String.format("Found %d cities matching '%s'", cities.size(), query)));

        } catch (Exception e) {
            log.error("Error searching cities with query: {}", query, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to search cities: " + e.getMessage()));
        }
    }

    @GetMapping("/province/{province}")
    public ResponseEntity<ApiResponse<List<CityDropdownDto>>> getCitiesByProvince(
            @PathVariable String province) {
        try {
            if (province == null || province.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Province name cannot be empty"));
            }

            List<CityDropdownDto> cities = cityService.getCitiesByProvince(province.trim());
            log.info("Found {} cities in province: {}", cities.size(), province);

            return ResponseEntity.ok(ApiResponse.success(cities,
                    String.format("Found %d cities in %s", cities.size(), province)));

        } catch (Exception e) {
            log.error("Error retrieving cities for province: {}", province, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve cities for province: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CityDto>> getCityById(@PathVariable Long id) {
        try {
            Optional<CityDto> city = cityService.getCityById(id);

            if (city.isPresent()) {
                log.info("Retrieved city with ID: {}", id);
                return ResponseEntity.ok(ApiResponse.success(city.get(), "City retrieved successfully"));
            } else {
                log.warn("City with ID {} not found", id);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error retrieving city with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve city: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{cityCode}")
    public ResponseEntity<ApiResponse<CityDto>> getCityByCode(@PathVariable String cityCode) {
        try {
            Optional<CityDto> city = cityService.getCityByCode(cityCode);

            if (city.isPresent()) {
                log.info("Retrieved city with code: {}", cityCode);
                return ResponseEntity.ok(ApiResponse.success(city.get(), "City retrieved successfully"));
            } else {
                log.warn("City with code {} not found", cityCode);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error retrieving city with code: {}", cityCode, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve city: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CityDto>> createCity(@Valid @RequestBody CityDto cityDto) {
        try {
            log.info("Creating new city: {}", cityDto.getCityName());

            var createdCity = cityService.createCity(cityDto);
            log.info("Successfully created city: {} with ID: {}", createdCity.getCityName(), createdCity.getId());

            return ResponseEntity.ok(ApiResponse.success(
                    convertToDto(createdCity), "City created successfully"));

        } catch (Exception e) {
            log.error("Error creating city: {}", cityDto.getCityName(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create city: " + e.getMessage()));
        }
    }

    private CityDto convertToDto(hris.hris.model.City city) {
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