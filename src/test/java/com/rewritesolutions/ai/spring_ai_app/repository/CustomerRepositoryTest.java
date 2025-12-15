package com.rewritesolutions.ai.spring_ai_app.repository;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import com.rewritesolutions.ai.spring_ai_app.entity.Customer;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .build();
    }

    @Test
    void testCountCustomers_InitiallyZero() {
        // Given: No customers in database

        // When: Count customers
        long count = customerRepository.count();

        // Then: Count should be zero
        assertThat(count).isEqualTo(0);
    }

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
