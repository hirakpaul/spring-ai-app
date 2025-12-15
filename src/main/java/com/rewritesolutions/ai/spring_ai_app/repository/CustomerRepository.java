package com.rewritesolutions.ai.spring_ai_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rewritesolutions.ai.spring_ai_app.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // Find customer by email
    Optional<Customer> findByEmail(String email);

    // Find customers by first name or last name
    List<Customer> findByFirstNameContainingOrLastNameContaining(
            String firstName, String lastName);

    // Check if email exists
    boolean existsByEmail(String email);

    // Custom query to find by last name
    @Query("SELECT c FROM Customer c WHERE LOWER(c.lastName) = LOWER(:lastName)")
    List<Customer> findByLastNameIgnoreCase(@Param("lastName") String lastName);

    // Count customers by email domain
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.email LIKE %:domain%")
    long countByEmailDomain(@Param("domain") String domain);
}
