package project.home.automation.security;

import com.google.firebase.database.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import project.home.automation.entity.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

@Component
public class JwtUtil {

    private static final String COLLECTION_NAME = "user";
    private static final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    // Generate JWT Token
    public String generateToken(String email) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            CountDownLatch latch = new CountDownLatch(1);
            final String[] role = new String[1];

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && user.getEmail().equalsIgnoreCase(email)) {
                            role[0] = user.getRole(); // Assuming you have getRole() in User
                            break;
                        }
                    }
                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    latch.countDown();
                }
            });

            latch.await();

            if (role[0] == null) {
                throw new IllegalArgumentException("User not found: " + email);
            }

            return Jwts.builder()
                    .subject(email)
                    .claim("role", role[0])
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                    .signWith(secretKey)
                    .compact();

        } catch (Exception e) {
            throw new RuntimeException("Error generating token: " + e.getMessage(), e);
        }
    }

    // Extract email from token
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validate token
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("Invalid token signature: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Invalid token: " + e.getMessage());
        }
        return false;
    }
}