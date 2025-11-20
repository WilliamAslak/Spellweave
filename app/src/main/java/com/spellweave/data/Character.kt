package com.spellweave.data

import java.util.UUID

data class Character(
    //Unique id for editing/updating
    val id: String = UUID.randomUUID().toString(),
    var name: String? = "",
    var charClass: String? = "Wizard",
    var level: Int? = 1,
    var hp: Int? = 10,
    var currentHp: Int? = null,
    var speed: Int? = 30,
    var strength: Int? = 10,
    var dexterity: Int? = 10,
    var constitution: Int? = 10,
    var intelligence: Int? = 10,
    var wisdom: Int? = 10,
    var charisma: Int? = 10,
    var spellSlots: MutableList<SpellSlot> = mutableListOf()
)