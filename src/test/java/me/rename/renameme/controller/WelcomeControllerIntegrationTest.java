package me.rename.renameme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.rename.renameme.TestType;
import me.rename.renameme.request.WelcomeMessageRequest;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag(TestType.IntegrationTest)
public class WelcomeControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should not have welcomed when the WelcomeMessage has not been found")
    void shouldNotHaveWelcomed() throws Exception {
        final var id = RandomUtils.nextInt(2, Integer.MAX_VALUE);
        mockMvc.perform(get("/api/welcome/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should have welcomed when the WelcomeMessage has been found")
    void shouldHaveWelcomed() throws Exception {
        mockMvc.perform(get("/api/welcome/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Should have persisted the new WelcomeMessage")
    void shouldHaveAddedWelcome() throws Exception {
        final var welcomeMessageRequest = new WelcomeMessageRequest();
        welcomeMessageRequest.setContent(randomAlphanumeric(16));
        mockMvc.perform(post("/api/welcome")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(welcomeMessageRequest))
        ).andExpect(status().isCreated());
    }
}
