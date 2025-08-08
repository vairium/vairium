package tech.siloxa.vairium.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;
import tech.siloxa.vairium.domain.SchemaDefinition;
import tech.siloxa.vairium.repository.SchemaDefinitionRepository;
import tech.siloxa.vairium.repository.search.SchemaDefinitionSearchRepository;
import tech.siloxa.vairium.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link tech.siloxa.vairium.domain.SchemaDefinition}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class SchemaDefinitionResource {

    private final Logger log = LoggerFactory.getLogger(SchemaDefinitionResource.class);

    private static final String ENTITY_NAME = "schemaDefinition";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SchemaDefinitionRepository schemaDefinitionRepository;

    private final SchemaDefinitionSearchRepository schemaDefinitionSearchRepository;

    public SchemaDefinitionResource(
        SchemaDefinitionRepository schemaDefinitionRepository,
        SchemaDefinitionSearchRepository schemaDefinitionSearchRepository
    ) {
        this.schemaDefinitionRepository = schemaDefinitionRepository;
        this.schemaDefinitionSearchRepository = schemaDefinitionSearchRepository;
    }

    /**
     * {@code POST  /schema-definitions} : Create a new schemaDefinition.
     *
     * @param schemaDefinition the schemaDefinition to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new schemaDefinition, or with status {@code 400 (Bad Request)} if the schemaDefinition has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/schema-definitions")
    public ResponseEntity<SchemaDefinition> createSchemaDefinition(@RequestBody SchemaDefinition schemaDefinition)
        throws URISyntaxException {
        log.debug("REST request to save SchemaDefinition : {}", schemaDefinition);
        if (schemaDefinition.getId() != null) {
            throw new BadRequestAlertException("A new schemaDefinition cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SchemaDefinition result = schemaDefinitionRepository.save(schemaDefinition);
        schemaDefinitionSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/schema-definitions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /schema-definitions/:id} : Updates an existing schemaDefinition.
     *
     * @param id the id of the schemaDefinition to save.
     * @param schemaDefinition the schemaDefinition to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated schemaDefinition,
     * or with status {@code 400 (Bad Request)} if the schemaDefinition is not valid,
     * or with status {@code 500 (Internal Server Error)} if the schemaDefinition couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/schema-definitions/{id}")
    public ResponseEntity<SchemaDefinition> updateSchemaDefinition(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SchemaDefinition schemaDefinition
    ) throws URISyntaxException {
        log.debug("REST request to update SchemaDefinition : {}, {}", id, schemaDefinition);
        if (schemaDefinition.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, schemaDefinition.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!schemaDefinitionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        SchemaDefinition result = schemaDefinitionRepository.save(schemaDefinition);
        schemaDefinitionSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, schemaDefinition.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /schema-definitions/:id} : Partial updates given fields of an existing schemaDefinition, field will ignore if it is null
     *
     * @param id the id of the schemaDefinition to save.
     * @param schemaDefinition the schemaDefinition to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated schemaDefinition,
     * or with status {@code 400 (Bad Request)} if the schemaDefinition is not valid,
     * or with status {@code 404 (Not Found)} if the schemaDefinition is not found,
     * or with status {@code 500 (Internal Server Error)} if the schemaDefinition couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/schema-definitions/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SchemaDefinition> partialUpdateSchemaDefinition(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SchemaDefinition schemaDefinition
    ) throws URISyntaxException {
        log.debug("REST request to partial update SchemaDefinition partially : {}, {}", id, schemaDefinition);
        if (schemaDefinition.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, schemaDefinition.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!schemaDefinitionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SchemaDefinition> result = schemaDefinitionRepository
            .findById(schemaDefinition.getId())
            .map(existingSchemaDefinition -> {
                if (schemaDefinition.getName() != null) {
                    existingSchemaDefinition.setName(schemaDefinition.getName());
                }

                return existingSchemaDefinition;
            })
            .map(schemaDefinitionRepository::save)
            .map(savedSchemaDefinition -> {
                schemaDefinitionSearchRepository.save(savedSchemaDefinition);

                return savedSchemaDefinition;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, schemaDefinition.getId().toString())
        );
    }

    /**
     * {@code GET  /schema-definitions} : get all the schemaDefinitions.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of schemaDefinitions in body.
     */
    @GetMapping("/schema-definitions")
    public List<SchemaDefinition> getAllSchemaDefinitions() {
        log.debug("REST request to get all SchemaDefinitions");
        return schemaDefinitionRepository.findAll();
    }

    /**
     * {@code GET  /schema-definitions/:id} : get the "id" schemaDefinition.
     *
     * @param id the id of the schemaDefinition to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the schemaDefinition, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/schema-definitions/{id}")
    public ResponseEntity<SchemaDefinition> getSchemaDefinition(@PathVariable Long id) {
        log.debug("REST request to get SchemaDefinition : {}", id);
        Optional<SchemaDefinition> schemaDefinition = schemaDefinitionRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(schemaDefinition);
    }

    /**
     * {@code DELETE  /schema-definitions/:id} : delete the "id" schemaDefinition.
     *
     * @param id the id of the schemaDefinition to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/schema-definitions/{id}")
    public ResponseEntity<Void> deleteSchemaDefinition(@PathVariable Long id) {
        log.debug("REST request to delete SchemaDefinition : {}", id);
        schemaDefinitionRepository.deleteById(id);
        schemaDefinitionSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/schema-definitions?query=:query} : search for the schemaDefinition corresponding
     * to the query.
     *
     * @param query the query of the schemaDefinition search.
     * @return the result of the search.
     */
    @GetMapping("/_search/schema-definitions")
    public List<SchemaDefinition> searchSchemaDefinitions(@RequestParam String query) {
        log.debug("REST request to search SchemaDefinitions for query {}", query);
        return StreamSupport.stream(schemaDefinitionSearchRepository.search(query).spliterator(), false).collect(Collectors.toList());
    }
}
