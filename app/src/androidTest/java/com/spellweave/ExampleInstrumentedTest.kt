package com.spellweave

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spellweave.data.Character
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
        JsonHelper.saveCharacter(appContext, newCharacter)

        // 3. Read the characters back from the file
        val characters = JsonHelper.readCharacters(appContext)

        // 4. Assert that the new character is in the list
        val savedCharacter = characters.find { it.id == "test-id" }
        assertNotNull(savedCharacter)
        assertEquals("Test Character", savedCharacter?.name)
        assertEquals("Wizard", savedCharacter?.charClass)
        assertEquals(5, savedCharacter?.level)
    }
}