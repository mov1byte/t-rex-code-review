package morcheka_task.service;

import lombok.Data;
import lombok.NonNull;
import morcheka_task.model.User;
import morcheka_task.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@Data
public final class UserService implements UserDetailsService {

    final UserRepository userRepository;

    private Collection<GrantedAuthority> authorities;
    private String password;
    private String username;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull final String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("This user is not found"));
    }

    public void save(@NonNull final User user) {
        userRepository.save(user);
    }

    public boolean existsByUsername(@NonNull final String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findUserByUsername(@NonNull final String username) {
        return userRepository.findByUsername(username);
    }
}
