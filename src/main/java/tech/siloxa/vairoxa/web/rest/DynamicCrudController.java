package tech.siloxa.vairoxa.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.siloxa.vairoxa.service.DataService;
import tech.siloxa.vairoxa.web.rest.errors.BadRequestAlertException;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/{schemaName}")
public class DynamicCrudController {

    @Resource
    private DataService dataService;

    @PostMapping
    public Map<String, Object> create(@PathVariable String schemaName, @RequestBody Map<String, Object> payload) throws JsonProcessingException {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("Payload must not be empty");
        }
        if (payload.get("id") != null) {
            throw new BadRequestAlertException("A new item cannot already have an ID", schemaName, "idexists");
        }

        return dataService.create(schemaName, payload);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> read(@PathVariable String schemaName, @PathVariable String id) {
        final Map<String, Object> read = dataService.read(schemaName, id);
        if (read == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(read);
    }

    @GetMapping
    public List<Map<String, Object>> readAll(@PathVariable String schemaName) {
        return dataService.readAll(schemaName);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String schemaName, @PathVariable String id, @RequestBody Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("Payload must not be empty");
        }
        if (payload.get("id") == null) {
            throw new BadRequestAlertException("Invalid id", schemaName, "idnull");
        }
        if (!Objects.equals(id, payload.get("id"))) {
            throw new BadRequestAlertException("Invalid ID", schemaName, "idinvalid");
        }

        final Pair<Integer, Map<String, Object>> updated = dataService.update(schemaName, id, payload);
        if (updated.getFirst() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated.getSecond());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String schemaName, @PathVariable String id) {
        final int deleted = dataService.delete(schemaName, id);
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
