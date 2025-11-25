package com.spellweave

import android.os.Bundle
import android.view.View
import androidx.gridlayout.widget.GridLayout
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.spellweave.data.Character
import com.spellweave.ui.characters.CharactercreatorFragment
import com.spellweave.util.JsonHelper
import com.spellweave.util.JsonHelperImpl
import com.spellweave.util.JsonProvider
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import junit.framework.TestCase.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class CharactercreatorFragmentTest {

    private lateinit var mockJson: JsonHelper

    @Before
    fun setup() {
        // Mock JsonProvider instead of JsonHelper
        mockJson = mock(JsonHelper::class.java)

        // Swap real provider for mock
        JsonProvider.instance = mockJson
    }

    @After
    fun cleanup() {
        // Restore real provider after each test
        JsonProvider.instance = JsonHelperImpl()
    }

    // Utility function to launch fragment in test container
    inline fun <reified F : Fragment> launchFragmentInTestContainer(
        fragmentArgs: Bundle? = null,
        initialState: Lifecycle.State = Lifecycle.State.RESUMED
    ): FragmentScenario<F> {
        return launchFragmentInContainer<F>(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.Theme_Spellweave,
            initialState = initialState
        )
    }

    @Test
    fun create_new_character_successfully() {
        launchFragmentInTestContainer<CharactercreatorFragment>()


        onView(withId(R.id.et_name)).perform(typeText("Zook"), closeSoftKeyboard())
        onView(withId(R.id.et_level)).perform(typeText("3"), closeSoftKeyboard())

        // Select class from spinner
        onView(withId(R.id.spinner_class)).perform(click())
        onView(withText("Wizard"))
            .inRoot(isPlatformPopup())
            .perform(click())


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
        launchFragmentInTestContainer<CharactercreatorFragment>(args)

        onView(withId(R.id.et_name)).check(matches(withText("Mira")))
        onView(withId(R.id.spinner_class)).check(matches(withSpinnerText(containsString("Cleric"))))
        onView(withId(R.id.et_level)).check(matches(withText("4")))
    }

    @Test
    fun add_spell_slot_increases_count() {
        // Launch the fragment in isolation
        val scenario = launchFragmentInContainer<CharactercreatorFragment>(themeResId = R.style.Theme_Spellweave)

        scenario.onFragment { fragment ->
            val container = fragment.view!!.findViewById<GridLayout>(R.id.container_spell_slots)

            val initialCount = container.childCount

            // Directly invoke the click listener for adding a spell slot
            fragment.view!!.findViewById<View>(R.id.btn_add_spell_slot).performClick()

            // Verify the container has one more child
            assertEquals(initialCount + 1, container.childCount)
        }
    }

    @Test
    fun remove_spell_slot_reduces_count() {
        val scenario = launchFragmentInContainer<CharactercreatorFragment>(themeResId = R.style.Theme_Spellweave)

        scenario.onFragment { fragment ->
            val container = fragment.view!!.findViewById<GridLayout>(R.id.container_spell_slots)

            // Make sure we have at least one slot to remove
            if (container.childCount == 0) {
                fragment.view!!.findViewById<View>(R.id.btn_add_spell_slot).performClick()
            }

            val countBeforeRemove = container.childCount

            // Directly invoke the click listener for removing a spell slot
            fragment.view!!.findViewById<View>(R.id.btn_remove_slot).performClick()

            // Verify the container has one less child
            assertEquals(countBeforeRemove - 1, container.childCount)
        }
    }

    @Test
    fun delete_character_calls_jsonprovider() {
        val c = Character(id = "deleteID", name = "Test", charClass = "Wizard")
        `when`(mockJson.getCharacterById(any(), eq("deleteID"))).thenReturn(c)

        val args = Bundle().apply { putString("characterId", "deleteID") }
        launchFragmentInTestContainer<CharactercreatorFragment>(args)

        onView(withId(R.id.btn_delete_character)).perform(click())
        onView(withText("Delete")).perform(click())

        verify(mockJson, times(1)).deleteCharacter(any(), eq("deleteID"))
    }
}
