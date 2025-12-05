package com.spellweave.util

import android.content.Context
import android.util.Log
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

    fun saveCharacter(context: Context, characterToSave: Character): Boolean {
        if (characterToSave.name.isNullOrBlank()) {
            Log.w("JsonHelper", "Validation failed: Character name is null or blank.")
            return false // Prevent saving a character with an empty name
        }

        // Validate character level is between 1 and 20
        if (characterToSave.level !in 1..20) {
            Log.w("JsonHelper", "Validation failed: Level must be between 1 and 20. Was ${characterToSave.level}.")
            return false
        }

        // Validate all ability scores are between 1 and 30
        val abilityScores = listOf(
            characterToSave.strength,
            characterToSave.dexterity,
            characterToSave.constitution,
            characterToSave.intelligence,
            characterToSave.wisdom,
            characterToSave.charisma
        )
        if (abilityScores.any { it !in 1..30 }) {
            Log.w("JsonHelper", "Validation failed: All ability scores must be between 1 and 30.")
            return false
        }

        val characters = readCharacters(context)

        // Check if a character with the same name already exists
        val existingCharacterByName = characters.find { it.name.equals(characterToSave.name, ignoreCase = true) }
        if (existingCharacterByName != null && existingCharacterByName.id != characterToSave.id) {
            return false // A character with this name already exists
        }

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
        return true
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