package com.spellweave

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellweave.data.Character
import com.spellweave.data.SpellSlot
import com.spellweave.util.JsonHelper
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.File

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val characterFile = File(appContext.filesDir, "characters.json")

    @Before
    fun setup() {
        if (characterFile.exists()) {
            characterFile.delete()
        }
    }

    @After
    fun teardown() {
        if (characterFile.exists()) {
            characterFile.delete()
        }
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        assertEquals("com.spellweave", appContext.packageName)
    }

    @Test
    fun saveCharacter_writesToJson() {
        // 1. Create a new character
        val newCharacter = Character(
            id = "test-id",
            name = "Test Character",
            charClass = "Wizard",
            level = 5
        )

        // 2. Save the character
        val result = JsonHelper.saveCharacter(appContext, newCharacter)
        assertTrue(result)

        // 3. Read the characters back from the file
        val characters = JsonHelper.readCharacters(appContext)

        // 4. Assert that the new character is in the list
        val savedCharacter = characters.find { it.id == "test-id" }
        assertNotNull(savedCharacter)
        assertEquals("Test Character", savedCharacter?.name)
        assertEquals("Wizard", savedCharacter?.charClass)
        assertEquals(5, savedCharacter?.level)
    }

    @Test
    fun saveCharacter_preventsDuplicateNames() {
        // 1. Create and save a character
        val character1 = Character(
            id = "id-1",
            name = "Duplicate Name",
            charClass = "Rogue",
            level = 3
        )
        JsonHelper.saveCharacter(appContext, character1)

        // 2. Attempt to save a new character with the same name
        val character2 = Character(
            id = "id-2",
            name = "Duplicate Name",
            charClass = "Bard",
            level = 4
        )
        val result = JsonHelper.saveCharacter(appContext, character2)
        assertFalse(result)

        // 3. Verify that only the first character is saved
        val characters = JsonHelper.readCharacters(appContext)
        assertEquals(1, characters.size)
        assertEquals("id-1", characters[0].id)
    }

    @Test
    fun saveCharacter_preventsEmptyName() {
        // 1. Attempt to save a character with an empty name
        val character = Character(
            id = "id-empty-name",
            name = "",
            charClass = "Wizard",
            level = 1
        )

        val result = JsonHelper.saveCharacter(appContext, character)
        assertFalse(result)

        // 2. Verify that no characters have been saved
        val characters = JsonHelper.readCharacters(appContext)
        assertTrue(characters.isEmpty())
    }

    // Add this test for level validation
    @Test
    fun saveCharacter_preventsInvalidLevel() {
        // 1. Attempt to save a character with a level outside the 1-20 range
        val characterLowLevel = Character(
            id = "id-low-level",
            name = "Level 0 Guy",
            charClass = "Wizard",
            level = 0 // Invalid level
        )
        val characterHighLevel = Character(
            id = "id-high-level",
            name = "Level 21 Guy",
            charClass = "Wizard",
            level = 21 // Invalid level
        )

        // 2. Assert that the save operation fails
        assertFalse(JsonHelper.saveCharacter(appContext, characterLowLevel))
        assertFalse(JsonHelper.saveCharacter(appContext, characterHighLevel))

        // 3. Verify that no characters have been saved
        val characters = JsonHelper.readCharacters(appContext)
        assertTrue(characters.isEmpty())
    }

    // Add this test for ability score validation
    @Test
    fun saveCharacter_preventsInvalidAbilityScore() {
        // 1. Attempt to save a character with an ability score outside the 1-30 range
        val characterInvalidScore = Character(
            id = "id-invalid-score",
            name = "Weakling",
            charClass = "Wizard",
            level = 5,
            strength = 0 // Invalid score
        )

        // 2. Assert that the save operation fails
        val result = JsonHelper.saveCharacter(appContext, characterInvalidScore)
        assertFalse(result)

        // 3. Verify that no characters have been saved
        val characters = JsonHelper.readCharacters(appContext)
        assertTrue(characters.isEmpty())
    }

    @Test
    fun useSpellSlot_updatesUsedStatus() {
        // 1. Create a character with a spell slot
        val character = Character(
            id = "test-spell-user",
            name = "Caster",
            charClass = "Wizard",
            level = 1,
            spellSlots = mutableListOf(SpellSlot(level = 1, used = false))
        )

        // 2. Save the character
        JsonHelper.saveCharacter(appContext, character)

        // 3. Read the character and mark the spell slot as used
        val savedCharacter = JsonHelper.getCharacterById(appContext, "test-spell-user")!!
        savedCharacter.spellSlots[0].used = true
        JsonHelper.saveCharacter(appContext, savedCharacter)

        // 4. Read the character again and verify the change
        val updatedCharacter = JsonHelper.getCharacterById(appContext, "test-spell-user")!!
        assertTrue(updatedCharacter.spellSlots[0].used)
    }
}