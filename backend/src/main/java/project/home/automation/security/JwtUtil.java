package project.home.automation.security;

import project.home.automation.entity.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;
import project.home.automation.entity.User;
import project.home.automation.repository.UserRepository;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    //    Secret key
    private static final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    //    Expiration Time 1 day in milliseconds
    private final int expirationTime = 86400000;

    private final UserRepository userRepository;

    public JwtUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //    Generate token
    public String generateToken(String username) {
        Optional<User> usersData = userRepository.findByUsername(username);
        Set<Role> roles;
        if (usersData.isPresent()) {
            roles = usersData.get().getRoles();
        } else {
            throw new IllegalArgumentException("User not found: " + username);
        }
        String token = Jwts.builder().setSubject(username).claim("roles", roles.stream()
                        .map(role -> role.getRoleName()).collect(Collectors.joining(",")))
                .setIssuedAt(new Date()).setExpiration(new Date(new Date().getTime() + expirationTime))
                .signWith(secretKey).compact();
        return token;
    }

    //    Extract username
    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    //    Extract roles
    public Set<String> extractRoles(String token) {
        String roleString = Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().get("roles", String.class);
        return Set.of(roleString);
    }

    // Token validation
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.out.println("Invalid token signature: " + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Invalid token: " + e.getMessage());
            return false;
        }
    }
}