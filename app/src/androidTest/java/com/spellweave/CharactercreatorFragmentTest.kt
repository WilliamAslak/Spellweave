package com.spellweave

import android.os.Bundle
import android.widget.GridLayout
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.spellweave.data.Character
import com.spellweave.ui.characters.CharactercreatorFragment
import com.spellweave.util.JsonHelper
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
@LargeTest
class CharactercreatorFragmentTest {

    private lateinit var mockJson: JsonHelper

    @Before
    fun setup() {
        // Replace JsonHelper with a mock for deterministic tests
        mockJson = mock(JsonHelper::class.java)
        JsonHelper.overrideInstanceForTests(mockJson)
    }

    @After
    fun cleanup() {
        JsonHelper.resetTestOverride()
    }

    @Test
    fun create_new_character_successfully() {
        launchFragmentInTestActivity(CharactercreatorFragment())

        onView(withId(R.id.et_name)).perform(typeText("Zook"), closeSoftKeyboard())
        onView(withId(R.id.et_level)).perform(typeText("3"), closeSoftKeyboard())

        // Select class from spinner
        onView(withId(R.id.spinner_class)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Wizard"))).perform(click())

        onView(withId(R.id.btn_add_spell_slot)).perform(click())
        onView(withId(R.id.btn_save_character)).perform(click())

        verify(mockJson, times(1)).saveCharacter(any(), any())
    }

    @Test
    fun update_existing_character_loads_values() {
        val c = Character(
            id = "abc123",
            name = "Mira",
            charClass = "Cleric",
            level = 4
        )
        `when`(mockJson.getCharacterById(any(), eq("abc123"))).thenReturn(c)

        val args = Bundle().apply { putString("characterId", "abc123") }
        launchFragmentInTestActivity(CharactercreatorFragment(), args)

        onView(withId(R.id.et_name)).check(matches(withText("Mira")))
        onView(withId(R.id.spinner_class)).check(matches(withSpinnerText(containsString("Cleric"))))
        onView(withId(R.id.et_level)).check(matches(withText("4")))
    }

    @Test
    fun add_spell_slot_increases_count() {
        launchFragmentInTestActivity(CharactercreatorFragment())

        // Initially 2 default spell slots
        Thread.sleep(200) // allow rendering
        onView(withId(R.id.container_spell_slots))
            .check { view, _ ->
                val count = view as GridLayout
                assert(count.childCount >= 2)
            }

        onView(withId(R.id.btn_add_spell_slot)).perform(click())
        Thread.sleep(200)

        onView(withId(R.id.container_spell_slots))
            .check { view, _ ->
                val count = view as GridLayout
                assert(count.childCount >= 3)
            }
    }

    @Test
    fun remove_spell_slot_reduces_count() {
        launchFragmentInTestActivity(CharactercreatorFragment())

        Thread.sleep(300)
        onView(withId(R.id.container_spell_slots))
            .check { v, _ -> assert((v as GridLayout).childCount >= 2) }

        onView(withId(R.id.btn_remove_slot)).perform(click())
        Thread.sleep(300)

        onView(withId(R.id.container_spell_slots))
            .check { v, _ -> assert((v as GridLayout).childCount >= 1) }
    }

    @Test
    fun delete_character_calls_jsonhelper() {
        val c = Character(id = "deleteID", name = "Test", charClass = "Wizard")
        `when`(mockJson.getCharacterById(any(), eq("deleteID"))).thenReturn(c)

        val args = Bundle().apply { putString("characterId", "deleteID") }
        launchFragmentInTestActivity(CharactercreatorFragment(), args)

        onView(withId(R.id.btn_delete_character)).perform(click())

        onView(withText("Delete")).perform(click())

        verify(mockJson, times(1)).deleteCharacter(any(), eq("deleteID"))
    }
}
