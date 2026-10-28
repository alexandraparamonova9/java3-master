package ru.itmo.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.web.auth.*;
import ru.itmo.web.security.JwtService;
import ru.itmo.web.user.*;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UserRepository repository;
    private BCryptPasswordEncoder encoder;
    private JwtService jwt;
    private AuthService service;
    private User user;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        encoder = new BCryptPasswordEncoder();
        jwt = new JwtService(Base64.getEncoder().encodeToString(new byte[32]), 15, 7);
        service = new AuthService(new UserService(repository, encoder), repository, encoder, jwt);
        user = new User("alice", "alice@example.com", encoder.encode("password123"), Role.USER);
        when(repository.findByUsername("alice")).thenReturn(Optional.of(user));
    }

    @Test
    void correctPasswordProducesBothTokenTypes() {
        AuthResponseDto response = service.login(new LoginRequestDto("alice", "password123"));
        assertEquals("alice", jwt.validateAccessToken(response.accessToken()));
        assertEquals("alice", jwt.validateRefreshToken(response.refreshToken()));
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void wrongPasswordAndUnknownUserReturn401() {
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequestDto("alice", "incorrect"))).getStatusCode().value());
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequestDto("missing", "password123"))).getStatusCode().value());
    }

    @Test
    void refreshIssuesUsableAccessToken() {
        AuthResponseDto response = service.refresh(new RefreshRequestDto(jwt.generateRefreshToken(user)));
        assertEquals("alice", jwt.validateAccessToken(response.accessToken()));
        assertEquals("alice", jwt.validateRefreshToken(response.refreshToken()));
    }

    @Test
    void accessTokenCannotBeUsedForRefreshAndViceVersa() {
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> service.refresh(new RefreshRequestDto(jwt.generateAccessToken(user))))
                .getStatusCode().value());
        assertThrows(ResponseStatusException.class,
                () -> jwt.validateAccessToken(jwt.generateRefreshToken(user)));
    }

    @Test
    void expiredAndMalformedTokensAreRejected() {
        JwtService expired = new JwtService(Base64.getEncoder().encodeToString(new byte[32]), -1, -1);
        assertThrows(ResponseStatusException.class,
                () -> jwt.validateAccessToken(expired.generateAccessToken(user)));
        assertThrows(ResponseStatusException.class,
                () -> service.refresh(new RefreshRequestDto(expired.generateRefreshToken(user))));
        assertThrows(ResponseStatusException.class,
                () -> service.refresh(new RefreshRequestDto("not-a-jwt")));
    }

    @Test
    void registrationStoresHashAndAssignsUserRole() {
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            assertNotEquals("password123", saved.getPassword());
            assertTrue(encoder.matches("password123", saved.getPassword()));
            assertEquals(Role.USER, saved.getRole());
            return saved;
        });

        AuthResponseDto response = service.register(
                new RegisterRequestDto("newuser", "new@example.com", "password123"));
        assertEquals("newuser", jwt.validateAccessToken(response.accessToken()));
        verify(repository).save(any(User.class));
    }

    @Test
    void duplicateUsernameAndEmailReturn409() {
        when(repository.existsByUsername("alice")).thenReturn(true);
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.register(
                new RegisterRequestDto("alice", "new@example.com", "password123")))
                .getStatusCode().value());
        when(repository.existsByEmail("alice@example.com")).thenReturn(true);
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.register(
                new RegisterRequestDto("newuser", "alice@example.com", "password123")))
                .getStatusCode().value());
        verify(repository, never()).save(any());
    }
}
