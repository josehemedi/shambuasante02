package hospicloud.saas;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record SaasPlanDefinition(
        String canonicalName,
        BigDecimal monthlyPrice,
        Integer maxStaffUsers,
        Integer teleconsultationMonthlyLimit,
        boolean popular,
        String targetAudienceFr,
        String targetAudienceEn,
        Set<SaasPlanFeature> features,
        List<String> marketingFeaturesFr,
        List<String> marketingFeaturesEn
) {
    public boolean hasFeature(SaasPlanFeature feature) {
        return features.contains(feature);
    }

    public boolean isUnlimitedStaff() {
        return maxStaffUsers == null;
    }

    public boolean isUnlimitedTeleconsultation() {
        return teleconsultationMonthlyLimit == null;
    }
}
