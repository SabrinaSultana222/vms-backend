package com.vms.backend.controller;

import com.vms.backend.config.SecurityConfig;
import com.vms.backend.config.WebConfig;
import com.vms.backend.security.JwtAuthFilter;
import com.vms.backend.security.JwtService;
import com.vms.backend.service.HealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import({HealthService.class, SecurityConfig.class, WebConfig.class, JwtAuthFilter.class})
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getHealthReturnsUpStatus() throws Exception {
        mockMvc.perform(get("/api/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("Vehicle Management System Backend is running"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
