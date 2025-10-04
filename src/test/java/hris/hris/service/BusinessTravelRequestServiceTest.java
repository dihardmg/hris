package hris.hris.service;

import hris.hris.dto.BusinessTravelRequestDto;
import hris.hris.model.BusinessTravelRequest;
import hris.hris.model.Employee;
import hris.hris.model.LeaveRequest;
import hris.hris.repository.BusinessTravelRequestRepository;
import hris.hris.repository.EmployeeRepository;
import hris.hris.repository.LeaveRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessTravelRequestService Tests")
class BusinessTravelRequestServiceTest {

    @Mock
    private BusinessTravelRequestRepository businessTravelRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @InjectMocks
    private BusinessTravelRequestService businessTravelRequestService;

    private Employee testEmployee;
    private Employee testSupervisor;
    private BusinessTravelRequestDto testRequestDto;
    private BusinessTravelRequest testTravelRequest;
    private LeaveRequest testLeaveRequest;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@example.com");
        testEmployee.setSupervisorId(2L);

        // Setup test supervisor
        testSupervisor = new Employee();
        testSupervisor.setId(2L);
        testSupervisor.setEmployeeId("EMP002");
        testSupervisor.setFirstName("Jane");
        testSupervisor.setLastName("Smith");
        testSupervisor.setEmail("jane.smith@example.com");

        // Setup test request DTO
        testRequestDto = new BusinessTravelRequestDto();
        testRequestDto.setCity("Jakarta");
        testRequestDto.setStartDate(LocalDate.of(2024, 1, 15));
        testRequestDto.setEndDate(LocalDate.of(2024, 1, 17));
        testRequestDto.setReason("Client meeting");

        // Setup test travel request
        testTravelRequest = new BusinessTravelRequest();
        testUuid = UUID.randomUUID();
        testTravelRequest.setUuid(testUuid);
        testTravelRequest.setEmployee(testEmployee);
        testTravelRequest.setEmployeeId(1L);
        testTravelRequest.setCity("Jakarta");
        testTravelRequest.setStartDate(LocalDate.of(2024, 1, 15));
        testTravelRequest.setEndDate(LocalDate.of(2024, 1, 17));
        testTravelRequest.setTotalDays(3);
        testTravelRequest.setReason("Client meeting");
        testTravelRequest.setStatus(BusinessTravelRequest.RequestStatus.PENDING);
        testTravelRequest.setCreatedBy(testEmployee);

