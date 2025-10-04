package hris.hris.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmployeeDto Tests")
class EmployeeDtoTest {

    private EmployeeDto employeeDto;

    @BeforeEach
    void setUp() {
        employeeDto = new EmployeeDto();
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get id correctly")
        void shouldSetAndGetId() {
            // Given
            Long expectedId = 123L;

            // When
            employeeDto.setId(expectedId);

            // Then
            assertEquals(expectedId, employeeDto.getId(), "ID should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get employeeCode correctly")
        void shouldSetAndGetEmployeeCode() {
            // Given
            String expectedEmployeeCode = "EMP001";

            // When
            employeeDto.setEmployeeCode(expectedEmployeeCode);

            // Then
            assertEquals(expectedEmployeeCode, employeeDto.getEmployeeCode(), "Employee code should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get firstName correctly")
        void shouldSetAndGetFirstName() {
            // Given
            String expectedFirstName = "John";

            // When
            employeeDto.setFirstName(expectedFirstName);

            // Then
            assertEquals(expectedFirstName, employeeDto.getFirstName(), "First name should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get lastName correctly")
        void shouldSetAndGetLastName() {
            // Given
            String expectedLastName = "Doe";

            // When
            employeeDto.setLastName(expectedLastName);

            // Then
            assertEquals(expectedLastName, employeeDto.getLastName(), "Last name should be set and retrieved correctly");
        }

        @Test
        @DisplayName("Should set and get email correctly")
        void shouldSetAndGetEmail() {
            // Given
            String expectedEmail = "john.doe@example.com";

            // When
            employeeDto.setEmail(expectedEmail);

            // Then
            assertEquals(expectedEmail, employeeDto.getEmail(), "Email should be set and retrieved correctly");
        }
    }

    @Nested
    @DisplayName("Null Value Tests")
    class NullValueTests {

        @Test
        @DisplayName("Should handle null id")
        void shouldHandleNullId() {
            // When
            employeeDto.setId(null);

            // Then
            assertNull(employeeDto.getId(), "ID should be able to be null");
        }

        @Test
        @DisplayName("Should handle null employeeCode")
        void shouldHandleNullEmployeeCode() {
            // When
            employeeDto.setEmployeeCode(null);

            // Then
            assertNull(employeeDto.getEmployeeCode(), "Employee code should be able to be null");
        }

        @Test
        @DisplayName("Should handle null firstName")
        void shouldHandleNullFirstName() {
            // When
            employeeDto.setFirstName(null);

            // Then
            assertNull(employeeDto.getFirstName(), "First name should be able to be null");
        }

        @Test
        @DisplayName("Should handle null lastName")
        void shouldHandleNullLastName() {
            // When
            employeeDto.setLastName(null);

            // Then
            assertNull(employeeDto.getLastName(), "Last name should be able to be null");
        }

        @Test
        @DisplayName("Should handle null email")
        void shouldHandleNullEmail() {
            // When
            employeeDto.setEmail(null);

            // Then
            assertNull(employeeDto.getEmail(), "Email should be able to be null");
        }
    }

    @Nested
    @DisplayName("Empty String Tests")
    class EmptyStringTests {

        @Test
        @DisplayName("Should handle empty employeeCode")
        void shouldHandleEmptyEmployeeCode() {
            // When
            employeeDto.setEmployeeCode("");

            // Then
            assertEquals("", employeeDto.getEmployeeCode(), "Employee code should be able to be empty string");
        }

        @Test
        @DisplayName("Should handle empty firstName")
        void shouldHandleEmptyFirstName() {
            // When
            employeeDto.setFirstName("");

            // Then
            assertEquals("", employeeDto.getFirstName(), "First name should be able to be empty string");
        }

        @Test
        @DisplayName("Should handle empty lastName")
        void shouldHandleEmptyLastName() {
            // When
            employeeDto.setLastName("");

            // Then
            assertEquals("", employeeDto.getLastName(), "Last name should be able to be empty string");
        }

        @Test
        @DisplayName("Should handle empty email")
        void shouldHandleEmptyEmail() {
            // When
            employeeDto.setEmail("");

            // Then
            assertEquals("", employeeDto.getEmail(), "Email should be able to be empty string");
        }
    }

    @Nested
    @DisplayName("Complete Object Tests")
    class CompleteObjectTests {

        @Test
        @DisplayName("Should create complete employee DTO")
        void shouldCreateCompleteEmployeeDto() {
            // Given
            Long id = 123L;
            String employeeCode = "EMP001";
            String firstName = "John";
            String lastName = "Doe";
            String email = "john.doe@example.com";

            // When
            employeeDto.setId(id);
            employeeDto.setEmployeeCode(employeeCode);
            employeeDto.setFirstName(firstName);
            employeeDto.setLastName(lastName);
            employeeDto.setEmail(email);

            // Then
            assertEquals(id, employeeDto.getId(), "ID should match");
            assertEquals(employeeCode, employeeDto.getEmployeeCode(), "Employee code should match");
            assertEquals(firstName, employeeDto.getFirstName(), "First name should match");
            assertEquals(lastName, employeeDto.getLastName(), "Last name should match");
            assertEquals(email, employeeDto.getEmail(), "Email should match");
        }

        @Test
        @DisplayName("Should handle employee with minimal data")
        void shouldHandleEmployeeWithMinimalData() {
            // Given - only setting required fields
            Long id = 1L;
            String firstName = "Jane";

            // When
            employeeDto.setId(id);
            employeeDto.setFirstName(firstName);

            // Then
            assertEquals(id, employeeDto.getId(), "ID should match");
            assertEquals(firstName, employeeDto.getFirstName(), "First name should match");
            assertNull(employeeDto.getEmployeeCode(), "Employee code should be null");
            assertNull(employeeDto.getLastName(), "Last name should be null");
            assertNull(employeeDto.getEmail(), "Email should be null");
        }
    }

    @Nested
    @DisplayName("Lombok Annotation Tests")
    class LombokAnnotationTests {

        @Test
        @DisplayName("Should have working toString method")
        void shouldHaveWorkingToString() {
            // Given
            employeeDto.setId(123L);
            employeeDto.setEmployeeCode("EMP001");
            employeeDto.setFirstName("John");
            employeeDto.setLastName("Doe");
            employeeDto.setEmail("john.doe@example.com");

            // When
            String toString = employeeDto.toString();

            // Then
            assertNotNull(toString, "toString should not be null");
            assertTrue(toString.contains("EmployeeDto"), "toString should contain class name");
            assertTrue(toString.contains("123"), "toString should contain ID");
            assertTrue(toString.contains("EMP001"), "toString should contain employee code");
            assertTrue(toString.contains("John"), "toString should contain first name");
            assertTrue(toString.contains("Doe"), "toString should contain last name");
            assertTrue(toString.contains("john.doe@example.com"), "toString should contain email");
        }

        @Test
        @DisplayName("Should have working equals method")
        void shouldHaveWorkingEquals() {
            // Given
            EmployeeDto employee1 = new EmployeeDto();
            EmployeeDto employee2 = new EmployeeDto();

            employee1.setId(123L);
            employee1.setEmployeeCode("EMP001");
            employee1.setFirstName("John");
            employee1.setLastName("Doe");
            employee1.setEmail("john.doe@example.com");

            employee2.setId(123L);
            employee2.setEmployeeCode("EMP001");
            employee2.setFirstName("John");
            employee2.setLastName("Doe");
            employee2.setEmail("john.doe@example.com");

            // Then
            assertEquals(employee1, employee2, "Employee DTOs with same data should be equal");
            assertEquals(employee1, employee1, "Employee DTO should be equal to itself");
            assertNotEquals(null, employee1, "Employee DTO should not be equal to null");
            assertNotEquals(employee1, new Object(), "Employee DTO should not be equal to different type");
        }

        @Test
        @DisplayName("Should have working hashCode method")
        void shouldHaveWorkingHashCode() {
            // Given
            EmployeeDto employee1 = new EmployeeDto();
            EmployeeDto employee2 = new EmployeeDto();

            employee1.setId(123L);
            employee1.setEmployeeCode("EMP001");
            employee1.setFirstName("John");
            employee1.setLastName("Doe");
            employee1.setEmail("john.doe@example.com");

            employee2.setId(123L);
            employee2.setEmployeeCode("EMP001");
            employee2.setFirstName("John");
            employee2.setLastName("Doe");
            employee2.setEmail("john.doe@example.com");

            // Then
            assertEquals(employee1.hashCode(), employee2.hashCode(), "Equal objects should have same hashCode");
        }

        @Test
        @DisplayName("Different objects should have different hashCodes")
        void differentObjectsShouldHaveDifferentHashCodes() {
            // Given
            EmployeeDto employee1 = new EmployeeDto();
            EmployeeDto employee2 = new EmployeeDto();

            employee1.setId(123L);
            employee1.setFirstName("John");

            employee2.setId(456L);
            employee2.setFirstName("Jane");

            // Then
            assertNotEquals(employee1.hashCode(), employee2.hashCode(), "Different objects should have different hashCodes");
        }
    }
}