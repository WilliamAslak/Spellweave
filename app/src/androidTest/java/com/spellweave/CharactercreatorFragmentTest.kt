package com.spellweave

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import androidx.gridlayout.widget.GridLayout
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.spellweave.data.Character
import com.spellweave.data.SpellSlot
import com.spellweave.ui.characters.CharactercreatorFragment
import com.spellweave.util.JsonHelper
import com.spellweave.util.JsonHelperImpl
import com.spellweave.util.JsonProvider
import junit.framework.TestCase.assertEquals
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

inline fun <reified T : Any> any(): T = org.mockito.Mockito.any(T::class.java)!!

@RunWith(AndroidJUnit4::class)
@LargeTest
class CharactercreatorFragmentTest {

    private lateinit var mockJson: JsonHelper

    @Before
    fun setup() {
        mockJson = mock(JsonHelper::class.java)
        JsonProvider.instance = mockJson
    }

    @After
    fun cleanup() {
        JsonProvider.instance = JsonHelperImpl()
    }

    private inline fun <reified F : CharactercreatorFragment> launchFragment(
        fragmentArgs: Bundle? = null,
        initialState: Lifecycle.State = Lifecycle.State.RESUMED
    ): FragmentScenario<F> {
        return launchFragmentInContainer(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.Theme_Spellweave,
            initialState = initialState
        )
    }

    @Test
    fun create_new_character_successfully() {
        launchFragment<CharactercreatorFragment>().onFragment { fragment ->
            onView(withId(R.id.et_name)).perform(typeText("Zook"), closeSoftKeyboard())
            onView(withId(R.id.et_level)).perform(typeText("3"), closeSoftKeyboard())
            onView(withId(R.id.spinner_class)).perform(click())
            onView(withText("Wizard")).inRoot(isPlatformPopup()).perform(click())
            onView(withId(R.id.btn_add_spell_slot)).perform(click())
            onView(withId(R.id.btn_save_character)).perform(click())

            verify(mockJson, times(1)).saveCharacter(any<Context>(), any())
        }
    }

    @Test
    fun update_existing_character_loads_values() {
        val c = Character(id = "abc123", name = "Mira", charClass = "Cleric", level = 4)
        `when`(mockJson.getCharacterById(any<Context>(), eq("abc123"))).thenReturn(c)

        val args = Bundle().apply { putString("characterId", "abc123") }

        launchFragment<CharactercreatorFragment>(args).onFragment { fragment ->
            // Safely load character after mock is ready
            fragment.loadCharacterIfNeeded()

            val etName = fragment.view!!.findViewById<EditText>(R.id.et_name)
            val etLevel = fragment.view!!.findViewById<EditText>(R.id.et_level)
            val spinnerClass = fragment.view!!.findViewById<Spinner>(R.id.spinner_class)

            assertEquals("Mira", etName.text.toString())
            assertEquals("4", etLevel.text.toString())
            assertEquals("Cleric", spinnerClass.selectedItem.toString())
        }
    }

    @Test
    fun add_spell_slot_increases_count() {
        launchFragment<CharactercreatorFragment>().onFragment { fragment ->
            val container = fragment.view!!.findViewById<GridLayout>(R.id.container_spell_slots)
            val initialCount = container.childCount

            fragment.view!!.findViewById<android.view.View>(R.id.btn_add_spell_slot).performClick()

            assertEquals(initialCount + 1, container.childCount)
        }
    }

    @Test
    fun remove_spell_slot_reduces_count() {
        launchFragment<CharactercreatorFragment>().onFragment { fragment ->
            val container = fragment.view!!.findViewById<GridLayout>(R.id.container_spell_slots)

            if (container.childCount == 0) {
                fragment.view!!.findViewById<android.view.View>(R.id.btn_add_spell_slot).performClick()
            }

            val countBeforeRemove = container.childCount
            fragment.view!!.findViewById<android.view.View>(R.id.btn_remove_slot).performClick()
            assertEquals(countBeforeRemove - 1, container.childCount)
        }
    }

    @Test
    fun delete_character_calls_jsonprovider() {
        val c = Character(id = "deleteID", name = "Test", charClass = "Wizard")
        `when`(mockJson.getCharacterById(any<Context>(), eq("deleteID"))).thenReturn(c)

        val args = Bundle().apply { putString("characterId", "deleteID") }

        launchFragment<CharactercreatorFragment>(args).onFragment { fragment ->
            fragment.loadCharacterIfNeeded()

            onView(withId(R.id.btn_delete_character)).perform(click())
            onView(withText("Delete")).perform(click())

            verify(mockJson, times(1)).deleteCharacter(any<Context>(), eq("deleteID"))
        }
    }
}
