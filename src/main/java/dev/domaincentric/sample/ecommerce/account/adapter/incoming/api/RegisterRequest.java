package dev.domaincentric.sample.ecommerce.account.adapter.incoming.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request DTO for register endpoint.
 *
 * @param email the user's email address
 * @param password the user's password
 * @param firstName the account owner's first name
 * @param lastName the account owner's last name
 * @param dateOfBirth the account owner's date of birth, in ISO format
 */
public record RegisterRequest(
    @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
    @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
    @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name is too long")
        String firstName,
    @NotBlank(message = "Last name is required") @Size(max = 100, message = "Last name is too long")
        String lastName,
    @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must lie in the past")
        LocalDate dateOfBirth) {}
