package project.home.automation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailDTO {
    @NotBlank(message = "This field is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "This field is required")
    private String otp;

    @NotBlank(message = "This field is required")
    private String password;

    @NotBlank(message = "This field is required")
    private String newPassword;
}