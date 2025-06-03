package project.home.automation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import project.home.automation.dto.*;
import project.home.automation.entity.Device;
import project.home.automation.entity.Room;
import project.home.automation.entity.Scenes;
import project.home.automation.entity.User;
import project.home.automation.repository.DeviceRepository;
import project.home.automation.repository.RoomRepository;
import project.home.automation.repository.ScenesRepository;
import project.home.automation.repository.UserRepository;
import project.home.automation.security.JwtUtil;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService1 {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final ScenesRepository scenesRepository;
    private final OtpService otpService;

    @Value("${openhab.token}")
    private String openHABToken;

    public UserService1(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, RoomRepository roomRepository, DeviceRepository deviceRepository, ScenesRepository scenesRepository, OtpService otpService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.scenesRepository = scenesRepository;
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

    private ResponseEntity<?> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", message));
    }

    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("error", message));
    }

    private ResponseEntity<?> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", message));
    }

    // Checking token, extract email and return user object or throw runtime exception
    private User getUserFromTokenOrThrow(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token or Bearer");
        }

        String jwtToken = token.substring(7).trim();
        if (!jwtUtil.isTokenValid(jwtToken)) {
            throw new RuntimeException("Invalid token");
        }

        String email = jwtUtil.extractEmail(jwtToken);
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

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

    // Customized room id
    public String generateRoomId() {
        // Get the last room
        Room lastRoom = roomRepository.findTopByOrderByRoomIdDesc();
        int nextId = 1;
        if (lastRoom != null) {
            String lastId = lastRoom.getRoomId();
            String numberPart = lastId.replace("room", "");
            nextId = Integer.parseInt(numberPart) + 1;
        }
        return String.format("room%03d", nextId);
    }

    // Room path
    public String roomPath(String roomName) {
        return roomName.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    // Customized device id
    public String generateDeviceId() {
        // Get the last device
        Device lastDevice = deviceRepository.findTopByOrderByDeviceIdDesc();
        int nextId = 1;
        if (lastDevice != null) {
            String lastId = lastDevice.getDeviceId();
            String numberPart = lastId.replace("device", "");
            nextId = Integer.parseInt(numberPart) + 1;
        }
        return String.format("device%03d", nextId);
    }

    // Customized scenes id
    public String generateScenesId() {
        // Get the last scenes
        Scenes lastScenes = scenesRepository.findTopByOrderByScenesIdDesc();
        int nextId = 1;
        if (lastScenes != null) {
            String lastId = lastScenes.getScenesId();
            String numberPart = lastId.replace("scenes", "");
            nextId = Integer.parseInt(numberPart) + 1;
        }
        return String.format("scenes%03d", nextId);
    }

    // Post register
    public ResponseEntity<?> postRegister(UserDTO postRequest) {
        try {
            if (userRepository.findByEmail(postRequest.getEmail()).isPresent()) {
                return conflict("Email already exist");
            } else {
                User newUser = new User();
                newUser.setUserId(generateUserId());
                newUser.setName(postRequest.getName().trim());
                newUser.setMobileNumber(postRequest.getMobileNumber().trim());
                newUser.setEmail(postRequest.getEmail().trim().toLowerCase());
                newUser.setPassword(passwordEncoder.encode(postRequest.getPassword()));
                newUser.setRole(newUser.getRole());
                userRepository.save(newUser);
                return ok("Registered successfully");
            }
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Post login
    public ResponseEntity<?> postLogin(UserDTO postRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(postRequest.getEmail(), postRequest.getPassword()));
            // Generate token
            String token = jwtUtil.generateToken(postRequest.getEmail());
            // Fetch user to get user role
            Optional<User> userData = userRepository.findByEmail(postRequest.getEmail());
            if (userData.isEmpty()) {
                return unauthorized("User not found");
            } else {
                String role = userData.get().getRole();
                // It returns as JSON response
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("role", role);
                response.put("userID", userData.get().getUserId());
                response.put("name", userData.get().getName());
                return ResponseEntity.ok(response);
            }
        } catch (BadCredentialsException e) {
            return unauthorized("Invalid username or password");
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Post email
    public ResponseEntity<?> postEmail(OtpDTO postRequest) {
        try {
            // Fetch user to check email is correct
            if (userRepository.findByEmail(postRequest.getEmail()).isEmpty()) {
                return unauthorized("Email not found");
            } else {
                otpService.sendOtp(postRequest.getEmail());
                return ok("OTP sent to your registered email");
            }
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Post otp
    public ResponseEntity<?> postOtp(OtpDTO postRequest) {
        try {
            boolean isValid = otpService.isOtpValid(postRequest.getEmail(), postRequest.getOtp());
            if (!isValid) {
                return badRequest("Invalid OTP");
            } else {
                return ok("OTP verified successfully");
            }
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Patch password update 1
    public ResponseEntity<?> patchPassword_1Update(OtpDTO updateRequest) {
        try {
            Optional<User> userData = userRepository.findByEmail(updateRequest.getEmail());
            if (userData.isEmpty()) {
                return notFound("Email not found");
            } else {
                User userObj = userData.get();
                userObj.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
                userRepository.save(userObj);
                return ok("Password updated successfully");
            }
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Get profile
    public ResponseEntity<?> getProfile(String token) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            Map<String, Object> response = new HashMap<>();
            response.put("name", userObj.getName());
            response.put("email", userObj.getEmail());
            response.put("mobileNumber", userObj.getMobileNumber());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Patch profile update
    public ResponseEntity<?> patchProfileUpdate(String token, UserDTO updateRequest) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            // Only update fields that are not null
            if (updateRequest.getName() != null) {
                userObj.setName(updateRequest.getName());
            }
            if (updateRequest.getMobileNumber() != null) {
                userObj.setMobileNumber(updateRequest.getMobileNumber());
            }
            if (updateRequest.getEmail() != null) {
                userObj.setEmail(updateRequest.getEmail().toLowerCase());
            }
            userRepository.save(userObj);
            return ok("Profile updated successfully");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Failed to update profile: " + e.getMessage());
        }
    }

    // Patch password update 2
    public ResponseEntity<?> patchPassword_2Update(String token, ChangePasswordDTO updateRequest) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            if (!passwordEncoder.matches(updateRequest.getCurrentPassword(), userObj.getPassword())) {
                return unauthorized("Current password is incorrect");
            } else {
                userObj.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
                userRepository.save(userObj);
                return ok("New password updated successfully");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Post password
    public ResponseEntity<?> postPasswordAndGetOtp(String token, OtpDTO otpRequest) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            if (otpRequest.getPassword() == null || !passwordEncoder.matches(otpRequest.getPassword(), userObj.getPassword())) {
                return unauthorized("Incorrect password");
            } else {
                otpService.sendOtp(userObj.getEmail());
                return ok("OTP sent to your registered email");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Post room
    public ResponseEntity<?> postRoom(String token, RoomDTO registerRequest) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            if (registerRequest.getRoomName() == null || registerRequest.getRoomName().trim().isEmpty()) {
                return badRequest("Room name cannot be empty");
            }
            if (roomRepository.existsByRoomNameIgnoreCaseAndUser_UserId(registerRequest.getRoomName(), userObj.getUserId())) {
                return conflict("Room already exists for this user");
            }
            int roomCount = roomRepository.countByUser_UserId(userObj.getUserId());
            if (roomCount >= 5) {
                return badRequest("Maximum 5 rooms allowed per user");
            }
            Room roomObj = new Room();
            roomObj.setRoomId(generateRoomId());
            roomObj.setRoomPath(roomPath(registerRequest.getRoomName()));
            roomObj.setRoomName(registerRequest.getRoomName());
            roomObj.setUser(userObj);
            roomRepository.save(roomObj);
            return ok("Room added successfully");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Get room
    public ResponseEntity<?> getRoom(String token) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            List<Room> roomObj = roomRepository.findByUser_UserId(userObj.getUserId());
            List<Map<String, String>> result = roomObj.stream().map(ref -> {
                Map<String, String> roomMap = new HashMap<>();
                roomMap.put("roomId", ref.getRoomId());
                roomMap.put("roomPath", ref.getRoomPath());
                roomMap.put("roomName", ref.getRoomName());
                return roomMap;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Delete room
    public ResponseEntity<?> deleteRoom(String token, String roomId) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            Optional<Room> roomData = roomRepository.findByRoomIdAndUser_UserId(roomId, userObj.getUserId());
            if (roomData.isEmpty()) {
                return notFound("Room not found");
            }
            Room roomObj = roomData.get();
            List<Device> deviceList = deviceRepository.findByRoom(roomObj);
            if (!deviceList.isEmpty()) {
                return error("Cannot delete room. Devices are still associated with it.");
            }
            roomRepository.delete(roomData.get());
            return ok("Room deleted successfully");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Post scan and get inbox
    public ResponseEntity<?> postScanAndGetInbox(String token, String binding) {
        try {
            getUserFromTokenOrThrow(token);
            if (binding == null || binding.trim().isEmpty()) {
                return badRequest("Binding name is required");
            }

            String scanUrl = "http://localhost:8080/rest/discovery/bindings/" + binding + "/scan";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openHABToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> scanResponse = restTemplate.postForEntity(scanUrl, request, String.class);

            if (!scanResponse.getStatusCode().is2xxSuccessful()) {
                return badRequest("Failed to trigger scan");
            }
            Thread.sleep(2000); // 2 seconds (adjust if needed)

            String inboxUrl = "http://localhost:8080/rest/inbox";
            headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(openHABToken);

            HttpEntity<String> inboxRequest = new HttpEntity<>(headers);

            ResponseEntity<String> inboxResponse = restTemplate.exchange(inboxUrl, HttpMethod.GET, inboxRequest, String.class);

            if (inboxResponse.getStatusCode() == HttpStatus.OK) {
                JSONArray inboxArray = new JSONArray(inboxResponse.getBody());
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
                return badRequest("Scan triggered but failed to fetch inbox items");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Post thing and link items
    public ResponseEntity<?> addThingAndLinkItems(String token, ThingDTO thingRequest) {
        try {
            User userObj = getUserFromTokenOrThrow(token);

            if (thingRequest.getRoomName() == null || thingRequest.getRoomName().isEmpty()) {
                return badRequest("Room Name is required");
            }

            Optional<Room> roomData = roomRepository.findByRoomNameAndUser(thingRequest.getRoomName(), userObj);
            if (roomData.isEmpty()) {
                return notFound("Room not found");
            }

            Room roomObj = roomData.get();
            thingRequest.setRoomId(roomObj.getRoomId());

            String sanitizedUserId = userObj.getUserId().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
            String sanitizedLabel = thingRequest.getLabel().toLowerCase().replaceAll("[^a-z0-9_-]", "_");

            String generatedUID;
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();

            switch (thingRequest.getThingTypeUID()) {
                case "mqtt:topic":
                    generatedUID = "mqtt:topic:mybroker:" + sanitizedUserId + "_" + sanitizedLabel;
                    rootNode.put("UID", generatedUID);
                    rootNode.put("label", thingRequest.getLabel());
                    rootNode.put("thingTypeUID", "mqtt:topic");
                    rootNode.put("bridgeUID", "mqtt:broker:mybroker");
                    rootNode.set("configuration", mapper.createObjectNode());
                    break;

                case "network:pingdevice":
                    if (thingRequest.getHost() == null || thingRequest.getHost().isEmpty()) {
                        return badRequest("Host is required");
                    }
                    generatedUID = "network:pingdevice:" + sanitizedUserId + "_" + sanitizedLabel;
                    rootNode.put("UID", generatedUID);
                    rootNode.put("label", thingRequest.getLabel());
                    rootNode.put("thingTypeUID", "network:pingdevice");
                    ObjectNode pingConfig = mapper.createObjectNode();
                    pingConfig.put("hostname", thingRequest.getHost());
                    pingConfig.put("timeout", 5000);
                    pingConfig.put("refreshInterval", 60000);
                    rootNode.set("configuration", pingConfig);
                    break;

                case "wiz:color-bulb":
                    if (thingRequest.getHost() == null || thingRequest.getHost().isEmpty() || thingRequest.getMacAddress() == null) {
                        return badRequest("Host and MacAddress are required for WiZ");
                    }
                    generatedUID = "wiz:color-bulb:" + sanitizedUserId + "_" + sanitizedLabel;
                    rootNode.put("UID", generatedUID);
                    rootNode.put("label", thingRequest.getLabel());
                    rootNode.put("thingTypeUID", "wiz:color-bulb");
                    ObjectNode wizConfig = mapper.createObjectNode();
                    wizConfig.put("ipAddress", thingRequest.getHost());
                    wizConfig.put("pollingInterval", 60);
                    wizConfig.put("macAddress", thingRequest.getMacAddress());
                    rootNode.set("configuration", wizConfig);
                    break;

                default:
                    return badRequest("Unsupported thing type");
            }

            rootNode.set("channels", mapper.createArrayNode());

            String openHABUrl = "http://localhost:8080/rest/things";

            RestTemplate restTemplateObj = new RestTemplate();
            HttpHeaders headersObj = new HttpHeaders();

            headersObj.setContentType(MediaType.APPLICATION_JSON);
            headersObj.setBearerAuth(openHABToken);

            HttpEntity<String> requestObj = new HttpEntity<>(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode), headersObj);
            ResponseEntity<String> responseObj = restTemplateObj.postForEntity(openHABUrl, requestObj, String.class);

            if (responseObj.getStatusCode() != HttpStatus.CREATED) {
                return error("Failed to add device to OpenHAB");
            }

            Device deviceObj = new Device();
            deviceObj.setDeviceId(generateDeviceId());
            deviceObj.setThingUID(generatedUID);
            deviceObj.setThingTypeUID(thingRequest.getThingTypeUID());
            deviceObj.setLabel(thingRequest.getLabel());
            deviceObj.setHost(thingRequest.getHost());
            deviceObj.setUser(userObj);
            deviceObj.setRoom(roomObj);
            deviceRepository.save(deviceObj);

            String thingUrl = "http://localhost:8080/rest/things/" + generatedUID;

            headersObj = new HttpHeaders();
            headersObj.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headersObj.setBearerAuth(openHABToken);

            HttpEntity<String> entity = new HttpEntity<>(headersObj);

            ResponseEntity<String> thingResponse;
            try {
                thingResponse = restTemplateObj.exchange(thingUrl, HttpMethod.GET, entity, String.class);
            } catch (RestClientException e) {
                return error("Failed to fetch thing information");
            }

            if (!thingResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(thingResponse.getStatusCode()).body(Collections.singletonMap("error", "Failed to fetch thing info: " + thingResponse.getStatusCode()));
            }

            JSONObject thingJson = new JSONObject(thingResponse.getBody());
            JSONArray channelsArray = thingJson.getJSONArray("channels");

            List<String> linkedItems = new ArrayList<>();

            for (int i = 0; i < channelsArray.length(); i++) {
                JSONObject channel = channelsArray.getJSONObject(i);
                String channelUID = channel.getString("uid");
                String channelId = channel.getString("id");
                String itemType = channel.optString("itemType", null);

                if (itemType == null || itemType.isEmpty()) {
                    continue;
                }

                String itemName = (generatedUID + "_" + channelId).replaceAll("[:\\-]", "_");

                JSONObject itemJson = new JSONObject();
                itemJson.put("type", itemType);
                itemJson.put("name", itemName);
                itemJson.put("label", "Auto " + itemName);
                itemJson.put("category", "Light");
                itemJson.put("tags", new JSONArray());
                itemJson.put("groupNames", new JSONArray());

                String itemUrl = "http://localhost:8080/rest/items/" + itemName;

                headersObj = new HttpHeaders();
                headersObj.setContentType(MediaType.APPLICATION_JSON);
                headersObj.setBearerAuth(openHABToken);

                HttpEntity<String> itemEntity = new HttpEntity<>(itemJson.toString(), headersObj);

                ResponseEntity<String> itemResponse;
                try {
                    itemResponse = restTemplateObj.exchange(itemUrl, HttpMethod.PUT, itemEntity, String.class);
                } catch (RestClientException e) {
                    return error("Failed to create item");
                }

                if (!itemResponse.getStatusCode().is2xxSuccessful()) {
                    return ResponseEntity.status(itemResponse.getStatusCode()).body(Collections.singletonMap("error", "Failed to create item: " + itemResponse.getStatusCode() + " - " + itemResponse.getBody()));
                }

                String linkUrl = "http://localhost:8080/rest/links/" + itemName + "/" + channelUID;

                headersObj = new HttpHeaders();
                headersObj.setContentType(MediaType.APPLICATION_JSON);
                headersObj.setBearerAuth(openHABToken);

                HttpEntity<String> linkEntity = new HttpEntity<>("", headersObj);

                ResponseEntity<String> linkResponse;
                try {
                    linkResponse = restTemplateObj.exchange(linkUrl, HttpMethod.PUT, linkEntity, String.class);
                } catch (RestClientException e) {
                    return error("Failed to link item to channel");
                }

                if (!linkResponse.getStatusCode().is2xxSuccessful()) {
                    return ResponseEntity.status(linkResponse.getStatusCode()).body(Collections.singletonMap("error", "Failed to link item to channel: " + linkResponse.getStatusCode() + " - " + linkResponse.getBody()));
                }

                linkedItems.add(itemName);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Device added and controls created successfully");
            response.put("thingUID", generatedUID);
            response.put("linkedItems", linkedItems);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Get thing and items
    public ResponseEntity<?> getThingsWithItems(String token) {
        try {
            // Get user object
            User userObj = getUserFromTokenOrThrow(token);
            // Get specific user room list
            List<Room> userRooms = roomRepository.findByUser_UserId(userObj.getUserId());

            String openHABItemsUrl = "http://localhost:8080/rest/items";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();

            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(openHABToken);

            HttpEntity<String> requestObj = new HttpEntity<>(headers);

            ResponseEntity<String> responseObj = restTemplate.exchange(openHABItemsUrl, HttpMethod.GET, requestObj, String.class);
            if (!responseObj.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(responseObj.getStatusCode()).body(Collections.singletonMap("error", "Failed to fetch items from OpenHAB"));
            }

            JSONArray allItems = new JSONArray(responseObj.getBody());

            List<Map<String, Object>> thingsList = new ArrayList<>();

            for (Room room : userRooms) {
                List<Device> devices = deviceRepository.findByRoom_RoomId(room.getRoomId());

                for (Device device : devices) {
                    String deviceId = device.getDeviceId();
                    String thingUID = device.getThingUID();
                    String thingUrl = "http://localhost:8080/rest/things/" + thingUID;

                    ResponseEntity<String> thingResponse;
                    try {
                        thingResponse = restTemplate.exchange(thingUrl, HttpMethod.GET, requestObj, String.class);
                    } catch (RestClientException e) {
                        continue;
                    }
                    if (!thingResponse.getStatusCode().is2xxSuccessful()) {
                        continue;
                    }
                    JSONObject thingJson = new JSONObject(thingResponse.getBody());
                    JSONArray channels = thingJson.optJSONArray("channels");

                    List<String> channelUIDs = new ArrayList<>();
                    if (channels != null) {
                        for (int i = 0; i < channels.length(); i++) {
                            JSONObject channel = channels.getJSONObject(i);
                            channelUIDs.add(channel.getString("uid"));
                        }
                    }
                    String normalizedUID = thingUID.replaceAll("[:\\-]", "_");
                    List<Map<String, Object>> deviceItems = new ArrayList<>();
                    for (int i = 0; i < allItems.length(); i++) {
                        JSONObject item = allItems.getJSONObject(i);
                        String itemName = item.getString("name");

                        if (itemName.contains(normalizedUID)) {
                            Map<String, Object> itemMap = new HashMap<>();
                            itemMap.put("name", itemName);
                            itemMap.put("label", item.optString("label", ""));
                            itemMap.put("state", item.optString("state", ""));
                            itemMap.put("type", item.optString("type", ""));
                            deviceItems.add(itemMap);
                        }
                    }
                    Map<String, Object> thingInfo = new HashMap<>();
                    thingInfo.put("deviceId", deviceId);
                    thingInfo.put("thingUID", thingUID);
//                    thingInfo.put("thingTypeUID", thingJson.optString("thingTypeUID"));
                    thingInfo.put("label", thingJson.optString("label"));
//                    thingInfo.put("bridgeUID", thingJson.optString("bridgeUID"));
//                    thingInfo.put("configuration", thingJson.optJSONObject("configuration").toMap());
//                    thingInfo.put("channels", channelUIDs);
                    thingInfo.put("roomId", room.getRoomId());
                    thingInfo.put("roomName", room.getRoomName());
                    thingInfo.put("items", deviceItems);
                    thingsList.add(thingInfo);
                }
            }
            return ResponseEntity.ok(Collections.singletonMap("things", thingsList));
        } catch (Exception e) {
            return error("Something went wrong " + e.getMessage());
        }
    }

    // Delete device
    public ResponseEntity<?> deleteDevice(String token, String thingUID) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            Optional<Device> deviceOpt = deviceRepository.findByThingUIDAndUser_UserId(thingUID, userObj.getUserId());
            if (deviceOpt.isEmpty()) {
                return notFound("Device not found for this user");
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(openHABToken); // Make sure this is set

            // 1. Fetch items linked to the thing
            String itemUrl = "http://localhost:8080/rest/things/" + thingUID;
            ResponseEntity<Map> thingResponse = restTemplate.exchange(itemUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            if (!thingResponse.getStatusCode().is2xxSuccessful()) {
                return error("Failed to fetch thing info from OpenHAB.");
            }

            // Extract channels and item names
            List<Map<String, Object>> channels = (List<Map<String, Object>>) thingResponse.getBody().get("channels");
            if (channels != null) {
                for (Map<String, Object> channel : channels) {
                    List<String> linkedItems = (List<String>) channel.get("linkedItems");
                    if (linkedItems != null) {
                        for (String itemName : linkedItems) {
                            // 2. Delete each linked item
                            String deleteItemUrl = "http://localhost:8080/rest/items/" + itemName;
                            try {
                                restTemplate.exchange(deleteItemUrl, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
                            } catch (RestClientException e) {
                                // Continue deleting other items even if one fails
                                System.err.println("Failed to delete item " + itemName + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }

            // 3. Delete the Thing itself
            String deleteThingUrl = "http://localhost:8080/rest/things/" + thingUID;
            ResponseEntity<Void> openHABResponse = restTemplate.exchange(deleteThingUrl, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
            if (!openHABResponse.getStatusCode().is2xxSuccessful()) {
                return error("Failed to delete thing from OpenHAB. Status: " + openHABResponse.getStatusCode());
            }

            // 4. Remove from your DB
            deviceRepository.delete(deviceOpt.get());

            return ok("Device and related items deleted successfully");

        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Post control
    public ResponseEntity<?> postControl(String token, Map<String, String> payload) {
        try {
            User userObj = getUserFromTokenOrThrow(token);

            String itemName = payload.get("itemName");
            String command = payload.get("command");

            if (itemName == null || command == null) {
                return badRequest("Missing itemName or command");
            }

            List<Room> rooms = roomRepository.findByUser(userObj);
            boolean authorized = false;

            for (Room room : rooms) {
                List<Device> devices = deviceRepository.findByRoom(room);
                for (Device device : devices) {
                    String thingUID = device.getThingUID();
                    if (thingUID != null && itemName.contains(thingUID.replaceAll("[:\\-]", "_"))) {
                        authorized = true;
                        break;
                    }
                }
                if (authorized) break;
            }

            if (!authorized) {
                return unauthorized("Unauthorized user");
            }

            String url = "http://localhost:8080/rest/items/" + itemName;
            HttpHeaders headers = new HttpHeaders();

            headers.setBearerAuth(openHABToken);
            headers.setContentType(MediaType.TEXT_PLAIN);

            HttpEntity<String> entity = new HttpEntity<>(command, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ok("Command sent successfully");
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(Collections.singletonMap("error", "Failed to send command to item"));
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Post scenes
    public ResponseEntity<?> postScenes(String token, ScenesDTO postRequest) {
        try {
            User user = getUserFromTokenOrThrow(token);
            if (postRequest.getFromTime() == null || postRequest.getToTime() == null || postRequest.getDays() == null
                    || postRequest.getDays().isEmpty() || postRequest.getDeviceId() == null
                    || postRequest.getRoomId() == null || postRequest.getCommand() == null) {
                return badRequest("Missing required scenes fields");
            }
            List<Room> userRooms = roomRepository.findByUser(user);
            boolean authorizedRoom = userRooms.stream().anyMatch(r -> r.getRoomId().equals(postRequest.getRoomId()));
            if (!authorizedRoom) {
                return unauthorized("User not authorized for the specified room");
            }
            List<Device> devices = deviceRepository.findByRoom_RoomId(postRequest.getRoomId());
            System.out.println(devices);
            System.out.println(postRequest.getDeviceId());
            Device device = devices.stream()
                    .filter(d -> d.getDeviceId().equals(postRequest.getDeviceId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Device not found"));
            boolean authorizedDevice = devices.contains(device);
            if (!authorizedDevice) {
                return unauthorized("User not authorized for the specified device");
            }
            // Save scenes entity
            Scenes scenes = new Scenes();
            scenes.setScenesId(generateScenesId());
            scenes.setScenesName(postRequest.getScenesName());
            scenes.setFromTime(postRequest.getFromTime());
            scenes.setToTime(postRequest.getToTime());
            scenes.setDays(postRequest.getDays());
            scenes.setRoomId(postRequest.getRoomId());
            scenes.setDeviceId(postRequest.getDeviceId());
            scenes.setCommand(postRequest.getCommand());
            scenes.setUser(user);

            Room room = roomRepository.findById(postRequest.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            scenes.setRoomName(room.getRoomName()); // <-- This line sets the room name

            scenesRepository.save(scenes);

            // Validate time format
            if (!postRequest.getFromTime().matches("\\d{2}:\\d{2}") || !postRequest.getToTime().matches("\\d{2}:\\d{2}")) {
                return badRequest("Time format must be HH:mm");
            }
            String[] fromParts = postRequest.getFromTime().split(":");
            String[] toParts = postRequest.getToTime().split(":");
            String fromMinute = fromParts[1], fromHour = fromParts[0];
            String toMinute = toParts[1], toHour = toParts[0];

            String daysCron = buildDaysCron(postRequest.getDays());
            if (daysCron == null || daysCron.trim().isEmpty()
                    || !daysCron.matches("^(SUN|MON|TUE|WED|THU|FRI|SAT)(,(SUN|MON|TUE|WED|THU|FRI|SAT))*$")) {
                return badRequest("Invalid days cron expression. Use valid days like MON,TUE");
            }

            String cronOn = String.format("0 %s %s ? * %s *", fromMinute, fromHour, daysCron);
            String cronOff = String.format("0 %s %s ? * %s *", toMinute, toHour, daysCron);

            String thingUID = device.getThingUID();

            // Fetch itemName dynamically from thingUID
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openHABToken);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> thingResponse = restTemplate.exchange(
                    "http://localhost:8080/rest/things/" + thingUID,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (!thingResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(thingResponse.getStatusCode())
                        .body("Failed to fetch thing details from OpenHAB: " + thingResponse.getBody());
            }

            String thingJson = thingResponse.getBody();
            Map<String, Object> thingMap = new ObjectMapper().readValue(thingJson, new TypeReference<>() {
            });

            // Extract channel linkedItems
            List<Map<String, Object>> channels = (List<Map<String, Object>>) thingMap.get("channels");
            if (channels == null || channels.isEmpty()) {
                return badRequest("No channels found for the device");
            }

            // Try to get the itemName from the "color" channel (or fallback to first channel with a linked item)
            String itemName = channels.stream()
                    .filter(c -> ((List<?>) c.get("linkedItems")) != null && !((List<?>) c.get("linkedItems")).isEmpty())
                    .filter(c -> ((String) c.get("id")).toLowerCase().contains("color")) // Prefer "color" channel
                    .map(c -> ((List<String>) c.get("linkedItems")).get(0))
                    .findFirst()
                    .orElseGet(() -> channels.stream()
                            .filter(c -> ((List<?>) c.get("linkedItems")) != null && !((List<?>) c.get("linkedItems")).isEmpty())
                            .map(c -> ((List<String>) c.get("linkedItems")).get(0))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No linked items found in channels")));

            // Rule names
            String ruleOnName = "Auto_ON_" + itemName + "_" + scenes.getScenesId();
            String ruleOffName = "Auto_OFF_" + itemName + "_" + scenes.getScenesId();

            // Rule ON JSON
            String ruleOnJson = "{"
                    + "\"uid\": \"" + ruleOnName + "\","
                    + "\"name\": \"" + ruleOnName + "\","
                    + "\"description\": \"Auto ON rule for " + itemName + "\","
                    + "\"visibility\": \"VISIBLE\","
                    + "\"triggers\": [{"
                    + "    \"id\": \"cronTriggerOn_" + scenes.getScenesId() + "\","
                    + "    \"label\": \"Turn ON at " + postRequest.getFromTime() + "\","
                    + "    \"description\": \"Turns device ON based on schedule\","
                    + "    \"configuration\": { \"cronExpression\": \"" + cronOn + "\" },"
                    + "    \"type\": \"timer.GenericCronTrigger\""
                    + "}],"
                    + "\"conditions\": [],"
                    + "\"actions\": [{"
                    + "    \"id\": \"actionOn_" + scenes.getScenesId() + "\","
                    + "    \"label\": \"Send ON command\","
                    + "    \"description\": \"Turns ON the item\","
                    + "    \"configuration\": {"
                    + "        \"itemName\": \"" + itemName + "\","
                    + "        \"command\": \"ON\""
                    + "    },"
                    + "    \"type\": \"core.ItemCommandAction\","
                    + "    \"inputs\": {}"
                    + "}]"
                    + "}";

            // Rule OFF JSON
            String ruleOffJson = "{"
                    + "\"uid\": \"" + ruleOffName + "\","
                    + "\"name\": \"" + ruleOffName + "\","
                    + "\"description\": \"Auto OFF rule for " + itemName + "\","
                    + "\"visibility\": \"VISIBLE\","
                    + "\"triggers\": [{"
                    + "    \"id\": \"cronTriggerOff_" + scenes.getScenesId() + "\","
                    + "    \"label\": \"Turn OFF at " + postRequest.getToTime() + "\","
                    + "    \"description\": \"Turns device OFF based on schedule\","
                    + "    \"configuration\": { \"cronExpression\": \"" + cronOff + "\" },"
                    + "    \"type\": \"timer.GenericCronTrigger\""
                    + "}],"
                    + "\"conditions\": [],"
                    + "\"actions\": [{"
                    + "    \"id\": \"actionOff_" + scenes.getScenesId() + "\","
                    + "    \"label\": \"Send OFF command\","
                    + "    \"description\": \"Turns OFF the item\","
                    + "    \"configuration\": {"
                    + "        \"itemName\": \"" + itemName + "\","
                    + "        \"command\": \"OFF\""
                    + "    },"
                    + "    \"type\": \"core.ItemCommandAction\","
                    + "    \"inputs\": {}"
                    + "}]"
                    + "}";

            // Post ON Rule
            HttpEntity<String> entityOn = new HttpEntity<>(ruleOnJson, headers);
            ResponseEntity<String> responseOn = restTemplate.postForEntity("http://localhost:8080/rest/rules", entityOn, String.class);
            if (!responseOn.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(responseOn.getStatusCode()).body("Failed to create ON rule in OpenHAB: " + responseOn.getBody());
            }

            // Post OFF Rule
            HttpEntity<String> entityOff = new HttpEntity<>(ruleOffJson, headers);
            ResponseEntity<String> responseOff = restTemplate.postForEntity("http://localhost:8080/rest/rules", entityOff, String.class);
            if (!responseOff.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(responseOff.getStatusCode()).body("Failed to create OFF rule in OpenHAB: " + responseOff.getBody());
            }

            return ok("Scenes and OpenHAB rules created successfully");

        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Get scenes
    public ResponseEntity<?> getScenes(String token) {
        try {
            User user = getUserFromTokenOrThrow(token);
            List<Scenes> scenesList = scenesRepository.findByUser(user);

            List<ScenesDTO> response = scenesList.stream().map(scene -> {
                ScenesDTO dto = new ScenesDTO();
                dto.setScenesId(scene.getScenesId());
                dto.setScenesName(scene.getScenesName());
                dto.setFromTime(formatTo12Hour(scene.getFromTime()));
                dto.setToTime(formatTo12Hour(scene.getToTime()));
                dto.setDays(scene.getDays());
                dto.setRoomName(scene.getRoomName());
                dto.setCommand(scene.getCommand());
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error("Failed to load scenes: " + e.getMessage());
        }
    }

    // Patch scenes
    public ResponseEntity<?> patchScenesAndRules(String token, List<ScenesDTO> updatedScenesList) {
        try {
            User user = getUserFromTokenOrThrow(token);
            RestTemplate restTemplate = new RestTemplate();

            // Headers for OpenHAB
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openHABToken);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Fetch all OpenHAB items once
            ResponseEntity<String> responseObj = restTemplate.exchange(
                    "http://localhost:8080/rest/items", HttpMethod.GET, requestEntity, String.class);
            JSONArray allItems = new JSONArray(responseObj.getBody());

            for (ScenesDTO dto : updatedScenesList) {
                // 1. Update DB
                Scenes scene = scenesRepository.findByScenesIdAndUser(dto.getScenesId(), user)
                        .orElseThrow(() -> new RuntimeException("Scene not found: " + dto.getScenesId()));

                scene.setScenesName(dto.getScenesName());
                scene.setFromTime(dto.getFromTime());
                scene.setToTime(dto.getToTime());
                scene.setDays(dto.getDays());
                scene.setRoomName(dto.getRoomName());
                scene.setCommand(dto.getCommand());
                scenesRepository.save(scene);

                // 2. Identify associated device
                Room room = roomRepository.findByRoomNameAndUser(dto.getRoomName(), user)
                        .orElseThrow(() -> new RuntimeException("Room not found: " + dto.getRoomName()));

                List<Device> roomDevices = deviceRepository.findByRoom_RoomId(room.getRoomId());
                if (roomDevices.isEmpty()) {
                    throw new RuntimeException("No device found in room: " + dto.getRoomName());
                }

                // Assume the first device in the room is used for this scene
                Device device = roomDevices.get(0);
                String thingUID = device.getThingUID();
                String normalizedUID = thingUID.replaceAll("[:\\-]", "_");

                // 3. Match corresponding OpenHAB item
                String itemName = null;
                for (int i = 0; i < allItems.length(); i++) {
                    JSONObject item = allItems.getJSONObject(i);
                    String name = item.getString("name");
                    if (name.contains(normalizedUID)) {
                        itemName = name;
                        break;
                    }
                }

                if (itemName == null) {
                    throw new RuntimeException("Matching OpenHAB item not found for thing: " + thingUID);
                }

                // 4. Create rule ID and cron expression
                String ruleId = "Auto_" + dto.getCommand().toUpperCase() + "_" + itemName;
                LocalTime localTime = LocalTime.parse(dto.getFromTime(), DateTimeFormatter.ofPattern("HH:mm"));
                String cronDays = buildDaysCron(dto.getDays());
                String cron = String.format("0 %d %d ? * %s", localTime.getMinute(), localTime.getHour(), cronDays);

                // 5. Build OpenHAB rule
                Map<String, Object> rulePayload = new HashMap<>();
                rulePayload.put("uid", ruleId);
                rulePayload.put("name", ruleId);
                rulePayload.put("description", "Auto rule for scene: " + dto.getScenesName());

                rulePayload.put("triggers", List.of(Map.of(
                        "id", "cronTrigger",
                        "type", "timer.GenericCronTrigger",
                        "configuration", Map.of("cronExpression", cron)
                )));

                rulePayload.put("actions", List.of(Map.of(
                        "id", "commandAction",
                        "type", "core.ItemCommandAction",
                        "configuration", Map.of(
                                "itemName", itemName,
                                "command", dto.getCommand()
                        )
                )));

                rulePayload.put("conditions", List.of());
                rulePayload.put("configuration", Map.of());
                rulePayload.put("tags", List.of());

                // 6. PUT to OpenHAB
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(rulePayload, headers);
                String openhabUrl = "http://localhost:8080/rest/rules/" + ruleId;
                restTemplate.exchange(openhabUrl, HttpMethod.PUT, entity, String.class);
            }

            return ResponseEntity.ok("Scenes and dynamic rules updated successfully.");

        } catch (Exception e) {
            return error("Dynamic rule update failed: " + e.getMessage());
        }
    }

    private String formatTo12Hour(String time24) {
        try {
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("h:mm a");
            return LocalTime.parse(time24, inputFormat).format(outputFormat).toLowerCase();
        } catch (Exception e) {
            return time24; // fallback
        }
    }

    // Helper method to convert list of days to OpenHAB cron days
    private String buildDaysCron(List<String> days) {
        // Map full day names to 3-letter uppercase abbreviations for cron
        Map<String, String> dayMap = Map.of(
                "SUNDAY", "SUN",
                "MONDAY", "MON",
                "TUESDAY", "TUE",
                "WEDNESDAY", "WED",
                "THURSDAY", "THU",
                "FRIDAY", "FRI",
                "SATURDAY", "SAT"
        );

        return days.stream()
                .map(day -> dayMap.getOrDefault(day.toUpperCase(), "MON")) // default MON if invalid
                .collect(Collectors.joining(","));
    }

}