package project.home.automation.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Device {
    private String thingUID;
    private String thingTypeUID;
    private String label;
    private String host;
    private String userId;
    private String roomId;
    private String macAddress;
}