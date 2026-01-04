package com.rewritesolutions.ai.spring_ai_app.mapper;

import com.rewritesolutions.ai.spring_ai_app.dto.CustomerRequest;
import com.rewritesolutions.ai.spring_ai_app.dto.CustomerResponse;
import com.rewritesolutions.ai.spring_ai_app.entity.Customer;
import org.springframework.stereotype.Component;

/**
 * Mapper component for converting between {@link Customer} entities and DTOs.
 * Provides mapping methods to transform customer data between different layers of the application.
 *
 * <p>This mapper handles conversions between:
 * <ul>
 *   <li>{@link CustomerRequest} (DTO) to {@link Customer} (Entity)</li>
 *   <li>{@link Customer} (Entity) to {@link CustomerResponse} (DTO)</li>
 *   <li>Updating existing {@link Customer} entities from {@link CustomerRequest}</li>
 * </ul>
 *
 * @author Rewrite Solutions
 * @version 1.0
 * @since 1.0
 */
@Component
public class CustomerMapper {

    /**
     * Converts a {@link CustomerRequest} DTO to a {@link Customer} entity.
     * Creates a new Customer entity with data from the request, excluding auto-generated fields.
     *
     * @param request the customer request DTO containing customer data
     * @return a new {@link Customer} entity populated with request data
     */
    public Customer toEntity(CustomerRequest request) {
        return Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }

    /**
     * Converts a {@link Customer} entity to a {@link CustomerResponse} DTO.
     * Transforms the entity into a response object suitable for API responses.
     *
     * @param customer the customer entity to convert
     * @return a {@link CustomerResponse} DTO containing all customer data including timestamps
     */
    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing {@link Customer} entity with data from a {@link CustomerRequest} DTO.
     * This method modifies the entity in-place, preserving ID and timestamp fields.
     *
     * @param request the customer request DTO containing updated data
     * @param customer the existing customer entity to update
     */
    public void updateEntityFromRequest(CustomerRequest request, Customer customer) {
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
    }
}
