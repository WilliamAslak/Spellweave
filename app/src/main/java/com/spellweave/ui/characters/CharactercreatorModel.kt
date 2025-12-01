package com.spellweave.ui.characters

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.spellweave.data.Character
import com.spellweave.data.SpellSlot
import com.spellweave.data.remote.ApiClient

class CharactercreatorModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Create character"
    }
    val text: LiveData<String> = _text

    var characterIdToEdit: String? = null

    val characterData = MutableLiveData<Character>(Character())

    suspend fun loadSpellSlotsFor(className: String, level: Int) {
        if (className.isBlank() || level <= 0) {
            return
        }

        try {
            val response = ApiClient.api.getClassLevel(className.lowercase(), level)

            val slots = mutableListOf<SpellSlot>()

            response.spellcasting?.let { sc ->

                val slotMap = mapOf(
                    1 to sc.spell_slots_level_1,
                    2 to sc.spell_slots_level_2,
                    3 to sc.spell_slots_level_3,
                    4 to sc.spell_slots_level_4,
                    5 to sc.spell_slots_level_5,
                    6 to sc.spell_slots_level_6,
                    7 to sc.spell_slots_level_7,
                    8 to sc.spell_slots_level_8,
                    9 to sc.spell_slots_level_9
                )

                slotMap.toList().sortedByDescending { (slotLevel, _) -> slotLevel }
                    .forEach { (slotLevel, count) -> repeat(count) {
                            slots.add(SpellSlot(level = slotLevel, used = false))
                        }
                    }
            }

            val c = characterData.value ?: Character()
            c.spellSlots = slots
            characterData.postValue(c)

        } catch (e: Exception) {
            Log.e("CharactercreatorModel", "Failed to load spell slots for $className level $level", e)
        }
    }
}
