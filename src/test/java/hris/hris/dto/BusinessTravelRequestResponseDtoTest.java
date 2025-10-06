package hris.hris.dto;

import hris.hris.model.BusinessTravelRequest;
import hris.hris.model.Employee;
import hris.hris.model.City;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusinessTravelRequestResponseDto Tests")
class BusinessTravelRequestResponseDtoTest {

    private BusinessTravelRequestResponseDto responseDto;
    private BusinessTravelRequest mockRequest;
    private Employee mockEmployee;
    private Employee mockCreatedBy;
    private Employee mockUpdatedBy;
    private City mockCity;

    @BeforeEach
    void setUp() {
        responseDto = new BusinessTravelRequestResponseDto();

        // Setup mock employee
        mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setEmployeeId("EMP001");
        mockEmployee.setFirstName("John");
        mockEmployee.setLastName("Doe");
        mockEmployee.setEmail("john.doe@example.com");

        // Setup mock created by employee
        mockCreatedBy = new Employee();
        mockCreatedBy.setId(2L);
        mockCreatedBy.setEmployeeId("EMP002");
        mockCreatedBy.setFirstName("Jane");
        mockCreatedBy.setLastName("Smith");
        mockCreatedBy.setEmail("jane.smith@example.com");

        // Setup mock updated by employee
        mockUpdatedBy = new Employee();
        mockUpdatedBy.setId(3L);
        mockUpdatedBy.setEmployeeId("EMP003");
        mockUpdatedBy.setFirstName("Bob");
        mockUpdatedBy.setLastName("Wilson");
        mockUpdatedBy.setEmail("bob.wilson@example.com");

        // Setup mock city
        mockCity = new City();
        mockCity.setId(1L);
        mockCity.setCityCode("JKT");
        mockCity.setCityName("Jakarta");
        mockCity.setProvinceName("DKI Jakarta");
        mockCity.setIsActive(true);

        // Setup mock business travel request
        mockRequest = new BusinessTravelRequest();
        mockRequest.setUuid(UUID.randomUUID());
        mockRequest.setEmployeeId(1L);
        mockRequest.setEmployee(mockEmployee);
        mockRequest.setCity(mockCity);
        mockRequest.setCityId(1L);
        mockRequest.setStartDate(LocalDate.of(2024, 1, 15));
        mockRequest.setEndDate(LocalDate.of(2024, 1, 17));
        mockRequest.setTotalDays(3);
        mockRequest.setReason("Client meeting");
        mockRequest.setStatus(BusinessTravelRequest.RequestStatus.APPROVED);
        mockRequest.setCreatedAt(LocalDateTime.of(2024, 1, 10, 10, 0));
        mockRequest.setUpdatedAt(LocalDateTime.of(2024, 1, 12, 14, 30));
        mockRequest.setCreatedBy(mockCreatedBy);
        mockRequest.setUpdatedBy(mockUpdatedBy);
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get uuid correctly")
        void shouldSetAndGetUuid() {
            UUID expectedUuid = UUID.randomUUID();

            responseDto.setUuid(expectedUuid);

            assertEquals(expectedUuid, responseDto.getUuid(), "UUID should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get employeeId correctly")
        void shouldSetAndGetEmployeeId() {
            Long expectedEmployeeId = 123L;

            responseDto.setEmployeeId(expectedEmployeeId);

            assertEquals(expectedEmployeeId, responseDto.getEmployeeId(), "Employee ID should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get employeeName correctly")
        void shouldSetAndGetEmployeeName() {
            String expectedEmployeeName = "John Doe";

            responseDto.setEmployeeName(expectedEmployeeName);

            assertEquals(expectedEmployeeName, responseDto.getEmployeeName(), "Employee name should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get city correctly")
        void shouldSetAndGetCity() {
            CityResponseDto expectedCity = new CityResponseDto();
            expectedCity.setId(2L);
            expectedCity.setCityName("Surabaya");
            expectedCity.setProvinceName("Jawa Timur");
            expectedCity.setCityDisplayName("Surabaya, Jawa Timur");

            responseDto.setCity(expectedCity);

            assertEquals(expectedCity, responseDto.getCity(), "City should be set and retrieved correctly");
            assertEquals(2L, responseDto.getCity().getId(), "City ID should match");
            assertEquals("Surabaya", responseDto.getCity().getCityName(), "City name should match");
            assertEquals("Jawa Timur", responseDto.getCity().getProvinceName(), "Province name should match");
            assertEquals("Surabaya, Jawa Timur", responseDto.getCity().getCityDisplayName(), "City display name should match");
        }

        @Test
        @DisplayName("Should set and get startDate correctly")
        void shouldSetAndGetStartDate() {
            String expectedStartDate = "2024-01-15";

            responseDto.setStartDate(expectedStartDate);

            assertEquals(expectedStartDate, responseDto.getStartDate(), "Start date should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get endDate correctly")
        void shouldSetAndGetEndDate() {
            String expectedEndDate = "2024-01-17";

            responseDto.setEndDate(expectedEndDate);

            assertEquals(expectedEndDate, responseDto.getEndDate(), "End date should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get totalDays correctly")
        void shouldSetAndGetTotalDays() {
            Integer expectedTotalDays = 5;

            responseDto.setTotalDays(expectedTotalDays);

            assertEquals(expectedTotalDays, responseDto.getTotalDays(), "Total days should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get reason correctly")
        void shouldSetAndGetReason() {
            String expectedReason = "Business conference";

            responseDto.setReason(expectedReason);

            assertEquals(expectedReason, responseDto.getReason(), "Reason should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get status correctly")
        void shouldSetAndGetStatus() {
            String expectedStatus = "APPROVED";

            responseDto.setStatus(expectedStatus);

            assertEquals(expectedStatus, responseDto.getStatus(), "Status should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get createdAt correctly")
        void shouldSetAndGetCreatedAt() {
            LocalDateTime expectedCreatedAt = LocalDateTime.of(2024, 1, 10, 10, 0);

            responseDto.setCreatedAt(expectedCreatedAt);

            assertEquals(expectedCreatedAt, responseDto.getCreatedAt(), "Created at should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get updatedAt correctly")
        void shouldSetAndGetUpdatedAt() {
            LocalDateTime expectedUpdatedAt = LocalDateTime.of(2024, 1, 12, 14, 30);

            responseDto.setUpdatedAt(expectedUpdatedAt);

            assertEquals(expectedUpdatedAt, responseDto.getUpdatedAt(), "Updated at should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get createdById correctly")
        void shouldSetAndGetCreatedById() {
            EmployeeDto expectedCreatedById = new EmployeeDto();
            expectedCreatedById.setId(2L);
            expectedCreatedById.setEmployeeCode("EMP002");
            expectedCreatedById.setFirstName("Jane");
            expectedCreatedById.setLastName("Smith");
            expectedCreatedById.setEmail("jane.smith@example.com");

            responseDto.setCreatedById(expectedCreatedById);

            assertEquals(expectedCreatedById, responseDto.getCreatedById(), "Created by ID should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get updatedById correctly")
        void shouldSetAndGetUpdatedById() {
            EmployeeDto expectedUpdatedById = new EmployeeDto();
            expectedUpdatedById.setId(3L);
            expectedUpdatedById.setEmployeeCode("EMP003");
            expectedUpdatedById.setFirstName("Bob");
            expectedUpdatedById.setLastName("Wilson");
            expectedUpdatedById.setEmail("bob.wilson@example.com");

            responseDto.setUpdatedById(expectedUpdatedById);

            assertEquals(expectedUpdatedById, responseDto.getUpdatedById(), "Updated by ID should be set and retrieved correctly");
        }
    }

    @Nested
    @DisplayName("fromBusinessTravelRequest Method Tests")
    class FromBusinessTravelRequestTests {

        @Test
        @DisplayName("Should convert complete BusinessTravelRequest to DTO correctly")
        void shouldConvertCompleteBusinessTravelRequestToDto() {
            // When
            BusinessTravelRequestResponseDto result = BusinessTravelRequestResponseDto.fromBusinessTravelRequest(mockRequest);

            // Then
            assertNotNull(result, "Result should not be null");
            assertEquals(mockRequest.getUuid(), result.getUuid(), "UUID should match");
            assertEquals(mockRequest.getEmployeeId(), result.getEmployeeId(), "Employee ID should match");
            assertEquals("John Doe", result.getEmployeeName(), "Employee name should be concatenated correctly");
            assertNotNull(result.getCity(), "City should not be null");
            assertEquals(mockRequest.getCity().getId(), result.getCity().getId(), "City ID should match");
            assertEquals(mockRequest.getCity().getCityName(), result.getCity().getCityName(), "City name should match");
            assertEquals(mockRequest.getCity().getProvinceName(), result.getCity().getProvinceName(), "Province name should match");
            assertEquals(mockRequest.getCity().getCityName() + ", " + mockRequest.getCity().getProvinceName(), result.getCity().getCityDisplayName(), "City display name should match");
            assertEquals(mockRequest.getStartDate().toString(), result.getStartDate(), "Start date should match");
            assertEquals(mockRequest.getEndDate().toString(), result.getEndDate(), "End date should match");
            assertEquals(mockRequest.getTotalDays(), result.getTotalDays(), "Total days should match");
            assertEquals(mockRequest.getReason(), result.getReason(), "Reason should match");
            assertEquals(mockRequest.getStatus().toString(), result.getStatus(), "Status should match");
            assertEquals(mockRequest.getCreatedAt(), result.getCreatedAt(), "Created at should match");
            assertEquals(mockRequest.getUpdatedAt(), result.getUpdatedAt(), "Updated at should match");

            // Verify created by employee details
            assertNotNull(result.getCreatedById(), "Created by should not be null");
            assertEquals(mockCreatedBy.getId(), result.getCreatedById().getId(), "Created by ID should match");
            assertEquals(mockCreatedBy.getEmployeeId(), result.getCreatedById().getEmployeeCode(), "Created by employee code should match");
            assertEquals(mockCreatedBy.getFirstName(), result.getCreatedById().getFirstName(), "Created by first name should match");
            assertEquals(mockCreatedBy.getLastName(), result.getCreatedById().getLastName(), "Created by last name should match");
            assertEquals(mockCreatedBy.getEmail(), result.getCreatedById().getEmail(), "Created by email should match");

            // Verify updated by employee details
            assertNotNull(result.getUpdatedById(), "Updated by should not be null");
            assertEquals(mockUpdatedBy.getId(), result.getUpdatedById().getId(), "Updated by ID should match");
            assertEquals(mockUpdatedBy.getEmployeeId(), result.getUpdatedById().getEmployeeCode(), "Updated by employee code should match");
            assertEquals(mockUpdatedBy.getFirstName(), result.getUpdatedById().getFirstName(), "Updated by first name should match");
            assertEquals(mockUpdatedBy.getLastName(), result.getUpdatedById().getLastName(), "Updated by last name should match");
            assertEquals(mockUpdatedBy.getEmail(), result.getUpdatedById().getEmail(), "Updated by email should match");
        }

        @Test
        @DisplayName("Should handle null employee correctly")
        void shouldHandleNullEmployee() {
            // Given
            mockRequest.setEmployee(null);

            // When
            BusinessTravelRequestResponseDto result = BusinessTravelRequestResponseDto.fromBusinessTravelRequest(mockRequest);

            // Then
            assertNull(result.getEmployeeName(), "Employee name should be null when employee is null");
        }

        @Test
        @DisplayName("Should handle null created by correctly")
        void shouldHandleNullCreatedBy() {
            // Given
            mockRequest.setCreatedBy(null);

            // When
            BusinessTravelRequestResponseDto result = BusinessTravelRequestResponseDto.fromBusinessTravelRequest(mockRequest);

            // Then
            assertNull(result.getCreatedById(), "Created by should be null when created by employee is null");
        }

        @Test
        @DisplayName("Should handle null updated by correctly")
        void shouldHandleNullUpdatedBy() {
            // Given
            mockRequest.setUpdatedBy(null);

            // When
            BusinessTravelRequestResponseDto result = BusinessTravelRequestResponseDto.fromBusinessTravelRequest(mockRequest);

            // Then
            assertNull(result.getUpdatedById(), "Updated by should be null when updated by employee is null");
        }

        @Test
        @DisplayName("Should handle all null relationships")
        void shouldHandleAllNullRelationships() {
            // Given
            mockRequest.setEmployee(null);
            mockRequest.setCreatedBy(null);
            mockRequest.setUpdatedBy(null);

            // When
            BusinessTravelRequestResponseDto result = BusinessTravelRequestResponseDto.fromBusinessTravelRequest(mockRequest);

            // Then
            assertNotNull(result, "Result should not be null");
            assertNull(result.getEmployeeName(), "Employee name should be null");
            assertNull(result.getCreatedById(), "Created by should be null");
            assertNull(result.getUpdatedById(), "Updated by should be null");

            // Other fields should still be populated
            assertEquals(mockRequest.getUuid(), result.getUuid(), "UUID should still match");
            assertNotNull(result.getCity(), "City should still not be null");
            assertEquals(mockRequest.getCity().getId(), result.getCity().getId(), "City ID should still match");
            assertEquals(mockRequest.getCity().getCityName(), result.getCity().getCityName(), "City name should still match");
        }
    }

    @Nested
    @DisplayName("Null Value Tests")
    class NullValueTests {

        @Test
        @DisplayName("Should handle null uuid")
        void shouldHandleNullUuid() {
            responseDto.setUuid(null);
            assertNull(responseDto.getUuid(), "UUID should be able to be null");
        }

        @Test
        @DisplayName("Should handle null employeeId")
        void shouldHandleNullEmployeeId() {
            responseDto.setEmployeeId(null);
            assertNull(responseDto.getEmployeeId(), "Employee ID should be able to be null");
        }

        @Test
        @DisplayName("Should handle null employeeName")
        void shouldHandleNullEmployeeName() {
            responseDto.setEmployeeName(null);
            assertNull(responseDto.getEmployeeName(), "Employee name should be able to be null");
        }

        @Test
        @DisplayName("Should handle null city")
        void shouldHandleNullCity() {
            responseDto.setCity(null);
            assertNull(responseDto.getCity(), "City should be able to be null");
        }

        @Test
        @DisplayName("Should handle null startDate")
        void shouldHandleNullStartDate() {
            responseDto.setStartDate(null);
            assertNull(responseDto.getStartDate(), "Start date should be able to be null");
        }

        @Test
        @DisplayName("Should handle null endDate")
        void shouldHandleNullEndDate() {
            responseDto.setEndDate(null);
            assertNull(responseDto.getEndDate(), "End date should be able to be null");
        }

        @Test
        @DisplayName("Should handle null totalDays")
        void shouldHandleNullTotalDays() {
            responseDto.setTotalDays(null);
            assertNull(responseDto.getTotalDays(), "Total days should be able to be null");
        }

        @Test
        @DisplayName("Should handle null reason")
        void shouldHandleNullReason() {
            responseDto.setReason(null);
            assertNull(responseDto.getReason(), "Reason should be able to be null");
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            responseDto.setStatus(null);
            assertNull(responseDto.getStatus(), "Status should be able to be null");
        }

        @Test
        @DisplayName("Should handle null createdAt")
        void shouldHandleNullCreatedAt() {
            responseDto.setCreatedAt(null);
            assertNull(responseDto.getCreatedAt(), "Created at should be able to be null");
        }

        @Test
        @DisplayName("Should handle null updatedAt")
        void shouldHandleNullUpdatedAt() {
            responseDto.setUpdatedAt(null);
            assertNull(responseDto.getUpdatedAt(), "Updated at should be able to be null");
        }

        @Test
        @DisplayName("Should handle null createdById")
        void shouldHandleNullCreatedById() {
            responseDto.setCreatedById(null);
            assertNull(responseDto.getCreatedById(), "Created by ID should be able to be null");
        }

        @Test
        @DisplayName("Should handle null updatedById")
        void shouldHandleNullUpdatedById() {
            responseDto.setUpdatedById(null);
            assertNull(responseDto.getUpdatedById(), "Updated by ID should be able to be null");
        }
    }

    @Nested
    @DisplayName("Lombok Annotation Tests")
    class LombokAnnotationTests {

        @Test
        @DisplayName("Should have working toString method")
        void shouldHaveWorkingToString() {
            // Given
            CityResponseDto city = new CityResponseDto();
            city.setId(1L);
            city.setCityName("Jakarta");
            city.setProvinceName("DKI Jakarta");
            city.setCityDisplayName("Jakarta, DKI Jakarta");

            responseDto.setUuid(UUID.randomUUID());
            responseDto.setEmployeeId(123L);
            responseDto.setEmployeeName("John Doe");
            responseDto.setCity(city);
            responseDto.setStartDate("2024-01-15");
            responseDto.setEndDate("2024-01-17");
            responseDto.setTotalDays(3);
            responseDto.setReason("Business meeting");
            responseDto.setStatus("APPROVED");
            responseDto.setCreatedAt(LocalDateTime.now());
            responseDto.setUpdatedAt(LocalDateTime.now());

            EmployeeDto createdBy = new EmployeeDto();
            createdBy.setId(2L);
            createdBy.setFirstName("Jane");
            responseDto.setCreatedById(createdBy);

            // When
            String toString = responseDto.toString();

            // Then
            assertNotNull(toString, "toString should not be null");
            assertTrue(toString.contains("BusinessTravelRequestResponseDto"), "toString should contain class name");
            assertTrue(toString.contains("John Doe"), "toString should contain employee name");
            assertTrue(toString.contains("Jakarta"), "toString should contain city");
            assertTrue(toString.contains("APPROVED"), "toString should contain status");
        }

        @Test
        @DisplayName("Should have working equals method")
        void shouldHaveWorkingEquals() {
            // Given
            BusinessTravelRequestResponseDto dto1 = new BusinessTravelRequestResponseDto();
            BusinessTravelRequestResponseDto dto2 = new BusinessTravelRequestResponseDto();
            UUID testUuid = UUID.randomUUID();

            CityResponseDto city1 = new CityResponseDto();
            city1.setId(1L);
            city1.setCityName("Jakarta");
            city1.setProvinceName("DKI Jakarta");
            city1.setCityDisplayName("Jakarta, DKI Jakarta");

            CityResponseDto city2 = new CityResponseDto();
            city2.setId(1L);
            city2.setCityName("Jakarta");
            city2.setProvinceName("DKI Jakarta");
            city2.setCityDisplayName("Jakarta, DKI Jakarta");

            dto1.setUuid(testUuid);
            dto1.setEmployeeId(123L);
            dto1.setEmployeeName("John Doe");
            dto1.setCity(city1);

            dto2.setUuid(testUuid);
            dto2.setEmployeeId(123L);
            dto2.setEmployeeName("John Doe");
            dto2.setCity(city2);

            // Then
            assertEquals(dto1, dto2, "DTOs with same data should be equal");
            assertEquals(dto1, dto1, "DTO should be equal to itself");
            assertNotEquals(null, dto1, "DTO should not be equal to null");
            assertNotEquals(dto1, new Object(), "DTO should not be equal to different type");
        }

        @Test
        @DisplayName("Should have working hashCode method")
        void shouldHaveWorkingHashCode() {
            // Given
            BusinessTravelRequestResponseDto dto1 = new BusinessTravelRequestResponseDto();
            BusinessTravelRequestResponseDto dto2 = new BusinessTravelRequestResponseDto();
            UUID testUuid = UUID.randomUUID();

            dto1.setUuid(testUuid);
            dto1.setEmployeeId(123L);

            dto2.setUuid(testUuid);
            dto2.setEmployeeId(123L);

            // Then
            assertEquals(dto1.hashCode(), dto2.hashCode(), "Equal objects should have same hashCode");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            CityResponseDto city = new CityResponseDto();
            city.setCityName("");
            city.setProvinceName("");
            city.setCityDisplayName("");

            responseDto.setEmployeeName("");
            responseDto.setCity(city);
            responseDto.setStartDate("");
            responseDto.setEndDate("");
            responseDto.setReason("");
            responseDto.setStatus("");

            assertEquals("", responseDto.getEmployeeName(), "Employee name should handle empty string");
            assertEquals("", responseDto.getCity().getCityName(), "City name should handle empty string");
            assertEquals("", responseDto.getCity().getProvinceName(), "Province name should handle empty string");
            assertEquals("", responseDto.getCity().getCityDisplayName(), "City display name should handle empty string");
            assertEquals("", responseDto.getStartDate(), "Start date should handle empty string");
            assertEquals("", responseDto.getEndDate(), "End date should handle empty string");
            assertEquals("", responseDto.getReason(), "Reason should handle empty string");
            assertEquals("", responseDto.getStatus(), "Status should handle empty string");
        }

        @Test
        @DisplayName("Should handle zero values")
        void shouldHandleZeroValues() {
            responseDto.setEmployeeId(0L);
            responseDto.setTotalDays(0);

            assertEquals(0L, responseDto.getEmployeeId(), "Employee ID should handle zero");
            assertEquals(0, responseDto.getTotalDays(), "Total days should handle zero");
        }

        @Test
        @DisplayName("Should handle negative values")
        void shouldHandleNegativeValues() {
            responseDto.setEmployeeId(-1L);
            responseDto.setTotalDays(-1);

            assertEquals(-1L, responseDto.getEmployeeId(), "Employee ID should handle negative value");
            assertEquals(-1, responseDto.getTotalDays(), "Total days should handle negative value");
        }
    }
}