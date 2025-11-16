package org.example.util;

import inet.ipaddr.mac.MACAddress;
import org.example.exception.MalformedMacAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringToMacConverterTest {

    @Test
    void shouldConvertStringToMac() {
        // given
        String validMacString = "00:1a:2b:3c:4d:5e";

        // when
        MACAddress result = StringToMacConverter.convertToMac(validMacString);

        // then
        assertNotNull(result);
        assertEquals(validMacString, result.toColonDelimitedString());
    }

    @Test
    void shouldThrowMalformedMacAddressWhenMacIsInvalid() {
        // given
        String malformedMacString = "00:1A:2B:3C:4D:5E:FF";

        // when / then
        Exception exception = assertThrows(MalformedMacAddress.class, () -> {
            StringToMacConverter.convertToMac(malformedMacString);
        });

        assertTrue(exception.getMessage().contains(malformedMacString));
        assertTrue(exception.getMessage().contains("Malformed MAC address"));
    }

    @Test
    void shouldIgnoreInputFormatting() {
        // given
        var a = StringToMacConverter.convertToMac("00:1A:2B:3C:4D:5E");
        var b = StringToMacConverter.convertToMac("00-1A-2B-3C-4D-5E");

        // then
        assertEquals(a, b);
    }
}