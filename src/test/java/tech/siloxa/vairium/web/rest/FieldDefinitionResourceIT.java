package tech.siloxa.vairium.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tech.siloxa.vairium.IntegrationTest;
import tech.siloxa.vairium.domain.FieldDefinition;
import tech.siloxa.vairium.domain.enumeration.DataType;
import tech.siloxa.vairium.domain.enumeration.RelationType;
import tech.siloxa.vairium.repository.FieldDefinitionRepository;
import tech.siloxa.vairium.repository.search.FieldDefinitionSearchRepository;

/**
 * Integration tests for the {@link FieldDefinitionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class FieldDefinitionResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final DataType DEFAULT_DATA_TYPE = DataType.STRING;
    private static final DataType UPDATED_DATA_TYPE = DataType.INTEGER;

    private static final Boolean DEFAULT_IS_REQUIRED = false;
    private static final Boolean UPDATED_IS_REQUIRED = true;

    private static final Boolean DEFAULT_IS_UNIQUE = false;
    private static final Boolean UPDATED_IS_UNIQUE = true;

    private static final String DEFAULT_DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_DEFAULT_VALUE = "BBBBBBBBBB";

    private static final Long DEFAULT_RELATION_SCHEMA_ID = 1L;
    private static final Long UPDATED_RELATION_SCHEMA_ID = 2L;

    private static final RelationType DEFAULT_RELATION_TYPE = RelationType.ONE_TO_ONE;
    private static final RelationType UPDATED_RELATION_TYPE = RelationType.ONE_TO_MANY;

    private static final String ENTITY_API_URL = "/api/field-definitions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/field-definitions";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private FieldDefinitionRepository fieldDefinitionRepository;

    @Autowired
    private FieldDefinitionSearchRepository fieldDefinitionSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFieldDefinitionMockMvc;

    private FieldDefinition fieldDefinition;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FieldDefinition createEntity(EntityManager em) {
        FieldDefinition fieldDefinition = new FieldDefinition()
            .name(DEFAULT_NAME)
            .dataType(DEFAULT_DATA_TYPE)
            .isRequired(DEFAULT_IS_REQUIRED)
            .isUnique(DEFAULT_IS_UNIQUE)
            .defaultValue(DEFAULT_DEFAULT_VALUE)
            .relationSchemaId(DEFAULT_RELATION_SCHEMA_ID)
            .relationType(DEFAULT_RELATION_TYPE);
        return fieldDefinition;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FieldDefinition createUpdatedEntity(EntityManager em) {
        FieldDefinition fieldDefinition = new FieldDefinition()
            .name(UPDATED_NAME)
            .dataType(UPDATED_DATA_TYPE)
            .isRequired(UPDATED_IS_REQUIRED)
            .isUnique(UPDATED_IS_UNIQUE)
            .defaultValue(UPDATED_DEFAULT_VALUE)
            .relationSchemaId(UPDATED_RELATION_SCHEMA_ID)
            .relationType(UPDATED_RELATION_TYPE);
        return fieldDefinition;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        fieldDefinitionSearchRepository.deleteAll();
        assertThat(fieldDefinitionSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        fieldDefinition = createEntity(em);
    }

    @Test
    @Transactional
    void createFieldDefinition() throws Exception {
        int databaseSizeBeforeCreate = fieldDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        // Create the FieldDefinition
        restFieldDefinitionMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldDefinition))
            )
            .andExpect(status().isCreated());

        // Validate the FieldDefinition in the database
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        FieldDefinition testFieldDefinition = fieldDefinitionList.get(fieldDefinitionList.size() - 1);
        assertThat(testFieldDefinition.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testFieldDefinition.getDataType()).isEqualTo(DEFAULT_DATA_TYPE);
        assertThat(testFieldDefinition.getIsRequired()).isEqualTo(DEFAULT_IS_REQUIRED);
        assertThat(testFieldDefinition.getIsUnique()).isEqualTo(DEFAULT_IS_UNIQUE);
        assertThat(testFieldDefinition.getDefaultValue()).isEqualTo(DEFAULT_DEFAULT_VALUE);
        assertThat(testFieldDefinition.getRelationSchemaId()).isEqualTo(DEFAULT_RELATION_SCHEMA_ID);
        assertThat(testFieldDefinition.getRelationType()).isEqualTo(DEFAULT_RELATION_TYPE);
    }

    @Test
    @Transactional
    void createFieldDefinitionWithExistingId() throws Exception {
        // Create the FieldDefinition with an existing ID
        fieldDefinition.setId(1L);

        int databaseSizeBeforeCreate = fieldDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restFieldDefinitionMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldDefinition))
            )
            .andExpect(status().isBadRequest());

        // Validate the FieldDefinition in the database
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllFieldDefinitions() throws Exception {
        // Initialize the database
        fieldDefinitionRepository.saveAndFlush(fieldDefinition);

        // Get all the fieldDefinitionList
        restFieldDefinitionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fieldDefinition.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].dataType").value(hasItem(DEFAULT_DATA_TYPE.toString())))
            .andExpect(jsonPath("$.[*].isRequired").value(hasItem(DEFAULT_IS_REQUIRED.booleanValue())))
            .andExpect(jsonPath("$.[*].isUnique").value(hasItem(DEFAULT_IS_UNIQUE.booleanValue())))
            .andExpect(jsonPath("$.[*].defaultValue").value(hasItem(DEFAULT_DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].relationSchemaId").value(hasItem(DEFAULT_RELATION_SCHEMA_ID.intValue())))
            .andExpect(jsonPath("$.[*].relationType").value(hasItem(DEFAULT_RELATION_TYPE.toString())));
    }

    @Test
    @Transactional
    void getFieldDefinition() throws Exception {
        // Initialize the database
        fieldDefinitionRepository.saveAndFlush(fieldDefinition);

        // Get the fieldDefinition
        restFieldDefinitionMockMvc
            .perform(get(ENTITY_API_URL_ID, fieldDefinition.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(fieldDefinition.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.dataType").value(DEFAULT_DATA_TYPE.toString()))
            .andExpect(jsonPath("$.isRequired").value(DEFAULT_IS_REQUIRED.booleanValue()))
            .andExpect(jsonPath("$.isUnique").value(DEFAULT_IS_UNIQUE.booleanValue()))
            .andExpect(jsonPath("$.defaultValue").value(DEFAULT_DEFAULT_VALUE))
            .andExpect(jsonPath("$.relationSchemaId").value(DEFAULT_RELATION_SCHEMA_ID.intValue()))
            .andExpect(jsonPath("$.relationType").value(DEFAULT_RELATION_TYPE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingFieldDefinition() throws Exception {
        // Get the fieldDefinition
        restFieldDefinitionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFieldDefinition() throws Exception {
        // Initialize the database
        fieldDefinitionRepository.saveAndFlush(fieldDefinition);

        int databaseSizeBeforeUpdate = fieldDefinitionRepository.findAll().size();
        fieldDefinitionSearchRepository.save(fieldDefinition);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());

        // Update the fieldDefinition
        FieldDefinition updatedFieldDefinition = fieldDefinitionRepository.findById(fieldDefinition.getId()).get();
        // Disconnect from session so that the updates on updatedFieldDefinition are not directly saved in db
        em.detach(updatedFieldDefinition);
        updatedFieldDefinition
            .name(UPDATED_NAME)
            .dataType(UPDATED_DATA_TYPE)
            .isRequired(UPDATED_IS_REQUIRED)
            .isUnique(UPDATED_IS_UNIQUE)
            .defaultValue(UPDATED_DEFAULT_VALUE)
            .relationSchemaId(UPDATED_RELATION_SCHEMA_ID)
            .relationType(UPDATED_RELATION_TYPE);

        restFieldDefinitionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedFieldDefinition.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedFieldDefinition))
            )
            .andExpect(status().isOk());

        // Validate the FieldDefinition in the database
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeUpdate);
        FieldDefinition testFieldDefinition = fieldDefinitionList.get(fieldDefinitionList.size() - 1);
        assertThat(testFieldDefinition.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testFieldDefinition.getDataType()).isEqualTo(UPDATED_DATA_TYPE);
        assertThat(testFieldDefinition.getIsRequired()).isEqualTo(UPDATED_IS_REQUIRED);
        assertThat(testFieldDefinition.getIsUnique()).isEqualTo(UPDATED_IS_UNIQUE);
        assertThat(testFieldDefinition.getDefaultValue()).isEqualTo(UPDATED_DEFAULT_VALUE);
        assertThat(testFieldDefinition.getRelationSchemaId()).isEqualTo(UPDATED_RELATION_SCHEMA_ID);
        assertThat(testFieldDefinition.getRelationType()).isEqualTo(UPDATED_RELATION_TYPE);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<FieldDefinition> fieldDefinitionSearchList = IterableUtils.toList(fieldDefinitionSearchRepository.findAll());
                FieldDefinition testFieldDefinitionSearch = fieldDefinitionSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testFieldDefinitionSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testFieldDefinitionSearch.getDataType()).isEqualTo(UPDATED_DATA_TYPE);
                assertThat(testFieldDefinitionSearch.getIsRequired()).isEqualTo(UPDATED_IS_REQUIRED);
                assertThat(testFieldDefinitionSearch.getIsUnique()).isEqualTo(UPDATED_IS_UNIQUE);
                assertThat(testFieldDefinitionSearch.getDefaultValue()).isEqualTo(UPDATED_DEFAULT_VALUE);
                assertThat(testFieldDefinitionSearch.getRelationSchemaId()).isEqualTo(UPDATED_RELATION_SCHEMA_ID);
                assertThat(testFieldDefinitionSearch.getRelationType()).isEqualTo(UPDATED_RELATION_TYPE);
            });
    }

    @Test
    @Transactional
    void putNonExistingFieldDefinition() throws Exception {
        int databaseSizeBeforeUpdate = fieldDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        fieldDefinition.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFieldDefinitionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, fieldDefinition.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(fieldDefinition))
            )
            .andExpect(status().isBadRequest());

        // Validate the FieldDefinition in the database
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchFieldDefinition() throws Exception {
        int databaseSizeBeforeUpdate = fieldDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        fieldDefinition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldDefinitionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(fieldDefinition))
            )
            .andExpect(status().isBadRequest());

        // Validate the FieldDefinition in the database
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFieldDefinition() throws Exception {
        int databaseSizeBeforeUpdate = fieldDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        fieldDefinition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldDefinitionMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fieldDefinition))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the FieldDefinition in the database
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateFieldDefinitionWithPatch() throws Exception {
        // Initialize the database
        fieldDefinitionRepository.saveAndFlush(fieldDefinition);

        int databaseSizeBeforeUpdate = fieldDefinitionRepository.findAll().size();

        // Update the fieldDefinition using partial update
        FieldDefinition partialUpdatedFieldDefinition = new FieldDefinition();
        partialUpdatedFieldDefinition.setId(fieldDefinition.getId());

        partialUpdatedFieldDefinition.name(UPDATED_NAME).isRequired(UPDATED_IS_REQUIRED).isUnique(UPDATED_IS_UNIQUE);

        restFieldDefinitionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFieldDefinition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFieldDefinition))
            )
            .andExpect(status().isOk());

        // Validate the FieldDefinition in the database
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeUpdate);
        FieldDefinition testFieldDefinition = fieldDefinitionList.get(fieldDefinitionList.size() - 1);
        assertThat(testFieldDefinition.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testFieldDefinition.getDataType()).isEqualTo(DEFAULT_DATA_TYPE);
        assertThat(testFieldDefinition.getIsRequired()).isEqualTo(UPDATED_IS_REQUIRED);
        assertThat(testFieldDefinition.getIsUnique()).isEqualTo(UPDATED_IS_UNIQUE);
        assertThat(testFieldDefinition.getDefaultValue()).isEqualTo(DEFAULT_DEFAULT_VALUE);
        assertThat(testFieldDefinition.getRelationSchemaId()).isEqualTo(DEFAULT_RELATION_SCHEMA_ID);
        assertThat(testFieldDefinition.getRelationType()).isEqualTo(DEFAULT_RELATION_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateFieldDefinitionWithPatch() throws Exception {
        // Initialize the database
        fieldDefinitionRepository.saveAndFlush(fieldDefinition);

        int databaseSizeBeforeUpdate = fieldDefinitionRepository.findAll().size();

        // Update the fieldDefinition using partial update
        FieldDefinition partialUpdatedFieldDefinition = new FieldDefinition();
        partialUpdatedFieldDefinition.setId(fieldDefinition.getId());

        partialUpdatedFieldDefinition
            .name(UPDATED_NAME)
            .dataType(UPDATED_DATA_TYPE)
            .isRequired(UPDATED_IS_REQUIRED)
            .isUnique(UPDATED_IS_UNIQUE)
            .defaultValue(UPDATED_DEFAULT_VALUE)
            .relationSchemaId(UPDATED_RELATION_SCHEMA_ID)
            .relationType(UPDATED_RELATION_TYPE);

        restFieldDefinitionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFieldDefinition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFieldDefinition))
            )
            .andExpect(status().isOk());

        // Validate the FieldDefinition in the database
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeUpdate);
        FieldDefinition testFieldDefinition = fieldDefinitionList.get(fieldDefinitionList.size() - 1);
        assertThat(testFieldDefinition.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testFieldDefinition.getDataType()).isEqualTo(UPDATED_DATA_TYPE);
        assertThat(testFieldDefinition.getIsRequired()).isEqualTo(UPDATED_IS_REQUIRED);
        assertThat(testFieldDefinition.getIsUnique()).isEqualTo(UPDATED_IS_UNIQUE);
        assertThat(testFieldDefinition.getDefaultValue()).isEqualTo(UPDATED_DEFAULT_VALUE);
        assertThat(testFieldDefinition.getRelationSchemaId()).isEqualTo(UPDATED_RELATION_SCHEMA_ID);
        assertThat(testFieldDefinition.getRelationType()).isEqualTo(UPDATED_RELATION_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingFieldDefinition() throws Exception {
        int databaseSizeBeforeUpdate = fieldDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        fieldDefinition.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFieldDefinitionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, fieldDefinition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(fieldDefinition))
            )
            .andExpect(status().isBadRequest());

        // Validate the FieldDefinition in the database
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFieldDefinition() throws Exception {
        int databaseSizeBeforeUpdate = fieldDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        fieldDefinition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldDefinitionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(fieldDefinition))
            )
            .andExpect(status().isBadRequest());

        // Validate the FieldDefinition in the database
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFieldDefinition() throws Exception {
        int databaseSizeBeforeUpdate = fieldDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        fieldDefinition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFieldDefinitionMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(fieldDefinition))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the FieldDefinition in the database
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteFieldDefinition() throws Exception {
        // Initialize the database
        fieldDefinitionRepository.saveAndFlush(fieldDefinition);
        fieldDefinitionRepository.save(fieldDefinition);
        fieldDefinitionSearchRepository.save(fieldDefinition);

        int databaseSizeBeforeDelete = fieldDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the fieldDefinition
        restFieldDefinitionMockMvc
            .perform(delete(ENTITY_API_URL_ID, fieldDefinition.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<FieldDefinition> fieldDefinitionList = fieldDefinitionRepository.findAll();
        assertThat(fieldDefinitionList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fieldDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchFieldDefinition() throws Exception {
        // Initialize the database
        fieldDefinition = fieldDefinitionRepository.saveAndFlush(fieldDefinition);
        fieldDefinitionSearchRepository.save(fieldDefinition);

        // Search the fieldDefinition
        restFieldDefinitionMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + fieldDefinition.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fieldDefinition.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].dataType").value(hasItem(DEFAULT_DATA_TYPE.toString())))
            .andExpect(jsonPath("$.[*].isRequired").value(hasItem(DEFAULT_IS_REQUIRED.booleanValue())))
            .andExpect(jsonPath("$.[*].isUnique").value(hasItem(DEFAULT_IS_UNIQUE.booleanValue())))
            .andExpect(jsonPath("$.[*].defaultValue").value(hasItem(DEFAULT_DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].relationSchemaId").value(hasItem(DEFAULT_RELATION_SCHEMA_ID.intValue())))
            .andExpect(jsonPath("$.[*].relationType").value(hasItem(DEFAULT_RELATION_TYPE.toString())));
    }
}
