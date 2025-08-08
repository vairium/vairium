package tech.siloxa.vairoxa.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A SchemaDefinition.
 */
@Entity
@Table(name = "schema_definition")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "schemadefinition")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SchemaDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "schemaDefinition", cascade = CascadeType.PERSIST)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "schemaDefinition" }, allowSetters = true)
    private Set<FieldDefinition> fieldDefinitions = new HashSet<>();

    @ManyToOne
    @JsonIgnore
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SchemaDefinition id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public SchemaDefinition name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<FieldDefinition> getFieldDefinitions() {
        return this.fieldDefinitions;
    }

    public void setFieldDefinitions(Set<FieldDefinition> fieldDefinitions) {
        if (this.fieldDefinitions != null) {
            this.fieldDefinitions.forEach(i -> i.setSchemaDefinition(null));
        }
        if (fieldDefinitions != null) {
            fieldDefinitions.forEach(i -> i.setSchemaDefinition(this));
        }
        this.fieldDefinitions = fieldDefinitions;
    }

    public SchemaDefinition fieldDefinitions(Set<FieldDefinition> fieldDefinitions) {
        this.setFieldDefinitions(fieldDefinitions);
        return this;
    }

    public SchemaDefinition addFieldDefinition(FieldDefinition fieldDefinition) {
        this.fieldDefinitions.add(fieldDefinition);
        fieldDefinition.setSchemaDefinition(this);
        return this;
    }

    public SchemaDefinition removeFieldDefinition(FieldDefinition fieldDefinition) {
        this.fieldDefinitions.remove(fieldDefinition);
        fieldDefinition.setSchemaDefinition(null);
        return this;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SchemaDefinition user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SchemaDefinition)) {
            return false;
        }
        return id != null && id.equals(((SchemaDefinition) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SchemaDefinition{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            "}";
    }
}
