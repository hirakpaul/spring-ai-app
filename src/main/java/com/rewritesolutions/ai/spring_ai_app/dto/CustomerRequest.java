package com.rewritesolutions.ai.spring_ai_app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for customer creation and update requests.
 * This class encapsulates customer data received from API clients.
 *
 * <p>All fields are validated using Bean Validation (JSR-303) annotations.
 * Validation errors are automatically handled by the {@link com.rewritesolutions.ai.spring_ai_app.exception.GlobalExceptionHandler}
 * and returned as structured error responses.</p>
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
@Schema(name = "CustomerRequest", description = "Request payload for creating or updating a customer")
public class CustomerRequest {

    /**
     * Customer's first name (required).
     * Must not be blank.
     */
    @NotBlank(message = "First name is required")
    @Schema(description = "Customer first name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    /**
     * Customer's last name (required).
     * Must not be blank.
     */
    @NotBlank(message = "Last name is required")
    @Schema(description = "Customer last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    /**
     * Customer's email address (required).
     * Must be a valid email format and not blank.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Customer email address", example = "john.doe@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    /**
     * Customer's phone number (optional).
     * Must follow E.164 international phone number format when provided.
     * Format: + followed by country code and number (1-15 digits total).
     */
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    @Schema(description = "Customer phone number in E.164 format", example = "+12025550123")
    private String phoneNumber;
}
