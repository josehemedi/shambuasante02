package hospicloud.servicesImpl;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestOrdonnanceReportService {

    public Map<String, Object> getTestOrdonnanceParams() {
        Map<String, Object> params = new HashMap<>();
        
        // Paramètres requis par le JasperReport
        params.put("NOM_PATIENT", "Jean Dupont");
        params.put("AgePatient", 35);
        // Test avec une valeur fixe pour vérifier que le paramètre est bien passé
        params.put("NOM_MEDECIN", "Dr Martin Dubois");
        System.out.println("TEST - NOM_MEDECIN param: Dr Martin Dubois");
        params.put("SERVICE_MEDECIN", "Médecine Générale");
        params.put("NOM_HOPITAL", "Hôpital Central");
        params.put("DATE_PRESCRIPTION", new java.sql.Timestamp(System.currentTimeMillis()));
        params.put("LOGO_HOPITAL", null);
        
        // IMPORTANT: Le contenu de l'ordonnance
        String contenuOrdonnance = "1. Paracétamol 1000mg: 1 comprimé 3 fois par jour\n" +
                                   "2. Ibuprofène 400mg: 1 comprimé matin et soir\n" +
                                   "3. Repos: 7 jours\n" +
                                   "4. Contrôle médical dans 1 semaine";
        params.put("contenuOrdonnance", contenuOrdonnance);
        
        // QR Code (simplifié)
        params.put("QR_CODE_IMAGE", null); // Vous pouvez ajouter un BufferedImage ici
        
        return params;
    }
    
    public JRDataSource createTestDataSource() {
        // Créer une Map avec le champ requis par le JasperReport
        Map<String, Object> fieldData = new HashMap<>();
        fieldData.put("contenuOrdonnance", 
            "1. Paracétamol 1000mg: 1 comprimé 3 fois par jour\n" +
            "2. Ibuprofène 400mg: 1 comprimé matin et soir\n" +
            "3. Repos: 7 jours\n" +
            "4. Contrôle médical dans 1 semaine");
        
        // Créer une liste avec un seul élément
        List<Map<String, Object>> dataList = new ArrayList<>();
        dataList.add(fieldData);
        
        // Créer le DataSource
        return new JRBeanCollectionDataSource(dataList);
    }
}