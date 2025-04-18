package project.home.automation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.home.automation.entity.Role;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThingDTO {
    private String thingUID;
    private String label;
    private String thingTypeUID;
    private String host;
    private String roomName;
}