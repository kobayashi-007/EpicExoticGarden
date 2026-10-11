/*
 * Derived from ExoticGarden (GPLv3) — https://github.com/Slimefun-Addon-Community/ExoticGarden
 * Modified 2026: no functional change, kept as-is for compatibility.
 */
package com.epic.exoticgarden.plant;

/**
 * Growth/harvest shape of an EpicExoticGarden plant.
 */
public enum PlantType {

    /** Ripens as a leafy bush on the spot where the bush was planted. */
    BUSH,
    /** Ripens as a single player-head fruit/vegetable. */
    FRUIT,
    /** Ripens as a two-block-tall plant (leaves below, fruit head above). */
    DOUBLE_PLANT,
    /** Magical crop that yields a resource essence when harvested. */
    ORE_PLANT

}
