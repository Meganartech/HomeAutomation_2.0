package project.home.automation.service;

import com.google.firebase.database.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.home.automation.dto.UserDTO;
import project.home.automation.entity.User;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

@Service
public class UserService {

    private static final String COLLECTION_NAME = "user";

    // Generate custom sequential user ID like user001, user002, etc.
    public String generateUserId(DataSnapshot snapshot) {
        int maxId = 0;

        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            User user = userSnapshot.getValue(User.class);
            if (user != null && user.getUserId() != null && user.getUserId().startsWith("user")) {
                try {
                    int idNum = Integer.parseInt(user.getUserId().replace("user", ""));
                    maxId = Math.max(maxId, idNum);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return String.format("user%03d", maxId + 1);
    }

    // Register user with unique email check
    public ResponseEntity<?> postRegister(UserDTO registerRequest) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            CountDownLatch latch = new CountDownLatch(1);
            final String[] resultMessage = new String[1];
            final boolean[] emailExists = {false};
            final String[] newUserId = new String[1];

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    // Check if email already exists
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User existingUser = userSnapshot.getValue(User.class);
                        if (existingUser != null && registerRequest.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
                            emailExists[0] = true;
                            break;
                        }
                    }

                    if (emailExists[0]) {
                        resultMessage[0] = "Email already exists";
                        latch.countDown();
                        return;
                    }

                    // Generate sequential ID and save user
                    newUserId[0] = generateUserId(snapshot);

                    User user = new User();
                    user.setUserId(newUserId[0]);
                    user.setName(registerRequest.getName());
                    user.setMobileNumber(registerRequest.getMobileNumber());
                    user.setEmail(registerRequest.getEmail());
                    user.setPassword(registerRequest.getPassword());

                    ref.child(newUserId[0]).setValueAsync(user);
                    resultMessage[0] = "Registered successfully";
                    latch.countDown();
                }

                public void onCancelled(DatabaseError error) {
                    resultMessage[0] = "Error: " + error.getMessage();
                    latch.countDown();
                }
            });

            latch.await();

            if (emailExists[0]) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Collections.singletonMap("error", resultMessage[0]));
            } else {
                return ResponseEntity.ok(Collections.singletonMap("message", resultMessage[0]));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Registration failed: " + e.getMessage()));
        }
    }
}