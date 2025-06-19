//package project.home.automation.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.google.firebase.database.*;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;
//import project.home.automation.dto.*;
//import project.home.automation.entity.Rooms;
//import project.home.automation.entity.User;
//import project.home.automation.security.JwtUtil;
//
//import java.util.*;
//import java.util.concurrent.CountDownLatch;
//
//@Service
//public class UserService {
//
//    private static final String COLLECTION_NAME1 = "user";
//    private static final String COLLECTION_NAME2 = "room";
//    private final JwtUtil jwtUtil;
//    private final PasswordEncoder passwordEncoder;
//    private final MailService otpService;
//
//    @Value("${openhab.token}")
//    private String openHABToken;
//
//    public UserService(JwtUtil jwtUtil, MailService otpService, PasswordEncoder passwordEncoder) {
//        this.jwtUtil = jwtUtil;
//        this.otpService = otpService;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    private ResponseEntity<?> ok(String message) {
//        return ResponseEntity.ok(Collections.singletonMap("message", message));
//    }
//
//    private ResponseEntity<?> error(String message) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", message));
//    }
//
//    private ResponseEntity<?> conflict(String message) {
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", message));
//    }
//
//    private ResponseEntity<?> forbidden() {
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
//    }
//
//    private ResponseEntity<?> notFound(String message) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", message));
//    }
//
//    private ResponseEntity<?> unauthorized(String message) {
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", message));
//    }
//
//    private DataSnapshot getSnapshotSync(DatabaseReference ref) throws InterruptedException {
//        CountDownLatch latch = new CountDownLatch(1);
//        final DataSnapshot[] snapshotHolder = new DataSnapshot[1];
//        final boolean[] errorOccurred = {false};
//
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            public void onDataChange(DataSnapshot snapshot) {
//                snapshotHolder[0] = snapshot;
//                latch.countDown();
//            }
//
//            public void onCancelled(DatabaseError error) {
//                errorOccurred[0] = true;
//                latch.countDown();
//            }
//        });
//
//        latch.await();
//        return errorOccurred[0] ? null : snapshotHolder[0];
//    }
//
//    private User findUserByEmail(DataSnapshot snapshot, String email) {
//        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
//            User user = userSnapshot.getValue(User.class);
//            if (user != null && user.getEmail().equalsIgnoreCase(email)) {
//                return user;
//            }
//        }
//        return null;
//    }
//
//    private String generateUserId(DataSnapshot snapshot) {
//        int maxId = 0;
//        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
//            User user = userSnapshot.getValue(User.class);
//            if (user != null && user.getUserId() != null && user.getUserId().startsWith("user")) {
//                try {
//                    int id = Integer.parseInt(user.getUserId().replace("user", ""));
//                    maxId = Math.max(maxId, id);
//                } catch (NumberFormatException ignored) {
//                }
//            }
//        }
//        return String.format("user%03d", maxId + 1);
//    }
//
//    private String generateRoomId(DataSnapshot snapshot) {
//        int maxId = 0;
//        for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
//            Rooms roomRef = roomSnapshot.getValue(Rooms.class);
//            if (roomRef != null && roomRef.getRoomId() != null && roomRef.getRoomId().startsWith("room")) {
//                try {
//                    int id = Integer.parseInt(roomRef.getRoomId().replace("room", ""));
//                    maxId = Math.max(maxId, id);
//                } catch (NumberFormatException ignored) {
//                }
//            }
//        }
//        return String.format("room%03d", maxId + 1);
//    }
//
//
//    public ResponseEntity<?> postRegister(UserDTO registerRequest) {
//        try {
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(ref);
//            if (snapshot == null) return error("Firebase error during registration");
//
//            if (findUserByEmail(snapshot, registerRequest.getEmail()) != null) {
//                return conflict("Email already exist");
//            }
//
//            String newUserId = generateUserId(snapshot);
//            User newUser = new User();
//            newUser.setUserId(newUserId);
//            newUser.setName(registerRequest.getName());
//            newUser.setMobileNumber(registerRequest.getMobileNumber());
//            newUser.setEmail(registerRequest.getEmail());
//            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
//
//            ref.child(newUserId).setValueAsync(newUser);
//            return ok("Registered successfully");
//
//        } catch (Exception e) {
//            return error("Registration failed: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> postLogin(UserDTO loginRequest) {
//        try {
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(ref);
//            if (snapshot == null) return error("Firebase error during login");
//
//            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
//                User user = userSnapshot.getValue(User.class);
//                if (user != null && user.getEmail().equalsIgnoreCase(loginRequest.getEmail()) && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
//
//                    String token = jwtUtil.generateToken(user.getEmail());
//                    Map<String, String> response = new HashMap<>();
//                    response.put("token", token);
//                    response.put("role", user.getRole());
//                    return ResponseEntity.ok(response);
//                }
//            }
//
//            return unauthorized("Invalid email or password");
//
//        } catch (Exception e) {
//            return error("Login failed: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> postEmail(MailDTO emailRequest) {
//        try {
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(ref);
//            if (snapshot == null) return error("Firebase error while sending OTP");
//
//            User user = findUserByEmail(snapshot, emailRequest.getEmail());
//            if (user == null) return notFound("Email not found");
//
//            otpService.sendOtp(emailRequest.getEmail());
//            return ok("OTP sent to email");
//
//        } catch (Exception e) {
//            return error("OTP request failed: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> postOtp(MailDTO otpRequest) {
//        try {
//            boolean isValid = otpService.isOtpValid(otpRequest.getEmail(), otpRequest.getOtp());
//            if (!isValid) {
//                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid or expired OTP"));
//            }
//            return ok("OTP verified successfully");
//        } catch (Exception e) {
//            return error("Error verifying OTP: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> putPassword(MailDTO request) {
//        try {
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(ref);
//            if (snapshot == null) return error("Firebase error while updating password");
//
//            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
//                User user = userSnapshot.getValue(User.class);
//                if (user != null && user.getEmail().equalsIgnoreCase(request.getEmail())) {
//                    userSnapshot.getRef().child("password").setValueAsync(passwordEncoder.encode(request.getNewPassword()));
//                    return ok("Password updated successfully in Realtime DB");
//                }
//            }
//
//            return notFound("User not found with the provided email.");
//
//        } catch (Exception e) {
//            return error("Failed to update password: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> changePassword(String token, ChangePasswordDTO updateRequest) {
//        try {
//            if (token == null || !token.startsWith("Bearer ")) {
//                return unauthorized("Missing token or bearer");
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return forbidden();
//            }
//
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1).child(updateRequest.getUserId());
//            DataSnapshot snapshot = getSnapshotSync(ref);
//            if (snapshot == null || !snapshot.exists()) {
//                return notFound("User not found with the provided ID.");
//            }
//
//            User user = snapshot.getValue(User.class);
//            if (user == null) {
//                return error("Failed to parse user data.");
//            }
//
//            if (!passwordEncoder.matches(updateRequest.getCurrentPassword(), user.getPassword())) {
//                return unauthorized("Current password is incorrect.");
//            }
//
//            ref.child("password").setValueAsync(passwordEncoder.encode(updateRequest.getNewPassword()));
//            return ok("Password updated successfully.");
//
//        } catch (Exception e) {
//            return error("Failed to update password: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> getProfile(String token) {
//        try {
//            if (token == null || !token.startsWith("Bearer ")) {
//                return unauthorized("Missing token or bearer");
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return forbidden();
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(ref);
//            if (snapshot == null) return error("Firebase error while fetching profile");
//
//            User user = findUserByEmail(snapshot, email);
//            if (user == null) return notFound("User not found");
//
//            return ResponseEntity.ok(user);
//
//        } catch (Exception e) {
//            return error("Failed to fetch profile: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> postPasswordAndGetOtp(String token, MailDTO otpRequest) {
//        try {
//            if (token == null || !token.startsWith("Bearer ")) {
//                return unauthorized("Missing token or bearer");
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return forbidden();
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(ref);
//            if (snapshot == null) return error("Firebase error while verifying password");
//
//            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
//                User user = userSnapshot.getValue(User.class);
//                if (user != null && user.getEmail().equalsIgnoreCase(email)) {
//
//                    if (otpRequest.getPassword() == null || !passwordEncoder.matches(otpRequest.getPassword(), user.getPassword())) {
//                        return unauthorized("Incorrect password");
//                    }
//
//                    otpService.sendOtp(user.getEmail());
//                    return ok("OTP sent to your registered email");
//                }
//            }
//
//            return notFound("User not found");
//        } catch (Exception e) {
//            return error("Failed to verify password and send OTP: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> putUpdateProfile(String token, UserDTO updateRequest) {
//        try {
//            if (token == null || !token.startsWith("Bearer ")) {
//                return unauthorized("Missing token or bearer");
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return forbidden();
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(ref);
//            if (snapshot == null) return error("Firebase error while updating profile");
//
//            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
//                User user = userSnapshot.getValue(User.class);
//                if (user != null && user.getEmail().equalsIgnoreCase(email)) {
//
//                    if (updateRequest.getName() != null) {
//                        userSnapshot.getRef().child("name").setValueAsync(updateRequest.getName());
//                    }
//                    if (updateRequest.getMobileNumber() != null) {
//                        userSnapshot.getRef().child("mobileNumber").setValueAsync(updateRequest.getMobileNumber());
//                    }
//                    if (updateRequest.getEmail() != null) {
//                        userSnapshot.getRef().child("email").setValueAsync(updateRequest.getEmail());
//                    }
//                    return ok("Profile updated successfully");
//                }
//            }
//
//            return notFound("User not found");
//
//        } catch (Exception e) {
//            return error("Failed to update profile: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> postRoom(String token, RoomsDTO registerRequest) {
//        try {
//            if (token == null || !token.startsWith("Bearer ")) {
//                return unauthorized("Missing token or bearer");
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return forbidden();
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(userRef);
//            if (snapshot == null) return error("Firebase error while accessing user");
//
//            User user = findUserByEmail(snapshot, email);
//            if (user == null) return notFound("User not found");
//
//            String userId = user.getUserId();
//
//            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
//            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
//
//            int userRoomCount = 0;
//            if (roomSnapshot != null) {
//                for (DataSnapshot snap : roomSnapshot.getChildren()) {
//                    Rooms existingRoom = snap.getValue(Rooms.class);
//                    if (existingRoom != null && existingRoom.getUserId().equals(userId)) {
//                        userRoomCount++;
//                        if (existingRoom.getRoomName().equalsIgnoreCase(registerRequest.getRoomName())) {
//                            return conflict("Rooms already exists for this user");
//                        }
//                    }
//                }
//
//                if (userRoomCount >= 5) {
//                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Maximum 5 rooms allowed per user"));
//                }
//            }
//
//            assert roomSnapshot != null;
//            String newRoomId = generateRoomId(roomSnapshot);
//            Rooms obj = new Rooms();
//            obj.setRoomId(newRoomId);
//            obj.setRoomName(registerRequest.getRoomName());
//            obj.setUserId(userId);
//
//            roomRef.child(newRoomId).setValueAsync(obj);
//
//            return ok("Rooms added successfully");
//
//        } catch (Exception e) {
//            return error("Something went wrong: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> getRooms(String token) {
//        try {
//            if (token == null || !token.startsWith("Bearer ")) {
//                return unauthorized("Missing token or bearer");
//            }
//
//            String jwtToken = token.substring(7);
//
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return forbidden();
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot userSnapshot = getSnapshotSync(userRef);
//            if (userSnapshot == null) return error("Firebase error while accessing user");
//
//            User user = findUserByEmail(userSnapshot, email);
//            if (user == null) return notFound("User not found");
//
//            String userId = user.getUserId();
//
//            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
//            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
//            if (roomSnapshot == null) return error("Firebase error while accessing room");
//
//            List<Rooms> userRooms = new ArrayList<>();
//            for (DataSnapshot snap : roomSnapshot.getChildren()) {
//                Rooms room = snap.getValue(Rooms.class);
//                if (room != null && room.getUserId().equals(userId)) {
//                    userRooms.add(room);
//                }
//            }
//
//            return ResponseEntity.ok(userRooms);
//
//        } catch (Exception e) {
//            return error("Something went wrong: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> deleteRoom(String token, String roomId) {
//        try {
//            if (token == null || !token.startsWith("Bearer ")) {
//                return unauthorized("Missing token or bearer");
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return forbidden();
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            // Get user by email
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot userSnapshot = getSnapshotSync(userRef);
//            if (userSnapshot == null) return error("Firebase error while accessing user");
//
//            User user = findUserByEmail(userSnapshot, email);
//            if (user == null) return notFound("User not found");
//
//            String userId = user.getUserId();
//
//            // Fetch room data
//            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
//            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
//            if (roomSnapshot == null) return error("Firebase error while accessing rooms");
//
//            boolean roomFound = false;
//
//            for (DataSnapshot snap : roomSnapshot.getChildren()) {
//                Rooms room = snap.getValue(Rooms.class);
//                if (room != null && room.getRoomId().equals(roomId) && room.getUserId().equals(userId)) {
//                    // User owns the room, delete it
//                    roomRef.child(roomId).removeValueAsync();
//                    roomFound = true;
//                    break;
//                }
//            }
//
//            if (!roomFound) {
//                return notFound("Rooms not found or does not belong to user");
//            }
//
//            return ok("Rooms deleted successfully");
//
//        } catch (Exception e) {
//            return error("Something went wrong: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> postScan(String token, String binding) {
//        try {
//            if (token == null || !token.startsWith("Bearer ")) {
//                return unauthorized("Missing token or bearer");
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return forbidden();
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(userRef);
//            if (snapshot == null) return error("Firebase error while accessing user");
//
//            User user = findUserByEmail(snapshot, email);
//            if (user == null) return notFound("User not found");
//
//            // Step 3: Validate binding input
//            if (binding == null || binding.trim().isEmpty()) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Binding name is required"));
//            }
//
//            String openHabUrl = "http://localhost:8080/rest/discovery/bindings/" + binding + "/scan";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setBearerAuth(openHABToken); // Ensure this token is valid for OpenHAB
//
//            HttpEntity<String> request = new HttpEntity<>(headers);
//            RestTemplate restTemplate = new RestTemplate();
//            ResponseEntity<String> response = restTemplate.postForEntity(openHabUrl, request, String.class);
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                return ResponseEntity.ok(Collections.singletonMap("message", "Scan triggered successfully for binding: " + binding));
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Collections.singletonMap("error", "Failed to trigger scan: " + response.getBody()));
//            }
//
//        } catch (Exception e) {
//            return error("Something went wrong: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> getInbox(String token) {
//        try {
//            if (token == null || !token.startsWith("Bearer ")) {
//                return unauthorized("Missing token or bearer");
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return forbidden();
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(userRef);
//            if (snapshot == null) return error("Firebase error while accessing user");
//
//            User user = findUserByEmail(snapshot, email);
//            if (user == null) return notFound("User not found");
//
//            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(openHABToken);
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//            String url = "http://localhost:8080/rest/inbox";
//
//            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//
//            if (response.getStatusCode() == HttpStatus.OK) {
//                JSONArray inboxArray = new JSONArray(response.getBody());
//                List<Map<String, Object>> formattedInbox = new ArrayList<>();
//
//                for (int i = 0; i < inboxArray.length(); i++) {
//                    JSONObject inboxEntry = inboxArray.getJSONObject(i);
//                    JSONObject thing = inboxEntry.optJSONObject("thing");
//
//                    Map<String, Object> device = new HashMap<>();
//                    device.put("label", inboxEntry.optString("label", "N/A"));
//                    device.put("thingTypeUID", inboxEntry.optString("thingTypeUID", "N/A"));
//                    device.put("thingUID", thing != null ? thing.optString("UID", "N/A") : "N/A");
//
//                    JSONObject properties = inboxEntry.optJSONObject("properties");
//                    JSONObject configuration = inboxEntry.optJSONObject("configuration");
//
//                    String host = "N/A";
//                    if (inboxEntry.has("representationProperty")) {
//                        String representationProperty = inboxEntry.optString("representationProperty");
//                        if (properties != null && properties.has(representationProperty)) {
//                            host = properties.optString(representationProperty);
//                        } else if (configuration != null && configuration.has(representationProperty)) {
//                            host = configuration.optString(representationProperty);
//                        }
//                    }
//
//                    device.put("host", host);
//                    formattedInbox.add(device);
//                }
//
//                return ResponseEntity.ok(formattedInbox);
//
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Collections.singletonMap("error", "Failed to fetch inbox items"));
//            }
//
//        } catch (Exception e) {
//            return error("Something went wrong: " + e.getMessage());
//        }
//    }
//
//    public ResponseEntity<?> getThingsWithItems(String token) {
//        try {
//            // Step 1: JWT Validation
//            if (token == null || !token.startsWith("Bearer ")) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(Collections.singletonMap("error", "Authorization header missing"));
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(Collections.singletonMap("error", "Invalid or expired token"));
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            // Step 2: Firebase user lookup
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot userSnapshot = getSnapshotSync(userRef);
//            if (userSnapshot == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(Collections.singletonMap("error", "Firebase error while accessing user"));
//            }
//
//            User user = findUserByEmail(userSnapshot, email);
//            if (user == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(Collections.singletonMap("error", "User not found"));
//            }
//
//            // Step 3: Get user's rooms and devices
//            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
//            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
//            if (roomSnapshot == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(Collections.singletonMap("error", "Firebase error while accessing rooms"));
//            }
//
//            // Step 4: Fetch all OpenHAB items once
//            String openHABItemsUrl = "http://localhost:8080/rest/items";
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(openHABToken);
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//            RestTemplate restTemplate = new RestTemplate();
//
//            ResponseEntity<String> openHABResponse = restTemplate.exchange(openHABItemsUrl, HttpMethod.GET, entity, String.class);
//            if (!openHABResponse.getStatusCode().is2xxSuccessful()) {
//                return ResponseEntity.status(openHABResponse.getStatusCode())
//                        .body(Collections.singletonMap("error", "Failed to fetch items from OpenHAB"));
//            }
//
//            JSONArray allItems = new JSONArray(openHABResponse.getBody());
//
//            // Step 5: Collect device and item info
//            List<Map<String, Object>> thingsList = new ArrayList<>();
//
//            for (DataSnapshot roomNode : roomSnapshot.getChildren()) {
//                String ownerId = roomNode.child("userId").getValue(String.class);
//                String roomId = roomNode.child("roomId").getValue(String.class);
//                String roomName = roomNode.child("roomName").getValue(String.class);
//
//                if (user.getUserId().equals(ownerId)) {
//                    DataSnapshot devicesSnapshot = roomNode.child("devices");
//
//                    for (DataSnapshot deviceNode : devicesSnapshot.getChildren()) {
//                        String thingUID = deviceNode.child("thingUID").getValue(String.class);
//
//                        // Step 6: Fetch thing details from OpenHAB
//                        String thingUrl = "http://localhost:8080/rest/things/" + thingUID;
//                        ResponseEntity<String> thingResponse;
//                        try {
//                            thingResponse = restTemplate.exchange(thingUrl, HttpMethod.GET, entity, String.class);
//                        } catch (RestClientException e) {
//                            continue; // Skip problematic devices
//                        }
//
//                        if (!thingResponse.getStatusCode().is2xxSuccessful()) {
//                            continue;
//                        }
//
//                        JSONObject thingJson = new JSONObject(thingResponse.getBody());
//                        JSONArray channels = thingJson.optJSONArray("channels");
//
//                        List<String> channelUIDs = new ArrayList<>();
//                        if (channels != null) {
//                            for (int i = 0; i < channels.length(); i++) {
//                                JSONObject channel = channels.getJSONObject(i);
//                                channelUIDs.add(channel.getString("uid"));
//                            }
//                        }
//
//                        // Step 7: Match items belonging to this thing
//                        String normalizedUID = thingUID.replaceAll("[:\\-]", "_");
//                        List<Map<String, Object>> deviceItems = new ArrayList<>();
//                        for (int i = 0; i < allItems.length(); i++) {
//                            JSONObject item = allItems.getJSONObject(i);
//                            String itemName = item.getString("name");
//
//                            if (itemName.contains(normalizedUID)) {
//                                Map<String, Object> itemMap = new HashMap<>();
//                                itemMap.put("name", itemName);
//                                itemMap.put("label", item.optString("label", ""));
//                                itemMap.put("state", item.optString("state", ""));
//                                itemMap.put("type", item.optString("type", ""));
//                                deviceItems.add(itemMap);
//                            }
//                        }
//
//                        // Step 8: Build device info with items
//                        Map<String, Object> thingInfo = new HashMap<>();
//                        thingInfo.put("thingUID", thingUID);
//                        thingInfo.put("thingTypeUID", thingJson.optString("thingTypeUID"));
//                        thingInfo.put("label", thingJson.optString("label"));
//                        thingInfo.put("bridgeUID", thingJson.optString("bridgeUID"));
//                        thingInfo.put("configuration", thingJson.optJSONObject("configuration").toMap());
//                        thingInfo.put("channels", channelUIDs);
//                        thingInfo.put("roomId", roomId);
//                        thingInfo.put("roomName", roomName);
//                        thingInfo.put("items", deviceItems);
//
//                        thingsList.add(thingInfo);
//                    }
//                }
//            }
//
//            return ResponseEntity.ok(Collections.singletonMap("things", thingsList));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "Error: " + e.getMessage()));
//        }
//    }
//
//
//    public ResponseEntity<?> postThing(String token, ThingsDTO thingRequest) {
//        if (token == null || !token.startsWith("Bearer ")) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
//        }
//
//        String jwtToken = token.substring(7);
//
//        try {
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            // Get user from Firebase
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot userSnapshot = getSnapshotSync(userRef);
//            if (userSnapshot == null) return error("Firebase error while accessing user");
//
//            User user = findUserByEmail(userSnapshot, email);
//            if (user == null) return notFound("User not found");
//
//            // Fetch roomId based on roomName
//            if (thingRequest.getRoomName() != null && !thingRequest.getRoomName().isEmpty()) {
//                DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
//                DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
//                if (roomSnapshot == null) return error("Firebase error while accessing room");
//
//                String matchedRoomId = null;
//                for (DataSnapshot roomNode : roomSnapshot.getChildren()) {
//                    String roomName = roomNode.child("roomName").getValue(String.class);
//                    String ownerId = roomNode.child("userId").getValue(String.class);
//
//                    if (thingRequest.getRoomName().equalsIgnoreCase(roomName) &&
//                            user.getUserId().equals(ownerId)) {
//                        matchedRoomId = roomNode.child("roomId").getValue(String.class);
//                        break;
//                    }
//                }
//
//                if (matchedRoomId == null) {
//                    return notFound("Rooms not found for given name and user");
//                }
//
//                // Set roomId using the matched roomId
//                thingRequest.setRoomId(matchedRoomId);
//            } else {
//                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "roomName is required"));
//            }
//
//            // Validate roomId after roomName lookup
//            if (thingRequest.getRoomId() == null || thingRequest.getRoomId().isEmpty()) {
//                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "roomId is required"));
//            }
//
//            // Get room by roomId
//            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2).child(thingRequest.getRoomId());
//            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
//            if (roomSnapshot == null || !roomSnapshot.exists()) {
//                return notFound("Rooms not found");
//            }
//
//            String roomOwnerId = roomSnapshot.child("userId").getValue(String.class);
//            if (!user.getUserId().equals(roomOwnerId)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "User does not own the room"));
//            }
//
//            // UID generation
//            String sanitizedUsername = user.getUserId().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
//            String sanitizedLabel = thingRequest.getLabel().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
//
//            String generatedUID;
//            String bridgeUID = null;
//            ObjectMapper mapper = new ObjectMapper();
//            ObjectNode rootNode = mapper.createObjectNode();
//
//            switch (thingRequest.getThingTypeUID()) {
//                case "mqtt:topic":
//                    bridgeUID = "mqtt:broker:mybroker";
//                    generatedUID = "mqtt:topic:mybroker:" + sanitizedUsername + "_" + sanitizedLabel;
//                    rootNode.put("bridgeUID", bridgeUID);
//                    break;
//
//                case "zwave:device":
//                    bridgeUID = "zwave:controller:zwave_network";
//                    generatedUID = "zwave:device:zwave_network:" + sanitizedUsername + "_" + sanitizedLabel;
//                    rootNode.put("bridgeUID", bridgeUID);
//                    break;
//
//                case "knx:device":
//                    bridgeUID = "knx:bridge:myknx";
//                    generatedUID = "knx:device:myknx:" + sanitizedUsername + "_" + sanitizedLabel;
//                    rootNode.put("bridgeUID", bridgeUID);
//                    break;
//
//                case "network:pingdevice":
//                    if (thingRequest.getHost() == null || thingRequest.getHost().isEmpty()) {
//                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Host is required for network devices"));
//                    }
//                    generatedUID = "network:pingdevice:" + sanitizedUsername + "_" + sanitizedLabel;
//                    ObjectNode pingConfig = mapper.createObjectNode();
//                    pingConfig.put("hostname", thingRequest.getHost());
//                    pingConfig.put("timeout", 5000);
//                    pingConfig.put("refreshInterval", 60000);
//                    rootNode.set("configuration", pingConfig);
//                    break;
//
//                case "wiz:color-bulb":
//                    if (thingRequest.getHost() == null || thingRequest.getHost().isEmpty()) {
//                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Host is required for WiZ devices"));
//                    }
//                    if (thingRequest.getMacAddress() == null || thingRequest.getMacAddress().isEmpty()) {
//                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "macAddress is required for WiZ devices"));
//                    }
//                    generatedUID = "wiz:color-bulb:" + sanitizedUsername + "_" + sanitizedLabel;
//                    ObjectNode wizConfig = mapper.createObjectNode();
//                    wizConfig.put("ipAddress", thingRequest.getHost());
//                    wizConfig.put("pollingInterval", 60);
//                    wizConfig.put("macAddress", thingRequest.getMacAddress()); // Add macAddress here
//                    rootNode.set("configuration", wizConfig);
//                    break;
//
//                default:
//                    return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Unsupported thing type"));
//            }
//
//            // Compose OpenHAB JSON
//            rootNode.put("UID", generatedUID);
//            rootNode.put("label", thingRequest.getLabel());
//            rootNode.put("thingTypeUID", thingRequest.getThingTypeUID());
//
//            ArrayNode channelsNode = mapper.createArrayNode();
//            rootNode.set("channels", channelsNode);
//            if (!rootNode.has("configuration")) {
//                rootNode.set("configuration", mapper.createObjectNode());
//            }
//
//            // Send to OpenHAB
//            String openHABUrl = "http://localhost:8080/rest/things";
//            String jsonPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setBearerAuth(openHABToken);
//            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
//
//            RestTemplate restTemplate = new RestTemplate();
//            ResponseEntity<String> openHABResponse = restTemplate.postForEntity(openHABUrl, request, String.class);
//
//            if (openHABResponse.getStatusCode() == HttpStatus.CREATED) {
//                // Store device under room/{roomId}/devices/{thingUID}
//                DatabaseReference deviceRef = FirebaseDatabase.getInstance()
//                        .getReference("room")
//                        .child(thingRequest.getRoomId())
//                        .child("devices")
//                        .child(generatedUID);
//
//                Map<String, Object> deviceMap = new HashMap<>();
//                deviceMap.put("thingUID", generatedUID);
//                deviceMap.put("thingTypeUID", thingRequest.getThingTypeUID());
//                deviceMap.put("label", thingRequest.getLabel());
//                deviceMap.put("host", thingRequest.getHost());
//                deviceMap.put("userId", user.getUserId());
//                deviceMap.put("roomId", thingRequest.getRoomId());
//
//                deviceRef.setValueAsync(deviceMap);
//
//                return ResponseEntity.ok(Collections.singletonMap("message", "Things added successfully to OpenHAB and Firebase"));
//            } else {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(Collections.singletonMap("error", "Failed to add device to OpenHAB"));
//            }
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "Error: " + e.getMessage()));
//        }
//    }
//
//
//    public ResponseEntity<?> autoCreateAndLinkItems(String token, String thingUID) {
//        try {
//            // Step 1: JWT Validation
//            if (token == null || !token.startsWith("Bearer ")) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(Collections.singletonMap("error", "Missing token or bearer"));
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid token"));
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            // Firebase user lookup (assuming your methods exist)
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(userRef);
//            if (snapshot == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "Firebase error while accessing user"));
//
//            User user = findUserByEmail(snapshot, email);
//            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Collections.singletonMap("error", "User not found"));
//
//            // Step 2: Fetch Thing details (including channels)
//            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(openHABToken);
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//            String url = "http://localhost:8080/rest/things/" + thingUID;
//
//            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//            if (!response.getStatusCode().is2xxSuccessful()) {
//                return ResponseEntity.status(response.getStatusCode()).body(Collections.singletonMap("error", "Failed to fetch thing info"));
//            }
//
//            JSONObject thingJson = new JSONObject(response.getBody());
//            JSONArray channelsArray = thingJson.getJSONArray("channels");
//
//            List<String> linkedItems = new ArrayList<>();
//
//            for (int i = 0; i < channelsArray.length(); i++) {
//                JSONObject channel = channelsArray.getJSONObject(i);
//                String channelUID = channel.getString("uid");           // e.g., wiz:color-bulb:user001:color
//                String channelId = channel.getString("id");             // e.g., color
//                String itemType = channel.optString("itemType", null);  // e.g., Color
//
//                if (itemType == null || itemType.isEmpty()) continue; // skip if no itemType
//
//                // Step 3: Generate itemName - replace ":" and "-" with "_"
//                String itemName = (thingUID + "_" + channelId).replaceAll("[:\\-]", "_");
//
//                // Step 4: Create Item via POST /rest/items/{itemName}
//                JSONObject itemJson = new JSONObject();
//                itemJson.put("type", itemType);
//                itemJson.put("label", "Auto " + itemName);
//                itemJson.put("category", "Light");
//                itemJson.put("tags", new JSONArray());
//                itemJson.put("groupNames", new JSONArray());
//
//                HttpHeaders itemHeaders = new HttpHeaders();
//                itemHeaders.setBearerAuth(openHABToken);
//                itemHeaders.setContentType(MediaType.APPLICATION_JSON);
//
//                HttpEntity<String> itemEntity = new HttpEntity<>(itemJson.toString(), itemHeaders);
//
//                String itemUrl = "http://localhost:8080/rest/items/" + itemName;
//
//                restTemplate.exchange(itemUrl, HttpMethod.POST, itemEntity, String.class);
//
//                // Step 5: Link Channel to Item via PUT /rest/links/{itemName}/{channelUID}
//                HttpHeaders linkHeaders = new HttpHeaders();
//                linkHeaders.setBearerAuth(openHABToken);
//                linkHeaders.setContentType(MediaType.TEXT_PLAIN);
//
//                HttpEntity<String> linkEntity = new HttpEntity<>("", linkHeaders);
//
//                String linkUrl = "http://localhost:8080/rest/links/" + itemName + "/" + channelUID;
//
//                restTemplate.exchange(linkUrl, HttpMethod.PUT, linkEntity, String.class);
//
//                linkedItems.add(itemName);
//            }
//
//            return ResponseEntity.ok(Collections.singletonMap("linkedItems", linkedItems));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "Something went wrong: " + e.getMessage()));
//        }
//    }


