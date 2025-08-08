package tech.siloxa.vairium.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.siloxa.vairium.domain.Authority;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {}
