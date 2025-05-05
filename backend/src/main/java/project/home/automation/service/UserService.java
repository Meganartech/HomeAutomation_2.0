package project.home.automation.service;

import com.google.firebase.database.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.home.automation.dto.ChangePasswordDTO;
import project.home.automation.dto.OtpDTO;
import project.home.automation.dto.UserDTO;
import project.home.automation.entity.User;
import project.home.automation.security.JwtUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Service
public class UserService {

    private static final String COLLECTION_NAME = "user";
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public UserService(JwtUtil jwtUtil, OtpService otpService, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    private ResponseEntity<?> ok(String message) {
        return ResponseEntity.ok(Collections.singletonMap("message", message));
    }

    private ResponseEntity<?> error(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", message));
    }

    private ResponseEntity<?> conflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", "Email already exists"));
    }

    private ResponseEntity<?> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", message));
    }

    private ResponseEntity<?> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", message));
    }

    private DataSnapshot getSnapshotSync(DatabaseReference ref) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final DataSnapshot[] snapshotHolder = new DataSnapshot[1];
        final boolean[] errorOccurred = {false};

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                snapshotHolder[0] = snapshot;
                latch.countDown();
            }

            public void onCancelled(DatabaseError error) {
                errorOccurred[0] = true;
                latch.countDown();
            }
        });

        latch.await();
        return errorOccurred[0] ? null : snapshotHolder[0];
    }

    private User findUserByEmail(DataSnapshot snapshot, String email) {
        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            User user = userSnapshot.getValue(User.class);
            if (user != null && user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    private String generateUserId(DataSnapshot snapshot) {
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

    public ResponseEntity<?> postRegister(UserDTO registerRequest) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            DataSnapshot snapshot = getSnapshotSync(ref);
            if (snapshot == null) return error("Firebase error during registration");

            if (findUserByEmail(snapshot, registerRequest.getEmail()) != null) {
                return conflict();
            }

            String newUserId = generateUserId(snapshot);
            User newUser = new User();
            newUser.setUserId(newUserId);
            newUser.setName(registerRequest.getName());
            newUser.setMobileNumber(registerRequest.getMobileNumber());
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

            ref.child(newUserId).setValueAsync(newUser);
            return ok("Registered successfully");

        } catch (Exception e) {
            return error("Registration failed: " + e.getMessage());
        }
    }

    public ResponseEntity<?> postLogin(UserDTO loginRequest) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            DataSnapshot snapshot = getSnapshotSync(ref);
            if (snapshot == null) return error("Firebase error during login");

            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                User user = userSnapshot.getValue(User.class);
                if (user != null && user.getEmail().equalsIgnoreCase(loginRequest.getEmail()) &&
                        passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {

                    String token = jwtUtil.generateToken(user.getEmail());
                    Map<String, String> response = new HashMap<>();
                    response.put("token", token);
                    response.put("role", user.getRole());
                    response.put("name", user.getName());
                    return ResponseEntity.ok(response);
                }
            }

            return unauthorized("Invalid email or password");

        } catch (Exception e) {
            return error("Login failed: " + e.getMessage());
        }
    }

    public ResponseEntity<?> postEmail(OtpDTO emailRequest) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            DataSnapshot snapshot = getSnapshotSync(ref);
            if (snapshot == null) return error("Firebase error while sending OTP");

            User user = findUserByEmail(snapshot, emailRequest.getEmail());
            if (user == null) return notFound("Email not found");

            otpService.sendOtp(emailRequest.getEmail());
            return ok("OTP sent to email");

        } catch (Exception e) {
            return error("OTP request failed: " + e.getMessage());
        }
    }

    public ResponseEntity<?> postOtp(OtpDTO otpRequest) {
        try {
            boolean isValid = otpService.isOtpValid(otpRequest.getEmail(), otpRequest.getOtp());
            if (!isValid) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid or expired OTP"));
            }
            return ok("OTP verified successfully");
        } catch (Exception e) {
            return error("Error verifying OTP: " + e.getMessage());
        }
    }

    public ResponseEntity<?> putPassword(OtpDTO request) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            DataSnapshot snapshot = getSnapshotSync(ref);
            if (snapshot == null) return error("Firebase error while updating password");

            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                User user = userSnapshot.getValue(User.class);
                if (user != null && user.getEmail().equalsIgnoreCase(request.getEmail())) {
                    userSnapshot.getRef().child("password").setValueAsync(passwordEncoder.encode(request.getNewPassword()));
                    return ok("Password updated successfully in Realtime DB");
                }
            }

            return notFound("User not found with the provided email.");

        } catch (Exception e) {
            return error("Failed to update password: " + e.getMessage());
        }
    }

    public ResponseEntity<?> changePassword(String token, ChangePasswordDTO updateRequest) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return unauthorized("Missing token or bearer");
            }

            String jwtToken = token.substring(7);
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME).child(updateRequest.getUserId());
            DataSnapshot snapshot = getSnapshotSync(ref);
            if (snapshot == null || !snapshot.exists()) {
                return notFound("User not found with the provided ID.");
            }

            User user = snapshot.getValue(User.class);
            if (user == null) {
                return error("Failed to parse user data.");
            }

            if (!passwordEncoder.matches(updateRequest.getCurrentPassword(), user.getPassword())) {
                return unauthorized("Current password is incorrect.");
            }

            ref.child("password").setValueAsync(passwordEncoder.encode(updateRequest.getNewPassword()));
            return ok("Password updated successfully.");

        } catch (Exception e) {
            return error("Failed to update password: " + e.getMessage());
        }
    }

    public ResponseEntity<?> getProfile(String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return unauthorized("Missing token or bearer");
            }

            String jwtToken = token.substring(7);
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }

            String email = jwtUtil.extractEmail(jwtToken);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            DataSnapshot snapshot = getSnapshotSync(ref);
            if (snapshot == null) return error("Firebase error while fetching profile");

            User user = findUserByEmail(snapshot, email);
            if (user == null) return notFound("User not found");

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return error("Failed to fetch profile: " + e.getMessage());
        }
    }

    public ResponseEntity<?> postPasswordAndGetOtp(String token, OtpDTO otpRequest) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return unauthorized("Missing token or bearer");
            }

            String jwtToken = token.substring(7);
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }

            String email = jwtUtil.extractEmail(jwtToken);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            DataSnapshot snapshot = getSnapshotSync(ref);
            if (snapshot == null) return error("Firebase error while verifying password");

            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                User user = userSnapshot.getValue(User.class);
                if (user != null && user.getEmail().equalsIgnoreCase(email)) {

                    if (otpRequest.getPassword() == null || !passwordEncoder.matches(otpRequest.getPassword(), user.getPassword())) {
                        return unauthorized("Incorrect password");
                    }

                    otpService.sendOtp(user.getEmail());
                    return ok("OTP sent to your registered email");
                }
            }

            return notFound("User not found");
        } catch (Exception e) {
            return error("Failed to verify password and send OTP: " + e.getMessage());
        }
    }

    public ResponseEntity<?> putUpdateProfile(String token, UserDTO updateRequest) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return unauthorized("Missing token or bearer");
            }

            String jwtToken = token.substring(7);
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }

            String email = jwtUtil.extractEmail(jwtToken);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            DataSnapshot snapshot = getSnapshotSync(ref);
            if (snapshot == null) return error("Firebase error while updating profile");

            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                User user = userSnapshot.getValue(User.class);
                if (user != null && user.getEmail().equalsIgnoreCase(email)) {

                    if (updateRequest.getName() != null) {
                        userSnapshot.getRef().child("name").setValueAsync(updateRequest.getName());
                    }
                    if (updateRequest.getMobileNumber() != null) {
                        userSnapshot.getRef().child("mobileNumber").setValueAsync(updateRequest.getMobileNumber());
                    }
                    if (updateRequest.getEmail() != null) {
                        userSnapshot.getRef().child("email").setValueAsync(updateRequest.getEmail());
                    }
                    return ok("Profile updated successfully");
                }
            }

            return notFound("User not found");

        } catch (Exception e) {
            return error("Failed to update profile: " + e.getMessage());
        }
    }

}