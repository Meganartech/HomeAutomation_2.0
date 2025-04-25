package project.home.automation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String role = "USER";
}