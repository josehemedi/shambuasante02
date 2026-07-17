package hospicloud.controlleurs;

import hospicloud.dtos.rag.RagDocumentDto;
import hospicloud.services.rag.RagAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagAdminService ragAdminService;

    public RagController(RagAdminService ragAdminService) {
        this.ragAdminService = ragAdminService;
    }

    @GetMapping("/documents")
    public ResponseEntity<List<RagDocumentDto>> listDocuments() {
        return ResponseEntity.ok(ragAdminService.listDocuments());
    }

    @PostMapping("/documents")
    public ResponseEntity<RagDocumentDto> create(@RequestBody RagDocumentDto body) {
        return ResponseEntity.ok(ragAdminService.createDocument(body));
    }

    @PutMapping("/documents/{id}")
    public ResponseEntity<RagDocumentDto> update(@PathVariable Long id, @RequestBody RagDocumentDto body) {
        return ResponseEntity.ok(ragAdminService.updateDocument(id, body));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ragAdminService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> analytics() {
        return ResponseEntity.ok(ragAdminService.analytics());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> categories() {
        return ResponseEntity.ok(ragAdminService.categories());
    }
}
