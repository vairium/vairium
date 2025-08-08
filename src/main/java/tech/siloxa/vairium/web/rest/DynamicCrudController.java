package tech.siloxa.vairium.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.*;
import tech.siloxa.vairium.service.DataService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/{schemaName}")
public class DynamicCrudController {

    @Resource
    private DataService dataService;

    @PostMapping
    public Object create(@PathVariable String schemaName, @RequestBody Map<String, Object> payload) throws JsonProcessingException {
        return dataService.create(schemaName, payload);
    }

    @GetMapping("/{id}")
    public Object read(@PathVariable String schemaName, @PathVariable String id) {
        return dataService.read(schemaName, id);
    }

    @GetMapping
    public List<Map<String, Object>> readAll(@PathVariable String schemaName) {
        return dataService.readAll(schemaName);
    }
}
