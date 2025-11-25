package com.spellweave.ui.characterlist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.spellweave.data.Character
import com.spellweave.util.JsonHelper
import com.spellweave.util.JsonProvider

class CharacterlistModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Your Characters"
    }
    val text: LiveData<String> = _text

    private val _characterList = MutableLiveData<List<Character>>()
    val characterList: LiveData<List<Character>> = _characterList

    //Function to load characters from JSON
    fun loadCharacters(context: Context) {
        _characterList.value = JsonProvider.instance.readCharacters(context)
    }
}