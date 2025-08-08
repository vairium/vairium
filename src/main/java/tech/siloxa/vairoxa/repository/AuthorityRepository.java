package tech.siloxa.vairoxa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.siloxa.vairoxa.domain.Authority;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {}
