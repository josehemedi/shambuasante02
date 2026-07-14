package hospicloud.servicesImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiveKitServiceTest {

    private LiveKitService liveKitService;

    @BeforeEach
    void setUp() {
        liveKitService = new LiveKitService();
        ReflectionTestUtils.setField(liveKitService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(liveKitService, "apiSecret", "test-api-secret-with-enough-length");
        ReflectionTestUtils.setField(liveKitService, "liveKitUrl", "wss://example.livekit.cloud");
    }

    @Test
    void shouldGenerateTenantScopedRoomName() {
        assertEquals("tenant-7-teleconsultation-42", liveKitService.generateRoomName(7, 42));
    }

    @Test
    void shouldGenerateJwtTokenForRoom() {
        String token = liveKitService.generateToken(
                "tenant-1-teleconsultation-16",
                "medecin-1",
                "Dr Test");

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void shouldReportConfiguredWhenKeysPresent() {
        assertTrue(liveKitService.isConfigured());
        assertEquals("wss://example.livekit.cloud", liveKitService.getLiveKitUrl());
    }
}
