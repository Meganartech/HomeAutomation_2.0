package project.home.automation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThingDTO {
    private String roomName;
    private String roomId;
    private String label;
    private String thingTypeUID;
    private String host;
    private String macAddress;
}
