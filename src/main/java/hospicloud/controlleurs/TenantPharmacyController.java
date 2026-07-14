package hospicloud.controlleurs;

import hospicloud.dtos.CreatePharmacieMedicamentRequest;
import hospicloud.dtos.PharmacieMedicamentDTO;
import hospicloud.dtos.PharmacieStockAlertDTO;
import hospicloud.dtos.PharmacyDispenseRequestDTO;
import hospicloud.services.TenantPharmacyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant-admin/pharmacy")
public class TenantPharmacyController {

    private final TenantPharmacyService tenantPharmacyService;

    public TenantPharmacyController(TenantPharmacyService tenantPharmacyService) {
        this.tenantPharmacyService = tenantPharmacyService;
    }

    @GetMapping("/medicaments")
    public ResponseEntity<List<PharmacieMedicamentDTO>> listMedicaments() {
        return ResponseEntity.ok(tenantPharmacyService.listMedicaments());
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<PharmacieStockAlertDTO>> getStockAlerts() {
        return ResponseEntity.ok(tenantPharmacyService.getStockAlerts());
    }

    @PostMapping("/medicaments")
    public ResponseEntity<PharmacieMedicamentDTO> createMedicament(
            @Valid @RequestBody CreatePharmacieMedicamentRequest request) {
        PharmacieMedicamentDTO created = tenantPharmacyService.createMedicament(request);
        URI location = URI.create("/api/tenant-admin/pharmacy/medicaments/" + created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(created);
    }

    /** Délivre des médicaments → taxe automatiquement le prix de vente sur la facture patient. */
    @PostMapping("/dispense")
    public ResponseEntity<Map<String, Object>> dispense(@RequestBody PharmacyDispenseRequestDTO request) {
        return ResponseEntity.ok(tenantPharmacyService.dispenseToPatient(request));
    }
}