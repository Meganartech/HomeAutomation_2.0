package project.home.automation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.home.automation.dto.*;
import project.home.automation.entity.Role;
import project.home.automation.entity.Room;
import project.home.automation.entity.Thing;
import project.home.automation.entity.User;
import project.home.automation.repository.RoleRepository;
import project.home.automation.repository.RoomRepository;
import project.home.automation.repository.ThingRepository;
import project.home.automation.repository.UserRepository;
import project.home.automation.security.JwtUtil;

import java.util.*;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;

// your own imports
import project.home.automation.entity.User;
import project.home.automation.entity.Role;

@Service
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoomRepository roomRepository;
    private final ThingRepository thingRepository;
    private final OtpService otpService;

    public UserService(AuthenticationManager authenticationManager, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, UserRepository userRepository, RoleRepository roleRepository, RoomRepository roomRepository, ThingRepository thingRepository, OtpService otpService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.roomRepository = roomRepository;
        this.thingRepository = thingRepository;
        this.otpService = otpService;
    }

    @Value("${role.user}")
    private String roleUser;

    @Value("${openhab.token}")
    private String openHABToken;

    // Customized user id
    public String generateUserId() {
        // Get the last user
        User lastUser = userRepository.findTopByOrderByUserIdDesc();
        int nextId = 1;
        if (lastUser != null) {
            String lastId = lastUser.getUserId();
            String numberPart = lastId.replace("user", "");
            nextId = Integer.parseInt(numberPart) + 1;
        }
        return String.format("user%03d", nextId);
    }

    // Post register
    public ResponseEntity<?> postRegister(UserDTO registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", "Username already exist"));
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", "Email already exist"));
        }
        User newUsers = new User();
        newUsers.setUserId(generateUserId());
        newUsers.setName(registerRequest.getName());
        newUsers.setEmail(registerRequest.getEmail());
        newUsers.setUsername(registerRequest.getUsername());
        newUsers.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        // Assign roles
        Set<Role> rolesObj = new HashSet<>();
        for (String roleNames : registerRequest.getRoles()) {
            Role roleObj = roleRepository.findByRoleName(roleNames).orElseThrow(() -> new RuntimeException("Role not found: " + roleNames));
            rolesObj.add(roleObj);
        }
        newUsers.setRoles(rolesObj);
        userRepository.save(newUsers);
        return ResponseEntity.ok(Collections.singletonMap("message", "Registered successfully"));
    }

    // Post login
    public ResponseEntity<?> postLogin(UserDTO loginRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            // Generate token
            String token = jwtUtil.generateToken(loginRequest.getUsername());

            // Fetch user to get role
            Optional<User> user = userRepository.findByUsername(loginRequest.getUsername());
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "User not found"));
            }

            // Extract role names
            Set<Role> roles = user.get().getRoles();
            String roleName = roles.stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.joining(",")); // "USER", or "ADMIN,USER" if multiple roles

            // Prepare response
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("role", roleName);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong: " + e.getMessage()));
        }
    }

    // Get profile
    public ResponseEntity<?> getProfile(String token) {
        // Checking token is null and start with bearer
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }
        // Remove "Bearer " prefix
        String jwtToken = token.substring(7);
        try {
            // Checking jwt token
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }
            // Extract and checking role
            Set<String> roles = jwtUtil.extractRoles(jwtToken);
            if (!roles.contains(roleUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
            }
            // Extract username
            String username = jwtUtil.extractUsername(jwtToken);
            Optional<User> userData = userRepository.findByUsername(username);
            if (userData.isPresent()) {
                // Return actual users object
                return ResponseEntity.ok(userData.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Profile not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong" + e.getMessage()));
        }
    }

    // Put update
    public ResponseEntity<?> putUpdate(String token, UserDTO registerRequest) {
        // Checking token is null and start with bearer
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }
        // Remove "Bearer " prefix
        String jwtToken = token.substring(7);
        try {
            // Checking jwt token
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }
            // Extract and checking role
            Set<String> roles = jwtUtil.extractRoles(jwtToken);
            if (!roles.contains(roleUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
            }
            // Extract username
            String username = jwtUtil.extractUsername(jwtToken);
            Optional<User> userData = userRepository.findByUsername(username);
            if (userData.isPresent()) {
                User usersObj = userData.get();
                if (registerRequest.getName() != null && !registerRequest.getName().isEmpty()) {
                    usersObj.setName(registerRequest.getName());
                }
                if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()) {
                    usersObj.setEmail(registerRequest.getEmail());
                }
                userRepository.save(usersObj);
                return ResponseEntity.ok(Collections.singletonMap("message", "Profile updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong" + e.getMessage()));
        }
    }

    // Post room
    public ResponseEntity<?> postRoom(String token, RoomDTO roomRequest) {
        // Checking token is null and start with bearer
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }
        // Remove "Bearer " prefix
        String jwtToken = token.substring(7);
        // Extract and checking role
        Set<String> roles = jwtUtil.extractRoles(jwtToken);
        if (!roles.contains(roleUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
        }
        try {
            // Checking jwt token
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }
            // Extract username
            String username = jwtUtil.extractUsername(jwtToken);
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            if (roomRepository.findByRoomNameAndUser(roomRequest.getRoomName(), user).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", "Room already exist"));
            }
            Room room = new Room();
            room.setRoomName(roomRequest.getRoomName());
            room.setUser(user);
            roomRepository.save(room);
            return ResponseEntity.ok(Collections.singletonMap("message", "Room added successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong: " + e.getMessage()));
        }
    }

    // Get room
    public ResponseEntity<?> getRoomList(String token) {
        // Checking token is null and start with bearer
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }
        // Remove "Bearer " prefix
        String jwtToken = token.substring(7);
        try {
            // Checking jwt token
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }
            // Extract and checking role
            Set<String> roles = jwtUtil.extractRoles(jwtToken);
            if (!roles.contains(roleUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
            }
            // Extract username
            String username = jwtUtil.extractUsername(jwtToken);
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            List<Room> room = roomRepository.findByUser(user);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong: " + e.getMessage()));
        }
    }

    // Post thing
    public ResponseEntity<?> postThing(String token, ThingDTO thingRequest) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }

        String jwtToken = token.substring(7);

        try {
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }

            Set<String> roles = jwtUtil.extractRoles(jwtToken);
            if (!roles.contains(roleUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
            }

            String username = jwtUtil.extractUsername(jwtToken);
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

            if (thingRepository.findByThingUID(thingRequest.getThingUID()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", "Device already exists"));
            }

            Thing thing = new Thing();
            thing.setLabel(thingRequest.getLabel());
            thing.setThingTypeUID(thingRequest.getThingTypeUID());
            thing.setUser(user);

            Room room = roomRepository.findByRoomNameAndUser(thingRequest.getRoomName(), user)
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            thing.setRoom(room);

            // Sanitize username and label for UID
            String sanitizedUsername = user.getUsername().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
            String sanitizedLabel = thing.getLabel().toLowerCase().replaceAll("[^a-z0-9_-]", "_");

            String bridgeUID = null;
            String generatedUID = null;

            // Use Jackson ObjectMapper to build JSON
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();

            switch (thing.getThingTypeUID()) {
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

                    // Add configuration
                    ObjectNode configNode = mapper.createObjectNode();
                    configNode.put("hostname", thingRequest.getHost());
                    configNode.put("timeout", 5000);
                    configNode.put("refreshInterval", 60000);
                    rootNode.set("configuration", configNode);
                    break;

                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Unsupported thing type"));
            }

            thing.setThingUID(generatedUID);

            rootNode.put("UID", generatedUID);
            rootNode.put("label", thing.getLabel());
            rootNode.put("thingTypeUID", thing.getThingTypeUID());

            // Add empty channels array
            ArrayNode channelsNode = mapper.createArrayNode();
            rootNode.set("channels", channelsNode);

            // Ensure configuration exists for other types as well
            if (!rootNode.has("configuration")) {
                rootNode.set("configuration", mapper.createObjectNode());
            }

            String jsonPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

            String openHABUrl = "http://localhost:8080/rest/things";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openHABToken);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(openHABUrl, request, String.class);
                if (response.getStatusCode() == HttpStatus.CREATED) {
                    thingRepository.save(thing);
                    return ResponseEntity.ok(Collections.singletonMap("message", "Device added successfully"));
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Collections.singletonMap("error", "Failed to add OpenHAB: " + response.getBody()));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "Error adding thing to OpenHAB: " + e.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Something went wrong: " + e.getMessage()));
        }
    }

    // Get thing
    public ResponseEntity<?> getThing(String token) {
        // Checking token is null and starts with bearer
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }
        // Remove "Bearer " prefix
        String jwtToken = token.substring(7);
        try {
            // Checking jwt token
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }
            // Extract username from token
            String username = jwtUtil.extractUsername(jwtToken);
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

            // Step 1: Retrieve Devices from OpenHAB (via REST API)
            String openHABUrl = "http://localhost:8080/rest/things";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openHABToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(openHABUrl, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error fetching devices from OpenHAB"));
            }

            // Step 2: Parse OpenHAB Response (Assuming it's in JSON format)
            JSONArray thingsFromOpenHAB = new JSONArray(response.getBody());  // Assuming OpenHAB returns a JSON array of things
            List<Map<String, Object>> devicesToReturn = new ArrayList<>();

            for (int i = 0; i < thingsFromOpenHAB.length(); i++) {
                JSONObject thing = thingsFromOpenHAB.getJSONObject(i);
                String thingUID = thing.getString("UID");

                // Step 3: Check if device exists in the database for the current user
                Optional<Thing> thingFromDb = thingRepository.findByThingUIDAndUser(thingUID, user);

                if (thingFromDb.isPresent()) {
                    // Add to the list of matched devices as a Map
                    Thing dbThing = thingFromDb.get();

                    Map<String, Object> deviceDetails = new HashMap<>();
                    deviceDetails.put("thingUID", dbThing.getThingUID());
                    deviceDetails.put("label", dbThing.getLabel());
                    deviceDetails.put("thingTypeUID", dbThing.getThingTypeUID());
                    deviceDetails.put("roomName", dbThing.getRoom().getRoomName());

                    devicesToReturn.add(deviceDetails);
                }
            }
            return ResponseEntity.ok(devicesToReturn);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong: " + e.getMessage()));
        }
    }

    // Post Scan
    public ResponseEntity<?> postScan(String token) {
        // Checking token is null and start with bearer
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }
        // Remove "Bearer " prefix
        String jwtToken = token.substring(7);
        try {
            // Checking jwt token
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }
            // Extract and checking role
            Set<String> roles = jwtUtil.extractRoles(jwtToken);
            if (!roles.contains(roleUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openHABToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            String openHabUrl = "http://localhost:8080/rest/discovery/bindings/network/scan";

            ResponseEntity<String> response = restTemplate.postForEntity(openHabUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(Collections.singletonMap("message", "Scanned successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Collections.singletonMap("error", "Failed to trigger scan: " + response.getBody()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong: " + e.getMessage()));
        }
    }

    // Get inbox
    public ResponseEntity<?> getInbox(String token) {
        // Checking token is null and start with bearer
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Authorization header missing"));
        }
        // Remove "Bearer " prefix
        String jwtToken = token.substring(7);
        try {
            // Checking jwt token
            if (!jwtUtil.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Invalid or expired token"));
            }
            // Extract and checking role
            Set<String> roles = jwtUtil.extractRoles(jwtToken);
            if (!roles.contains(roleUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "Access denied"));
            }

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

                    // Step 1: Check representationProperty first
                    if (inboxEntry.has("representationProperty")) {
                        String representationProperty = inboxEntry.optString("representationProperty");
                        if (properties != null && properties.has(representationProperty)) {
                            host = properties.optString(representationProperty);
                        } else if (configuration != null && configuration.has(representationProperty)) {
                            host = configuration.optString(representationProperty);
                        }
                    }

                    // Step 2: If still N/A, fallback checks
//                    if ("N/A".equals(host)) {
//                        if (properties != null) {
//                            if (properties.has("host")) {
//                                host = properties.optString("host");
//                            } else if (properties.has("hostname")) {
//                                host = properties.optString("hostname");
//                            }
//                        }
//                        if ("N/A".equals(host) && configuration != null) {
//                            if (configuration.has("host")) {
//                                host = configuration.optString("host");
//                            } else if (configuration.has("hostname")) {
//                                host = configuration.optString("hostname");
//                            }
//                        }
//                    }
                    device.put("host", host);
                    formattedInbox.add(device);
                }
                return ResponseEntity.ok(formattedInbox);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Collections.singletonMap("error", "Failed to fetch inbox items"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Something went wrong: " + e.getMessage()));
        }
    }

    // Post otp
    public ResponseEntity<?> postOtp(OtpDTO request) {
        try {
            User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("Email not registered"));
            otpService.sendOtp(request.getEmail());
            return ResponseEntity.ok(Collections.singletonMap("message", "OTP sent to your email"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to send OTP: " + e.getMessage()));
        }
    }

    // Post otp
    public ResponseEntity<?> verifyOtp(OtpDTO request) {
        try {
            boolean isValid = otpService.isOtpValid(request.getEmail(), request.getOtp());
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Invalid or expired OTP"));
            }
            return ResponseEntity.ok(Collections.singletonMap("message", "OTP verified successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error verifying OTP: " + e.getMessage()));
        }
    }

    // Post reset password
    public ResponseEntity<?> resetPassword(ResetPasswordDTO request) {
        try {
            User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            return ResponseEntity.ok(Collections.singletonMap("message", "Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to reset password: " + e.getMessage()));
        }
    }


}