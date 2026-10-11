/*
 * Minimal NBT reader adapted from the JNBT library (GPLv3), originally by Graham Edgecombe,
 * adapted for EpicExoticGarden. See Tag.java.
 */
package com.epic.exoticgarden.schematics.nbt;

import java.util.Collections;
import java.util.List;

public class ListTag extends Tag<List<Tag<?>>> {

    private final Class<? extends Tag<?>> type;

    public ListTag(String name, Class<? extends Tag<?>> type, List<Tag<?>> value) {
        super(name, Collections.unmodifiableList(value));
        this.type = type;
    }

    public Class<? extends Tag<?>> getType() {
        return type;
    }
}
