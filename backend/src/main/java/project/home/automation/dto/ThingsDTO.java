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
public class ThingsDTO {
    @NotBlank(message = "This field is required")
    private String roomId;

    @NotBlank(message = "This field is required")
    private String roomName;

    @NotBlank(message = "This field is required")
    private String label;

    @NotBlank(message = "This field is required")
    private String thingTypeUID;

    private String host;

    private String macAddress;
}