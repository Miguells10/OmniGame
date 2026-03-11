package com.omnigame.domain.model;

/**
 * Classification of game entities within the OmniGame ecosystem.
 *
 * <p>Each entity uploaded or harvested belongs to one of these categories,
 * enabling type-safe filtering and EAV attribute scoping.</p>
 */
public enum EntityType {

    /** Community-created game modification (e.g., gameplay overhaul, texture pack). */
    MOD,

    /** Official or community patch fixing bugs or compatibility issues. */
    PATCH,

    /** Standalone asset such as a 3D model, texture, or sound file. */
    ASSET,

    /** Utility tool for mod management, load-order sorting, or conflict resolution. */
    TOOL
}
