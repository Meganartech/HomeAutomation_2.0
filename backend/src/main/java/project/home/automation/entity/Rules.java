package project.home.automation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Rules {
    @Id
    private String ruleId;

    @Column(nullable = false)
    private String ruleName;

    @Column(nullable = false)
    private String fromTime;

    @Column(nullable = false)
    private String toTime;

    @ElementCollection
    private List<String> days;

    @Column(nullable = false)
    private String command;

    @Column(nullable = false)
    private String thingId;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private boolean enabled = true;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
