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
import tech.siloxa.vairium.domain.SchemaDefinition;
import tech.siloxa.vairium.repository.SchemaDefinitionRepository;
import tech.siloxa.vairium.repository.search.SchemaDefinitionSearchRepository;

/**
 * Integration tests for the {@link SchemaDefinitionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SchemaDefinitionResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/schema-definitions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/schema-definitions";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private SchemaDefinitionRepository schemaDefinitionRepository;

    @Autowired
    private SchemaDefinitionSearchRepository schemaDefinitionSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSchemaDefinitionMockMvc;

    private SchemaDefinition schemaDefinition;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SchemaDefinition createEntity(EntityManager em) {
        SchemaDefinition schemaDefinition = new SchemaDefinition().name(DEFAULT_NAME);
        return schemaDefinition;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SchemaDefinition createUpdatedEntity(EntityManager em) {
        SchemaDefinition schemaDefinition = new SchemaDefinition().name(UPDATED_NAME);
        return schemaDefinition;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        schemaDefinitionSearchRepository.deleteAll();
        assertThat(schemaDefinitionSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        schemaDefinition = createEntity(em);
    }

    @Test
    @Transactional
    void createSchemaDefinition() throws Exception {
        int databaseSizeBeforeCreate = schemaDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        // Create the SchemaDefinition
        restSchemaDefinitionMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(schemaDefinition))
            )
            .andExpect(status().isCreated());

        // Validate the SchemaDefinition in the database
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        SchemaDefinition testSchemaDefinition = schemaDefinitionList.get(schemaDefinitionList.size() - 1);
        assertThat(testSchemaDefinition.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void createSchemaDefinitionWithExistingId() throws Exception {
        // Create the SchemaDefinition with an existing ID
        schemaDefinition.setId(1L);

        int databaseSizeBeforeCreate = schemaDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restSchemaDefinitionMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(schemaDefinition))
            )
            .andExpect(status().isBadRequest());

        // Validate the SchemaDefinition in the database
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllSchemaDefinitions() throws Exception {
        // Initialize the database
        schemaDefinitionRepository.saveAndFlush(schemaDefinition);

        // Get all the schemaDefinitionList
        restSchemaDefinitionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(schemaDefinition.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getSchemaDefinition() throws Exception {
        // Initialize the database
        schemaDefinitionRepository.saveAndFlush(schemaDefinition);

        // Get the schemaDefinition
        restSchemaDefinitionMockMvc
            .perform(get(ENTITY_API_URL_ID, schemaDefinition.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(schemaDefinition.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingSchemaDefinition() throws Exception {
        // Get the schemaDefinition
        restSchemaDefinitionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSchemaDefinition() throws Exception {
        // Initialize the database
        schemaDefinitionRepository.saveAndFlush(schemaDefinition);

        int databaseSizeBeforeUpdate = schemaDefinitionRepository.findAll().size();
        schemaDefinitionSearchRepository.save(schemaDefinition);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());

        // Update the schemaDefinition
        SchemaDefinition updatedSchemaDefinition = schemaDefinitionRepository.findById(schemaDefinition.getId()).get();
        // Disconnect from session so that the updates on updatedSchemaDefinition are not directly saved in db
        em.detach(updatedSchemaDefinition);
        updatedSchemaDefinition.name(UPDATED_NAME);

        restSchemaDefinitionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSchemaDefinition.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedSchemaDefinition))
            )
            .andExpect(status().isOk());

        // Validate the SchemaDefinition in the database
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeUpdate);
        SchemaDefinition testSchemaDefinition = schemaDefinitionList.get(schemaDefinitionList.size() - 1);
        assertThat(testSchemaDefinition.getName()).isEqualTo(UPDATED_NAME);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<SchemaDefinition> schemaDefinitionSearchList = IterableUtils.toList(schemaDefinitionSearchRepository.findAll());
                SchemaDefinition testSchemaDefinitionSearch = schemaDefinitionSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testSchemaDefinitionSearch.getName()).isEqualTo(UPDATED_NAME);
            });
    }

    @Test
    @Transactional
    void putNonExistingSchemaDefinition() throws Exception {
        int databaseSizeBeforeUpdate = schemaDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        schemaDefinition.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSchemaDefinitionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, schemaDefinition.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(schemaDefinition))
            )
            .andExpect(status().isBadRequest());

        // Validate the SchemaDefinition in the database
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchSchemaDefinition() throws Exception {
        int databaseSizeBeforeUpdate = schemaDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        schemaDefinition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSchemaDefinitionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(schemaDefinition))
            )
            .andExpect(status().isBadRequest());

        // Validate the SchemaDefinition in the database
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSchemaDefinition() throws Exception {
        int databaseSizeBeforeUpdate = schemaDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        schemaDefinition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSchemaDefinitionMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(schemaDefinition))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SchemaDefinition in the database
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateSchemaDefinitionWithPatch() throws Exception {
        // Initialize the database
        schemaDefinitionRepository.saveAndFlush(schemaDefinition);

        int databaseSizeBeforeUpdate = schemaDefinitionRepository.findAll().size();

        // Update the schemaDefinition using partial update
        SchemaDefinition partialUpdatedSchemaDefinition = new SchemaDefinition();
        partialUpdatedSchemaDefinition.setId(schemaDefinition.getId());

        restSchemaDefinitionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSchemaDefinition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSchemaDefinition))
            )
            .andExpect(status().isOk());

        // Validate the SchemaDefinition in the database
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeUpdate);
        SchemaDefinition testSchemaDefinition = schemaDefinitionList.get(schemaDefinitionList.size() - 1);
        assertThat(testSchemaDefinition.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void fullUpdateSchemaDefinitionWithPatch() throws Exception {
        // Initialize the database
        schemaDefinitionRepository.saveAndFlush(schemaDefinition);

        int databaseSizeBeforeUpdate = schemaDefinitionRepository.findAll().size();

        // Update the schemaDefinition using partial update
        SchemaDefinition partialUpdatedSchemaDefinition = new SchemaDefinition();
        partialUpdatedSchemaDefinition.setId(schemaDefinition.getId());

        partialUpdatedSchemaDefinition.name(UPDATED_NAME);

        restSchemaDefinitionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSchemaDefinition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSchemaDefinition))
            )
            .andExpect(status().isOk());

        // Validate the SchemaDefinition in the database
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeUpdate);
        SchemaDefinition testSchemaDefinition = schemaDefinitionList.get(schemaDefinitionList.size() - 1);
        assertThat(testSchemaDefinition.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingSchemaDefinition() throws Exception {
        int databaseSizeBeforeUpdate = schemaDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        schemaDefinition.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSchemaDefinitionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, schemaDefinition.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(schemaDefinition))
            )
            .andExpect(status().isBadRequest());

        // Validate the SchemaDefinition in the database
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSchemaDefinition() throws Exception {
        int databaseSizeBeforeUpdate = schemaDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        schemaDefinition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSchemaDefinitionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(schemaDefinition))
            )
            .andExpect(status().isBadRequest());

        // Validate the SchemaDefinition in the database
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSchemaDefinition() throws Exception {
        int databaseSizeBeforeUpdate = schemaDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        schemaDefinition.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSchemaDefinitionMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(schemaDefinition))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SchemaDefinition in the database
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteSchemaDefinition() throws Exception {
        // Initialize the database
        schemaDefinitionRepository.saveAndFlush(schemaDefinition);
        schemaDefinitionRepository.save(schemaDefinition);
        schemaDefinitionSearchRepository.save(schemaDefinition);

        int databaseSizeBeforeDelete = schemaDefinitionRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the schemaDefinition
        restSchemaDefinitionMockMvc
            .perform(delete(ENTITY_API_URL_ID, schemaDefinition.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<SchemaDefinition> schemaDefinitionList = schemaDefinitionRepository.findAll();
        assertThat(schemaDefinitionList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(schemaDefinitionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchSchemaDefinition() throws Exception {
        // Initialize the database
        schemaDefinition = schemaDefinitionRepository.saveAndFlush(schemaDefinition);
        schemaDefinitionSearchRepository.save(schemaDefinition);

        // Search the schemaDefinition
        restSchemaDefinitionMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + schemaDefinition.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(schemaDefinition.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }
}
