package project.home.automation.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.home.automation.dto.OtpDTO;
import project.home.automation.dto.UserDTO;
import project.home.automation.entity.User;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

@Service
public class UserService {

    private static final String COLLECTION_NAME = "user";
    private final OtpService otpService;

    public UserService(OtpService otpService) {
        this.otpService = otpService;
    }

    // Custom user id
    public String generateUserId(DataSnapshot snapshot) {
        int maxId = 0;

        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            User user = userSnapshot.getValue(User.class);
            if (user != null && user.getUserId() != null && user.getUserId().startsWith("user")) {
                try {
                    int id = Integer.parseInt(user.getUserId().replace("user", ""));
                    maxId = Math.max(maxId, id);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return String.format("user%03d", maxId + 1);
    }

    // Post register
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
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", resultMessage[0]));
            } else {
                return ResponseEntity.ok(Collections.singletonMap("message", resultMessage[0]));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Registration failed: " + e.getMessage()));
        }
    }

    // Post login
    public ResponseEntity<?> postLogin(UserDTO loginRequest) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            CountDownLatch latch = new CountDownLatch(1);
            final String[] resultMessage = new String[1];
            final boolean[] loginSuccess = {false};
            final User[] loggedInUser = new User[1];

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User existingUser = userSnapshot.getValue(User.class);
                        if (existingUser != null &&
                                loginRequest.getEmail().equalsIgnoreCase(existingUser.getEmail()) &&
                                loginRequest.getPassword().equals(existingUser.getPassword())) {

                            loginSuccess[0] = true;
                            loggedInUser[0] = existingUser;
                            break;
                        }
                    }

                    if (loginSuccess[0]) {
                        resultMessage[0] = "Login successful";
                    } else {
                        resultMessage[0] = "Invalid email or password";
                    }
                    latch.countDown();
                }

                public void onCancelled(DatabaseError error) {
                    resultMessage[0] = "Error: " + error.getMessage();
                    latch.countDown();
                }
            });

            latch.await();

            if (loginSuccess[0]) {
                return ResponseEntity.ok(Collections.singletonMap("message", resultMessage[0]));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", resultMessage[0]));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Login failed: " + e.getMessage()));
        }
    }

    // Post email
    public ResponseEntity<?> postEmail(OtpDTO emailRequest) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            CountDownLatch latch = new CountDownLatch(1);
            final String[] resultMessage = new String[1];
            final boolean[] emailExists = {false};

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User existingUser = userSnapshot.getValue(User.class);
                        if (existingUser != null && emailRequest.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
                            emailExists[0] = true;

                            // Send OTP
                            otpService.sendOtp(emailRequest.getEmail());
                            resultMessage[0] = "OTP sent to email";
                            break;
                        }
                    }

                    if (!emailExists[0]) {
                        resultMessage[0] = "Email not found";
                    }
                    latch.countDown();
                }

                public void onCancelled(DatabaseError error) {
                    resultMessage[0] = "Error: " + error.getMessage();
                    latch.countDown();
                }
            });

            latch.await();

            if (emailExists[0]) {
                return ResponseEntity.ok(Collections.singletonMap("message", resultMessage[0]));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", resultMessage[0]));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "OTP request failed: " + e.getMessage()));
        }
    }

    // Post OTP
    public ResponseEntity<?> postOtp(OtpDTO otpRequest) {
        try {
            boolean isValid = otpService.isOtpValid(otpRequest.getEmail(), otpRequest.getOtp());
            System.out.println("Received OTP request: email=" + otpRequest.getEmail() + ", otp=" + otpRequest.getOtp());

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Invalid or expired OTP"));
            }
            return ResponseEntity.ok(Collections.singletonMap("message", "OTP verified successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error verifying OTP: " + e.getMessage()));
        }
    }

    // Put password
    public ResponseEntity<?> putPassword(OtpDTO request) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            CountDownLatch latch = new CountDownLatch(1);
            final String[] resultMessage = new String[1];
            final boolean[] userFound = {false};
            System.out.println(request.getEmail());

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User existingUser = userSnapshot.getValue(User.class);
                        if (existingUser != null && request.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
                            userFound[0] = true;
                            // Update password
                            userSnapshot.getRef().child("password").setValueAsync(request.getNewPassword());
                            resultMessage[0] = "Password updated successfully in Realtime DB";
                            break;
                        }
                    }

                    if (!userFound[0]) {
                        resultMessage[0] = "User not found with the provided email.";
                    }

                    latch.countDown();
                }

                public void onCancelled(DatabaseError error) {
                    resultMessage[0] = "Error: " + error.getMessage();
                    latch.countDown();
                }
            });

            latch.await();

            if (userFound[0]) {
                return ResponseEntity.ok(Collections.singletonMap("message", resultMessage[0]));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", resultMessage[0]));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to update password: " + e.getMessage()));
        }
    }


}