package com.spellweave.ui.characters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.spellweave.data.Character

class CharactercreatorModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Create character"
    }
    val text: LiveData<String> = _text

    //This will hold the ID of the character we are editing.
    //If it's null, we are creating a new character.
    var characterIdToEdit: String? = null

    val characterData = MutableLiveData<Character>(Character())
}