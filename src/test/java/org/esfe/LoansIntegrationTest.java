package org.esfe;

import org.esfe.modelos.Laptop;
import org.esfe.modelos.Loans;
import org.esfe.modelos.User;
import org.esfe.repositorios.LaptopRepository;
import org.esfe.repositorios.ILoansRepository;
import org.esfe.repositorios.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoansIntegrationTest {

    @Autowired
    private ILoansRepository loansRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private LaptopRepository laptopRepository;

    private User testUser;
    private Laptop testLaptop;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setFullName("Integration Test User");
        testUser.setEmail("integration@test.com");
        testUser.setStudentId("INT001");
        testUser.setCareer("Ingeniería en Sistemas");
        testUser.setRole("student");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // Create test laptop
        testLaptop = new Laptop();
        testLaptop.setBrand("Dell");
        testLaptop.setModel("Latitude 5520");
        testLaptop.setAssetTag("DL123456");
        testLaptop.setStatus("available");
        testLaptop.setCreatedAt(LocalDateTime.now());
        testLaptop = laptopRepository.save(testLaptop);
    }

    @Test
    @SuppressWarnings("null")
    void testCreateAndRetrieveLoan() {
        // Given
        Loans loan = new Loans();
        loan.setUser(testUser);
        loan.setLaptop(testLaptop);
        loan.setRequestedHours(4);
        loan.setStatus("pending");
        loan.setTermsAccepted(true);
        loan.setRequestedAt(LocalDateTime.now());

        // When
        Loans savedLoan = loansRepository.save(loan);

        // Then
        assertNotNull(savedLoan.getId());
        assertEquals("pending", savedLoan.getStatus());
        assertEquals(4, savedLoan.getRequestedHours());
        assertEquals(testUser.getId(), savedLoan.getUser().getId());
        assertEquals(testLaptop.getId(), savedLoan.getLaptop().getId());
    }

    @Test
    @SuppressWarnings("null")
    void testLoanWorkflowProgression() {
        // Given - Create initial loan
        Loans loan = createTestLoan();
        Loans savedLoan = loansRepository.save(loan);

        // When - Approve loan
        savedLoan.setStatus("approved");
        savedLoan.setApprovedAt(LocalDateTime.now());
        loansRepository.save(savedLoan);

        // Then - Verify approval
        Optional<Loans> approvedLoan = loansRepository.findById(savedLoan.getId());
        assertTrue(approvedLoan.isPresent());
        assertEquals("approved", approvedLoan.get().getStatus());
        assertNotNull(approvedLoan.get().getApprovedAt());

        // When - Deliver laptop
        savedLoan.setStatus("active");
        savedLoan.setDeliveredAt(LocalDateTime.now());
        loansRepository.save(savedLoan);

        // Then - Verify delivery
        Optional<Loans> activeLoan = loansRepository.findById(savedLoan.getId());
        assertTrue(activeLoan.isPresent());
        assertEquals("active", activeLoan.get().getStatus());
        assertNotNull(activeLoan.get().getDeliveredAt());

        // When - Return laptop
        savedLoan.setStatus("completed");
        savedLoan.setReturnedAt(LocalDateTime.now());
        loansRepository.save(savedLoan);

        // Then - Verify completion
        Optional<Loans> completedLoan = loansRepository.findById(savedLoan.getId());
        assertTrue(completedLoan.isPresent());
        assertEquals("completed", completedLoan.get().getStatus());
        assertNotNull(completedLoan.get().getReturnedAt());
    }

    @Test
    void testFindLoansByStatus() {
        // Given - Create loans with different statuses
        Loans pendingLoan = createTestLoan();
        pendingLoan.setStatus("pending");

        Loans activeLoan = createTestLoan();
        activeLoan.setStatus("active");

        loansRepository.save(pendingLoan);
        loansRepository.save(activeLoan);

        // When - Find all loans and filter manually for testing
        List<Loans> allLoans = loansRepository.findAll();

        // Then - Verify loans were created
        assertTrue(allLoans.size() >= 2);
        assertTrue(allLoans.stream().anyMatch(l -> "pending".equals(l.getStatus())));
        assertTrue(allLoans.stream().anyMatch(l -> "active".equals(l.getStatus())));
    }

    @Test
    @SuppressWarnings("null")
    void testDeleteLoan() {
        // Given
        Loans loan = createTestLoan();
        Loans savedLoan = loansRepository.save(loan);
        Integer loanId = savedLoan.getId();

        // When
        loansRepository.deleteById(loanId);

        // Then
        Optional<Loans> deletedLoan = loansRepository.findById(loanId);
        assertFalse(deletedLoan.isPresent());
    }

    @Test
    void testLoanValidationConstraints() {
        // Given - Loan with invalid data that should trigger validation errors
        Loans invalidLoan = new Loans();
        invalidLoan.setUser(testUser);
        invalidLoan.setLaptop(testLaptop);
        invalidLoan.setRequestedHours(0); // Invalid: must be >= 1
        invalidLoan.setStatus("invalid_status"); // Invalid status pattern
        invalidLoan.setTermsAccepted(false);
        invalidLoan.setRequestedAt(LocalDateTime.now());

        // When & Then - Should throw validation exception
        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            loansRepository.save(invalidLoan);
        });

        // Test with valid data to ensure validation passes
        Loans validLoan = new Loans();
        validLoan.setUser(testUser);
        validLoan.setLaptop(testLaptop);
        validLoan.setRequestedHours(4); // Valid: >= 1
        validLoan.setStatus("pending"); // Valid status
        validLoan.setTermsAccepted(true);
        validLoan.setRequestedAt(LocalDateTime.now());

        // This should save successfully
        Loans savedLoan = loansRepository.save(validLoan);
        assertNotNull(savedLoan.getId());
        assertEquals("pending", savedLoan.getStatus());
        assertEquals(4, savedLoan.getRequestedHours());
    }

    private Loans createTestLoan() {
        Loans loan = new Loans();
        loan.setUser(testUser);
        loan.setLaptop(testLaptop);
        loan.setRequestedHours(4);
        loan.setStatus("pending");
        loan.setTermsAccepted(true);
        loan.setRequestedAt(LocalDateTime.now());
        return loan;
    }
}
