package project.home.automation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RulesDTO {
    @NotBlank(message = "This field is required")
    private String ruleId;

    @NotBlank(message = "This field is required")
    private String ruleName;

    @NotBlank(message = "This field is required")
    private String fromTime;

    @NotBlank(message = "This field is required")
    private String toTime;

    @NotEmpty(message = "This field is required")
    private List<String> days;

    @NotBlank(message = "This field is required")
    private String command;

    @NotBlank(message = "This field is required")
    private String thingId;

    @NotBlank(message = "This field is required")
    private String roomId;

    @NotBlank(message = "This field is required")
    private String roomName;

    @NotBlank(message = "This field is required")
    private Boolean enabled;
}