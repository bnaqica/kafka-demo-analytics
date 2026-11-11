package com.kafkademo.analytics.api;

import static com.kafkademo.analytics.TestTokens.admin;
import static com.kafkademo.analytics.TestTokens.viewer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kafkademo.analytics.TestTokens;
import com.kafkademo.analytics.config.SecurityConfig;
import com.kafkademo.analytics.dto.response.AnalyticsSummary;
import com.kafkademo.analytics.service.AnalyticsSummaryService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
@Import(SecurityConfig.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsSummaryService summaryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void returnsSummary() throws Exception {
        when(summaryService.summary()).thenReturn(new AnalyticsSummary(3, Map.of("customer-addition", 3L)));

        mockMvc.perform(get("/api/analytics/summary").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(3))
                .andExpect(jsonPath("$.eventsByType['customer-addition']").value(3));
    }

    @Test
    void viewerCanReadTheSummary() throws Exception {
        when(summaryService.summary()).thenReturn(new AnalyticsSummary(0, Map.of()));

        mockMvc.perform(get("/api/analytics/summary").with(viewer())).andExpect(status().isOk());
    }

    @Test
    void realmRoleFromARealTokenAllowsAnAdmin() throws Exception {
        tokenWithRole("customer-admin");
        when(summaryService.summary()).thenReturn(new AnalyticsSummary(0, Map.of()));

        mockMvc.perform(get("/api/analytics/summary").header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void realmRoleFromARealTokenAllowsAViewer() throws Exception {
        tokenWithRole("customer-viewer");
        when(summaryService.summary()).thenReturn(new AnalyticsSummary(0, Map.of()));

        mockMvc.perform(get("/api/analytics/summary").header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void aTokenWithNoRealmRoleIsForbidden() throws Exception {
        tokenWithRole("offline_access");

        mockMvc.perform(get("/api/analytics/summary").header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void summaryWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/analytics/summary")).andExpect(status().isUnauthorized());
    }

    private void tokenWithRole(String role) {
        when(jwtDecoder.decode("token")).thenReturn(TestTokens.keycloakToken("user", role));
    }
}
