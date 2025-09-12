package me.taeuk.record.service;

import me.taeuk.record.domain.User;
import me.taeuk.record.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findById(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 평문 비밀번호와 암호화된 DB 비밀번호를 비교
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return user;
            }
        }
        return null; // 인증 실패
    }

    public User register(String username, String rawPassword) {
        if (userRepository.existsById(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, encodedPassword);
        return userRepository.save(user);
    }
}
