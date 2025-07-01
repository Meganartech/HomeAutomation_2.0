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
public class RulesToggleDTO {
    @NotBlank(message = "This field is required")
    private String ruleId;

    @NotBlank(message = "This field is required")
    private boolean enable;
}