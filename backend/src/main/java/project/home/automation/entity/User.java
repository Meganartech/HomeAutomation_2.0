package project.home.automation.entity;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String userId;
    private String name;
    private String email;
    private String mobileNumber;
    private String password;
}