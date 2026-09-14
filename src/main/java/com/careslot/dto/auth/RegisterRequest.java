package com.careslot.dto.auth;


import com.careslot.db.generated.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6 ,message = "Password should be at least 6 character")
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

}
