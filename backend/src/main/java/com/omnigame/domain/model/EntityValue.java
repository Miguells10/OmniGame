package com.omnigame.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Stores a concrete attribute value for a specific {@link GameEntity}.
 *
 * <p>This is the "Value" part of the EAV (Entity-Attribute-Value) model.
 * All values are stored as text strings; type coercion is driven by the
 * associated {@link Attribute#getDataType()} definition.</p>
 *
 * <p><strong>Example:</strong> For a Skyrim mod entity, an EntityValue might
 * link to the "Load Order" attribute with value "42".</p>
 *
 * @author OmniGame Engineering
 * @since 0.1.0
 */
@Entity
@Table(name = "entity_values", indexes = {
        @Index(name = "idx_entity_values_entity_id", columnList = "entity_id"),
        @Index(name = "idx_entity_values_attribute_id", columnList = "attribute_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entity_id", nullable = false)
    private GameEntity gameEntity;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    /**
     * The string-encoded value. Type interpretation is determined
     * by {@link Attribute#getDataType()}.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;
}
