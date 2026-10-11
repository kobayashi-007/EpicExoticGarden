/*
 * Minimal NBT reader adapted from the JNBT library (GPLv3), originally by Graham Edgecombe,
 * adapted for EpicExoticGarden. See Tag.java.
 */
package com.epic.exoticgarden.schematics.nbt;

/** Package-private scalar NBT tags used while reading schematics. */

class EndTag extends Tag<Object> {
    EndTag() {
        super("", null);
    }
}

class ByteTag extends Tag<Byte> {
    ByteTag(String name, byte value) {
        super(name, value);
    }
}

class IntTag extends Tag<Integer> {
    IntTag(String name, int value) {
        super(name, value);
    }
}

class LongTag extends Tag<Long> {
    LongTag(String name, long value) {
        super(name, value);
    }
}

class FloatTag extends Tag<Float> {
    FloatTag(String name, float value) {
        super(name, value);
    }
}

class DoubleTag extends Tag<Double> {
    DoubleTag(String name, double value) {
        super(name, value);
    }
}

class StringTag extends Tag<String> {
    StringTag(String name, String value) {
        super(name, value);
    }
}
