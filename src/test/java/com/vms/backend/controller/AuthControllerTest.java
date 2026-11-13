package com.vms.backend.controller;

import com.vms.backend.config.SecurityConfig;
import com.vms.backend.config.WebConfig;
import com.vms.backend.security.JwtAuthFilter;
import com.vms.backend.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("POST /api/auth/login returns 200 OK with token on valid credentials")
    void login_validCredentials_returns200WithToken() throws Exception {
        when(jwtService.generateToken(anyString())).thenReturn("mock-jwt-token");

        String loginJson = """
                {
                  "email": "admin@vms.test",
                  "password": "password"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.email").value("admin@vms.test"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 Unauthorized on invalid password")
    void login_invalidPassword_returns401() throws Exception {
        String loginJson = """
                {
                  "email": "admin@vms.test",
                  "password": "wrong-password"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 Unauthorized on invalid email")
    void login_invalidEmail_returns401() throws Exception {
        String loginJson = """
                {
                  "email": "unknown@vms.test",
                  "password": "password"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 400 Bad Request when fields are blank")
    void login_blankFields_returns400() throws Exception {
        String loginJson = """
                {
                  "email": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }
}
