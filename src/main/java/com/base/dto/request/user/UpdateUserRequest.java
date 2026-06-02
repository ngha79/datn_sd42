package com.base.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @NotBlank(message = "FullName is required")
    private String fullName;

    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9,10}$",
            message = "Invalid Vietnamese phone number"
    )
    @NotBlank(message = "Phone is required")
    private String phone;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatar;

    @Pattern(
            regexp = "^(MALE|FEMALE|OTHER)$",
            message = "Gender must be MALE, FEMALE or OTHER"
    )
    private String gender;

    @Past(message = "Birthday must be in the past")
    private LocalDate birthday;
}