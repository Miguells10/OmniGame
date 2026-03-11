package com.omnigame.domain.model;

/**
 * Data types supported by the EAV (Entity-Attribute-Value) attribute definitions.
 *
 * <p>Since EAV stores all values as text, this enum drives client-side
 * parsing and validation logic for proper type coercion.</p>
 */
public enum AttributeDataType {

    /** Free-form text value (e.g., mod description, author name). */
    STRING,

    /** Whole number value (e.g., load order position, version build number). */
    INTEGER,

    /** Boolean flag value stored as "true"/"false" (e.g., NSFW content flag). */
    BOOLEAN,

    /** Floating-point numeric value (e.g., compatibility score, file size in MB). */
    FLOAT
}
