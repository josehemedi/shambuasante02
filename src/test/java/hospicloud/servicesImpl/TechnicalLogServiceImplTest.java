package hospicloud.servicesImpl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TechnicalLogServiceImplTest {

    @Test
    void normalizeStatus_mapsBusinessValuesToEnum() {
        assertEquals("INFO", TechnicalLogServiceImpl.normalizeStatus("SUCCES"));
        assertEquals("INFO", TechnicalLogServiceImpl.normalizeStatus("INFO"));
        assertEquals("WARNING", TechnicalLogServiceImpl.normalizeStatus("ECHEC"));
        assertEquals("WARNING", TechnicalLogServiceImpl.normalizeStatus("REFUSE"));
        assertEquals("ERROR", TechnicalLogServiceImpl.normalizeStatus("ERROR"));
    }
}
