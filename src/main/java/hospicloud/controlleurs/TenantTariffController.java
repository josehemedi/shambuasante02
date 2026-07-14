package hospicloud.controlleurs;

import hospicloud.dtos.TarifHopitalDTO;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;
import hospicloud.repositories.BillingCompositionRepository;
import hospicloud.security.UtilisateurPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Grille tarifaire de l'hôpital — fixée uniquement par l'administrateur.
 * Ces prix sont appliqués automatiquement à chaque soin consommé.
 */
@RestController
@RequestMapping("/api/tenant-admin/tariffs")
public class TenantTariffController {

    private final BillingCompositionRepository billingCompositionRepository;

    public TenantTariffController(BillingCompositionRepository billingCompositionRepository) {
        this.billingCompositionRepository = billingCompositionRepository;
    }

    @GetMapping
    public ResponseEntity<List<TarifHopitalDTO>> list() {
        Integer hopitalId = requireAdminHopitalId();
        return ResponseEntity.ok(billingCompositionRepository.listTarifs(hopitalId));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> upsert(@RequestBody TarifHopitalDTO tarif) {
        Integer hopitalId = requireAdminHopitalId();
        if (tarif == null || tarif.getLibelle() == null || tarif.getLibelle().isBlank()) {
            throw new BadRequestException("Libellé obligatoire");
        }
        if (tarif.getCategorie() == null || tarif.getCategorie().isBlank()) {
            throw new BadRequestException("Catégorie obligatoire");
        }
        if (tarif.getPrixUnitaire() == null || tarif.getPrixUnitaire().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Prix invalide");
        }
        if (tarif.getCode() == null || tarif.getCode().isBlank()) {
            tarif.setCode(tarif.getLibelle().trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_"));
        }
        int rows = billingCompositionRepository.upsertTarif(hopitalId, tarif);
        return ResponseEntity.ok(Map.of(
                "updated", rows,
                "tariffs", billingCompositionRepository.listTarifs(hopitalId)
        ));
    }

    private Integer requireAdminHopitalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new AccessDeniedException("Authentification requise");
        }
        if (principal.getAppRole() != Role.TENANT_ADMIN) {
            throw new ForbiddenException("Accès réservé à l'administrateur de l'hôpital");
        }
        if (principal.getIdHopital() == null) {
            throw new ForbiddenException("Aucun établissement associé");
        }
        return principal.getIdHopital();
    }
}
