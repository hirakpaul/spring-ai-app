package com.rewritesolutions.ai.spring_ai_app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rewritesolutions.ai.spring_ai_app.dto.CustomerRequest;
import com.rewritesolutions.ai.spring_ai_app.dto.CustomerResponse;
import com.rewritesolutions.ai.spring_ai_app.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * REST Controller for customer management operations.
 * Provides HTTP endpoints for CRUD operations and search functionality on customer resources.
 *
 * <p>All endpoints are prefixed with {@code /api/v1/customers}. This controller handles
 * validation, delegates business logic to {@link CustomerService}, and returns appropriate
 * HTTP responses.</p>
 *
 * <p>API documentation is available via Swagger/OpenAPI annotations.</p>
 *
 * @author Rewrite Solutions
 * @version 1.0
 * @since 1.0
 */
@RestController
@Tag(name = "Customer API", description = "Customer management operations")
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    /** Service layer for customer business logic */
    private final CustomerService customerService;

    /**
     * Creates a new customer.
     * Endpoint: POST /api/v1/customers
     *
     * @param request the validated customer data from request body
     * @return HTTP 201 with the created customer response
     */
    @Operation(summary = "Create a new customer", description = "Creates a new customer and returns the created customer details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        log.info("REST request to create customer: {}", request.getEmail());
        CustomerResponse response = customerService.createCustomer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a customer by their unique ID.
     * Endpoint: GET /api/v1/customers/{id}
     *
     * @param id the customer's unique identifier
     * @return HTTP 200 with the customer response, or HTTP 404 if not found
     */
    @Operation(summary = "Get customer by ID", description = "Fetch a customer using its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        log.info("REST request to get customer by ID: {}", id);
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a customer by their email address.
     * Endpoint: GET /api/v1/customers/email/{email}
     *
     * @param email the customer's email address
     * @return HTTP 200 with the customer response, or HTTP 404 if not found
     */
    @Operation(summary = "Get customer by email", description = "Fetch customer details using email address")
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@PathVariable String email) {
        log.info("REST request to get customer by email: {}", email);
        CustomerResponse response = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all customers in the system.
     * Endpoint: GET /api/v1/customers
     *
     * @return HTTP 200 with a list of all customers
     */
    @Operation(summary = "Get all customers", description = "Returns a list of all customers")
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        log.info("REST request to get all customers");
        List<CustomerResponse> responses = customerService.getAllCustomers();
        return ResponseEntity.ok(responses);
    }

    /**
     * Searches for customers by name.
     * Endpoint: GET /api/v1/customers/search?term={searchTerm}
     *
     * @param term the search term to match against customer names
     * @return HTTP 200 with a list of matching customers
     */
    @Operation(summary = "Search customers", description = "Search customers using a keyword")
    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> searchCustomers(@RequestParam String term) {
        log.info("REST request to search customers with term: {}", term);
        List<CustomerResponse> responses = customerService.searchCustomers(term);
        return ResponseEntity.ok(responses);
    }

    /**
     * Returns the total count of customers.
     * Endpoint: GET /api/v1/customers/count
     *
     * @return HTTP 200 with the customer count
     */
    @Operation(summary = "Count customers", description = "Returns total number of customers")
    @GetMapping("/count")
    public ResponseEntity<Long> countCustomers() {
        log.info("REST request to count customers");
        long count = customerService.countCustomers();
        return ResponseEntity.ok(count);
    }

    /**
     * Updates an existing customer.
     * Endpoint: PUT /api/v1/customers/{id}
     *
     * @param id the customer's unique identifier
     * @param customerRequest the validated updated customer data
     * @return HTTP 200 with the updated customer response
     */
    @Operation(summary = "Update customer", description = "Update existing customer details by ID")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest customerRequest) {
        log.info("REST request to update customer with ID: {}", id);
        CustomerResponse response = customerService.updateCustomer(id, customerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a customer from the system.
     * Endpoint: DELETE /api/v1/customers/{id}
     *
     * @param id the customer's unique identifier
     * @return HTTP 204 on successful deletion, or HTTP 404 if not found
     */
    @Operation(summary = "Delete customer", description = "Delete a customer by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("REST request to delete customer with ID: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
