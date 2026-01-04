package com.rewritesolutions.ai.spring_ai_app.repository;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import com.rewritesolutions.ai.spring_ai_app.entity.Customer;

/**
 * Integration tests for {@link CustomerRepository}.
 * Uses {@link DataJpaTest} to test repository methods with an in-memory database.
 *
 * <p>This test class verifies the correct behavior of custom query methods and
 * basic CRUD operations provided by Spring Data JPA.</p>
 *
 * @author Rewrite Solutions
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
class CustomerRepositoryTest {

    /** Entity manager for direct database operations in tests */
    @Autowired
    private TestEntityManager entityManager;

    /** Repository under test */
    @Autowired
    private CustomerRepository customerRepository;

    /** Test customer instance reused across test methods */
    private Customer testCustomer;

    /**
     * Sets up test data before each test method.
     * Creates a standard test customer with sample data.
     */
    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .build();
    }

    /**
     * Tests that the customer count is initially zero in an empty database.
     * Verifies the basic count functionality when no data exists.
     */
    @Test
    void testCountCustomers_InitiallyZero() {
        // Given: No customers in database

        // When: Count customers
        long count = customerRepository.count();

        // Then: Count should be zero
        assertThat(count).isEqualTo(0);
    }

    /**
     * Tests successful creation and persistence of a customer entity.
     * Verifies that the customer is saved with an auto-generated ID and all fields are persisted correctly.
     */
    @Test
    void testSaveCustomer_Success() {
        // Given: A new customer

        // When: Save the customer
        Customer savedCustomer = customerRepository.save(testCustomer);

        // Then: Customer should be saved with ID
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getFirstName()).isEqualTo("John");
        assertThat(savedCustomer.getEmail()).isEqualTo("john.doe@example.com");
    }

    /**
     * Tests that the customer count increases correctly after saving a new customer.
     * Verifies the count operation reflects database changes.
     */
    @Test
    void testCountCustomers_AfterSave() {
        // Given: Initial count is zero
        long initialCount = customerRepository.count();
        assertThat(initialCount).isEqualTo(0);

        // When: Create and save a customer
        customerRepository.save(testCustomer);

        // Then: Count should be one
        long finalCount = customerRepository.count();
        assertThat(finalCount).isEqualTo(1);
    }

    /**
     * Tests the {@link CustomerRepository#findByEmail(String)} method.
     * Verifies that a customer can be successfully retrieved by their email address.
     */
    @Test
    void testFindByEmail_Success() {
        // Given: A saved customer
        entityManager.persist(testCustomer);
        entityManager.flush();

        // When: Find by email
        Optional<Customer> found = customerRepository.findByEmail("john.doe@example.com");

        // Then: Customer should be found
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }

    /**
     * Tests the {@link CustomerRepository#existsByEmail(String)} method.
     * Verifies that the method correctly identifies when an email exists in the database.
     */
    @Test
    void testExistsByEmail_Success() {
        // Given: A saved customer
        entityManager.persist(testCustomer);
        entityManager.flush();

        // When: Check if email exists
        boolean exists = customerRepository.existsByEmail("john.doe@example.com");

        // Then: Should return true
        assertThat(exists).isTrue();
    }
}
