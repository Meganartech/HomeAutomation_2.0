package project.home.automation.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    private String roomId;
    private String roomName;
    private String userId;
    private Map<String, Device> devices;
}