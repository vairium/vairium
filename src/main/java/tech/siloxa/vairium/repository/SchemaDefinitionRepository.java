package tech.siloxa.vairium.repository;

import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import tech.siloxa.vairium.domain.SchemaDefinition;

/**
 * Spring Data JPA repository for the SchemaDefinition entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SchemaDefinitionRepository extends JpaRepository<SchemaDefinition, Long> {
    @Query("select schemaDefinition from SchemaDefinition schemaDefinition where schemaDefinition.user.login = ?#{principal.username}")
    List<SchemaDefinition> findByUserIsCurrentUser();
}