//    public ResponseEntity<?> autoCreateAndLinkItems(String token, String thingUID) {
//        try {
//            // Step 1: JWT Validation
//            if (token == null || !token.startsWith("Bearer ")) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(Collections.singletonMap("error", "Missing token or bearer"));
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(Collections.singletonMap("error", "Invalid token"));
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            // Step 2: Firebase user lookup
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot snapshot = getSnapshotSync(userRef);
//            if (snapshot == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(Collections.singletonMap("error", "Firebase error while accessing user"));
//            }
//
//            User user = findUserByEmail(snapshot, email);
//            if (user == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(Collections.singletonMap("error", "User not found"));
//            }
//
//            // Step 3: Fetch Thing details (including channels) from OpenHAB
//            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(openHABToken);
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//            String url = "http://localhost:8080/rest/things/" + thingUID;
//
//            ResponseEntity<String> response;
//            try {
//                response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//            } catch (RestClientException e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(Collections.singletonMap("error", "Failed to fetch thing info: " + e.getMessage()));
//            }
//
//            if (!response.getStatusCode().is2xxSuccessful()) {
//                return ResponseEntity.status(response.getStatusCode())
//                        .body(Collections.singletonMap("error", "Failed to fetch thing info: " + response.getStatusCode()));
//            }
//
//            JSONObject thingJson = new JSONObject(response.getBody());
//            JSONArray channelsArray = thingJson.getJSONArray("channels");
//
//            List<String> linkedItems = new ArrayList<>();
//
//            // Step 4: Process each channel
//            for (int i = 0; i < channelsArray.length(); i++) {
//                JSONObject channel = channelsArray.getJSONObject(i);
//                String channelUID = channel.getString("uid");           // e.g., wiz:color-bulb:user001:color
//                String channelId = channel.getString("id");             // e.g., color
//                String itemType = channel.optString("itemType", null);  // e.g., Color
//
//                if (itemType == null || itemType.isEmpty()) {
//                    continue; // Skip if no itemType
//                }
//
//                // Step 5: Generate itemName - replace ":" and "-" with "_"
//                String itemName = (thingUID + "_" + channelId).replaceAll("[:\\-]", "_");
//
//                // Step 6: Create Item via PUT /rest/items/{itemName}
//                JSONObject itemJson = new JSONObject();
//                itemJson.put("type", itemType);
//                itemJson.put("name", itemName); // Explicitly include the name
//                itemJson.put("label", "Auto " + itemName);
//                itemJson.put("category", "Light");
//                itemJson.put("tags", new JSONArray());
//                itemJson.put("groupNames", new JSONArray());
//
//                HttpHeaders itemHeaders = new HttpHeaders();
//                itemHeaders.setBearerAuth(openHABToken);
//                itemHeaders.setContentType(MediaType.APPLICATION_JSON);
//
//                HttpEntity<String> itemEntity = new HttpEntity<>(itemJson.toString(), itemHeaders);
//
//                String itemUrl = "http://localhost:8080/rest/items/" + itemName;
//
//                ResponseEntity<String> itemResponse;
//                try {
//                    itemResponse = restTemplate.exchange(itemUrl, HttpMethod.PUT, itemEntity, String.class);
//                } catch (RestClientException e) {
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .body(Collections.singletonMap("error", "Failed to create item: " + e.getMessage()));
//                }
//
//                if (!itemResponse.getStatusCode().is2xxSuccessful()) {
//                    return ResponseEntity.status(itemResponse.getStatusCode())
//                            .body(Collections.singletonMap("error", "Failed to create item: " + itemResponse.getStatusCode() + " - " + itemResponse.getBody()));
//                }
//
//                // Step 7: Link Channel to Item via PUT /rest/links/{itemName}/{channelUID}
//                HttpHeaders linkHeaders = new HttpHeaders();
//                linkHeaders.setBearerAuth(openHABToken);
//                // Do NOT set Content-Type since the body is empty
//
//                HttpEntity<String> linkEntity = new HttpEntity<>(linkHeaders); // Empty body, no Content-Type
//
//                String linkUrl = "http://localhost:8080/rest/links/wiz_color_bulb_user003/" + channelUID;
//
//                ResponseEntity<String> linkResponse;
//                try {
//                    linkResponse = restTemplate.exchange(linkUrl, HttpMethod.PUT, linkEntity, String.class);
//                } catch (RestClientException e) {
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .body(Collections.singletonMap("error", "Failed to link item to channel: " + e.getMessage()));
//                }
//
//                if (!linkResponse.getStatusCode().is2xxSuccessful()) {
//                    return ResponseEntity.status(linkResponse.getStatusCode())
//                            .body(Collections.singletonMap("error", "Failed to link item to channel: " + linkResponse.getStatusCode() + " - " + linkResponse.getBody()));
//                }
//
//                linkedItems.add(itemName);
//            }
//
//            return ResponseEntity.ok(Collections.singletonMap("linkedItems", linkedItems));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "Something went wrong: " + e.getMessage()));
//        }
//    }


