package com.rewritesolutions.ai.spring_ai_app.service;

import java.util.List;
import com.rewritesolutions.ai.spring_ai_app.dto.CustomerRequest;
import com.rewritesolutions.ai.spring_ai_app.dto.CustomerResponse;

/**
 * Service interface defining operations for customer management.
 * Provides a contract for customer-related business logic including CRUD operations and search functionality.
 *
 * <p>All service methods work with DTOs ({@link CustomerRequest} and {@link CustomerResponse})
 * rather than entities to maintain separation between layers.</p>
 *
 * @author Rewrite Solutions
 * @version 1.0
 * @since 1.0
 * @see com.rewritesolutions.ai.spring_ai_app.service.impl.CustomerServiceImpl
 */
public interface CustomerService {

    /**
     * Creates a new customer in the system.
     *
     * @param request the customer data to create
     * @return a {@link CustomerResponse} containing the created customer details
     * @throws IllegalArgumentException if a customer with the same email already exists
     */
    CustomerResponse createCustomer(CustomerRequest request);

    /**
     * Retrieves a customer by their unique identifier.
     *
     * @param id the unique identifier of the customer
     * @return a {@link CustomerResponse} containing the customer details
     * @throws com.rewritesolutions.ai.spring_ai_app.exception.CustomerNotFoundException
     *         if no customer exists with the specified ID
     */
    CustomerResponse getCustomerById(Long id);

    /**
     * Retrieves a customer by their email address.
     *
     * @param email the email address of the customer
     * @return a {@link CustomerResponse} containing the customer details
     * @throws com.rewritesolutions.ai.spring_ai_app.exception.CustomerNotFoundException
     *         if no customer exists with the specified email
     */
    CustomerResponse getCustomerByEmail(String email);

    /**
     * Retrieves all customers in the system.
     *
     * @return a list of {@link CustomerResponse} objects, or an empty list if no customers exist
     */
    List<CustomerResponse> getAllCustomers();

    /**
     * Updates an existing customer's information.
     *
     * @param id the unique identifier of the customer to update
     * @param request the updated customer data
     * @return a {@link CustomerResponse} containing the updated customer details
     * @throws com.rewritesolutions.ai.spring_ai_app.exception.CustomerNotFoundException
     *         if no customer exists with the specified ID
     * @throws IllegalArgumentException if the new email is already in use by another customer
     */
    CustomerResponse updateCustomer(Long id, CustomerRequest request);

    /**
     * Deletes a customer from the system.
     *
     * @param id the unique identifier of the customer to delete
     * @throws com.rewritesolutions.ai.spring_ai_app.exception.CustomerNotFoundException
     *         if no customer exists with the specified ID
     */
    void deleteCustomer(Long id);

    /**
     * Returns the total count of customers in the system.
     *
     * @return the total number of customers
     */
    long countCustomers();

    /**
     * Searches for customers whose first name or last name contains the search term.
     *
     * @param searchTerm the term to search for in customer names
     * @return a list of {@link CustomerResponse} objects matching the search criteria,
     *         or an empty list if no matches found
     */
    List<CustomerResponse> searchCustomers(String searchTerm);
}
