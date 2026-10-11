/*
 * Minimal NBT reader adapted from the JNBT library (GPLv3), originally by Graham Edgecombe,
 * adapted for EpicExoticGarden. See Tag.java.
 */
package com.epic.exoticgarden.schematics.nbt;

public class ShortTag extends Tag<Short> {

    public ShortTag(String name, short value) {
        super(name, value);
    }
}
