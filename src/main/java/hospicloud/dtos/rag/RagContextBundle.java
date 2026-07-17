package hospicloud.dtos.rag;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RagContextBundle {
    private String scope;
    private String contextText = "";
    private Set<String> sources = new LinkedHashSet<>();
    private List<String> warnings = new ArrayList<>();
    private List<String> missingFields = new ArrayList<>();

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getContextText() { return contextText; }
    public void setContextText(String contextText) { this.contextText = contextText != null ? contextText : ""; }
    public Set<String> getSources() { return sources; }
    public void setSources(Set<String> sources) { this.sources = sources != null ? sources : new LinkedHashSet<>(); }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings != null ? warnings : new ArrayList<>(); }
    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields != null ? missingFields : new ArrayList<>();
    }
}
