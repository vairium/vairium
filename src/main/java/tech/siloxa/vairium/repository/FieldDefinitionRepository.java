package tech.siloxa.vairium.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import tech.siloxa.vairium.domain.FieldDefinition;

/**
 * Spring Data JPA repository for the FieldDefinition entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FieldDefinitionRepository extends JpaRepository<FieldDefinition, Long> {}
