package com.omnigame.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Defines a reusable attribute in the EAV (Entity-Attribute-Value) model.
 *
 * <p>Attributes are game-agnostic metadata descriptors. Each attribute has a
 * {@link AttributeDataType} that informs clients how to parse the string-stored
 * value in {@link EntityValue}. Examples: "Load Order" (INTEGER),
 * "Forge Version" (STRING), "NSFW" (BOOLEAN).</p>
 *
 * @author OmniGame Engineering
 * @since 0.1.0
 */
@Entity
@Table(name = "attributes", indexes = {
        @Index(name = "idx_attributes_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private AttributeDataType dataType;

    @Column(length = 500)
    private String description;
}
