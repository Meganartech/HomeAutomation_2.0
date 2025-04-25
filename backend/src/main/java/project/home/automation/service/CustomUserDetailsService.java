package project.home.automation.service;

import com.google.firebase.database.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import project.home.automation.entity.User;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final String COLLECTION_NAME = "user";

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(COLLECTION_NAME);
            CountDownLatch latch = new CountDownLatch(1);
            final User[] foundUser = new User[1];

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && user.getEmail().equalsIgnoreCase(email)) {
                            foundUser[0] = user;
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

            if (foundUser[0] != null) {
                return new org.springframework.security.core.userdetails.User(
                        foundUser[0].getEmail(),
                        foundUser[0].getPassword(),
                        Collections.singleton(new SimpleGrantedAuthority(foundUser[0].getRole()))
                );
            } else {
                throw new UsernameNotFoundException("User not found with email: " + email);
            }

        } catch (Exception e) {
            throw new UsernameNotFoundException("Error: " + e.getMessage());
        }
    }
}