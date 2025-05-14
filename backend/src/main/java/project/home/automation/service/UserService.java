package project.home.automation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.firebase.database.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.home.automation.dto.*;
import project.home.automation.entity.Room;
import project.home.automation.entity.User;
import project.home.automation.security.JwtUtil;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@Service
public class UserService {

    private static final String COLLECTION_NAME1 = "user";
    private static final String COLLECTION_NAME2 = "room";
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    @Value("${openhab.token}")
    private String openHABToken;

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

    private ResponseEntity<?> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", message));
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
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

    private String generateRoomId(DataSnapshot snapshot) {
        int maxId = 0;
        for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
            Room roomRef = roomSnapshot.getValue(Room.class);
            if (roomRef != null && roomRef.getRoomId() != null && roomRef.getRoomId().startsWith("room")) {
                try {
                    int id = Integer.parseInt(roomRef.getRoomId().replace("room", ""));
                    maxId = Math.max(maxId, id);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("room%03d", maxId + 1);
    }


    public ResponseEntity<?> postRegister(UserDTO registerRequest) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
            DataSnapshot snapshot = getSnapshotSync(ref);
            if (snapshot == null) return error("Firebase error during registration");

            if (findUserByEmail(snapshot, registerRequest.getEmail()) != null) {
                return conflict("Email already exist");
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
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
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
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
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
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
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
                return forbidden();
            }

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1).child(updateRequest.getUserId());
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
                return forbidden();
            }

