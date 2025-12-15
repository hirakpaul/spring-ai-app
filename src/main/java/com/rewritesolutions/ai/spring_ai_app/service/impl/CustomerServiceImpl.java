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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

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

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        log.info("Fetching customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email) {
        log.info("Fetching customer with email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));

        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        log.info("Fetching all customers");

        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

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

    @Override
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);

        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + id);
        }

        customerRepository.deleteById(id);
        log.info("Customer deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCustomers() {
        long count = customerRepository.count();
        log.info("Total customers count: {}", count);
        return count;
    }

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
