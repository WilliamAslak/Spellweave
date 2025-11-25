package com.spellweave

import com.spellweave.data.Character
import com.spellweave.data.SpellSlot
import org.junit.Test

import org.junit.Assert.*

/**
 * Should be deleted later, just example unit tests for Character data class
 */
class CharacterTests {

    @Test
    fun character_creation_isCorrect() {
        val character = Character(
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
    fun character_has_unique_id() {
        val c1 = Character()
        val c2 = Character()
        assertNotEquals(c1.id, c2.id)
    }

    @Test
    fun default_character_values_are_correct() {
        val c = Character()

        assertEquals("", c.name)
        assertEquals("Wizard", c.charClass)
        assertEquals(1, c.level)
        assertEquals(10, c.hp)
        assertEquals(30, c.speed)
        assertEquals(10, c.strength)
    }

    @Test
    fun empty_name_returns_error() {
        val character = Character(
            name = "",
            charClass = "Wizard",
            level = 1
        )
        fun validateCharacterName(char: Character): Boolean {
            return !char.name.isNullOrBlank()
        }
        assertFalse(validateCharacterName(character))
    }

    @Test
    fun validateClassSelection() {
        val validClasses = setOf("Bard", "Cleric", "Druid", "Fighter", "Paladin", "Ranger", "Rogue", "Sorcerer", "Warlock", "Wizard")
        val character = Character(
            name = "Zook",
            charClass = "Mage",
            level = 3
        )
        fun isValidClass(char: Character): Boolean {
            return char.charClass != null && validClasses.contains(char.charClass)
        }
        assertFalse(isValidClass(character))
    }

    @Test
    fun level_within_bounds() {
        val character = Character(
            name = "Zook",
            charClass = "Wizard",
            level = 25
        )

        fun isLevelValid(char: Character): Boolean {
            return char.level != null && char.level in 1..20
        }
        assertFalse(isLevelValid(character))
    }

    @Test
    fun spell_slots_can_be_added() {
        val c = Character()
        c.spellSlots.add(SpellSlot(level = 1))
        assertEquals(1, c.spellSlots.size)
        assertEquals(1, c.spellSlots[0].level)
    }

    private fun exists(existing: List<Character>, name: String, clazz: String): Boolean {
        return existing.any { c ->
            (c.name?.trim()?.lowercase() == name.lowercase()) &&
                    (c.charClass?.trim() == clazz)
        }
    }

    @Test
    fun detects_duplicate_character() {
        val list = listOf(
            Character(name = "Aria", charClass = "Wizard"),
            Character(name = "Brak", charClass = "Fighter")
        )

        assertTrue(exists(list, "aria", "Wizard"))
        assertFalse(exists(list, "aria", "Cleric"))
        assertFalse(exists(list, "NewGuy", "Wizard"))
    }
}