            String email = jwtUtil.extractEmail(jwtToken);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
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
                return forbidden();
            }

            String email = jwtUtil.extractEmail(jwtToken);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
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
                return forbidden();
            }

            String email = jwtUtil.extractEmail(jwtToken);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
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

    public ResponseEntity<?> postRoom(String token, RoomDTO registerRequest) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return unauthorized("Missing token or bearer");
            }

            String jwtToken = token.substring(7);
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return forbidden();
            }

            String email = jwtUtil.extractEmail(jwtToken);

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
            DataSnapshot snapshot = getSnapshotSync(userRef);
            if (snapshot == null) return error("Firebase error while accessing user");

            User user = findUserByEmail(snapshot, email);
            if (user == null) return notFound("User not found");

            String userId = user.getUserId();

            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);

            int userRoomCount = 0;
            if (roomSnapshot != null) {
                for (DataSnapshot snap : roomSnapshot.getChildren()) {
                    Room existingRoom = snap.getValue(Room.class);
                    if (existingRoom != null && existingRoom.getUserId().equals(userId)) {
                        userRoomCount++;
                        if (existingRoom.getRoomName().equalsIgnoreCase(registerRequest.getRoomName())) {
                            return conflict("Room already exists for this user");
                        }
                    }
                }

                if (userRoomCount >= 5) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Maximum 5 rooms allowed per user"));
                }
            }

            assert roomSnapshot != null;
            String newRoomId = generateRoomId(roomSnapshot);
            Room obj = new Room();
            obj.setRoomId(newRoomId);
            obj.setRoomName(registerRequest.getRoomName());
            obj.setUserId(userId);

            roomRef.child(newRoomId).setValueAsync(obj);

            return ok("Room added successfully");

        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    public ResponseEntity<?> getRoom(String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return unauthorized("Missing token or bearer");
            }

            String jwtToken = token.substring(7);

            if (!jwtUtil.isTokenValid(jwtToken)) {
                return forbidden();
            }

            String email = jwtUtil.extractEmail(jwtToken);

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
            DataSnapshot userSnapshot = getSnapshotSync(userRef);
            if (userSnapshot == null) return error("Firebase error while accessing user");

            User user = findUserByEmail(userSnapshot, email);
            if (user == null) return notFound("User not found");

            String userId = user.getUserId();

            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
            if (roomSnapshot == null) return error("Firebase error while accessing room");

            List<Room> userRooms = new ArrayList<>();
            for (DataSnapshot snap : roomSnapshot.getChildren()) {
                Room room = snap.getValue(Room.class);
                if (room != null && room.getUserId().equals(userId)) {
                    userRooms.add(room);
                }
            }

            return ResponseEntity.ok(userRooms);

        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    public ResponseEntity<?> postScan(String token, String binding) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return unauthorized("Missing token or bearer");
            }

            String jwtToken = token.substring(7);
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return forbidden();
            }

            String email = jwtUtil.extractEmail(jwtToken);
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
            DataSnapshot snapshot = getSnapshotSync(userRef);
            if (snapshot == null) return error("Firebase error while accessing user");

            User user = findUserByEmail(snapshot, email);
            if (user == null) return notFound("User not found");

            // Step 3: Validate binding input
            if (binding == null || binding.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Binding name is required"));
            }

            String openHabUrl = "http://localhost:8080/rest/discovery/bindings/" + binding + "/scan";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openHABToken); // Ensure this token is valid for OpenHAB

            HttpEntity<String> request = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(openHabUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(Collections.singletonMap("message", "Scan triggered successfully for binding: " + binding));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Collections.singletonMap("error", "Failed to trigger scan: " + response.getBody()));
            }

        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    public ResponseEntity<?> getInbox(String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return unauthorized("Missing token or bearer");
            }

            String jwtToken = token.substring(7);
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return forbidden();
            }

            String email = jwtUtil.extractEmail(jwtToken);

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
            DataSnapshot snapshot = getSnapshotSync(userRef);
            if (snapshot == null) return error("Firebase error while accessing user");

            User user = findUserByEmail(snapshot, email);
            if (user == null) return notFound("User not found");

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(openHABToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = "http://localhost:8080/rest/inbox";

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONArray inboxArray = new JSONArray(response.getBody());
                List<Map<String, Object>> formattedInbox = new ArrayList<>();

                for (int i = 0; i < inboxArray.length(); i++) {
                    JSONObject inboxEntry = inboxArray.getJSONObject(i);
                    JSONObject thing = inboxEntry.optJSONObject("thing");

                    Map<String, Object> device = new HashMap<>();
                    device.put("label", inboxEntry.optString("label", "N/A"));
                    device.put("thingTypeUID", inboxEntry.optString("thingTypeUID", "N/A"));
                    device.put("thingUID", thing != null ? thing.optString("UID", "N/A") : "N/A");

                    JSONObject properties = inboxEntry.optJSONObject("properties");
                    JSONObject configuration = inboxEntry.optJSONObject("configuration");

                    String host = "N/A";
                    if (inboxEntry.has("representationProperty")) {
                        String representationProperty = inboxEntry.optString("representationProperty");
                        if (properties != null && properties.has(representationProperty)) {
                            host = properties.optString(representationProperty);
                        } else if (configuration != null && configuration.has(representationProperty)) {
                            host = configuration.optString(representationProperty);
                        }
                    }

                    device.put("host", host);
                    formattedInbox.add(device);
                }

                return ResponseEntity.ok(formattedInbox);

            } else {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Collections.singletonMap("error", "Failed to fetch inbox items"));
            }

        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    public ResponseEntity<?> postThing(String token, ThingDTO thingRequest) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }

        String jwtToken = token.substring(7);

        try {
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }

            String email = jwtUtil.extractEmail(jwtToken);

            // Get user from Firebase
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME1);
            DataSnapshot userSnapshot = getSnapshotSync(userRef);
            if (userSnapshot == null) return error("Firebase error while accessing user");

            User user = findUserByEmail(userSnapshot, email);
            if (user == null) return notFound("User not found");

            // Fetch roomId based on roomName
            if (thingRequest.getRoomName() != null && !thingRequest.getRoomName().isEmpty()) {
                DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2);
                DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
                if (roomSnapshot == null) return error("Firebase error while accessing room");

                String matchedRoomId = null;
                for (DataSnapshot roomNode : roomSnapshot.getChildren()) {
                    String roomName = roomNode.child("roomName").getValue(String.class);
                    String ownerId = roomNode.child("userId").getValue(String.class);

                    if (thingRequest.getRoomName().equalsIgnoreCase(roomName) &&
                            user.getUserId().equals(ownerId)) {
                        matchedRoomId = roomNode.child("roomId").getValue(String.class);
                        break;
                    }
                }

                if (matchedRoomId == null) {
                    return notFound("Room not found for given name and user");
                }

                // Set roomId using the matched roomId
                thingRequest.setRoomId(matchedRoomId);
            } else {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "roomName is required"));
            }

            // Validate roomId after roomName lookup
            if (thingRequest.getRoomId() == null || thingRequest.getRoomId().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "roomId is required"));
            }

            // Get room by roomId
            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME2).child(thingRequest.getRoomId());
            DataSnapshot roomSnapshot = getSnapshotSync(roomRef);
            if (roomSnapshot == null || !roomSnapshot.exists()) {
                return notFound("Room not found");
            }

            String roomOwnerId = roomSnapshot.child("userId").getValue(String.class);
            if (!user.getUserId().equals(roomOwnerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "User does not own the room"));
            }

            // UID generation
            String sanitizedUsername = user.getUserId().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
            String sanitizedLabel = thingRequest.getLabel().toLowerCase().replaceAll("[^a-z0-9_-]", "_");

            String generatedUID;
            String bridgeUID = null;
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();

            switch (thingRequest.getThingTypeUID()) {
                case "mqtt:topic":
                    bridgeUID = "mqtt:broker:mybroker";
                    generatedUID = "mqtt:topic:mybroker:" + sanitizedUsername + "_" + sanitizedLabel;
                    rootNode.put("bridgeUID", bridgeUID);
                    break;

                case "zwave:device":
                    bridgeUID = "zwave:controller:zwave_network";
                    generatedUID = "zwave:device:zwave_network:" + sanitizedUsername + "_" + sanitizedLabel;
                    rootNode.put("bridgeUID", bridgeUID);
                    break;

                case "knx:device":
                    bridgeUID = "knx:bridge:myknx";
                    generatedUID = "knx:device:myknx:" + sanitizedUsername + "_" + sanitizedLabel;
                    rootNode.put("bridgeUID", bridgeUID);
                    break;

                case "network:pingdevice":
                    if (thingRequest.getHost() == null || thingRequest.getHost().isEmpty()) {
                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Host is required for network devices"));
                    }
                    generatedUID = "network:pingdevice:" + sanitizedUsername + "_" + sanitizedLabel;
                    ObjectNode pingConfig = mapper.createObjectNode();
                    pingConfig.put("hostname", thingRequest.getHost());
                    pingConfig.put("timeout", 5000);
                    pingConfig.put("refreshInterval", 60000);
                    rootNode.set("configuration", pingConfig);
                    break;

                case "wiz:color-bulb":
                    if (thingRequest.getHost() == null || thingRequest.getHost().isEmpty()) {
                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Host is required for WiZ devices"));
                    }
                    if (thingRequest.getMacAddress() == null || thingRequest.getMacAddress().isEmpty()) {
                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "macAddress is required for WiZ devices"));
                    }
                    generatedUID = "wiz:color-bulb:" + sanitizedUsername + "_" + sanitizedLabel;
                    ObjectNode wizConfig = mapper.createObjectNode();
                    wizConfig.put("ipAddress", thingRequest.getHost());
                    wizConfig.put("pollingInterval", 60);
                    wizConfig.put("macAddress", thingRequest.getMacAddress()); // Add macAddress here
                    rootNode.set("configuration", wizConfig);
                    break;

                default:
                    return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Unsupported thing type"));
            }

            // Compose OpenHAB JSON
            rootNode.put("UID", generatedUID);
            rootNode.put("label", thingRequest.getLabel());
            rootNode.put("thingTypeUID", thingRequest.getThingTypeUID());

            ArrayNode channelsNode = mapper.createArrayNode();
            rootNode.set("channels", channelsNode);
            if (!rootNode.has("configuration")) {
                rootNode.set("configuration", mapper.createObjectNode());
            }

            // Send to OpenHAB
            String openHABUrl = "http://localhost:8080/rest/things";
            String jsonPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openHABToken);
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> openHABResponse = restTemplate.postForEntity(openHABUrl, request, String.class);

            if (openHABResponse.getStatusCode() == HttpStatus.CREATED) {
                // Store device under room/{roomId}/devices/{thingUID}
                DatabaseReference deviceRef = FirebaseDatabase.getInstance()
                        .getReference("room")
                        .child(thingRequest.getRoomId())
                        .child("devices")
                        .child(generatedUID);

                Map<String, Object> deviceMap = new HashMap<>();
                deviceMap.put("thingUID", generatedUID);
                deviceMap.put("thingTypeUID", thingRequest.getThingTypeUID());
                deviceMap.put("label", thingRequest.getLabel());
                deviceMap.put("host", thingRequest.getHost());
                deviceMap.put("userId", user.getUserId());
                deviceMap.put("roomId", thingRequest.getRoomId());

                deviceRef.setValueAsync(deviceMap);

                return ResponseEntity.ok(Collections.singletonMap("message", "Device added successfully to OpenHAB and Firebase"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "Failed to add device to OpenHAB"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error: " + e.getMessage()));
        }
    }



}