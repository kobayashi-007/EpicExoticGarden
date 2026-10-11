/*
 * Minimal NBT reader adapted from the JNBT library (GPLv3), originally by Graham Edgecombe,
 * adapted for EpicExoticGarden. See Tag.java.
 */
package com.epic.exoticgarden.schematics.nbt;

import java.util.Collections;
import java.util.Map;

public class CompoundTag extends Tag<Map<String, Tag<?>>> {

    public CompoundTag(String name, Map<String, Tag<?>> value) {
        super(name, Collections.unmodifiableMap(value));
    }
}
