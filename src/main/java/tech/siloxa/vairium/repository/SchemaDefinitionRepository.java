package tech.siloxa.vairium.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import tech.siloxa.vairium.domain.SchemaDefinition;

/**
 * Spring Data JPA repository for the SchemaDefinition entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SchemaDefinitionRepository extends JpaRepository<SchemaDefinition, Long> {}
