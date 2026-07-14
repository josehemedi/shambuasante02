package hospicloud.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PhoneNormalizerTest {

    @Test
    void normalize_shouldPrefixGuineaWhenLocalNumber() {
        assertEquals("+224620000001", PhoneNormalizer.normalize("0620000001", "+224"));
    }

    @Test
    void normalize_shouldKeepInternationalFormat() {
        assertEquals("+33612345678", PhoneNormalizer.normalize("+33 6 12 34 56 78", "+224"));
    }

    @Test
    void normalize_shouldConvertDoubleZeroPrefix() {
        assertEquals("+224620000001", PhoneNormalizer.normalize("00224620000001", "+224"));
    }

    @Test
    void normalize_shouldReturnNullForBlank() {
        assertNull(PhoneNormalizer.normalize("   ", "+224"));
    }
}
