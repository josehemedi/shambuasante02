package hospicloud.dtos;

import java.math.BigDecimal;
import java.util.List;

public class HospitalPlanCatalogDTO {
    private String name;
    private BigDecimal price;
    private long subscribers;
    private boolean popular;
    private List<String> features;
    private List<String> featuresEn;
    private List<String> featureKeys;
    private Integer maxUsers;
    private Integer teleconsultationMonthlyLimit;
    private String targetAudienceFr;
    private String targetAudienceEn;

    public HospitalPlanCatalogDTO() {
    }

    public HospitalPlanCatalogDTO(String name, BigDecimal price, long subscribers, boolean popular, List<String> features) {
        this.name = name;
        this.price = price;
        this.subscribers = subscribers;
        this.popular = popular;
        this.features = features;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public long getSubscribers() { return subscribers; }
    public void setSubscribers(long subscribers) { this.subscribers = subscribers; }

    public boolean isPopular() { return popular; }
    public void setPopular(boolean popular) { this.popular = popular; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public List<String> getFeaturesEn() { return featuresEn; }
    public void setFeaturesEn(List<String> featuresEn) { this.featuresEn = featuresEn; }

    public List<String> getFeatureKeys() { return featureKeys; }
    public void setFeatureKeys(List<String> featureKeys) { this.featureKeys = featureKeys; }

    public Integer getMaxUsers() { return maxUsers; }
    public void setMaxUsers(Integer maxUsers) { this.maxUsers = maxUsers; }

    public Integer getTeleconsultationMonthlyLimit() { return teleconsultationMonthlyLimit; }
    public void setTeleconsultationMonthlyLimit(Integer teleconsultationMonthlyLimit) {
        this.teleconsultationMonthlyLimit = teleconsultationMonthlyLimit;
    }

    public String getTargetAudienceFr() { return targetAudienceFr; }
    public void setTargetAudienceFr(String targetAudienceFr) { this.targetAudienceFr = targetAudienceFr; }

    public String getTargetAudienceEn() { return targetAudienceEn; }
    public void setTargetAudienceEn(String targetAudienceEn) { this.targetAudienceEn = targetAudienceEn; }
}
