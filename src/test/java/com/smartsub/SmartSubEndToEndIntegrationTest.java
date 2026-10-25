package com.smartsub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SmartSubEndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Unauthenticated request to protected endpoint should return 401 Unauthorized")
    public void testUnauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/api/analytics/dashboard")
                        .header("Authorization", "Bearer invalid-token-xyz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Favicon should return 200 OK")
    public void testFaviconReturns200() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Demo quick-login should accept both GET and POST without 404")
    public void testDemoLoginGetAndPost() throws Exception {
        // Test GET
        mockMvc.perform(get("/api/auth/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("demo"));

        // Test POST
        mockMvc.perform(post("/api/auth/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("demo"));
    }

    @Test
    @DisplayName("Test CORS preflight on protected endpoints allows frontend origin and headers")
    public void testCorsPreflightOnProtectedEndpoints() throws Exception {
        mockMvc.perform(options("/api/analytics/dashboard")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));

        mockMvc.perform(options("/api/analytics/overlaps")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));

        mockMvc.perform(options("/api/ai/advisor")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    @DisplayName("End-to-end Firebase token flow: sync user, access dashboard, overlaps, and AI advisor")
    public void testFirebaseTokenFlow() throws Exception {
        // Construct a valid development Firebase ID token structure
        String header = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"RS256\",\"kid\":\"test-key\",\"typ\":\"JWT\"}".getBytes());
        long exp = (System.currentTimeMillis() / 1000) + 3600;
        String payload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(String.format("{\"user_id\":\"fb-user-999\",\"sub\":\"fb-user-999\",\"email\":\"fbuser@smartsub.io\",\"name\":\"Firebase User\",\"exp\":%d}", exp).getBytes());
        String sig = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("sig".getBytes());
        String firebaseToken = header + "." + payload + "." + sig;

        // 1. Sync via /api/auth/firebase-login
        MvcResult syncResult = mockMvc.perform(post("/api/auth/firebase-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idToken", firebaseToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(firebaseToken))
                .andExpect(jsonPath("$.email").value("fbuser@smartsub.io"))
                .andReturn();

        String authHeader = "Bearer " + firebaseToken;

        // 2. Access /api/analytics/dashboard without 403
        mockMvc.perform(get("/api/analytics/dashboard")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActiveSubscriptions").value(0))
                .andExpect(jsonPath("$.totalMonthlyCost").value(0.0));

        // 3. Access /api/analytics/overlaps without 403
        mockMvc.perform(get("/api/analytics/overlaps")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 4. Access /api/ai/advisor without 403
        String advisorRequest = objectMapper.writeValueAsString(Map.of(
                "query", "What are my subscription recommendations?",
                "maxSuggestions", 2
        ));

        mockMvc.perform(post("/api/ai/advisor")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(advisorRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").isNotEmpty());
    }




    @Test
    @DisplayName("Demo quick-login should authenticate and return valid token and user profile")
    public void testDemoLoginAndAuthenticatedEndpoints() throws Exception {
        // 1. Authenticate with demo endpoint
        MvcResult loginResult = mockMvc.perform(post("/api/auth/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("demo"))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(responseBody);
        String token = loginJson.get("token").asText();
        assertThat(token).isNotBlank();

        String authHeader = "Bearer " + token;

        // 2. Fetch subscriptions using Bearer token
        MvcResult subsResult = mockMvc.perform(get("/api/subscriptions")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode subsJson = objectMapper.readTree(subsResult.getResponse().getContentAsString());
        assertThat(subsJson.isArray()).isTrue();
        assertThat(subsJson.size()).isGreaterThan(0);

        Long firstSubId = subsJson.get(0).get("id").asLong();

        // 3. Test Renewal Calendar endpoint
        mockMvc.perform(get("/api/analytics/calendar")
                        .param("filter", "all")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/analytics/calendar")
                        .param("filter", "7days")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 4. Test AI Advisor endpoint (verifies deterministic fallback when external OpenAI API is offline/unconfigured)
        String advisorRequest = objectMapper.writeValueAsString(Map.of(
                "query", "How can I reduce my recurring monthly subscription expenses by ₹500?",
                "maxSuggestions", 3
        ));

        mockMvc.perform(post("/api/ai/advisor")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(advisorRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").isNotEmpty())
                .andExpect(jsonPath("$.actionItems").isArray());

        // 5. Test What-If Savings Simulator
        String simulateRequest = objectMapper.writeValueAsString(Map.of("candidateCancelIds", List.of(firstSubId)));

        mockMvc.perform(post("/api/analytics/simulate-savings")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simulateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.simulatedCancelledCount").value(1))
                .andExpect(jsonPath("$.potentialMonthlySavings").isNumber())
                .andExpect(jsonPath("$.potentialYearlySavings").isNumber());

        // 6. Test Receipt / Invoice Scanner endpoint
        String mockReceiptText = "Invoice from Netflix Entertainment Services\nAmount: ₹649.00\nBilling Cycle: Monthly\nRenewal Date: 2026-11-15";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.txt",
                "text/plain",
                mockReceiptText.getBytes()
        );

        mockMvc.perform(multipart("/api/import/receipt")
                        .file(file)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detectedServiceName").value("Netflix"))
                .andExpect(jsonPath("$.detectedCost").value(649.0))
                .andExpect(jsonPath("$.detectedBillingCycle").value("MONTHLY"));
    }
}
