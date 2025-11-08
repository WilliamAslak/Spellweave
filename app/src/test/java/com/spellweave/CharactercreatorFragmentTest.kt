package com.spellweave.ui.characters

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
            name = "Azerin"; CharClass = "Mage"; level = 3
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
            name = "Thorin"; CharClass = "Warrior"; level = 5
        })
        val id = JsonHelper.readCharacters(ctx).first { it.name == "Thorin" }.id

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
}
