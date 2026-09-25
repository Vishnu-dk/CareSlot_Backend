package com.careslot.service;


import com.careslot.db.generated.enums.UserRole;
import com.careslot.db.generated.tables.records.UsersRecord;
import com.careslot.dto.auth.AuthResponse;
import com.careslot.dto.auth.LoginRequest;
import com.careslot.dto.auth.RegisterRequest;
import com.careslot.exception.InvalidCredentialsException;
import com.careslot.exception.ResourceNotFoundException;
import com.careslot.exception.UserAlreadyExistsException;
import com.careslot.repository.AuthRepository;
import com.careslot.repository.UserRepository;
import com.careslot.security.JwtServices;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServices jwtServices;

    public AuthService(UserRepository userRepository, AuthRepository authRepository, PasswordEncoder passwordEncoder, JwtServices jwtServices) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtServices = jwtServices;
    }

    public void register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException("Email already exists ");
        }
        UserRole role;

        try {
            role = UserRole.valueOf(request.getRole().name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Role");
        }

        String hashPassword=passwordEncoder.encode(request.getPassword());

        authRepository.register(request.getEmail(),hashPassword, String.valueOf(request.getRole()));

    }

    public AuthResponse login(LoginRequest request) {
        UsersRecord user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Credentials"));

        if (user.getDeletedAt() != null) {
            throw new InvalidCredentialsException("Account has been deactivated. Contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        String token = jwtServices.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(token, user.getEmail(), user.getRole());
    }
}
