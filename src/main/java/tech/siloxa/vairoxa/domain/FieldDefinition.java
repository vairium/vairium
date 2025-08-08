package tech.siloxa.vairoxa.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import tech.siloxa.vairoxa.domain.enumeration.DataType;
import tech.siloxa.vairoxa.domain.enumeration.RelationType;

/**
 * A FieldDefinition.
 */
@Entity
@Table(name = "field_definition")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "fielddefinition")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FieldDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type")
    private DataType dataType;

    @Column(name = "is_required")
    private Boolean isRequired = false;

    @Column(name = "is_unique")
    private Boolean isUnique = false;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "relation_schema_id")
    private Long relationSchemaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type")
    private RelationType relationType;

    @ManyToOne
    @JsonIgnoreProperties(value = { "fieldDefinitions", "user" }, allowSetters = true)
    private SchemaDefinition schemaDefinition;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public FieldDefinition id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public FieldDefinition name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getDataType() {
        return this.dataType;
    }

    public FieldDefinition dataType(DataType dataType) {
        this.setDataType(dataType);
        return this;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Boolean getIsRequired() {
        return this.isRequired;
    }

    public FieldDefinition isRequired(Boolean isRequired) {
        this.setIsRequired(isRequired);
        return this;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public Boolean getIsUnique() {
        return this.isUnique;
    }

    public FieldDefinition isUnique(Boolean isUnique) {
        this.setIsUnique(isUnique);
        return this;
    }

    public void setIsUnique(Boolean isUnique) {
        this.isUnique = isUnique;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public FieldDefinition defaultValue(String defaultValue) {
        this.setDefaultValue(defaultValue);
        return this;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Long getRelationSchemaId() {
        return this.relationSchemaId;
    }

    public FieldDefinition relationSchemaId(Long relationSchemaId) {
        this.setRelationSchemaId(relationSchemaId);
        return this;
    }

    public void setRelationSchemaId(Long relationSchemaId) {
        this.relationSchemaId = relationSchemaId;
    }

    public RelationType getRelationType() {
        return this.relationType;
    }

    public FieldDefinition relationType(RelationType relationType) {
        this.setRelationType(relationType);
        return this;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    public SchemaDefinition getSchemaDefinition() {
        return this.schemaDefinition;
    }

    public void setSchemaDefinition(SchemaDefinition schemaDefinition) {
        this.schemaDefinition = schemaDefinition;
    }

    public FieldDefinition schemaDefinition(SchemaDefinition schemaDefinition) {
        this.setSchemaDefinition(schemaDefinition);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldDefinition)) {
            return false;
        }
        return id != null && id.equals(((FieldDefinition) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FieldDefinition{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", dataType='" + getDataType() + "'" +
            ", isRequired='" + getIsRequired() + "'" +
            ", isUnique='" + getIsUnique() + "'" +
            ", defaultValue='" + getDefaultValue() + "'" +
            ", relationSchemaId=" + getRelationSchemaId() +
            ", relationType='" + getRelationType() + "'" +
            "}";
    }
}
