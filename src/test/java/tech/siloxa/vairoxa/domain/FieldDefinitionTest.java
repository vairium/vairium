package tech.siloxa.vairoxa.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tech.siloxa.vairoxa.web.rest.TestUtil;

class FieldDefinitionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FieldDefinition.class);
        FieldDefinition fieldDefinition1 = new FieldDefinition();
        fieldDefinition1.setId(1L);
        FieldDefinition fieldDefinition2 = new FieldDefinition();
        fieldDefinition2.setId(fieldDefinition1.getId());
        assertThat(fieldDefinition1).isEqualTo(fieldDefinition2);
        fieldDefinition2.setId(2L);
        assertThat(fieldDefinition1).isNotEqualTo(fieldDefinition2);
        fieldDefinition1.setId(null);
        assertThat(fieldDefinition1).isNotEqualTo(fieldDefinition2);
    }
}
