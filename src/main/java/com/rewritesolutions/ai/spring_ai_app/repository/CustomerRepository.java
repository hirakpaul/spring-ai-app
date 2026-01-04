package com.rewritesolutions.ai.spring_ai_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rewritesolutions.ai.spring_ai_app.entity.Customer;

/**
 * Repository interface for {@link Customer} entity.
 * Provides database access operations for customer management using Spring Data JPA.
 *
 * <p>This interface extends {@link JpaRepository} to inherit standard CRUD operations
 * and includes custom query methods for customer-specific operations.</p>
 *
 * @author Rewrite Solutions
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Finds a customer by their email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the customer if found, or empty if not found
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Finds customers whose first name or last name contains the specified search terms.
     * This method performs a case-sensitive partial match search.
     *
     * @param firstName the search term for first name
     * @param lastName the search term for last name
     * @return a list of customers matching the search criteria, or empty list if none found
     */
    List<Customer> findByFirstNameContainingOrLastNameContaining(
            String firstName, String lastName);

    /**
     * Checks if a customer with the specified email address exists in the database.
     *
     * @param email the email address to check
     * @return {@code true} if a customer with the email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds all customers by last name using case-insensitive matching.
     * This is a custom JPQL query that performs exact matching ignoring case.
     *
     * @param lastName the last name to search for (case-insensitive)
     * @return a list of customers with the matching last name, or empty list if none found
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.lastName) = LOWER(:lastName)")
    List<Customer> findByLastNameIgnoreCase(@Param("lastName") String lastName);

    /**
     * Counts the number of customers whose email addresses contain the specified domain.
     *
     * @param domain the email domain to search for (e.g., "gmail.com")
     * @return the count of customers with email addresses containing the domain
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.email LIKE %:domain%")
    long countByEmailDomain(@Param("domain") String domain);
}
