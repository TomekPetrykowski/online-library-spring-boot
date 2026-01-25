package com.online.library.domain.dto;

import com.online.library.domain.enums.UserRole;
import com.online.library.validation.ValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {

    private Long id;

    @NotBlank(message = "Username is required", groups = { ValidationGroups.Create.class,
            ValidationGroups.Update.class })
    @Size(max = 50, message = "Username must be at most 50 characters", groups = { ValidationGroups.Create.class,
            ValidationGroups.Update.class })
    private String username;

    @NotBlank(message = "Password is required", groups = ValidationGroups.Create.class)
    @Size(min = 12, max = 256, message = "Password must be between 12 and 256 characters", groups = ValidationGroups.Create.class)
    private String password;

    @NotBlank(message = "Email is required", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class })
    @Email(message = "Email should be valid", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class })
    @Size(max = 100, message = "Email must be at most 100 characters", groups = { ValidationGroups.Create.class,
            ValidationGroups.Update.class })
    private String email;

    // @NotNull(message = "Role is required")
    private UserRole role;

    private Boolean enabled;
}
