package com.spellweave.ui.gameplay

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.spellweave.data.Character
import com.spellweave.util.JsonHelper

class GameplayModel : ViewModel() {

    private val _characterData = MutableLiveData<Character>()
    val characterData: LiveData<Character> = _characterData

    // ID passed in via arguments
    var characterId: String? = null

    fun loadCharacter(context: Context) {
        val id = characterId ?: return
        val loaded = JsonHelper.getCharacterById(context, id) ?: return

        // Initialize currentHp if missing
        if (loaded.currentHp == null) {
            loaded.currentHp = loaded.hp
        }

        // Ensure spell slots are sorted the way you want for gameplay
        loaded.spellSlots = loaded.spellSlots
            .sortedByDescending { it.level }
            .toMutableList()

        _characterData.value = loaded
    }

    fun updateHp(impactToHp: Int) {
        val c = _characterData.value ?: return
        val maxHp = c.hp
        val current = c.currentHp ?: maxHp
        val newHp = (current?.plus(impactToHp))?.coerceIn(0, maxHp)
        c.currentHp = newHp
        _characterData.value = c
    }

    fun longRest() {
        val c = _characterData.value ?: return
        c.currentHp = c.hp
        c.spellSlots.forEach { it.used = false }
        _characterData.value = c
    }

    fun toggleSpellSlotUsed(index: Int) {
        val c = _characterData.value ?: return
        if (index !in c.spellSlots.indices) return
        val slot = c.spellSlots[index]
        slot.used = !slot.used
        _characterData.value = c
    }
}
