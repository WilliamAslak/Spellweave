package com.spellweave.util

import android.content.Context
import com.spellweave.data.Character

interface JsonHelper {
    fun readCharacters(context: Context): MutableList<Character>
    fun saveCharacter(context: Context, characterToSave: Character)
    fun deleteCharacter(context: Context, id: String): Boolean
    fun getCharacterById(context: Context, id: String): Character?
}
