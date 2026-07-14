package hospicloud.dtos;

import java.util.ArrayList;
import java.util.List;

public class PharmacyDispenseRequestDTO {
    private Integer idPatient;
    private Integer idConsultation;
    private List<Item> items = new ArrayList<>();

    public Integer getIdPatient() { return idPatient; }
    public void setIdPatient(Integer idPatient) { this.idPatient = idPatient; }

    public Integer getIdConsultation() { return idConsultation; }
    public void setIdConsultation(Integer idConsultation) { this.idConsultation = idConsultation; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        private Long medicamentId;
        private Integer quantite = 1;

        public Long getMedicamentId() { return medicamentId; }
        public void setMedicamentId(Long medicamentId) { this.medicamentId = medicamentId; }

        public Integer getQuantite() { return quantite; }
        public void setQuantite(Integer quantite) { this.quantite = quantite; }
    }
}
