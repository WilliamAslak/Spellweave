package com.spellweave.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spellweave.data.Character
import java.io.File

class JsonHelperImpl : JsonHelper {

    private val fileName = "characters.json"

    private fun getCharacterFile(context: Context): File {
        return File(context.filesDir, fileName)
    }

    override fun readCharacters(context: Context): MutableList<Character> {
        val file = getCharacterFile(context)
        if (!file.exists()) return mutableListOf()

        return try {
            val json = file.readText()
            if (json.isBlank()) return mutableListOf()

            val type = object : TypeToken<MutableList<Character>>() {}.type
            Gson().fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    override fun saveCharacter(context: Context, characterToSave: Character) {
        val characters = readCharacters(context)

        val index = characters.indexOfFirst { it.id == characterToSave.id }
        if (index != -1) {
            characters[index] = characterToSave
        } else {
            characters.add(characterToSave)
        }

        getCharacterFile(context).writeText(Gson().toJson(characters))
    }

    override fun deleteCharacter(context: Context, id: String): Boolean {
        val characters = readCharacters(context)
        val removed = characters.removeAll { it.id == id }

        if (removed) {
            getCharacterFile(context).writeText(Gson().toJson(characters))
        }

        return removed
    }

    override fun getCharacterById(context: Context, id: String): Character? {
        return readCharacters(context).find { it.id == id }
    }

    /*
    //For testing purposes
    private var testOverride: JsonHelper? = null
    fun overrideInstanceForTests(override: JsonHelper) {
        testOverride = override
    }
    fun resetTestOverride() {
        testOverride = null
    }

    fun testReadCharacters(context: Context) =
        testOverride?.readCharacters(context) ?: JsonHelper.readCharacters(context)

    fun testGetCharacterById(context: Context, id: String) =
        testOverride?.getCharacterById(context, id) ?: JsonHelper.getCharacterById(context, id)

    fun testSaveCharacter(context: Context, char: Character) =
        testOverride?.saveCharacter(context, char) ?: JsonHelper.saveCharacter(context, char)

    fun testDeleteCharacter(context: Context, id: String) =
        testOverride?.deleteCharacter(context, id) ?: JsonHelper.deleteCharacter(context, id)

     */
}
