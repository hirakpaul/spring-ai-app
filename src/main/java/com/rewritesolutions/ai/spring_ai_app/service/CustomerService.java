package com.rewritesolutions.ai.spring_ai_app.service;

import java.util.List;
import com.rewritesolutions.ai.spring_ai_app.dto.CustomerRequest;
import com.rewritesolutions.ai.spring_ai_app.dto.CustomerResponse;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse getCustomerById(Long id);

    CustomerResponse getCustomerByEmail(String email);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse updateCustomer(Long id, CustomerRequest request);

    void deleteCustomer(Long id);

    long countCustomers();

    List<CustomerResponse> searchCustomers(String searchTerm);
}
