package hospicloud.utils;

import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Hopital;
import hospicloud.repositories.HopitalRepository;
import hospicloud.security.TenantAuthorization;

import java.util.Map;

public final class TenantReportParamsHelper {

    private TenantReportParamsHelper() {
    }

    public static Hopital resolveActiveHopital(HopitalRepository hopitalRepository, Integer idHopital) {
        Hopital hopital = resolveHopital(hopitalRepository, idHopital);
        if (!hopital.isEstActif()) {
            throw new ForbiddenException("Établissement suspendu — document indisponible.");
        }
        return hopital;
    }

    public static Hopital resolveHopital(HopitalRepository hopitalRepository, Integer idHopital) {
        TenantAuthorization.assertSameTenant(idHopital);
        if (idHopital == null) {
            throw new ForbiddenException("Établissement (tenant) introuvable.");
        }
        Hopital hopital = hopitalRepository.rechercherhopitalParId(idHopital.longValue());
        if (hopital == null) {
            throw new ForbiddenException("Établissement SaaS introuvable.");
        }
        if (!idHopital.equals(hopital.getIdHopital())) {
            throw new ForbiddenException("Violation de périmètre SaaS : établissement incohérent.");
        }
        return hopital;
    }

    public static void applyTenantBranding(Map<String, Object> params, Hopital hopital, Integer idHopital) {
        params.put("NOM_HOPITAL", resolveNomCommercial(hopital, "Shambua Santé"));
        params.put("ID_TENANT", idHopital != null ? String.valueOf(idHopital) : "—");
        params.put("SOUS_DOMAINE", resolveSousDomaine(hopital));
        params.put("INFOS_ETABLISSEMENT", formatInfosEtablissement(hopital));
    }

    public static String resolveNomCommercial(Hopital hopital, String fallback) {
        if (hopital == null) {
            return fallback;
        }
        if (hopital.getNomCommercial() != null && !hopital.getNomCommercial().isBlank()) {
            return hopital.getNomCommercial().trim();
        }
        if (hopital.getNom() != null && !hopital.getNom().isBlank()) {
            return hopital.getNom().trim();
        }
        return fallback;
    }

    public static String resolveSousDomaine(Hopital hopital) {
        if (hopital == null || hopital.getSousDomaine() == null || hopital.getSousDomaine().isBlank()) {
            return "—";
        }
        return hopital.getSousDomaine().trim();
    }

    public static String formatInfosEtablissement(Hopital hopital) {
        if (hopital == null) {
            return "—";
        }
        String adresse = hopital.getAdresseComplete() != null && !hopital.getAdresseComplete().isBlank()
                ? hopital.getAdresseComplete().trim()
                : (hopital.getAdresse() != null ? hopital.getAdresse().trim() : "");
        String ville = hopital.getVille() != null ? hopital.getVille().trim() : "";
        String pays = hopital.getPays() != null ? hopital.getPays().trim() : "";
        StringBuilder sb = new StringBuilder();
        if (!adresse.isBlank()) {
            sb.append(adresse);
        }
        if (!ville.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" · ");
            }
            sb.append(ville);
        }
        if (!pays.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" · ");
            }
            sb.append(pays);
        }
        return sb.isEmpty() ? "—" : sb.toString();
    }
}
