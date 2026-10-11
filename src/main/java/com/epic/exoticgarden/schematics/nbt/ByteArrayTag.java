/*
 * Minimal NBT reader adapted from the JNBT library (GPLv3), originally by Graham Edgecombe,
 * adapted for EpicExoticGarden. See Tag.java.
 */
package com.epic.exoticgarden.schematics.nbt;

public class ByteArrayTag extends Tag<byte[]> {

    public ByteArrayTag(String name, byte[] value) {
        super(name, value);
    }
}