        // Setup test leave request
        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setStartDate(LocalDate.of(2024, 1, 16));
        testLeaveRequest.setEndDate(LocalDate.of(2024, 1, 16));
        testLeaveRequest.setStatus(LeaveRequest.RequestStatus.APPROVED);
    }

    @Nested
    @DisplayName("createBusinessTravelRequest Method Tests")
    class CreateBusinessTravelRequestTests {

        @Test
        @DisplayName("Should create business travel request successfully")
        void shouldCreateBusinessTravelRequestSuccessfully() {
            // Given
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(leaveRequestRepository.findCurrentLeave(1L, LocalDate.of(2024, 1, 15))).thenReturn(Optional.empty());
            when(businessTravelRequestRepository.save(any(BusinessTravelRequest.class))).thenReturn(testTravelRequest);

            // When
            BusinessTravelRequest result = businessTravelRequestService.createBusinessTravelRequest(1L, testRequestDto);

            // Then
            assertNotNull(result, "Result should not be null");
            assertEquals(testTravelRequest.getCity(), result.getCity(), "City should match");
            assertEquals(testTravelRequest.getStartDate(), result.getStartDate(), "Start date should match");
            assertEquals(testTravelRequest.getEndDate(), result.getEndDate(), "End date should match");
            assertEquals(BusinessTravelRequest.RequestStatus.PENDING, result.getStatus(), "Status should be PENDING");
            assertEquals(testEmployee, result.getCreatedBy(), "Created by should be set");

            verify(employeeRepository).findById(1L);
            verify(leaveRequestRepository).findCurrentLeave(1L, LocalDate.of(2024, 1, 15));
            verify(businessTravelRequestRepository).save(any(BusinessTravelRequest.class));
        }

        @Test
        @DisplayName("Should throw exception when employee not found")
        void shouldThrowExceptionWhenEmployeeNotFound() {
            // Given
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> businessTravelRequestService.createBusinessTravelRequest(999L, testRequestDto)
            );

            assertEquals("Employee not found", exception.getMessage(), "Exception message should match");

            verify(employeeRepository).findById(999L);
            verify(leaveRequestRepository, never()).findCurrentLeave(anyLong(), any());
            verify(businessTravelRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when start date is after end date")
        void shouldThrowExceptionWhenStartDateAfterEndDate() {
            // Given
            testRequestDto.setStartDate(LocalDate.of(2024, 1, 20));
            testRequestDto.setEndDate(LocalDate.of(2024, 1, 17));

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> businessTravelRequestService.createBusinessTravelRequest(1L, testRequestDto)
            );

            assertEquals("Start date cannot be after end date", exception.getMessage(), "Exception message should match");

            verify(employeeRepository).findById(1L);
            verify(leaveRequestRepository, never()).findCurrentLeave(anyLong(), any());
            verify(businessTravelRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when employee has approved leave during travel period")
        void shouldThrowExceptionWhenEmployeeHasApprovedLeave() {
            // Given
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(leaveRequestRepository.findCurrentLeave(1L, LocalDate.of(2024, 1, 15)))
                .thenReturn(Optional.of(testLeaveRequest));

            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> businessTravelRequestService.createBusinessTravelRequest(1L, testRequestDto)
            );

            assertEquals("You have approved leave during the requested travel period", exception.getMessage(), "Exception message should match");

            verify(employeeRepository).findById(1L);
            verify(leaveRequestRepository).findCurrentLeave(1L, LocalDate.of(2024, 1, 15));
            verify(businessTravelRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("approveBusinessTravelRequest Method Tests")
    class ApproveBusinessTravelRequestTests {

        @Test
        @DisplayName("Should approve business travel request successfully")
        void shouldApproveBusinessTravelRequestSuccessfully() {
            // Given
            testTravelRequest.setStatus(BusinessTravelRequest.RequestStatus.PENDING);
            when(businessTravelRequestRepository.findByUuid(testUuid)).thenReturn(Optional.of(testTravelRequest));
            when(employeeRepository.findById(2L)).thenReturn(Optional.of(testSupervisor));
            when(businessTravelRequestRepository.save(any(BusinessTravelRequest.class))).thenReturn(testTravelRequest);

            // When
            BusinessTravelRequest result = businessTravelRequestService.approveBusinessTravelRequest(testUuid, 2L, null);

            // Then
            assertNotNull(result, "Result should not be null");
            assertEquals(BusinessTravelRequest.RequestStatus.APPROVED, result.getStatus(), "Status should be APPROVED");
            assertEquals(testSupervisor, result.getUpdatedBy(), "Updated by should be set");
            assertNotNull(result.getUpdatedAt(), "Updated at should be set");

            verify(businessTravelRequestRepository).findByUuid(testUuid);
            verify(employeeRepository).findById(2L);
            verify(businessTravelRequestRepository).save(testTravelRequest);
        }

        @Test
        @DisplayName("Should throw exception when travel request not found")
        void shouldThrowExceptionWhenTravelRequestNotFound() {
            // Given
            UUID nonExistentUuid = UUID.randomUUID();
            when(businessTravelRequestRepository.findByUuid(nonExistentUuid)).thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> businessTravelRequestService.approveBusinessTravelRequest(nonExistentUuid, 2L, null)
            );

            assertEquals("Business travel request not found", exception.getMessage(), "Exception message should match");

            verify(businessTravelRequestRepository).findByUuid(nonExistentUuid);
            verify(employeeRepository, never()).findById(anyLong());
            verify(businessTravelRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when request is not pending")
        void shouldThrowExceptionWhenRequestIsNotPending() {
            // Given
            testTravelRequest.setStatus(BusinessTravelRequest.RequestStatus.APPROVED);
            when(businessTravelRequestRepository.findByUuid(testUuid)).thenReturn(Optional.of(testTravelRequest));

            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> businessTravelRequestService.approveBusinessTravelRequest(testUuid, 2L, null)
            );

            assertEquals("Business travel request is not in pending status", exception.getMessage(), "Exception message should match");

            verify(businessTravelRequestRepository).findByUuid(testUuid);
            verify(employeeRepository, never()).findById(anyLong());
            verify(businessTravelRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when supervisor not found")
        void shouldThrowExceptionWhenSupervisorNotFound() {
            // Given
            when(businessTravelRequestRepository.findByUuid(testUuid)).thenReturn(Optional.of(testTravelRequest));
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> businessTravelRequestService.approveBusinessTravelRequest(testUuid, 999L, null)
            );

            assertEquals("Supervisor not found", exception.getMessage(), "Exception message should match");

            verify(businessTravelRequestRepository).findByUuid(testUuid);
            verify(employeeRepository).findById(999L);
            verify(businessTravelRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("rejectBusinessTravelRequest Method Tests")
    class RejectBusinessTravelRequestTests {

        @Test
        @DisplayName("Should reject business travel request successfully")
        void shouldRejectBusinessTravelRequestSuccessfully() {
            // Given
            testTravelRequest.setStatus(BusinessTravelRequest.RequestStatus.PENDING);
            when(businessTravelRequestRepository.findByUuid(testUuid)).thenReturn(Optional.of(testTravelRequest));
            when(employeeRepository.findById(2L)).thenReturn(Optional.of(testSupervisor));
            when(businessTravelRequestRepository.save(any(BusinessTravelRequest.class))).thenReturn(testTravelRequest);

            // When
            BusinessTravelRequest result = businessTravelRequestService.rejectBusinessTravelRequest(testUuid, 2L, "Insufficient budget");

            // Then
            assertNotNull(result, "Result should not be null");
            assertEquals(BusinessTravelRequest.RequestStatus.REJECTED, result.getStatus(), "Status should be REJECTED");
            assertEquals(testSupervisor, result.getUpdatedBy(), "Updated by should be set");
            assertNotNull(result.getUpdatedAt(), "Updated at should be set");

            verify(businessTravelRequestRepository).findByUuid(testUuid);
            verify(employeeRepository).findById(2L);
            verify(businessTravelRequestRepository).save(testTravelRequest);
        }

        @Test
        @DisplayName("Should throw exception when rejecting non-pending request")
        void shouldThrowExceptionWhenRejectingNonPendingRequest() {
            // Given
            testTravelRequest.setStatus(BusinessTravelRequest.RequestStatus.REJECTED);
            when(businessTravelRequestRepository.findByUuid(testUuid)).thenReturn(Optional.of(testTravelRequest));

            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> businessTravelRequestService.rejectBusinessTravelRequest(testUuid, 2L, "Test reason")
            );

            assertEquals("Business travel request is not in pending status", exception.getMessage(), "Exception message should match");

            verify(businessTravelRequestRepository).findByUuid(testUuid);
            verify(employeeRepository, never()).findById(anyLong());
            verify(businessTravelRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Query Method Tests")
    class QueryMethodTests {

        @Test
        @DisplayName("Should get employee business travel requests")
        void shouldGetEmployeeBusinessTravelRequests() {
            // Given
            List<BusinessTravelRequest> expectedRequests = List.of(testTravelRequest);
            when(businessTravelRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(1L))
                .thenReturn(expectedRequests);

            // When
            List<BusinessTravelRequest> result = businessTravelRequestService.getEmployeeBusinessTravelRequests(1L);

            // Then
            assertNotNull(result, "Result should not be null");
            assertEquals(1, result.size(), "Should return 1 request");
            assertEquals(testTravelRequest.getUuid(), result.get(0).getUuid(), "UUID should match");

            verify(businessTravelRequestRepository).findByEmployeeIdOrderByCreatedAtDesc(1L);
        }

        @Test
        @DisplayName("Should get employee business travel requests with pagination")
        void shouldGetEmployeeBusinessTravelRequestsWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<BusinessTravelRequest> expectedPage = new PageImpl<>(List.of(testTravelRequest), pageable, 1);
            when(businessTravelRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(expectedPage);

            // When
            Page<BusinessTravelRequest> result = businessTravelRequestService.getEmployeeBusinessTravelRequests(1L, pageable);

            // Then
            assertNotNull(result, "Result should not be null");
            assertEquals(1, result.getContent().size(), "Should return 1 request");
            assertEquals(1, result.getTotalElements(), "Total elements should be 1");

            verify(businessTravelRequestRepository).findByEmployeeIdOrderByCreatedAtDesc(1L, pageable);
        }

        @Test
        @DisplayName("Should get business travel request by UUID")
        void shouldGetBusinessTravelRequestByUuid() {
            // Given
            when(businessTravelRequestRepository.findByUuid(testUuid)).thenReturn(Optional.of(testTravelRequest));

            // When
            Optional<BusinessTravelRequest> result = businessTravelRequestService.getBusinessTravelRequestByUuid(testUuid);

            // Then
            assertTrue(result.isPresent(), "Result should be present");
            assertEquals(testTravelRequest.getUuid(), result.get().getUuid(), "UUID should match");

            verify(businessTravelRequestRepository).findByUuid(testUuid);
        }

        @Test
        @DisplayName("Should return empty when business travel request UUID not found")
        void shouldReturnEmptyWhenUuidNotFound() {
            // Given
            UUID nonExistentUuid = UUID.randomUUID();
            when(businessTravelRequestRepository.findByUuid(nonExistentUuid)).thenReturn(Optional.empty());

            // When
            Optional<BusinessTravelRequest> result = businessTravelRequestService.getBusinessTravelRequestByUuid(nonExistentUuid);

            // Then
            assertFalse(result.isPresent(), "Result should be empty");

            verify(businessTravelRequestRepository).findByUuid(nonExistentUuid);
        }

        @Test
        @DisplayName("Should get pending requests for supervisor")
        void shouldGetPendingRequestsForSupervisor() {
            // Given
            List<BusinessTravelRequest> expectedRequests = List.of(testTravelRequest);
            when(businessTravelRequestRepository.findPendingRequestsBySupervisor(2L))
                .thenReturn(expectedRequests);

            // When
            List<BusinessTravelRequest> result = businessTravelRequestService.getPendingRequestsForSupervisor(2L);

            // Then
            assertNotNull(result, "Result should not be null");
            assertEquals(1, result.size(), "Should return 1 request");
            assertEquals(testTravelRequest.getUuid(), result.get(0).getUuid(), "UUID should match");

            verify(businessTravelRequestRepository).findPendingRequestsBySupervisor(2L);
        }

        @Test
        @DisplayName("Should get current travel for employee")
        void shouldGetCurrentTravelForEmployee() {
            // Given
            List<BusinessTravelRequest> expectedRequests = List.of(testTravelRequest);
            LocalDate currentDate = LocalDate.of(2024, 1, 16);
            when(businessTravelRequestRepository.findCurrentTravel(1L, currentDate))
                .thenReturn(expectedRequests);

            // When
            List<BusinessTravelRequest> result = businessTravelRequestService.getCurrentTravel(1L);

            // Then
            assertNotNull(result, "Result should not be null");
            assertEquals(1, result.size(), "Should return 1 request");
            assertEquals(testTravelRequest.getUuid(), result.get(0).getUuid(), "UUID should match");

            verify(businessTravelRequestRepository).findCurrentTravel(1L, currentDate);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle same start and end date")
        void shouldHandleSameStartAndEndDate() {
            // Given
            LocalDate sameDay = LocalDate.of(2024, 1, 15);
            testRequestDto.setStartDate(sameDay);
            testRequestDto.setEndDate(sameDay);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(leaveRequestRepository.findCurrentLeave(1L, sameDay)).thenReturn(Optional.empty());
            when(businessTravelRequestRepository.save(any(BusinessTravelRequest.class))).thenReturn(testTravelRequest);

            // When
            BusinessTravelRequest result = businessTravelRequestService.createBusinessTravelRequest(1L, testRequestDto);

            // Then
            assertNotNull(result, "Result should not be null");
            assertEquals(sameDay, result.getStartDate(), "Start date should match");
            assertEquals(sameDay, result.getEndDate(), "End date should match");
        }

        @Test
        @DisplayName("Should handle empty rejection reason")
        void shouldHandleEmptyRejectionReason() {
            // Given
            testTravelRequest.setStatus(BusinessTravelRequest.RequestStatus.PENDING);
            when(businessTravelRequestRepository.findByUuid(testUuid)).thenReturn(Optional.of(testTravelRequest));
            when(employeeRepository.findById(2L)).thenReturn(Optional.of(testSupervisor));
            when(businessTravelRequestRepository.save(any(BusinessTravelRequest.class))).thenReturn(testTravelRequest);

            // When
            BusinessTravelRequest result = businessTravelRequestService.rejectBusinessTravelRequest(testUuid, 2L, "");

            // Then
            assertNotNull(result, "Result should not be null");
            assertEquals(BusinessTravelRequest.RequestStatus.REJECTED, result.getStatus(), "Status should be REJECTED");
        }

        @Test
        @DisplayName("Should handle null rejection reason")
        void shouldHandleNullRejectionReason() {
            // Given
            testTravelRequest.setStatus(BusinessTravelRequest.RequestStatus.PENDING);
            when(businessTravelRequestRepository.findByUuid(testUuid)).thenReturn(Optional.of(testTravelRequest));
            when(employeeRepository.findById(2L)).thenReturn(Optional.of(testSupervisor));
            when(businessTravelRequestRepository.save(any(BusinessTravelRequest.class))).thenReturn(testTravelRequest);

            // When
            BusinessTravelRequest result = businessTravelRequestService.rejectBusinessTravelRequest(testUuid, 2L, null);

            // Then
            assertNotNull(result, "Result should not be null");
            assertEquals(BusinessTravelRequest.RequestStatus.REJECTED, result.getStatus(), "Status should be REJECTED");
        }

        @Test
        @DisplayName("Should handle empty current travel list")
        void shouldHandleEmptyCurrentTravelList() {
            // Given
            LocalDate currentDate = LocalDate.of(2024, 1, 16);
            when(businessTravelRequestRepository.findCurrentTravel(1L, currentDate))
                .thenReturn(List.of());

            // When
            List<BusinessTravelRequest> result = businessTravelRequestService.getCurrentTravel(1L);

            // Then
            assertNotNull(result, "Result should not be null");
            assertTrue(result.isEmpty(), "Result should be empty");

            verify(businessTravelRequestRepository).findCurrentTravel(1L, currentDate);
        }
    }

    @Nested
    @DisplayName("Transaction and Audit Tests")
    class TransactionAndAuditTests {

        @Test
        @DisplayName("Should set audit fields correctly on creation")
        void shouldSetAuditFieldsCorrectlyOnCreation() {
            // Given
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(leaveRequestRepository.findCurrentLeave(1L, LocalDate.of(2024, 1, 15))).thenReturn(Optional.empty());
            when(businessTravelRequestRepository.save(any(BusinessTravelRequest.class)))
                .thenAnswer(invocation -> {
                    BusinessTravelRequest saved = invocation.getArgument(0);
                    saved.setId(1L);
                    saved.setUuid(testUuid);
                    return saved;
                });

            // When
            BusinessTravelRequest result = businessTravelRequestService.createBusinessTravelRequest(1L, testRequestDto);

            // Then
            assertEquals(testEmployee, result.getCreatedBy(), "Created by should be set correctly");
            assertNull(result.getUpdatedBy(), "Updated by should be null on creation");
            assertNotNull(result.getCreatedAt(), "Created at should be set");
            assertNull(result.getUpdatedAt(), "Updated at should be null on creation");
        }

        @Test
        @DisplayName("Should update audit fields correctly on approval")
        void shouldUpdateAuditFieldsCorrectlyOnApproval() {
            // Given
            testTravelRequest.setStatus(BusinessTravelRequest.RequestStatus.PENDING);
            testTravelRequest.setCreatedBy(testEmployee);
            testTravelRequest.setCreatedAt(java.time.LocalDateTime.now());

            when(businessTravelRequestRepository.findByUuid(testUuid)).thenReturn(Optional.of(testTravelRequest));
            when(employeeRepository.findById(2L)).thenReturn(Optional.of(testSupervisor));
            when(businessTravelRequestRepository.save(any(BusinessTravelRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            BusinessTravelRequest result = businessTravelRequestService.approveBusinessTravelRequest(testUuid, 2L, null);

            // Then
            assertEquals(testSupervisor, result.getUpdatedBy(), "Updated by should be set to supervisor");
            assertNotNull(result.getUpdatedAt(), "Updated at should be set");
            assertNotEquals(result.getCreatedAt(), result.getUpdatedAt(), "Updated at should be different from created at");
        }
    }
}