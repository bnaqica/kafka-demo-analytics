package com.kafkademo.analytics.api;

import static com.kafkademo.analytics.TestTokens.admin;
import static com.kafkademo.analytics.TestTokens.viewer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kafkademo.analytics.config.SecurityConfig;
import com.kafkademo.analytics.dto.response.AnalyticsEventResponse;
import com.kafkademo.analytics.exception.AnalyticsEventNotFoundException;
import com.kafkademo.analytics.exception.DuplicateAnalyticsEventException;
import com.kafkademo.analytics.service.AnalyticsEventService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsEventController.class)
@Import(SecurityConfig.class)
class AnalyticsEventControllerTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final String BODY = """
            {"eventType":"customer-addition","sourceTopic":"customer-change-event","occurredAt":"2026-01-01T10:00:00Z"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsEventService eventService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void returnsRecentEvents() throws Exception {
        when(eventService.findRecent(5)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/analytics/events").with(admin()).param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value(ID.toString()))
                .andExpect(jsonPath("$[0].eventType").value("customer-addition"))
                .andExpect(jsonPath("$[0].sourceTopic").value("customer-change-event"))
                .andExpect(jsonPath("$[0].customerId").value(CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$[0].occurredAt").value("2026-01-01T10:00:00Z"))
                .andExpect(jsonPath("$[0].receivedAt").value("2026-01-01T10:00:01Z"));
    }

    @Test
    void rejectsLimitOutOfRange() throws Exception {
        mockMvc.perform(get("/api/analytics/events").with(admin()).param("limit", "1000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(eventService.create(any())).thenReturn(response());

        mockMvc.perform(post("/api/analytics/events")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/analytics/events/" + ID));
    }

    @Test
    void createDuplicateReturns409() throws Exception {
        when(eventService.create(any())).thenThrow(new DuplicateAnalyticsEventException(ID));

        mockMvc.perform(post("/api/analytics/events")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void getUnknownEventReturns404() throws Exception {
        when(eventService.get(ID)).thenThrow(new AnalyticsEventNotFoundException(ID));

        mockMvc.perform(get("/api/analytics/events/" + ID).with(admin())).andExpect(status().isNotFound());
    }

    @Test
    void updateReturnsEvent() throws Exception {
        when(eventService.update(eq(ID), any())).thenReturn(response());

        mockMvc.perform(put("/api/analytics/events/" + ID)
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(ID.toString()));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/analytics/events/" + ID).with(admin())).andExpect(status().isNoContent());

        verify(eventService).delete(ID);
    }

    @Test
    void viewerCanListEvents() throws Exception {
        mockMvc.perform(get("/api/analytics/events").with(viewer())).andExpect(status().isOk());
    }

    @Test
    void viewerCanGetAnEvent() throws Exception {
        when(eventService.get(ID)).thenReturn(response());

        mockMvc.perform(get("/api/analytics/events/" + ID).with(viewer())).andExpect(status().isOk());
    }

    @Test
    void viewerCannotCreate() throws Exception {
        mockMvc.perform(post("/api/analytics/events")
                        .with(viewer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"x","sourceTopic":"t","occurredAt":"2026-01-01T10:00:00Z"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotUpdate() throws Exception {
        mockMvc.perform(put("/api/analytics/events/" + ID)
                        .with(viewer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"x","sourceTopic":"t","occurredAt":"2026-01-01T10:00:00Z"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotDelete() throws Exception {
        mockMvc.perform(delete("/api/analytics/events/" + ID).with(viewer())).andExpect(status().isForbidden());

        verify(eventService, never()).delete(any());
    }

    private static AnalyticsEventResponse response() {
        return new AnalyticsEventResponse(
                ID,
                "customer-addition",
                "customer-change-event",
                CUSTOMER_ID,
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:00:01Z"),
                "{}");
    }
}
