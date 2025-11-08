package com.spellweave.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spellweave.data.Character
import java.io.File

object JsonHelper {

    private const val FILE_NAME = "characters.json"

    private fun getCharacterFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    fun readCharacters(context: Context): MutableList<Character> {
        val file = getCharacterFile(context)
        if (!file.exists()) return mutableListOf()

        return try {
            val gson = Gson()
            val json = file.readText()
            if (json.isBlank()) return mutableListOf()
            val type = object : TypeToken<MutableList<Character>>() {}.type
            gson.fromJson<MutableList<Character>>(json, type) ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    private fun writeCharacters(context: Context, characters: List<Character>) {
        val file = getCharacterFile(context)
        val gson = Gson()
        val json = gson.toJson(characters)
        file.writeText(json)
    }

    fun saveCharacter(context: Context, characterToSave: Character) {
        val characters = readCharacters(context)

        //Find the index of the character with the same id
        val existingIndex = characters.indexOfFirst { it.id == characterToSave.id }

        if (existingIndex != -1) {
            //Found: replace the old one with the new one
            characters[existingIndex] = characterToSave
        } else {
            //Not found: add it as a new character
            characters.add(characterToSave)
        }

        writeCharacters(context, characters)
    }

    fun deleteCharacter(context: Context, characterId: String): Boolean {
        val characters = readCharacters(context)
        val removed = characters.removeAll { it.id == characterId }
        if (removed) {
            val file = File(context.filesDir, FILE_NAME)
            val gson = Gson()
            file.writeText(gson.toJson(characters))
        }
        return removed
    }


    fun getCharacterById(context: Context, characterId: String): Character? {
        val characters = readCharacters(context)
        return characters.find { it.id == characterId }
    }
}