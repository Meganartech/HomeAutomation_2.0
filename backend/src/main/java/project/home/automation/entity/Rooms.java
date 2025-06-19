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
public class Rooms {
    @Id
    private String roomId;

    @Column(nullable = false)
    private String roomPath;

    @Column(nullable = false)
    private String roomName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "rooms", cascade = CascadeType.ALL)
    private List<Things> things;
}