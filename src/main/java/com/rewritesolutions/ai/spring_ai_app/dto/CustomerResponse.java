package com.rewritesolutions.ai.spring_ai_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object (DTO) for customer response data.
 * This class encapsulates customer data returned to API clients.
 *
 * <p>Contains all customer information including auto-generated fields (ID and timestamps)
 * that are not present in {@link CustomerRequest}.</p>
 *
 * <p>Uses Lombok annotations to reduce boilerplate code and Swagger annotations
 * for API documentation.</p>
 *
 * @author Rewrite Solutions
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "CustomerResponse", description = "Response payload containing customer details")
public class CustomerResponse {

    /**
     * Unique identifier for the customer.
     * Auto-generated when the customer is created.
     */
    @Schema(description = "Unique customer identifier", example = "1")
    private Long id;

    /** Customer's first name */
    @Schema(description = "Customer first name", example = "John")
    private String firstName;

    /** Customer's last name */
    @Schema(description = "Customer last name", example = "Doe")
    private String lastName;

    /** Customer's email address (unique across all customers) */
    @Schema(description = "Customer email address", example = "john.doe@email.com")
    private String email;

    /** Customer's phone number (optional) */
    @Schema(description = "Customer phone number", example = "+12025550123")
    private String phoneNumber;

    /**
     * Timestamp when the customer record was created.
     * Automatically set by the system on creation.
     */
    @Schema(description = "Customer creation timestamp", example = "2024-12-15T10:30:45")
    private LocalDateTime createdAt;

    /**
     * Timestamp when the customer record was last updated.
     * Automatically updated by the system on any modification.
     */
    @Schema(description = "Customer last update timestamp", example = "2024-12-16T14:20:10")
    private LocalDateTime updatedAt;
}
