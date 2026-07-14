package hospicloud.controlleurs;

import hospicloud.dtos.BonSortieRequestDto;
import hospicloud.dtos.BonSortieResponseDto;
import hospicloud.services.BonSortieService;
import hospicloud.servicesImpl.reportingimpl.JasperReportServiceImpl;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/discharge-notes")
public class BonSortieControllers {

    private final BonSortieService service;
    private final JasperReportServiceImpl jasperService;

    @Autowired
    public BonSortieControllers(BonSortieService service, JasperReportServiceImpl jasperService) {
        this.service = service;
        this.jasperService = jasperService;
    }

    @PostMapping
    public ResponseEntity<BonSortieResponseDto> create(@RequestBody BonSortieRequestDto requestDto) {
        BonSortieResponseDto created = service.createDischargeNote(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BonSortieResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getDischargeNoteById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<BonSortieResponseDto>> getByPatient(@PathVariable Integer patientId) {
        return ResponseEntity.ok(service.getDischargeNotesByPatient(patientId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BonSortieResponseDto> update(@PathVariable Integer id, 
                                                        @RequestBody BonSortieRequestDto requestDto) {
        return ResponseEntity.ok(service.updateDischargeNote(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteDischargeNote(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bulletin")
    public ResponseEntity<byte[]> generateBulletin(@PathVariable Integer id) {
        try {
            Map<String, Object> params = service.getBulletinSortieParams(id);
            
            Map<String, Object> fieldData = new HashMap<>();
            fieldData.put("dateSortie", params.get("dateSortie"));
            fieldData.put("nomPatient", params.get("nomPatient"));
            
            List<Map<String, Object>> dataList = new ArrayList<>();
            dataList.add(fieldData);
            JRDataSource dataSource = new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(dataList);
            
            byte[] pdfContent = jasperService.generate("Bulletin_Sortie.jasper", params, dataSource);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=Bulletin_Sortie_" + id + ".pdf")
                    .body(pdfContent);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage().getBytes());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}