//    public ResponseEntity<?> getThings(String token) {
//        try {
//            // Step 1: JWT Validation
//            if (token == null || !token.startsWith("Bearer ")) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            // Step 2: Firebase user lookup
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot userSnapshot = getSnapshotSync(userRef);
//            if (userSnapshot == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Firebase error while accessing user"));
//            }
//
//            User user = findUserByEmail(userSnapshot, email);
//            if (user == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "User not found"));
//            }
//
//            // Step 3: Get rooms by userId
//            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
//            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
//            if (roomSnapshot == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Firebase error while accessing rooms"));
//            }
//
//            List<Map<String, Object>> thingsList = new ArrayList<>();
//            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(openHABToken);
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//
//            // Step 4: Iterate through rooms to find user's devices
//            for (DataSnapshot roomNode : roomSnapshot.getChildren()) {
//                String ownerId = roomNode.child("userId").getValue(String.class);
//                String roomId = roomNode.child("roomId").getValue(String.class);
//                String roomName = roomNode.child("roomName").getValue(String.class);
//
//                if (user.getUserId().equals(ownerId)) {
//                    DataSnapshot devicesSnapshot = roomNode.child("devices");
//                    for (DataSnapshot deviceNode : devicesSnapshot.getChildren()) {
//                        String thingUID = deviceNode.child("thingUID").getValue(String.class);
//
//                        // Step 5: Fetch from OpenHAB
//                        String thingUrl = "http://localhost:8080/rest/things/" + thingUID;
//                        ResponseEntity<String> thingResponse;
//
//                        try {
//                            thingResponse = restTemplate.exchange(thingUrl, HttpMethod.GET, entity, String.class);
//                        } catch (RestClientException e) {
//                            continue; // Skip problematic devices
//                        }
//
//                        if (!thingResponse.getStatusCode().is2xxSuccessful()) {
//                            continue;
//                        }
//
//                        JSONObject thingJson = new JSONObject(thingResponse.getBody());
//                        JSONArray channels = thingJson.optJSONArray("channels");
//
//                        List<String> channelUIDs = new ArrayList<>();
//                        if (channels != null) {
//                            for (int i = 0; i < channels.length(); i++) {
//                                JSONObject channel = channels.getJSONObject(i);
//                                channelUIDs.add(channel.getString("uid"));
//                            }
//                        }
//
//                        Map<String, Object> thingInfo = new HashMap<>();
//                        thingInfo.put("thingUID", thingUID);
//                        thingInfo.put("thingTypeUID", thingJson.optString("thingTypeUID"));
//                        thingInfo.put("label", thingJson.optString("label"));
//                        thingInfo.put("bridgeUID", thingJson.optString("bridgeUID"));
//                        thingInfo.put("configuration", thingJson.optJSONObject("configuration").toMap());
//                        thingInfo.put("channels", channelUIDs);
//                        thingInfo.put("roomId", roomId);
//                        thingInfo.put("roomName", roomName);
//
//                        thingsList.add(thingInfo);
//                    }
//                }
//            }
//
//            return ResponseEntity.ok(Collections.singletonMap("things", thingsList));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error: " + e.getMessage()));
//        }
//    }
//
//
//    public ResponseEntity<?> addDeviceAndLinkItems(String token, ThingsDTO thingRequest) {
//        try {
//            // Step 1: JWT Validation
//            if (token == null || !token.startsWith("Bearer ")) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            // Step 2: Firebase user lookup
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot userSnapshot = getSnapshotSync(userRef);
//            if (userSnapshot == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Firebase error while accessing user"));
//            }
//
//            User user = findUserByEmail(userSnapshot, email);
//            if (user == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "User not found"));
//            }
//
//            // Step 3: Fetch roomId based on roomName
//            if (thingRequest.getRoomName() == null || thingRequest.getRoomName().isEmpty()) {
//                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "roomName is required"));
//            }
//
//            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
//            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
//            if (roomSnapshot == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Firebase error while accessing room"));
//            }
//
//            String matchedRoomId = null;
//            for (DataSnapshot roomNode : roomSnapshot.getChildren()) {
//                String roomName = roomNode.child("roomName").getValue(String.class);
//                String ownerId = roomNode.child("userId").getValue(String.class);
//
//                if (thingRequest.getRoomName().equalsIgnoreCase(roomName) && user.getUserId().equals(ownerId)) {
//                    matchedRoomId = roomNode.child("roomId").getValue(String.class);
//                    break;
//                }
//            }
//
//            if (matchedRoomId == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Rooms not found for given name and user"));
//            }
//
//            thingRequest.setRoomId(matchedRoomId);
//
//            // Step 4: Validate roomId
//            if (thingRequest.getRoomId() == null || thingRequest.getRoomId().isEmpty()) {
//                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "roomId is required"));
//            }
//
//            // Step 5: Get room by roomId
//            roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2).child(thingRequest.getRoomId());
//            roomSnapshot = getSnapshotSync(roomRef);
//            if (roomSnapshot == null || !roomSnapshot.exists()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Rooms not found"));
//            }
//
//            String roomOwnerId = roomSnapshot.child("userId").getValue(String.class);
//            if (!user.getUserId().equals(roomOwnerId)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "User does not own the room"));
//            }
//
//            // Step 6: UID generation
//            String sanitizedUsername = user.getUserId().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
//            String sanitizedLabel = thingRequest.getLabel().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
//
//            String generatedUID;
//            String bridgeUID = null;
//            ObjectMapper mapper = new ObjectMapper();
//            ObjectNode rootNode = mapper.createObjectNode();
//
//            switch (thingRequest.getThingTypeUID()) {
//                case "mqtt:topic":
//                    bridgeUID = "mqtt:broker:mybroker";
//                    generatedUID = "mqtt:topic:mybroker:" + sanitizedUsername + "_" + sanitizedLabel;
//                    rootNode.put("bridgeUID", bridgeUID);
//                    break;
//
//                case "zwave:device":
//                    bridgeUID = "zwave:controller:zwave_network";
//                    generatedUID = "zwave:device:zwave_network:" + sanitizedUsername + "_" + sanitizedLabel;
//                    rootNode.put("bridgeUID", bridgeUID);
//                    break;
//
//                case "knx:device":
//                    bridgeUID = "knx:bridge:myknx";
//                    generatedUID = "knx:device:myknx:" + sanitizedUsername + "_" + sanitizedLabel;
//                    rootNode.put("bridgeUID", bridgeUID);
//                    break;
//
//                case "network:pingdevice":
//                    if (thingRequest.getHost() == null || thingRequest.getHost().isEmpty()) {
//                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Host is required for network devices"));
//                    }
//                    generatedUID = "network:pingdevice:" + sanitizedUsername + "_" + sanitizedLabel;
//                    ObjectNode pingConfig = mapper.createObjectNode();
//                    pingConfig.put("hostname", thingRequest.getHost());
//                    pingConfig.put("timeout", 5000);
//                    pingConfig.put("refreshInterval", 60000);
//                    rootNode.set("configuration", pingConfig);
//                    break;
//
//                case "wiz:color-bulb":
//                    if (thingRequest.getHost() == null || thingRequest.getHost().isEmpty()) {
//                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Host is required for WiZ devices"));
//                    }
//                    if (thingRequest.getMacAddress() == null || thingRequest.getMacAddress().isEmpty()) {
//                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "macAddress is required for WiZ devices"));
//                    }
//                    generatedUID = "wiz:color-bulb:" + sanitizedUsername + "_" + sanitizedLabel;
//                    ObjectNode wizConfig = mapper.createObjectNode();
//                    wizConfig.put("ipAddress", thingRequest.getHost());
//                    wizConfig.put("pollingInterval", 60);
//                    wizConfig.put("macAddress", thingRequest.getMacAddress());
//                    rootNode.set("configuration", wizConfig);
//                    break;
//
//                default:
//                    return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Unsupported thing type"));
//            }
//
//            // Step 7: Compose OpenHAB JSON
//            rootNode.put("UID", generatedUID);
//            rootNode.put("label", thingRequest.getLabel());
//            rootNode.put("thingTypeUID", thingRequest.getThingTypeUID());
//
//            ArrayNode channelsNode = mapper.createArrayNode();
//            rootNode.set("channels", channelsNode);
//            if (!rootNode.has("configuration")) {
//                rootNode.set("configuration", mapper.createObjectNode());
//            }
//
//            // Step 8: Send to OpenHAB
//            String openHABUrl = "http://localhost:8080/rest/things";
//            String jsonPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setBearerAuth(openHABToken);
//            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
//
//            RestTemplate restTemplate = new RestTemplate();
//            ResponseEntity<String> openHABResponse = restTemplate.postForEntity(openHABUrl, request, String.class);
//
//            if (openHABResponse.getStatusCode() != HttpStatus.CREATED) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to add device to OpenHAB"));
//            }
//
//            // Step 9: Store device in Firebase
//            DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference("room").child(thingRequest.getRoomId()).child("devices").child(generatedUID);
//
//            Map<String, Object> deviceMap = new HashMap<>();
//            deviceMap.put("thingUID", generatedUID);
//            deviceMap.put("thingTypeUID", thingRequest.getThingTypeUID());
//            deviceMap.put("label", thingRequest.getLabel());
//            deviceMap.put("host", thingRequest.getHost());
//            deviceMap.put("userId", user.getUserId());
//            deviceMap.put("roomId", thingRequest.getRoomId());
//
//            deviceRef.setValueAsync(deviceMap);
//
//            // Step 10: Fetch Thing details (including channels) from OpenHAB
//            headers = new HttpHeaders();
//            headers.setBearerAuth(openHABToken);
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//            String thingUrl = "http://localhost:8080/rest/things/" + generatedUID;
//
//            ResponseEntity<String> thingResponse;
//            try {
//                thingResponse = restTemplate.exchange(thingUrl, HttpMethod.GET, entity, String.class);
//            } catch (RestClientException e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to fetch thing info: " + e.getMessage()));
//            }
//
//            if (!thingResponse.getStatusCode().is2xxSuccessful()) {
//                return ResponseEntity.status(thingResponse.getStatusCode()).body(Collections.singletonMap("error", "Failed to fetch thing info: " + thingResponse.getStatusCode()));
//            }
//
//            JSONObject thingJson = new JSONObject(thingResponse.getBody());
//            JSONArray channelsArray = thingJson.getJSONArray("channels");
//
//            List<String> linkedItems = new ArrayList<>();
//
//            // Step 11: Process each channel
//            for (int i = 0; i < channelsArray.length(); i++) {
//                JSONObject channel = channelsArray.getJSONObject(i);
//                String channelUID = channel.getString("uid");
//                String channelId = channel.getString("id");
//                String itemType = channel.optString("itemType", null);
//
//                if (itemType == null || itemType.isEmpty()) {
//                    continue; // Skip if no itemType
//                }
//
//                // Step 12: Generate itemName
//                String itemName = (generatedUID + "_" + channelId).replaceAll("[:\\-]", "_");
//
//                // Step 13: Create Item via PUT /rest/items/{itemName}
//                JSONObject itemJson = new JSONObject();
//                itemJson.put("type", itemType);
//                itemJson.put("name", itemName);
//                itemJson.put("label", "Auto " + itemName);
//                itemJson.put("category", "Light");
//                itemJson.put("tags", new JSONArray());
//                itemJson.put("groupNames", new JSONArray());
//
//                HttpHeaders itemHeaders = new HttpHeaders();
//                itemHeaders.setBearerAuth(openHABToken);
//                itemHeaders.setContentType(MediaType.APPLICATION_JSON);
//
//                HttpEntity<String> itemEntity = new HttpEntity<>(itemJson.toString(), itemHeaders);
//
//                String itemUrl = "http://localhost:8080/rest/items/" + itemName;
//
//                ResponseEntity<String> itemResponse;
//                try {
//                    itemResponse = restTemplate.exchange(itemUrl, HttpMethod.PUT, itemEntity, String.class);
//                } catch (RestClientException e) {
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to create item: " + e.getMessage()));
//                }
//
//                if (!itemResponse.getStatusCode().is2xxSuccessful()) {
//                    return ResponseEntity.status(itemResponse.getStatusCode()).body(Collections.singletonMap("error", "Failed to create item: " + itemResponse.getStatusCode() + " - " + itemResponse.getBody()));
//                }
//
//                // Step 14: Link Channel to Item via PUT /rest/links/{itemName}/{channelUID}
//                HttpHeaders linkHeaders = new HttpHeaders();
//                linkHeaders.setContentType(MediaType.APPLICATION_JSON);
//                linkHeaders.setBearerAuth(openHABToken);
//
//                // Use empty string as body
//                HttpEntity<String> linkEntity = new HttpEntity<>("", linkHeaders);
//
//
//                String linkUrl = "http://localhost:8080/rest/links/" + itemName + "/" + channelUID;
//
//                ResponseEntity<String> linkResponse;
//                try {
//                    linkResponse = restTemplate.exchange(linkUrl, HttpMethod.PUT, linkEntity, String.class);
//                } catch (RestClientException e) {
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to link item to channel: " + e.getMessage()));
//                }
//
//                if (!linkResponse.getStatusCode().is2xxSuccessful()) {
//                    return ResponseEntity.status(linkResponse.getStatusCode()).body(Collections.singletonMap("error", "Failed to link item to channel: " + linkResponse.getStatusCode() + " - " + linkResponse.getBody()));
//                }
//
//                linkedItems.add(itemName);
//            }
//
//            // Step 15: Return success response
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Things added and controls created successfully");
//            response.put("thingUID", generatedUID);
//            response.put("linkedItems", linkedItems);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error: " + e.getMessage()));
//        }
//    }
//
//    public ResponseEntity<?> getItems(String token) {
//        try {
//            // 1. Validate Token
//            if (token == null || !token.startsWith("Bearer ")) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            // 2. Find Firebase user by email
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot userSnapshot = getSnapshotSync(userRef);
//            if (userSnapshot == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Firebase error while accessing user"));
//            }
//
//            User user = findUserByEmail(userSnapshot, email);
//            if (user == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "User not found"));
//            }
//
//            // 3. Get all rooms for the user
//            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
//            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
//            if (roomSnapshot == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Firebase error while accessing rooms"));
//            }
//
//            List<String> thingUIDs = new ArrayList<>();
//            for (DataSnapshot roomNode : roomSnapshot.getChildren()) {
//                String ownerId = roomNode.child("userId").getValue(String.class);
//                if (user.getUserId().equals(ownerId)) {
//                    DataSnapshot devicesNode = roomNode.child("devices");
//                    for (DataSnapshot deviceNode : devicesNode.getChildren()) {
//                        String thingUID = deviceNode.child("thingUID").getValue(String.class);
//                        if (thingUID != null) {
//                            thingUIDs.add(thingUID);
//                        }
//                    }
//                }
//            }
//
//            // 4. Fetch items from OpenHAB
//            String openHABItemsUrl = "http://localhost:8080/rest/items";
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(openHABToken);
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//            RestTemplate restTemplate = new RestTemplate();
//
//            ResponseEntity<String> openHABResponse = restTemplate.exchange(openHABItemsUrl, HttpMethod.GET, entity, String.class);
//            if (!openHABResponse.getStatusCode().is2xxSuccessful()) {
//                return ResponseEntity.status(openHABResponse.getStatusCode()).body(Collections.singletonMap("error", "Failed to fetch items from OpenHAB"));
//            }
//
//            JSONArray allItems = new JSONArray(openHABResponse.getBody());
//            List<Map<String, Object>> userItems = new ArrayList<>();
//
//            for (int i = 0; i < allItems.length(); i++) {
//                JSONObject item = allItems.getJSONObject(i);
//                String itemName = item.getString("name");
//
//                for (String uid : thingUIDs) {
//                    if (itemName.contains(uid.replaceAll("[:\\-]", "_"))) {
//                        Map<String, Object> itemMap = new HashMap<>();
//                        itemMap.put("name", itemName);
//                        itemMap.put("label", item.optString("label", ""));
//                        itemMap.put("state", item.optString("state", ""));
//                        itemMap.put("type", item.optString("type", ""));
//                        userItems.add(itemMap);
//                        break;
//                    }
//                }
//            }
//
//            return ResponseEntity.ok(Collections.singletonMap("items", userItems));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "Error fetching items: " + e.getMessage()));
//        }
//    }
//
//    public ResponseEntity<?> postControl(String token, Map<String, String> payload) {
//        try {
//            // 1. Validate Token
//            if (token == null || !token.startsWith("Bearer ")) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
//            }
//
//            String jwtToken = token.substring(7);
//            if (!jwtUtil.isTokenValid(jwtToken)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
//            }
//
//            String email = jwtUtil.extractEmail(jwtToken);
//
//            // 2. Find Firebase user by email
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
//            DataSnapshot userSnapshot = getSnapshotSync(userRef);
//            if (userSnapshot == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Firebase error while accessing user"));
//            }
//
//            User user = findUserByEmail(userSnapshot, email);
//            if (user == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "User not found"));
//            }
//
//            // 3. Extract data from request body
//            String itemName = payload.get("itemName");
//            String command = payload.get("command");
//
//            if (itemName == null || command == null) {
//                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Missing itemName or command"));
//            }
//
//            // 4. Verify user owns the item
//            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
//            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
//            if (roomSnapshot == null) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Firebase error while accessing rooms"));
//            }
//
//            boolean authorized = false;
//            for (DataSnapshot roomNode : roomSnapshot.getChildren()) {
//                String ownerId = roomNode.child("userId").getValue(String.class);
//                if (user.getUserId().equals(ownerId)) {
//                    DataSnapshot devicesNode = roomNode.child("devices");
//                    for (DataSnapshot deviceNode : devicesNode.getChildren()) {
//                        String thingUID = deviceNode.child("thingUID").getValue(String.class);
//                        if (thingUID != null && itemName.contains(thingUID.replaceAll("[:\\-]", "_"))) {
//                            authorized = true;
//                            break;
//                        }
//                    }
//                }
//                if (authorized) break;
//            }
//
//            if (!authorized) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "You are not authorized to control this item"));
//            }
//
//            // 5. Send command to OpenHAB
//            String url = "http://localhost:8080/rest/items/" + itemName;
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(openHABToken);
//            headers.setContentType(MediaType.TEXT_PLAIN);
//
//            HttpEntity<String> entity = new HttpEntity<>(command, headers);
//            RestTemplate restTemplate = new RestTemplate();
//
//            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                return ResponseEntity.ok(Collections.singletonMap("message", "Command sent successfully"));
//            } else {
//                return ResponseEntity.status(response.getStatusCode())
//                        .body(Collections.singletonMap("error", "Failed to send command to item"));
//            }
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "Error controlling item: " + e.getMessage()));
//        }
//    }

//}