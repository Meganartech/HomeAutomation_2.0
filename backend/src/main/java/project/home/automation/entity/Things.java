package project.home.automation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Things {
    @Id
    private String thingId;

    @Column(nullable = false, unique = true)
    private String thingUID;

    @Column(nullable = false)
    private String thingTypeUID;

    @Column(nullable = false)
    private String label;

    private String host;

    private String macAddress;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "rooms_id")
    private Rooms rooms;
}