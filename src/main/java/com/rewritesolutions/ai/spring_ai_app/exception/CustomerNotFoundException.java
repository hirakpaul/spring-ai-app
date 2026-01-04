package com.rewritesolutions.ai.spring_ai_app.exception;

/**
 * Exception thrown when a requested customer cannot be found in the system.
 * This is a runtime exception that indicates a customer lookup operation failed.
 *
 * <p>This exception is typically thrown by service layer methods when attempting to
 * retrieve a customer by ID or email, and no matching customer exists in the database.</p>
 *
 * <p>When caught by the {@link GlobalExceptionHandler}, this exception results in
 * an HTTP 404 NOT FOUND response to the client.</p>
 *
 * @author Rewrite Solutions
 * @version 1.0
 * @since 1.0
 * @see GlobalExceptionHandler#handleCustomerNotFoundException(CustomerNotFoundException)
 */
public class CustomerNotFoundException extends RuntimeException {

    /**
     * Constructs a new CustomerNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining why the customer was not found
     *                (e.g., "Customer not found with ID: 123")
     */
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
