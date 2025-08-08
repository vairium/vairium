package tech.siloxa.vairoxa.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import tech.siloxa.vairoxa.domain.FieldDefinition;

/**
 * Spring Data JPA repository for the FieldDefinition entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FieldDefinitionRepository extends JpaRepository<FieldDefinition, Long> {}
