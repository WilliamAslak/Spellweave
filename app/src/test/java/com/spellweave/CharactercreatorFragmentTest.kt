package com.spellweave.ui.characters

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.test.core.app.ApplicationProvider
import com.spellweave.R
import com.spellweave.data.Character
import com.spellweave.util.JsonHelper
import com.spellweave.launchWithNav
import com.spellweave.pump

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowToast
import androidx.appcompat.app.AlertDialog as AppAlertDialog

@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED) // make UI deterministic; we will advance it manually
class CharactercreatorFragmentTest {

    @Before
    fun clearJson() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        java.io.File(ctx.filesDir, "characters.json").delete()
    }

    @Test
    fun save_new_character_writes_to_json() {
        val (frag, _) = launchWithNav(
            fragmentFactory = { CharactercreatorFragment() },
            startDestId = R.id.nav_charactercreator
        )
        pump() // allow fragment onStart/onResume etc to settle

        val root = frag.requireView()
        root.findViewById<EditText>(R.id.et_name).setText("Azerin")
        root.findViewById<EditText>(R.id.et_level).setText("2")
        root.findViewById<Button>(R.id.btn_save_character).performClick()
        pump() // flush any IO / toast / state updates posted to main

        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val all = JsonHelper.readCharacters(ctx)
        assertEquals(1, all.size)
        assertEquals("Azerin", all[0].name)
    }

    @Test
    fun duplicate_name_plus_class_is_blocked_with_toast() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        JsonHelper.saveCharacter(ctx, Character().apply {
            name = "Azerin"; charClass = "Mage"; level = 3
        })

        val (frag, _) = launchWithNav(
            fragmentFactory = { CharactercreatorFragment() },
            startDestId = R.id.nav_charactercreator
        )
        pump()

        val root = frag.requireView()
        root.findViewById<EditText>(R.id.et_name).setText("Azerin")
        root.findViewById<Button>(R.id.btn_save_character).performClick()
        pump() // let the toast get posted

        assertEquals("character already exists", ShadowToast.getTextOfLatestToast())
        val all = JsonHelper.readCharacters(ctx)
        assertEquals(1, all.size)
    }

    @Test
    fun update_mode_shows_delete_and_deletes_after_confirm() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        JsonHelper.saveCharacter(ctx, Character().apply {
            name = "Zook"; charClass = "Wizard"; level = 5
        })
        val id = JsonHelper.readCharacters(ctx).first { it.name == "Zook" }.id

        val args = Bundle().apply { putString("characterId", id) }
        val (frag, _) = launchWithNav(
            fragmentFactory = { CharactercreatorFragment() },
            startDestId = R.id.nav_charactercreator,
            args = args
        )
        pump()

        // Open confirm dialog
        frag.requireView().findViewById<Button>(R.id.btn_delete_character).performClick()
        pump() // allow dialog to show

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        requireNotNull(dialog)

        (dialog as AppAlertDialog).getButton(AppAlertDialog.BUTTON_POSITIVE).performClick()
        pump() // process deletion + toast

        val all = JsonHelper.readCharacters(ctx)
        assertTrue(all.none { it.id == id })
        assertEquals("Character deleted", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun character_name_cannot_be_empty() {
        val (frag, _) = launchWithNav(
            fragmentFactory = { CharactercreatorFragment() },
            startDestId = R.id.nav_charactercreator
        )
        pump() // allow fragment lifecycle to settle

        val root = frag.requireView()

        // Try to save with an empty name
        root.findViewById<EditText>(R.id.et_name).setText("")       // empty name
        root.findViewById<EditText>(R.id.et_level).setText("5")     // valid level
        root.findViewById<Button>(R.id.btn_save_character).performClick()
        pump()

        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val all = JsonHelper.readCharacters(ctx)

        // Expect no characters saved
        assertEquals(
            "Character should not be saved when name is empty",
            0,
            all.size
        )
    }

    @Test
    fun character_level_must_be_between_1_and_20() {
        val (frag, _) = launchWithNav(
            fragmentFactory = { CharactercreatorFragment() },
            startDestId = R.id.nav_charactercreator
        )
        pump() // allow fragment lifecycle to settle

        val root = frag.requireView()

        // --- Test invalid: level < 1 ---
        root.findViewById<EditText>(R.id.et_name).setText("BadLevelLow")
        root.findViewById<EditText>(R.id.et_level).setText("0")   // invalid
        root.findViewById<Button>(R.id.btn_save_character).performClick()
        pump()

        var ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        var all = JsonHelper.readCharacters(ctx)
        assertEquals(
            "Characters with invalid levels should not be saved",
            0,
            all.size
        )

        // --- Test invalid: level > 20 ---
        root.findViewById<EditText>(R.id.et_name).setText("BadLevelHigh")
        root.findViewById<EditText>(R.id.et_level).setText("21")  // invalid
        root.findViewById<Button>(R.id.btn_save_character).performClick()
        pump()

        all = JsonHelper.readCharacters(ctx)
        assertEquals(
            "Characters with invalid levels should not be saved",
            0,
            all.size
        )

        // --- Test valid: 1 <= level <= 20 ---
        root.findViewById<EditText>(R.id.et_name).setText("ValidGuy")
        root.findViewById<EditText>(R.id.et_level).setText("12")  // valid
        root.findViewById<Button>(R.id.btn_save_character).performClick()
        pump()

        all = JsonHelper.readCharacters(ctx)
        assertEquals(1, all.size)
        assertEquals("ValidGuy", all[0].name)
        assertEquals(12, all[0].level)
    }

    @Test
    fun class_dropdown_contains_only_allowed_classes() {
        val allowedClasses = setOf(
            "Bard", "Cleric", "Druid", "Fighter", "Paladin",
            "Ranger", "Rogue", "Sorcerer", "Warlock", "Wizard"
        )

        val (frag, _) = launchWithNav(
            fragmentFactory = { CharactercreatorFragment() },
            startDestId = R.id.nav_charactercreator
        )
        pump()

        val root = frag.requireView()
        val spinner = root.findViewById<Spinner>(R.id.spinner_class)
        val adapter = spinner.adapter

        // Read all items from the Spinner adapter
        val actualClasses = (0 until adapter.count)
            .map { adapter.getItem(it).toString() }
            .toSet()

        // Verify exact match (order-independent)
        assertEquals(
            "Spinner must contain exactly the allowed spellcasting classes",
            allowedClasses,
            actualClasses
        )
    }

    @Test
    fun ability_scores_must_be_between_1_and_30() {
        val (frag, _) = launchWithNav(
            fragmentFactory = { CharactercreatorFragment() },
            startDestId = R.id.nav_charactercreator
        )
        pump()

        val root = frag.requireView()

        fun setStats(str: String, dex: String, con: String, int: String, wis: String, cha: String) {
            root.findViewById<EditText>(R.id.et_strength).setText(str)
            root.findViewById<EditText>(R.id.et_dexterity).setText(dex)
            root.findViewById<EditText>(R.id.et_constitution).setText(con)
            root.findViewById<EditText>(R.id.et_intelligence).setText(int)
            root.findViewById<EditText>(R.id.et_wisdom).setText(wis)
            root.findViewById<EditText>(R.id.et_charisma).setText(cha)
        }

        val saveButton = root.findViewById<Button>(R.id.btn_save_character)

        // Test invalid low: below 1
        setStats("0", "5", "5", "5", "5", "5")
        root.findViewById<EditText>(R.id.et_name).setText("BadStatsLow")
        root.findViewById<EditText>(R.id.et_level).setText("5")

        saveButton.performClick()
        pump()

        var ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        var all = JsonHelper.readCharacters(ctx)

        assertEquals(
            "Character should not save when any ability score is < 1",
            0,
            all.size
        )

        // ------------------------------
        // Test invalid high: above 30
        // ------------------------------
        setStats("10", "31", "10", "10", "10", "10")
        root.findViewById<EditText>(R.id.et_name).setText("BadStatsHigh")
        root.findViewById<EditText>(R.id.et_level).setText("5")

        saveButton.performClick()
        pump()

        all = JsonHelper.readCharacters(ctx)

        assertEquals(
            "Character should not save when any ability score is > 30",
            0,
            all.size
        )

        // ------------------------------
        // Test valid: 1–30
        // ------------------------------
        setStats("12", "14", "16", "10", "13", "15")
        root.findViewById<EditText>(R.id.et_name).setText("ValidStatsGuy")
        root.findViewById<EditText>(R.id.et_level).setText("10")

        saveButton.performClick()
        pump()

        all = JsonHelper.readCharacters(ctx)

        assertEquals(1, all.size)
        assertEquals("ValidStatsGuy", all[0].name)

        assertEquals(12, all[0].strength)
        assertEquals(14, all[0].dexterity)
        assertEquals(16, all[0].constitution)
        assertEquals(10, all[0].intelligence)
        assertEquals(13, all[0].wisdom)
        assertEquals(15, all[0].charisma)
    }

}
