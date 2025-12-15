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

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        log.info("REST request to create customer: {}", request.getEmail());
        CustomerResponse response = customerService.createCustomer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        log.info("REST request to get customer by ID: {}", id);
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@PathVariable String email) {
        log.info("REST request to get customer by email: {}", email);
        CustomerResponse response = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        log.info("REST request to get all customers");
        List<CustomerResponse> responses = customerService.getAllCustomers();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> searchCustomers(@RequestParam String term) {
        log.info("REST request to search customers with term: {}", term);
        List<CustomerResponse> responses = customerService.searchCustomers(term);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countCustomers() {
        log.info("REST request to count customers");
        long count = customerService.countCustomers();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest customerRequest) {
        log.info("REST request to update customer with ID: {}", id);
        CustomerResponse response = customerService.updateCustomer(id, customerRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("REST request to delete customer with ID: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
