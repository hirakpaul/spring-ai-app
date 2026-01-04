package com.rewritesolutions.ai.spring_ai_app.service.impl;

import com.rewritesolutions.ai.spring_ai_app.service.CustomerService;

import com.rewritesolutions.ai.spring_ai_app.dto.CustomerRequest;
import com.rewritesolutions.ai.spring_ai_app.dto.CustomerResponse;
import com.rewritesolutions.ai.spring_ai_app.entity.Customer;
import com.rewritesolutions.ai.spring_ai_app.exception.CustomerNotFoundException;
import com.rewritesolutions.ai.spring_ai_app.mapper.CustomerMapper;
import com.rewritesolutions.ai.spring_ai_app.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link CustomerService} providing business logic for customer management.
 * This service handles all customer-related operations including CRUD operations and search functionality.
 *
 * <p>All methods are transactional by default ({@code @Transactional} at class level).
 * Read-only operations are explicitly marked with {@code @Transactional(readOnly = true)}
 * for performance optimization.</p>
 *
 * <p>Uses constructor-based dependency injection via Lombok's {@code @RequiredArgsConstructor}.</p>
 *
 * @author Rewrite Solutions
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    /** Repository for customer database operations */
    private final CustomerRepository customerRepository;

    /** Mapper for converting between entities and DTOs */
    private final CustomerMapper customerMapper;

    /**
     * Creates a new customer in the system.
     * Validates that the email address is not already in use before creating the customer.
     *
     * @param request the customer data to create
     * @return a {@link CustomerResponse} containing the created customer details including generated ID
     * @throws IllegalArgumentException if a customer with the same email already exists
     */
    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creating new customer with email: {}", request.getEmail());

        // Check if customer already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Customer with email " + request.getEmail() + " already exists");
        }

        Customer customer = customerMapper.toEntity(request);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer created successfully with ID: {}", savedCustomer.getId());
        return customerMapper.toResponse(savedCustomer);
    }

    /**
     * Retrieves a customer by their unique identifier.
     *
     * @param id the unique identifier of the customer
     * @return a {@link CustomerResponse} containing the customer details
     * @throws CustomerNotFoundException if no customer exists with the specified ID
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        log.info("Fetching customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        return customerMapper.toResponse(customer);
    }

    /**
     * Retrieves a customer by their email address.
     *
     * @param email the email address of the customer
     * @return a {@link CustomerResponse} containing the customer details
     * @throws CustomerNotFoundException if no customer exists with the specified email
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email) {
        log.info("Fetching customer with email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));

        return customerMapper.toResponse(customer);
    }

    /**
     * Retrieves all customers in the system.
     *
     * @return a list of {@link CustomerResponse} objects representing all customers,
     *         or an empty list if no customers exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        log.info("Fetching all customers");

        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing customer's information.
     * Validates that if the email is being changed, the new email is not already in use.
     *
     * @param id the unique identifier of the customer to update
     * @param request the updated customer data
     * @return a {@link CustomerResponse} containing the updated customer details
     * @throws CustomerNotFoundException if no customer exists with the specified ID
     * @throws IllegalArgumentException if the new email is already in use by another customer
     */
    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        log.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        // Check if new email already exists (excluding current customer)
        if (!customer.getEmail().equals(request.getEmail()) &&
                customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email " + request.getEmail() + " is already in use");
        }

        customerMapper.updateEntityFromRequest(request, customer);
        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Customer updated successfully with ID: {}", updatedCustomer.getId());
        return customerMapper.toResponse(updatedCustomer);
    }

    /**
     * Deletes a customer from the system.
     *
     * @param id the unique identifier of the customer to delete
     * @throws CustomerNotFoundException if no customer exists with the specified ID
     */
    @Override
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);

        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + id);
        }

        customerRepository.deleteById(id);
        log.info("Customer deleted successfully with ID: {}", id);
    }

    /**
     * Returns the total count of customers in the system.
     *
     * @return the total number of customers
     */
    @Override
    @Transactional(readOnly = true)
    public long countCustomers() {
        long count = customerRepository.count();
        log.info("Total customers count: {}", count);
        return count;
    }

    /**
     * Searches for customers whose first name or last name contains the search term.
     * The search is case-sensitive and performs a partial match.
     *
     * @param searchTerm the term to search for in customer names
     * @return a list of {@link CustomerResponse} objects matching the search criteria,
     *         or an empty list if no matches found
     */
    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> searchCustomers(String searchTerm) {
        log.info("Searching customers with term: {}", searchTerm);

        return customerRepository.findByFirstNameContainingOrLastNameContaining(searchTerm, searchTerm)
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }
}
