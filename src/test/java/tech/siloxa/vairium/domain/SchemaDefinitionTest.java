package tech.siloxa.vairium.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tech.siloxa.vairium.web.rest.TestUtil;

class SchemaDefinitionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SchemaDefinition.class);
        SchemaDefinition schemaDefinition1 = new SchemaDefinition();
        schemaDefinition1.setId(1L);
        SchemaDefinition schemaDefinition2 = new SchemaDefinition();
        schemaDefinition2.setId(schemaDefinition1.getId());
        assertThat(schemaDefinition1).isEqualTo(schemaDefinition2);
        schemaDefinition2.setId(2L);
        assertThat(schemaDefinition1).isNotEqualTo(schemaDefinition2);
        schemaDefinition1.setId(null);
        assertThat(schemaDefinition1).isNotEqualTo(schemaDefinition2);
    }
}
