package io.github.juli0mendes.walletmanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JacksonConfig Unit Tests")
class JacksonConfigTest {

    private final JacksonConfig jacksonConfig = new JacksonConfig();

    @Test
    @DisplayName("Should create ObjectMapper with correct configuration")
    void shouldCreateObjectMapperWithCorrectConfiguration() {
        ObjectMapper objectMapper = jacksonConfig.objectMapper();

        assertNotNull(objectMapper);
        assertTrue(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) == false);
    }

    @Test
    @DisplayName("Should register JavaTimeModule")
    void shouldRegisterJavaTimeModule() {
        ObjectMapper objectMapper = jacksonConfig.objectMapper();

        assertNotNull(objectMapper.findModules().stream()
                .filter(module -> module instanceof JavaTimeModule)
                .findFirst()
                .orElse(null));
    }

    @Test
    @DisplayName("Should disable unknown properties failure")
    void shouldDisableUnknownPropertiesFailure() {
        ObjectMapper objectMapper = jacksonConfig.objectMapper();

        assertTrue(objectMapper.isEnabled(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) == false);
    }

    @Test
    @DisplayName("Should serialize LocalDateTime correctly")
    void shouldSerializeLocalDateTimeCorrectly() throws Exception {
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 28, 10, 30, 45);

        String json = objectMapper.writeValueAsString(dateTime);

        assertNotNull(json);
        assertTrue(json.contains("2024-03-28"));
        assertTrue(json.contains("10:30:45"));
        assertFalse(json.matches("\\d+"));
    }

    @Test
    @DisplayName("Should deserialize LocalDateTime correctly")
    void shouldDeserializeLocalDateTimeCorrectly() throws Exception {
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        String json = "\"2024-03-28T10:30:45\"";

        LocalDateTime result = objectMapper.readValue(json, LocalDateTime.class);

        assertNotNull(result);
        assertEquals(2024, result.getYear());
        assertEquals(3, result.getMonthValue());
        assertEquals(28, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    @Test
    @DisplayName("Should handle unknown properties gracefully")
    void shouldHandleUnknownPropertiesGracefully() throws Exception {
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        String json = "{\"knownField\":\"value\",\"unknownField\":\"ignored\"}";

        TestObject result = objectMapper.readValue(json, TestObject.class);

        assertNotNull(result);
        assertEquals("value", result.knownField);
    }

    private static class TestObject {
        public String knownField;
    }
}
