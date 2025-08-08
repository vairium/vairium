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
import tech.siloxa.vairium.domain.FieldDefinition;
import tech.siloxa.vairium.repository.FieldDefinitionRepository;
import tech.siloxa.vairium.repository.search.FieldDefinitionSearchRepository;
import tech.siloxa.vairium.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link tech.siloxa.vairium.domain.FieldDefinition}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class FieldDefinitionResource {

    private final Logger log = LoggerFactory.getLogger(FieldDefinitionResource.class);

    private static final String ENTITY_NAME = "fieldDefinition";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FieldDefinitionRepository fieldDefinitionRepository;

    private final FieldDefinitionSearchRepository fieldDefinitionSearchRepository;

    public FieldDefinitionResource(
        FieldDefinitionRepository fieldDefinitionRepository,
        FieldDefinitionSearchRepository fieldDefinitionSearchRepository
    ) {
        this.fieldDefinitionRepository = fieldDefinitionRepository;
        this.fieldDefinitionSearchRepository = fieldDefinitionSearchRepository;
    }

    /**
     * {@code POST  /field-definitions} : Create a new fieldDefinition.
     *
     * @param fieldDefinition the fieldDefinition to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new fieldDefinition, or with status {@code 400 (Bad Request)} if the fieldDefinition has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/field-definitions")
    public ResponseEntity<FieldDefinition> createFieldDefinition(@RequestBody FieldDefinition fieldDefinition) throws URISyntaxException {
        log.debug("REST request to save FieldDefinition : {}", fieldDefinition);
        if (fieldDefinition.getId() != null) {
            throw new BadRequestAlertException("A new fieldDefinition cannot already have an ID", ENTITY_NAME, "idexists");
        }
        FieldDefinition result = fieldDefinitionRepository.save(fieldDefinition);
        fieldDefinitionSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/field-definitions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /field-definitions/:id} : Updates an existing fieldDefinition.
     *
     * @param id the id of the fieldDefinition to save.
     * @param fieldDefinition the fieldDefinition to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fieldDefinition,
     * or with status {@code 400 (Bad Request)} if the fieldDefinition is not valid,
     * or with status {@code 500 (Internal Server Error)} if the fieldDefinition couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/field-definitions/{id}")
    public ResponseEntity<FieldDefinition> updateFieldDefinition(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody FieldDefinition fieldDefinition
    ) throws URISyntaxException {
        log.debug("REST request to update FieldDefinition : {}, {}", id, fieldDefinition);
        if (fieldDefinition.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fieldDefinition.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fieldDefinitionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        FieldDefinition result = fieldDefinitionRepository.save(fieldDefinition);
        fieldDefinitionSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fieldDefinition.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /field-definitions/:id} : Partial updates given fields of an existing fieldDefinition, field will ignore if it is null
     *
     * @param id the id of the fieldDefinition to save.
     * @param fieldDefinition the fieldDefinition to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fieldDefinition,
     * or with status {@code 400 (Bad Request)} if the fieldDefinition is not valid,
     * or with status {@code 404 (Not Found)} if the fieldDefinition is not found,
     * or with status {@code 500 (Internal Server Error)} if the fieldDefinition couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/field-definitions/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FieldDefinition> partialUpdateFieldDefinition(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody FieldDefinition fieldDefinition
    ) throws URISyntaxException {
        log.debug("REST request to partial update FieldDefinition partially : {}, {}", id, fieldDefinition);
        if (fieldDefinition.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fieldDefinition.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fieldDefinitionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FieldDefinition> result = fieldDefinitionRepository
            .findById(fieldDefinition.getId())
            .map(existingFieldDefinition -> {
                if (fieldDefinition.getName() != null) {
                    existingFieldDefinition.setName(fieldDefinition.getName());
                }
                if (fieldDefinition.getDataType() != null) {
                    existingFieldDefinition.setDataType(fieldDefinition.getDataType());
                }
                if (fieldDefinition.getIsRequired() != null) {
                    existingFieldDefinition.setIsRequired(fieldDefinition.getIsRequired());
                }
                if (fieldDefinition.getIsUnique() != null) {
                    existingFieldDefinition.setIsUnique(fieldDefinition.getIsUnique());
                }
                if (fieldDefinition.getDefaultValue() != null) {
                    existingFieldDefinition.setDefaultValue(fieldDefinition.getDefaultValue());
                }
                if (fieldDefinition.getRelationSchemaId() != null) {
                    existingFieldDefinition.setRelationSchemaId(fieldDefinition.getRelationSchemaId());
                }
                if (fieldDefinition.getRelationType() != null) {
                    existingFieldDefinition.setRelationType(fieldDefinition.getRelationType());
                }

                return existingFieldDefinition;
            })
            .map(fieldDefinitionRepository::save)
            .map(savedFieldDefinition -> {
                fieldDefinitionSearchRepository.save(savedFieldDefinition);

                return savedFieldDefinition;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fieldDefinition.getId().toString())
        );
    }

    /**
     * {@code GET  /field-definitions} : get all the fieldDefinitions.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fieldDefinitions in body.
     */
    @GetMapping("/field-definitions")
    public List<FieldDefinition> getAllFieldDefinitions() {
        log.debug("REST request to get all FieldDefinitions");
        return fieldDefinitionRepository.findAll();
    }

    /**
     * {@code GET  /field-definitions/:id} : get the "id" fieldDefinition.
     *
     * @param id the id of the fieldDefinition to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fieldDefinition, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/field-definitions/{id}")
    public ResponseEntity<FieldDefinition> getFieldDefinition(@PathVariable Long id) {
        log.debug("REST request to get FieldDefinition : {}", id);
        Optional<FieldDefinition> fieldDefinition = fieldDefinitionRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(fieldDefinition);
    }

    /**
     * {@code DELETE  /field-definitions/:id} : delete the "id" fieldDefinition.
     *
     * @param id the id of the fieldDefinition to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/field-definitions/{id}")
    public ResponseEntity<Void> deleteFieldDefinition(@PathVariable Long id) {
        log.debug("REST request to delete FieldDefinition : {}", id);
        fieldDefinitionRepository.deleteById(id);
        fieldDefinitionSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/field-definitions?query=:query} : search for the fieldDefinition corresponding
     * to the query.
     *
     * @param query the query of the fieldDefinition search.
     * @return the result of the search.
     */
    @GetMapping("/_search/field-definitions")
    public List<FieldDefinition> searchFieldDefinitions(@RequestParam String query) {
        log.debug("REST request to search FieldDefinitions for query {}", query);
        return StreamSupport.stream(fieldDefinitionSearchRepository.search(query).spliterator(), false).collect(Collectors.toList());
    }
}
