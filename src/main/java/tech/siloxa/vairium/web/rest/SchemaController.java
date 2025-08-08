package tech.siloxa.vairium.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tech.siloxa.vairium.domain.SchemaDefinition;
import tech.siloxa.vairium.service.SchemaService;
import tech.siloxa.vairium.web.rest.errors.BadRequestAlertException;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/schemas")
public class SchemaController {

    private static final String ENTITY_NAME = "schemaDefinition";

    @Resource
    private SchemaService schemaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createSchema(@Valid @RequestBody SchemaDefinition schemaDefinition) {
        if (schemaDefinition.getId() != null) {
            throw new BadRequestAlertException("A new schemaDefinition cannot already have an ID", ENTITY_NAME, "idexists");
        }

        schemaService.create(schemaDefinition);
    }
}
