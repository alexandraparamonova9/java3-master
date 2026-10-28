package ru.itmo.web.student;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.web.auth.AuthController;
import ru.itmo.web.auth.AuthService;
import ru.itmo.web.config.SecurityConfig;
import ru.itmo.web.security.CustomUserDetailsService;
import ru.itmo.web.security.JwtAuthFilter;
import ru.itmo.web.security.JwtService;
import ru.itmo.web.user.Role;
import ru.itmo.web.user.User;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({StudentController.class, AuthController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
@TestPropertySource(properties = {"jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", "logging.file.name="})
class StudentControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private JwtService jwt;
    @MockBean private StudentService students;
    @MockBean private AuthService auth;
    @MockBean private CustomUserDetailsService users;

    private String bearer(Role role) {
        String username = role.name().toLowerCase();
        UserDetails details = org.springframework.security.core.userdetails.User
                .withUsername(username).password("unused").roles(role.name()).build();
        when(users.loadUserByUsername(username)).thenReturn(details);
        return "Bearer " + jwt.generateAccessToken(new User(username, username + "@example.com", "unused", role));
    }

    @Test
    void idePreviewCanSendJsonAndBearerPreflight() throws Exception {
        for (String origin : List.of("http://localhost:63342", "http://127.0.0.1:63342")) {
            for (String method : List.of("GET", "POST", "PUT", "PATCH", "DELETE")) {
                mvc.perform(options("/api/students")
                                .header("Origin", origin)
                                .header("Access-Control-Request-Method", method)
                                .header("Access-Control-Request-Headers", "authorization,content-type"))
                        .andExpect(status().isOk())
                        .andExpect(header().string("Access-Control-Allow-Origin", origin));
            }
        }
        verifyNoInteractions(students);
    }

    @Test
    void corsDoesNotGrantOtherWebsitesAccess() throws Exception {
        mvc.perform(options("/api/auth/login")
                        .header("Origin", "https://other.example")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        verifyNoInteractions(auth);
    }

    @Test
    void idePreviewStillRequiresAuthenticationAndAdminRole() throws Exception {
        String origin = "http://localhost:63342";
        mvc.perform(get("/api/students").header("Origin", origin))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", origin));
        mvc.perform(delete("/api/students/1").header("Origin", origin)
                        .header("Authorization", bearer(Role.USER)))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Access-Control-Allow-Origin", origin));
        verifyNoInteractions(students);
    }

    @Test
    void anonymousRequestsReturn401() throws Exception {
        mvc.perform(get("/api/students")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.status").value(401));
        verifyNoInteractions(students);
    }

    @Test
    void userCanReadButCannotModifyStudents() throws Exception {
        String token = bearer(Role.USER);
        when(students.getAll()).thenReturn(List.of());
        mvc.perform(get("/api/students").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(post("/api/students").header("Authorization", token)).andExpect(status().isForbidden());
        mvc.perform(put("/api/students/1").header("Authorization", token)).andExpect(status().isForbidden());
        mvc.perform(patch("/api/students/1").header("Authorization", token)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/students/1").header("Authorization", token)).andExpect(status().isForbidden());
        verify(students).getAll();
        verifyNoMoreInteractions(students);
    }

    @Test
    void adminCanCreateAndDeleteStudent() throws Exception {
        String token = bearer(Role.ADMIN);
        when(students.create(any())).thenReturn(new StudentResponseDto(1L, "Иван Иванов", "P3210",
                "123456", 8, "315", LocalDate.of(2026, 9, 1), false, ""));
        mvc.perform(post("/api/students").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"fullName":"Иван Иванов","groupName":"P3210","isuId":"123456",
                         "dormNumber":8,"room":"315","moveInDate":"2026-09-01","isForeigner":false}
                        """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
        mvc.perform(delete("/api/students/1").header("Authorization", token)).andExpect(status().isNoContent());
        verify(students).delete(1L);
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        mvc.perform(get("/api/students").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.status").value(401));
    }

    @Test
    void wrongCredentialsKeep401InsteadOf500() throws Exception {
        when(auth.login(any())).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("Неверный логин или пароль"));
    }

    @Test
    void duplicateRegistrationKeeps409InsteadOf500() throws Exception {
        when(auth.register(any())).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Логин занят"));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error.status").value(409));
    }

    @Test
    void validationReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details.username").exists())
                .andExpect(jsonPath("$.error.details.password").exists());
        verifyNoInteractions(auth);
    }

    @Test
    void unsupportedMethodKeeps405AndAllowHeader() throws Exception {
        mvc.perform(get("/api/auth/login")).andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "POST"))
                .andExpect(jsonPath("$.error.status").value(405));
    }

    @Test
    void unsupportedContentTypeKeeps415() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.TEXT_PLAIN).content("invalid"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error.status").value(415));
    }

    @Test
    void missingStaticResourceKeeps404() throws Exception {
        mvc.perform(get("/js/missing.js")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.status").value(404));
    }
}
