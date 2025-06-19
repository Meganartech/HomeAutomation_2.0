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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import project.home.automation.dto.*;
import project.home.automation.entity.Rooms;
import project.home.automation.entity.Things;
import project.home.automation.entity.Rules;
import project.home.automation.entity.User;
import project.home.automation.repository.ThingsRepository;
import project.home.automation.repository.RoomsRepository;
import project.home.automation.repository.RulesRepository;
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
    private final RoomsRepository roomsRepository;
    private final ThingsRepository thingsRepository;
    private final RulesRepository rulesRepository;
    private final MailService mailService;

    @Value("${openhab.token}")
    private String openHABToken;

    public UserService1(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, RoomsRepository roomsRepository, ThingsRepository thingsRepository, RulesRepository rulesRepository, MailService mailService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.roomsRepository = roomsRepository;
        this.thingsRepository = thingsRepository;
        this.rulesRepository = rulesRepository;
        this.mailService = mailService;
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
            String numberPart = lastId.replace("USER", "");
            nextId = Integer.parseInt(numberPart) + 1;
        }
        return String.format("USER%03d", nextId);
    }

    // Customized room id
    public String generateRoomId() {
        // Get the last room
        Rooms lastRooms = roomsRepository.findTopByOrderByRoomIdDesc();
        int nextId = 1;
        if (lastRooms != null) {
            String lastId = lastRooms.getRoomId();
            String numberPart = lastId.replace("ROOM", "");
            nextId = Integer.parseInt(numberPart) + 1;
        }
        return String.format("ROOM%03d", nextId);
    }

    // Rooms path
    public String roomPath(String roomName) {
        return roomName.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    // Customized thing id
    public String generateThingId() {
        // Get the last thing
        Things lastThings = thingsRepository.findTopByOrderByThingIdDesc();
        int nextId = 1;
        if (lastThings != null) {
            String lastId = lastThings.getThingId();
            String numberPart = lastId.replace("THING", "");
            nextId = Integer.parseInt(numberPart) + 1;
        }
        return String.format("THING%03d", nextId);
    }

    // Customized rule id
    public String generateRuleId() {
        // Get the last rule
        Rules lastRules = rulesRepository.findTopByOrderByRuleIdDesc();
        int nextId = 1;
        if (lastRules != null) {
            String lastId = lastRules.getRuleId();
            String numberPart = lastId.replace("RULE", "");
            nextId = Integer.parseInt(numberPart) + 1;
        }
        return String.format("RULE%03d", nextId);
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
                newUser.setContactNumber(postRequest.getContactNumber().trim());
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
    public ResponseEntity<?> postEmail_1(MailDTO postRequest) {
        try {
            // Fetch user to check email is correct
            if (userRepository.findByEmail(postRequest.getEmail()).isEmpty()) {
                return unauthorized("Email not found");
            } else {
                mailService.sendOtp(postRequest.getEmail());
                return ok("OTP sent to your registered email");
            }
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Resend OTP
    public ResponseEntity<?> postEmail_2(MailDTO postRequest) {
        try {
            if (userRepository.findByEmail(postRequest.getEmail()).isEmpty()) {
                return unauthorized("Email not found");
            } else {
                mailService.resendOtp(postRequest.getEmail());
                return ok("OTP resent to your registered email");
            }
        } catch (Exception e) {
            return error("Failed to resend OTP: " + e.getMessage());
        }
    }
    
    // Post otp
    public ResponseEntity<?> postOtp(MailDTO postRequest) {
        try {
            boolean isValid = mailService.isOtpValid(postRequest.getEmail(), postRequest.getOtp());
            if (!isValid) {
                return badRequest("Invalid OTP");
            } else {
                return ok("OTP verified successfully");
            }
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Patch password
    public ResponseEntity<?> patchPassword_1(MailDTO patchRequest) {
        try {
            Optional<User> userData = userRepository.findByEmail(patchRequest.getEmail());
            if (userData.isEmpty()) {
                return notFound("Email not found");
            } else {
                User userObj = userData.get();
                userObj.setPassword(passwordEncoder.encode(patchRequest.getNewPassword()));
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
            response.put("contactNumber", userObj.getContactNumber());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Patch profile
    public ResponseEntity<?> patchProfile(String token, UserDTO patchRequest) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            // Only update fields that are not null
            if (patchRequest.getName() != null) {
                userObj.setName(patchRequest.getName());
            }
            if (patchRequest.getContactNumber() != null) {
                userObj.setContactNumber(patchRequest.getContactNumber());
            }
            if (patchRequest.getEmail() != null) {
                userObj.setEmail(patchRequest.getEmail().toLowerCase());
            }
            userRepository.save(userObj);
            return ok("Profile updated successfully");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Failed to update profile: " + e.getMessage());
        }
    }

    // Patch password
    public ResponseEntity<?> patchPassword_2(String token, ChangePasswordDTO patchRequest) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            if (!passwordEncoder.matches(patchRequest.getCurrentPassword(), userObj.getPassword())) {
                return unauthorized("Current password is incorrect");
            } else {
                userObj.setPassword(passwordEncoder.encode(patchRequest.getNewPassword()));
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
    public ResponseEntity<?> postPasswordAndGetOtp(String token, MailDTO otpRequest) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            if (otpRequest.getPassword() == null || !passwordEncoder.matches(otpRequest.getPassword(), userObj.getPassword())) {
                return unauthorized("Incorrect password");
            } else {
                mailService.sendOtp(userObj.getEmail());
                return ok("OTP sent to your registered email");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Post room
    public ResponseEntity<?> postRoom(String token, RoomsDTO registerRequest) {
        try {
            User userObj = getUserFromTokenOrThrow(token);
            if (registerRequest.getRoomName() == null || registerRequest.getRoomName().trim().isEmpty()) {
                return badRequest("Rooms name cannot be empty");
            }
            if (roomsRepository.existsByRoomNameIgnoreCaseAndUser_UserId(registerRequest.getRoomName(), userObj.getUserId())) {
                return conflict("Rooms already exists for this user");
            }
            int roomCount = roomsRepository.countByUser_UserId(userObj.getUserId());
            if (roomCount >= 5) {
                return badRequest("Maximum 5 rooms allowed per user");
            }
            Rooms roomsObj = new Rooms();
            roomsObj.setRoomId(generateRoomId());
            roomsObj.setRoomPath(roomPath(registerRequest.getRoomName()));
            roomsObj.setRoomName(registerRequest.getRoomName());
            roomsObj.setUser(userObj);
            roomsRepository.save(roomsObj);
            return ok("Rooms added successfully");
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
            List<Rooms> roomsObj = roomsRepository.findByUser_UserId(userObj.getUserId());
            List<Map<String, String>> result = roomsObj.stream().map(ref -> {
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
            Optional<Rooms> roomData = roomsRepository.findByRoomIdAndUser_UserId(roomId, userObj.getUserId());
            if (roomData.isEmpty()) {
                return notFound("Rooms not found");
            }
            Rooms roomsObj = roomData.get();
            List<Things> thingsList = thingsRepository.findByRooms(roomsObj);
            if (!thingsList.isEmpty()) {
                return error("Cannot delete room. Devices are still associated with it.");
            }
            roomsRepository.delete(roomData.get());
            return ok("Rooms deleted successfully");
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
    public ResponseEntity<?> postThingAndLinkItems(String token, ThingsDTO postRequest) {
        try {
            User userObj = getUserFromTokenOrThrow(token);

            if (postRequest.getRoomName() == null || postRequest.getRoomName().isEmpty()) {
                return badRequest("Rooms Name is required");
            }

            Optional<Rooms> roomData = roomsRepository.findByRoomNameAndUser(postRequest.getRoomName(), userObj);
            if (roomData.isEmpty()) {
                return notFound("Rooms not found");
            }

            Rooms roomsObj = roomData.get();
            postRequest.setRoomId(roomsObj.getRoomId());

            String sanitizedUserId = userObj.getUserId().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
            String sanitizedLabel = postRequest.getLabel().toLowerCase().replaceAll("[^a-z0-9_-]", "_");

            String generatedUID;
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();

            switch (postRequest.getThingTypeUID()) {
                case "mqtt:topic":
                    generatedUID = "mqtt:topic:mybroker:" + sanitizedUserId + "_" + sanitizedLabel;
                    rootNode.put("UID", generatedUID);
                    rootNode.put("label", postRequest.getLabel());
                    rootNode.put("thingTypeUID", "mqtt:topic");
                    rootNode.put("bridgeUID", "mqtt:broker:mybroker");
                    rootNode.set("configuration", mapper.createObjectNode());
                    break;

                case "network:pingdevice":
                    if (postRequest.getHost() == null || postRequest.getHost().isEmpty()) {
                        return badRequest("Host is required");
                    }
                    generatedUID = "network:pingdevice:" + sanitizedUserId + "_" + sanitizedLabel;
                    rootNode.put("UID", generatedUID);
                    rootNode.put("label", postRequest.getLabel());
                    rootNode.put("thingTypeUID", "network:pingdevice");
                    ObjectNode pingConfig = mapper.createObjectNode();
                    pingConfig.put("hostname", postRequest.getHost());
                    pingConfig.put("timeout", 5000);
                    pingConfig.put("refreshInterval", 60000);
                    rootNode.set("configuration", pingConfig);
                    break;

                case "wiz:color-bulb":
                    if (postRequest.getHost() == null || postRequest.getHost().isEmpty() || postRequest.getMacAddress() == null) {
                        return badRequest("Host and MacAddress are required for WiZ");
                    }
                    generatedUID = "wiz:color-bulb:" + sanitizedUserId + "_" + sanitizedLabel;
                    rootNode.put("UID", generatedUID);
                    rootNode.put("label", postRequest.getLabel());
                    rootNode.put("thingTypeUID", "wiz:color-bulb");
                    ObjectNode wizConfig = mapper.createObjectNode();
                    wizConfig.put("ipAddress", postRequest.getHost());
                    wizConfig.put("pollingInterval", 60);
                    wizConfig.put("macAddress", postRequest.getMacAddress());
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

            Things thingsObj = new Things();
            thingsObj.setThingId(generateThingId());
            thingsObj.setThingUID(generatedUID);
            thingsObj.setThingTypeUID(postRequest.getThingTypeUID());
            thingsObj.setLabel(postRequest.getLabel());
            thingsObj.setHost(postRequest.getHost());
            thingsObj.setUser(userObj);
            thingsObj.setRooms(roomsObj);
            thingsRepository.save(thingsObj);

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
            response.put("message", "Things added and controls created successfully");
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
    public ResponseEntity<?> getThingWithItems(String token) {
        try {
            // Get user object
            User userObj = getUserFromTokenOrThrow(token);
            // Get specific user room list
            List<Rooms> userRooms = roomsRepository.findByUser_UserId(userObj.getUserId());

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

            for (Rooms rooms : userRooms) {
                List<Things> things = thingsRepository.findByRooms_RoomId(rooms.getRoomId());

                for (Things things1 : things) {
                    String thingId = things1.getThingId();
                    String thingUID = things1.getThingUID();
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
                    thingInfo.put("thingId", thingId);
                    thingInfo.put("thingUID", thingUID);
//                    thingInfo.put("thingTypeUID", thingJson.optString("thingTypeUID"));
                    thingInfo.put("label", thingJson.optString("label"));
//                    thingInfo.put("bridgeUID", thingJson.optString("bridgeUID"));
//                    thingInfo.put("configuration", thingJson.optJSONObject("configuration").toMap());
//                    thingInfo.put("channels", channelUIDs);
                    thingInfo.put("roomId", rooms.getRoomId());
                    thingInfo.put("roomName", rooms.getRoomName());
                    thingInfo.put("items", deviceItems);
                    thingsList.add(thingInfo);
                }
            }
            return ResponseEntity.ok(Collections.singletonMap("things", thingsList));
        } catch (Exception e) {
            return error("Something went wrong " + e.getMessage());
        }
    }

//    private String getLinkedItemNameForThing(RestTemplate restTemplate, HttpHeaders headers, String thingUID) throws Exception {
//        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
//        ResponseEntity<String> thingResponse = restTemplate.exchange(
//                "http://localhost:8080/rest/things/" + thingUID,
//                HttpMethod.GET,
//                requestEntity,
//                String.class
//        );
//
//        if (!thingResponse.getStatusCode().is2xxSuccessful()) {
//            throw new RuntimeException("Failed to fetch thing details from OpenHAB");
//        }
//
//        String thingJson = thingResponse.getBody();
//        Map<String, Object> thingMap = new ObjectMapper().readValue(thingJson, new TypeReference<>() {});
//        List<Map<String, Object>> channels = (List<Map<String, Object>>) thingMap.get("channels");
//
//        return channels.stream()
//                .filter(c -> ((List<?>) c.get("linkedItems")) != null && !((List<?>) c.get("linkedItems")).isEmpty())
//                .filter(c -> ((String) c.get("id")).toLowerCase().contains("color"))
//                .map(c -> ((List<String>) c.get("linkedItems")).get(0))
//                .findFirst()
//                .orElseGet(() -> channels.stream()
//                        .filter(c -> ((List<?>) c.get("linkedItems")) != null && !((List<?>) c.get("linkedItems")).isEmpty())
//                        .map(c -> ((List<String>) c.get("linkedItems")).get(0))
//                        .findFirst()
//                        .orElseThrow(() -> new RuntimeException("No linked items found")));
//    }

    // Delete thing
    public ResponseEntity<?> deleteThing(String token, String thingUID) {
        try {
            // 1. Authenticate user and find device
            User userObj = getUserFromTokenOrThrow(token);
            Optional<Things> deviceOpt = thingsRepository.findByThingUIDAndUser_UserId(thingUID, userObj.getUserId());
            if (deviceOpt.isEmpty()) {
                return notFound("Thing not found for this user");
            }

            // 2. Prepare OpenHAB request
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(openHABToken);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // 3. Fetch Thing details
            ResponseEntity<Map> thingResponse = restTemplate.exchange(
                    "http://localhost:8080/rest/things/" + thingUID,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );
            if (!thingResponse.getStatusCode().is2xxSuccessful()) {
                return error("Failed to fetch thing info from OpenHAB.");
            }

            // 4. Extract linked items from channels
            List<Map<String, Object>> channels = (List<Map<String, Object>>) thingResponse.getBody().get("channels");
            if (channels != null) {
                for (Map<String, Object> channel : channels) {
                    List<String> linkedItems = (List<String>) channel.get("linkedItems");
                    if (linkedItems != null) {
                        for (String itemName : linkedItems) {
                            String deleteItemUrl = "http://localhost:8080/rest/items/" + itemName;
                            try {
                                restTemplate.exchange(deleteItemUrl, HttpMethod.DELETE, requestEntity, Void.class);
                            } catch (RestClientException e) {
                                System.err.println("Failed to delete item " + itemName + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }

            // 5. Delete associated rules
            // ✅ Get the correct thingId from the entity
            Things thingEntity = deviceOpt.get();
            String thingId = thingEntity.getThingId(); // use this, not thingUID

// ✅ Now safely fetch rules using thingId
            List<Rules> relatedRules = rulesRepository.findAllByThingIdAndUser(thingId, userObj);
            System.out.println("Rules to delete: " + relatedRules.size());

            for (Rules rule : relatedRules) {
                try {
                    // ✅ Get the linked item name for this thing
                    String itemName = channels.stream()
                            .filter(c -> ((List<?>) c.get("linkedItems")) != null && !((List<?>) c.get("linkedItems")).isEmpty())
                            .filter(c -> ((String) c.get("id")).toLowerCase().contains("color")) // optional filter
                            .map(c -> ((List<String>) c.get("linkedItems")).get(0))
                            .findFirst()
                            .orElseGet(() -> channels.stream()
                                    .filter(c -> ((List<?>) c.get("linkedItems")) != null && !((List<?>) c.get("linkedItems")).isEmpty())
                                    .map(c -> ((List<String>) c.get("linkedItems")).get(0))
                                    .findFirst()
                                    .orElseThrow(() -> new RuntimeException("No linked items found")));

                    // ✅ Construct rule names
                    String ruleOnName = "Auto_ON_" + itemName + "_" + rule.getRuleId();
                    String ruleOffName = "Auto_OFF_" + itemName + "_" + rule.getRuleId();

                    System.out.println("Deleting rules: " + ruleOnName + ", " + ruleOffName);

                    // ✅ Delete rules from OpenHAB
                    restTemplate.exchange("http://localhost:8080/rest/rules/" + ruleOnName,
                            HttpMethod.DELETE, requestEntity, String.class);
                    restTemplate.exchange("http://localhost:8080/rest/rules/" + ruleOffName,
                            HttpMethod.DELETE, requestEntity, String.class);

                    // ✅ Delete rule from database
                    rulesRepository.delete(rule);

                } catch (Exception e) {
                    System.err.println("Failed to delete rule for thing " + thingId + ": " + e.getMessage());
                }
            }


            // 6. Delete Thing from OpenHAB
            String deleteThingUrl = "http://localhost:8080/rest/things/" + thingUID;
            ResponseEntity<Void> openHABResponse = restTemplate.exchange(deleteThingUrl, HttpMethod.DELETE, requestEntity, Void.class);
            if (!openHABResponse.getStatusCode().is2xxSuccessful()) {
                return error("Failed to delete thing from OpenHAB. Status: " + openHABResponse.getStatusCode());
            }

            // 7. Delete Thing from DB
            thingsRepository.delete(deviceOpt.get());

            return ok("Thing, related items, and rules deleted successfully");

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

            List<Rooms> rooms = roomsRepository.findByUser(userObj);
            boolean authorized = false;

            for (Rooms room : rooms) {
                List<Things> things = thingsRepository.findByRooms(room);
                for (Things things1 : things) {
                    String thingUID = things1.getThingUID();
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

    // Post rule
    public ResponseEntity<?> postRule(String token, RulesDTO postRequest) {
        try {
            User user = getUserFromTokenOrThrow(token);
            if (postRequest.getFromTime() == null || postRequest.getToTime() == null || postRequest.getDays() == null
                    || postRequest.getDays().isEmpty() || postRequest.getThingId() == null
                    || postRequest.getRoomId() == null || postRequest.getCommand() == null) {
                return badRequest("Missing required rules fields");
            }
            List<Rooms> userRooms = roomsRepository.findByUser(user);
            boolean authorizedRoom = userRooms.stream().anyMatch(r -> r.getRoomId().equals(postRequest.getRoomId()));
            if (!authorizedRoom) {
                return unauthorized("User not authorized for the specified rooms");
            }
            List<Things> things = thingsRepository.findByRooms_RoomId(postRequest.getRoomId());
            System.out.println(things);
            System.out.println(postRequest.getThingId());
            Things thing = things.stream()
                    .filter(d -> d.getThingId().equals(postRequest.getThingId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Things not found"));
            boolean authorizedDevice = things.contains(thing);
            if (!authorizedDevice) {
                return unauthorized("User not authorized for the specified things");
            }
            // Save rules entity
            Rules rules = new Rules();
            rules.setRuleId(generateRuleId());
            rules.setRuleName(postRequest.getRuleName());
            rules.setFromTime(postRequest.getFromTime());
            rules.setToTime(postRequest.getToTime());
            rules.setDays(postRequest.getDays());
            rules.setRoomId(postRequest.getRoomId());
            rules.setThingId(postRequest.getThingId());
            rules.setCommand(postRequest.getCommand());
            rules.setUser(user);

            // Set enabled field, default true if null in DTO
            if (postRequest.getEnabled() != null) {
                rules.setEnabled(postRequest.getEnabled());
            } else {
                rules.setEnabled(true);
            }

            Rooms rooms = roomsRepository.findById(postRequest.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Rooms not found"));
            rules.setRoomName(rooms.getRoomName()); // <-- This line sets the rooms name


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

            String thingUID = thing.getThingUID();

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
                return badRequest("No channels found for the things");
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
            String ruleOnName = "Auto_ON_" + itemName + "_" + rules.getRuleId();
            String ruleOffName = "Auto_OFF_" + itemName + "_" + rules.getRuleId();

            // Rule ON JSON
            String ruleOnJson = "{"
                    + "\"uid\": \"" + ruleOnName + "\","
                    + "\"name\": \"" + ruleOnName + "\","
                    + "\"description\": \"Auto ON rule for " + itemName + "\","
                    + "\"visibility\": \"VISIBLE\","
                    + "\"triggers\": [{"
                    + "    \"id\": \"cronTriggerOn_" + rules.getRuleId() + "\","
                    + "    \"label\": \"Turn ON at " + postRequest.getFromTime() + "\","
                    + "    \"description\": \"Turns things ON based on schedule\","
                    + "    \"configuration\": { \"cronExpression\": \"" + cronOn + "\" },"
                    + "    \"type\": \"timer.GenericCronTrigger\""
                    + "}],"
                    + "\"conditions\": [],"
                    + "\"actions\": [{"
                    + "    \"id\": \"actionOn_" + rules.getRuleId() + "\","
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
                    + "    \"id\": \"cronTriggerOff_" + rules.getRuleId() + "\","
                    + "    \"label\": \"Turn OFF at " + postRequest.getToTime() + "\","
                    + "    \"description\": \"Turns things OFF based on schedule\","
                    + "    \"configuration\": { \"cronExpression\": \"" + cronOff + "\" },"
                    + "    \"type\": \"timer.GenericCronTrigger\""
                    + "}],"
                    + "\"conditions\": [],"
                    + "\"actions\": [{"
                    + "    \"id\": \"actionOff_" + rules.getRuleId() + "\","
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
            rulesRepository.save(rules);
            return ok("Rules and OpenHAB rules created successfully");

        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
        }
    }

    // Get rule
    public ResponseEntity<?> getRule(String token) {
        try {
            User user = getUserFromTokenOrThrow(token);
            List<Rules> rulesList = rulesRepository.findByUser(user);

            List<RulesDTO> response = rulesList.stream().map(scene -> {
                RulesDTO dto = new RulesDTO();
                dto.setRuleId(scene.getRuleId());
                dto.setRuleName(scene.getRuleName());
                dto.setFromTime(formatTo12Hour(scene.getFromTime()));
                dto.setToTime(formatTo12Hour(scene.getToTime()));
                dto.setDays(scene.getDays());
                dto.setRoomId(scene.getRoomId());
                dto.setRoomName(scene.getRoomName());
                dto.setThingId(scene.getThingId());
                dto.setCommand(scene.getCommand());
                dto.setEnabled(scene.isEnabled());
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error("Failed to load rule: " + e.getMessage());
        }
    }

    // Patch rule
    public ResponseEntity<?> patchRule(String token, RulesDTO dto) {
        try {
            User user = getUserFromTokenOrThrow(token);
            RestTemplate restTemplate = new RestTemplate();

            // OpenHAB headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openHABToken);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Fetch all OpenHAB items
            ResponseEntity<String> responseObj = restTemplate.exchange(
                    "http://localhost:8080/rest/items", HttpMethod.GET, requestEntity, String.class);
            JSONArray allItems = new JSONArray(responseObj.getBody());

            // Update rule in DB
            Rules scene = rulesRepository.findByRuleIdAndUser(dto.getRuleId(), user)
                    .orElseThrow(() -> new RuntimeException("Scene not found: " + dto.getRuleId()));

            scene.setRuleName(dto.getRuleName());
            scene.setFromTime(dto.getFromTime());
            scene.setToTime(dto.getToTime());
            scene.setDays(dto.getDays());
            scene.setRoomName(dto.getRoomName());
            scene.setCommand(dto.getCommand());
            scene.setUser(user);

            // Identify associated things and rooms
            Rooms rooms = roomsRepository.findByRoomNameAndUser(dto.getRoomName(), user)
                    .orElseThrow(() -> new RuntimeException("Rooms not found: " + dto.getRoomName()));

            List<Things> roomThings = thingsRepository.findByRooms_RoomId(rooms.getRoomId());
            if (roomThings.isEmpty()) {
                throw new RuntimeException("No things found in rooms: " + dto.getRoomName());
            }
            Things things = roomThings.get(0); // Or your logic to select things
            String thingUID = things.getThingUID();
            String normalizedUID = thingUID.replaceAll("[:\\-]", "_");

            scene.setThingId(things.getThingId());
            scene.setRoomId(rooms.getRoomId());

            // Find itemName for rule based on thingUID and "_color" suffix
            String expectedSuffix = "_color"; // Or dynamic
            String itemName = null;
            for (int i = 0; i < allItems.length(); i++) {
                JSONObject item = allItems.getJSONObject(i);
                String name = item.getString("name");
                if (name.contains(normalizedUID) && name.endsWith(expectedSuffix)) {
                    itemName = name;
                    break;
                }
            }
            if (itemName == null) {
                throw new RuntimeException("Matching OpenHAB item not found for thing: " + thingUID);
            }

            // Build cron expressions for ON and OFF times
            String daysCron = buildDaysCron(dto.getDays());
            LocalTime fromTime = LocalTime.parse(dto.getFromTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime toTime = LocalTime.parse(dto.getToTime(), DateTimeFormatter.ofPattern("HH:mm"));

            String cronOn = String.format("0 %d %d ? * %s *", fromTime.getMinute(), fromTime.getHour(), daysCron);
            String cronOff = String.format("0 %d %d ? * %s *", toTime.getMinute(), toTime.getHour(), daysCron);

            // Rule UIDs
            String ruleOnName = "Auto_ON_" + itemName + "_" + scene.getRuleId();
            String ruleOffName = "Auto_OFF_" + itemName + "_" + scene.getRuleId();

            // Create rule JSON for ON and OFF rules
            Map<String, Object> ruleOnPayload = Map.of(
                    "uid", ruleOnName,
                    "name", ruleOnName,
                    "description", "Auto ON rule for " + itemName,
                    "visibility", "VISIBLE",
                    "triggers", List.of(Map.of(
                            "id", "cronTriggerOn_" + scene.getRuleId(),
                            "type", "timer.GenericCronTrigger",
                            "configuration", Map.of("cronExpression", cronOn)
                    )),
                    "conditions", List.of(),
                    "actions", List.of(Map.of(
                            "id", "actionOn_" + scene.getRuleId(),
                            "type", "core.ItemCommandAction",
                            "configuration", Map.of(
                                    "itemName", itemName,
                                    "command", "ON"
                            )
                    )),
                    "configuration", Map.of(),
                    "tags", List.of()
            );

            Map<String, Object> ruleOffPayload = Map.of(
                    "uid", ruleOffName,
                    "name", ruleOffName,
                    "description", "Auto OFF rule for " + itemName,
                    "visibility", "VISIBLE",
                    "triggers", List.of(Map.of(
                            "id", "cronTriggerOff_" + scene.getRuleId(),
                            "type", "timer.GenericCronTrigger",
                            "configuration", Map.of("cronExpression", cronOff)
                    )),
                    "conditions", List.of(),
                    "actions", List.of(Map.of(
                            "id", "actionOff_" + scene.getRuleId(),
                            "type", "core.ItemCommandAction",
                            "configuration", Map.of(
                                    "itemName", itemName,
                                    "command", "OFF"
                            )
                    )),
                    "configuration", Map.of(),
                    "tags", List.of()
            );

            // Fetch all rules to check if ON and OFF exist
            ResponseEntity<String> rulesResponse = restTemplate.exchange(
                    "http://localhost:8080/rest/rules", HttpMethod.GET, requestEntity, String.class);
            JSONArray allRules = new JSONArray(rulesResponse.getBody());

            boolean ruleOnExists = false;
            boolean ruleOffExists = false;

            for (int i = 0; i < allRules.length(); i++) {
                JSONObject rule = allRules.getJSONObject(i);
                String uid = rule.getString("uid");
                if (ruleOnName.equals(uid)) ruleOnExists = true;
                if (ruleOffName.equals(uid)) ruleOffExists = true;
            }

            // Update or error for ON rule
            HttpEntity<Map<String, Object>> entityOn = new HttpEntity<>(ruleOnPayload, headers);
            String ruleOnUrl = "http://localhost:8080/rest/rules/" + ruleOnName;
            if (ruleOnExists) {
                restTemplate.exchange(ruleOnUrl, HttpMethod.PUT, entityOn, String.class);
            } else {
                // Optionally create it if missing
                restTemplate.postForEntity("http://localhost:8080/rest/rules", entityOn, String.class);
            }

            // Update or error for OFF rule
            HttpEntity<Map<String, Object>> entityOff = new HttpEntity<>(ruleOffPayload, headers);
            String ruleOffUrl = "http://localhost:8080/rest/rules/" + ruleOffName;
            if (ruleOffExists) {
                restTemplate.exchange(ruleOffUrl, HttpMethod.PUT, entityOff, String.class);
            } else {
                // Optionally create it if missing
                restTemplate.postForEntity("http://localhost:8080/rest/rules", entityOff, String.class);
            }
            rulesRepository.save(scene);
            return ResponseEntity.ok("Scene and ON/OFF rules updated successfully.");

        } catch (HttpClientErrorException.NotFound ex) {
            return error("Rule not found. Cannot update a non-existing rule.");
        } catch (Exception e) {
            return error("Dynamic rule update failed: " + e.getMessage());
        }
    }

    // Patch rule (toggle)
    public ResponseEntity<?> toggleRule(String token, RulesToggleDTO dto) {
        try {
            User user = getUserFromTokenOrThrow(token);
            Rules scene = rulesRepository.findByRuleIdAndUser(dto.getRuleId(), user)
                    .orElseThrow(() -> new RuntimeException("Scene not found: " + dto.getRuleId()));

            Things things = thingsRepository.findById(scene.getThingId())
                    .orElseThrow(() -> new RuntimeException("Things not found"));

            String thingUID = things.getThingUID();
            String normalizedUID = thingUID.replaceAll("[:\\-]", "_");
            String expectedSuffix = "_color"; // adjust as needed
            String itemName = normalizedUID + expectedSuffix;

            String ruleOnName = "Auto_ON_" + itemName + "_" + scene.getRuleId();
            String ruleOffName = "Auto_OFF_" + itemName + "_" + scene.getRuleId();

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(openHABToken);
            headers.setContentType(MediaType.TEXT_PLAIN); // OpenHAB expects plain text

            String enableStr = dto.isEnable() ? "true" : "false";
            HttpEntity<String> entity = new HttpEntity<>(enableStr, headers);

            // Enable/disable OpenHAB rules
            restTemplate.exchange("http://localhost:8080/rest/rules/" + ruleOnName + "/enable", HttpMethod.POST, entity, String.class);
            restTemplate.exchange("http://localhost:8080/rest/rules/" + ruleOffName + "/enable", HttpMethod.POST, entity, String.class);

            // ✅ Update DB as well
            scene.setEnabled(dto.isEnable());
            rulesRepository.save(scene); // persist updated enabled status

            return ResponseEntity.ok("Rules " + (dto.isEnable() ? "enabled" : "disabled") + " successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error toggling rules: " + e.getMessage());
        }
    }

    // Delete rule
    public ResponseEntity<?> deleteRule(String token, String ruleId) {
        try {
            User user = getUserFromTokenOrThrow(token);
            Rules scene = rulesRepository.findByRuleIdAndUser(ruleId, user)
                    .orElseThrow(() -> new RuntimeException("Scene not found or not authorized"));
            Things things = thingsRepository.findById(scene.getThingId())
                    .orElseThrow(() -> new RuntimeException("Things not found"));
            String thingUID = things.getThingUID();
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
            Map<String, Object> thingMap = new ObjectMapper().readValue(thingJson, new TypeReference<>() {});
            List<Map<String, Object>> channels = (List<Map<String, Object>>) thingMap.get("channels");
            String itemName = channels.stream()
                    .filter(c -> ((List<?>) c.get("linkedItems")) != null && !((List<?>) c.get("linkedItems")).isEmpty())
                    .filter(c -> ((String) c.get("id")).toLowerCase().contains("color"))
                    .map(c -> ((List<String>) c.get("linkedItems")).get(0))
                    .findFirst()
                    .orElseGet(() -> channels.stream()
                            .filter(c -> ((List<?>) c.get("linkedItems")) != null && !((List<?>) c.get("linkedItems")).isEmpty())
                            .map(c -> ((List<String>) c.get("linkedItems")).get(0))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No linked items found in channels")));
            String ruleOnName = "Auto_ON_" + itemName + "_" + scene.getRuleId();
            String ruleOffName = "Auto_OFF_" + itemName + "_" + scene.getRuleId();
            restTemplate.exchange("http://localhost:8080/rest/rules/" + ruleOnName,
                    HttpMethod.DELETE, requestEntity, String.class);
            restTemplate.exchange("http://localhost:8080/rest/rules/" + ruleOffName,
                    HttpMethod.DELETE, requestEntity, String.class);
            rulesRepository.delete(scene);
            return ok("Scene and associated OpenHAB rules deleted successfully");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("Something went wrong: " + e.getMessage());
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