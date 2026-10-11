/*
 * Minimal NBT reader adapted from the JNBT library (GPLv3), originally written by Graham Edgecombe
 * and adapted for ExoticGarden/EpicExoticGarden. Only reading is implemented - enough to load
 * classic .schematic files.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.epic.exoticgarden.schematics.nbt;

/**
 * Base class for all NBT tags.
 *
 * @param <T> the value type
 */
public abstract class Tag<T> {

    private final String name;
    private final T value;

    Tag(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }
}
