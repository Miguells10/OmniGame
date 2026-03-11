package com.omnigame.infrastructure.persistence;

import com.omnigame.domain.model.EntityType;
import com.omnigame.domain.model.GameEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link GameEntity} (mods, patches, assets, tools).
 */
@Repository
public interface GameEntityRepository extends JpaRepository<GameEntity, UUID> {

    Page<GameEntity> findByGameId(UUID gameId, Pageable pageable);

    Page<GameEntity> findByGameIdAndType(UUID gameId, EntityType type, Pageable pageable);

    long countByGameId(UUID gameId);
}
