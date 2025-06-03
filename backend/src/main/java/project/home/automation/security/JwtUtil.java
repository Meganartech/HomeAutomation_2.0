package project.home.automation.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import project.home.automation.entity.User;
import project.home.automation.repository.UserRepository;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final UserRepository userRepository;

    public JwtUtil(@Value("${jwt.secret}") String secretKeyBase64, UserRepository userRepository) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyBase64));
        this.userRepository = userRepository;
    }

    // Generate token
    public String generateToken(String email) {
        Optional<User> userData = userRepository.findByEmail(email);
        String role;
        if (userData.isPresent()) {
            role = userData.get().getRole();
        } else {
            throw new IllegalArgumentException(email + " - email is not found");
        }

        // Expiration Time: 1 day in milliseconds
        int expirationTime = 86400000;
        String token = Jwts.builder()
                .subject(email) // updated
                .claim("role", role)
                .issuedAt(new Date()) // updated
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) // updated
                .signWith(secretKey)
                .compact();
        System.out.println(token);
        return token;
    }

    // Extract email
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) // updated verification
                .build()
                .parseSignedClaims(token) // updated parsing
                .getPayload()
                .getSubject(); // get subject (email)
    }

    // Token validation
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
            return false;
        } catch (SecurityException e) {
            System.out.println("Invalid token signature: " + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Invalid token: " + e.getMessage());
            return false;
        }
    }

}


// Fire base code
//package project.home.automation.security;
//
//import com.google.firebase.database.*;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import io.jsonwebtoken.security.SecurityException;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//import project.home.automation.entity.User;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.concurrent.CountDownLatch;
//
//@Component
//public class JwtUtil {
//
//    private static final String COLLECTION_NAME = "user";
//    private static final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
//
//    // Generate JWT Token
//    public String generateToken(String email) {
//        try {
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
//            CountDownLatch latch = new CountDownLatch(1);
//            final String[] role = new String[1];
//
//            ref.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot snapshot) {
//                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
//                        User user = userSnapshot.getValue(User.class);
//                        if (user != null && user.getEmail().equalsIgnoreCase(email)) {
//                            role[0] = user.getRole(); // Assuming you have getRole() in User
//                            break;
//                        }
//                    }
//                    latch.countDown();
//                }
//
//                @Override
//                public void onCancelled(DatabaseError error) {
//                    latch.countDown();
//                }
//            });
//
//            latch.await();
//
//            if (role[0] == null) {
//                throw new IllegalArgumentException("User not found: " + email);
//            }
//
//            return Jwts.builder()
//                    .subject(email)
//                    .claim("role", role[0])
//                    .issuedAt(new Date())
//                    .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
//                    .signWith(secretKey)
//                    .compact();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error generating token: " + e.getMessage(), e);
//        }
//    }
//
//    // Extract email from token
//    public String extractEmail(String token) {
//        return Jwts.parser()
//                .verifyWith(secretKey)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .getSubject();
//    }
//
//    // Validate token
//    public boolean isTokenValid(String token) {
//        try {
//            Jwts.parser()
//                    .verifyWith(secretKey)
//                    .build()
//                    .parseSignedClaims(token);
//            return true;
//        } catch (ExpiredJwtException e) {
//            System.out.println("Token expired: " + e.getMessage());
//        } catch (SecurityException e) {
//            System.out.println("Invalid token signature: " + e.getMessage());
//        } catch (JwtException | IllegalArgumentException e) {
//            System.out.println("Invalid token: " + e.getMessage());
//        }
//        return false;
//    }
//}