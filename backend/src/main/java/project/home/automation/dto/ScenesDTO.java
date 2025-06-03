package project.home.automation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScenesDTO {
    private String scenesId;
    private String scenesName;
    private String fromTime;
    private String toTime;
    private List<String> days;
    private String command;
    private String deviceId;
    private String roomId;
    private String roomName;
}