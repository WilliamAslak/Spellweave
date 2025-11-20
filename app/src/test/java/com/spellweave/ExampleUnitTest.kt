package com.spellweave

import org.junit.Test

import org.junit.Assert.*

/**
 * Should be deleted later, just example unit tests for Character data class
 */
class CharacterTests {

    @Test
    fun character_creation_isCorrect() {
        val character = com.spellweave.data.Character(
            name = "Zook",
            charClass = "Wizard",
            level = 5,
            hp = 45,
            speed = 30,
            strength = 10,
            dexterity = 14,
            constitution = 16,
            intelligence = 20,
            wisdom = 12,
            charisma = 10
        )
        assertEquals("Zook", character.name)
        assertEquals("Wizard", character.charClass)
        assertEquals(5, character.level)
        assertEquals(45, character.hp)
        assertEquals(30, character.speed)
        assertEquals(10, character.strength)
        assertEquals(14, character.dexterity)
        assertEquals(16, character.constitution)
        assertEquals(20, character.intelligence)
        assertEquals(12, character.wisdom)
        assertEquals(10, character.charisma)
    }

    @Test
    fun empty_name_returns_error() {
        val character = com.spellweave.data.Character(
            name = "",
            charClass = "Wizard",
            level = 1
        )
        fun validateCharacterName(char: com.spellweave.data.Character): Boolean {
            return !char.name.isNullOrBlank()
        }
        assertFalse(validateCharacterName(character))
    }

    @Test
    fun validateClassSelection() {
        val validClasses = setOf("Bard", "Cleric", "Druid", "Fighter", "Paladin", "Ranger", "Rogue", "Sorcerer", "Warlock", "Wizard")
        val character = com.spellweave.data.Character(
            name = "Zook",
            charClass = "Mage",
            level = 3
        )
        fun isValidClass(char: com.spellweave.data.Character): Boolean {
            return char.charClass != null && validClasses.contains(char.charClass)
        }
        assertFalse(isValidClass(character))
    }

    @Test
    fun level_within_bounds() {
        val character = com.spellweave.data.Character(
            name = "Zook",
            charClass = "Wizard",
            level = 25
        )

        fun isLevelValid(char: com.spellweave.data.Character): Boolean {
            return char.level != null && char.level in 1..20
        }
    }
}

