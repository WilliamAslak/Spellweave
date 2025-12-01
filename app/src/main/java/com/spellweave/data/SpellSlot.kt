package com.spellweave.data

data class SpellSlot(
    var level: Int = 1,
    var used: Boolean = false,

    var usedSpellIndex: String? = null,
    var usedSpellName: String? = null,

    var castAtLevel: Int? = null
)