package tech.siloxa.vairoxa.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.siloxa.vairoxa.domain.SchemaDefinition;
import tech.siloxa.vairoxa.service.SchemaService;
import tech.siloxa.vairoxa.web.rest.errors.BadRequestAlertException;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/schemas")
public class SchemaController {

    private static final String ENTITY_NAME = "schemaDefinition";

    @Resource
    private SchemaService schemaService;

    @PostMapping
    public ResponseEntity<SchemaDefinition> createSchema(@Valid @RequestBody SchemaDefinition schemaDefinition) throws URISyntaxException {
        if (schemaDefinition.getId() != null) {
            throw new BadRequestAlertException("A new schemaDefinition cannot already have an ID", ENTITY_NAME, "idexists");
        }

        final SchemaDefinition result = schemaService.create(schemaDefinition);
        if(result == null) {
           return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.created(new URI("/api/schemas/" + result.getId()))
            .body(result);
    }
}
