package hris.hris.repository;

import hris.hris.model.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    @Query("SELECT c FROM City c WHERE c.isActive = true ORDER BY c.cityName ASC")
    List<City> findAllActiveCities();

    @Query("SELECT c FROM City c WHERE c.isActive = true AND c.cityName ILIKE %:search% ORDER BY c.cityName ASC")
    List<City> findActiveCitiesByNameContaining(@Param("search") String search);

    @Query("SELECT c FROM City c WHERE c.isActive = true AND c.provinceName ILIKE %:province% ORDER BY c.cityName ASC")
    List<City> findActiveCitiesByProvinceContaining(@Param("province") String province);

    @Query("SELECT c FROM City c WHERE c.isActive = true AND (c.cityName ILIKE %:search% OR c.provinceName ILIKE %:search%) ORDER BY c.cityName ASC")
    List<City> findActiveCitiesBySearch(@Param("search") String search);

    @Query("SELECT c FROM City c WHERE c.isActive = true ORDER BY c.cityName ASC")
    Page<City> findAllActiveCities(Pageable pageable);

    @Query("SELECT c FROM City c WHERE c.isActive = true AND (c.cityName ILIKE %:search% OR c.provinceName ILIKE %:search%) ORDER BY c.cityName ASC")
    Page<City> findActiveCitiesBySearch(@Param("search") String search, Pageable pageable);

    Optional<City> findByCityCodeAndIsActive(String cityCode, Boolean isActive);

    Optional<City> findByCityNameAndIsActive(String cityName, Boolean isActive);

    boolean existsByCityCodeAndIsActive(String cityCode, Boolean isActive);

    boolean existsByCityNameAndIsActive(String cityName, Boolean isActive);
}