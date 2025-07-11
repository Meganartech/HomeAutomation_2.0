package project.home.automation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDTO {
    @NotBlank(message = "This field is required")
    private String userId;

    @NotBlank(message = "This field is required")
    private String currentPassword;

    @NotBlank(message = "This field is required")
    private String newPassword;
}