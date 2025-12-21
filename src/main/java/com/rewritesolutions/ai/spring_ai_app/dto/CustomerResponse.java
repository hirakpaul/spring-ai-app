package com.rewritesolutions.ai.spring_ai_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "CustomerResponse", description = "Response payload containing customer details")
public class CustomerResponse {

    @Schema(description = "Unique customer identifier", example = "1")
    private Long id;

    @Schema(description = "Customer first name", example = "John")
    private String firstName;

    @Schema(description = "Customer last name", example = "Doe")
    private String lastName;

    @Schema(description = "Customer email address", example = "john.doe@email.com")
    private String email;

    @Schema(description = "Customer phone number", example = "+12025550123")
    private String phoneNumber;

    @Schema(description = "Customer creation timestamp", example = "2024-12-15T10:30:45")
    private LocalDateTime createdAt;

    @Schema(description = "Customer last update timestamp", example = "2024-12-16T14:20:10")
    private LocalDateTime updatedAt;
}
