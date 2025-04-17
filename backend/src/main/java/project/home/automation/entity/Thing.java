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
public class Thing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int thingId;

    @Column(nullable = false, unique = true)
    private String thingUID;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String thingTypeUID;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "room_id")
    private Room room;
}