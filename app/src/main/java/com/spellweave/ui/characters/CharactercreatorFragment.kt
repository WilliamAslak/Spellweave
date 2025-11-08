package com.spellweave.ui.characters

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.spellweave.R
import com.spellweave.data.Character
import com.spellweave.databinding.FragmentCharactercreatorBinding
import com.spellweave.util.JsonHelper

class CharactercreatorFragment : Fragment() {

    private var _binding: FragmentCharactercreatorBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CharactercreatorModel

    private var isUpdateMode = false
    private var characterIdArg: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(CharactercreatorModel::class.java)
        _binding = FragmentCharactercreatorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        characterIdArg = arguments?.getString("characterId")
        viewModel.characterIdToEdit = characterIdArg
        isUpdateMode = viewModel.characterIdToEdit != null

        if (isUpdateMode) {
            binding.textCharactercreator.text = "Update Character"
            binding.btnSaveCharacter.text = "Update Character"
            binding.btnDeleteCharacter.visibility = View.VISIBLE
            loadCharacterData(viewModel.characterIdToEdit!!)
        } else {
            viewModel.text.observe(viewLifecycleOwner) {
                binding.textCharactercreator.text = it
            }
            binding.btnSaveCharacter.text = "Save Character"
            binding.btnDeleteCharacter.visibility = View.GONE
        }

        binding.btnDeleteCharacter.setOnClickListener {
            confirmAndDelete()
        }

        setupClassSpinner()

        viewModel.characterData.observe(viewLifecycleOwner) { character ->
            if (character != null) {
                if (!binding.etName.hasFocus()) binding.etName.setText(character.name)
                if (!binding.etLevel.hasFocus()) binding.etLevel.setText(character.level.toString())
                if (!binding.etHp.hasFocus()) binding.etHp.setText(character.hp.toString())
                if (!binding.etSpeed.hasFocus()) binding.etSpeed.setText(character.speed.toString())

                if (!binding.etStrength.hasFocus()) binding.etStrength.setText(character.strength.toString())
                if (!binding.etDexterity.hasFocus()) binding.etDexterity.setText(character.dexterity.toString())
                if (!binding.etConstitution.hasFocus()) binding.etConstitution.setText(character.constitution.toString())
                if (!binding.etIntelligence.hasFocus()) binding.etIntelligence.setText(character.intelligence.toString())
                if (!binding.etWisdom.hasFocus()) binding.etWisdom.setText(character.wisdom.toString())
                if (!binding.etCharisma.hasFocus()) binding.etCharisma.setText(character.charisma.toString())

                setSpinnerSelection(character.CharClass)
            }
        }

        if (isUpdateMode) {
            binding.etName.isEnabled = false
            binding.spinnerClass.isEnabled = false
        }

        binding.btnSaveCharacter.setOnClickListener {
            saveCharacter()
        }

        return root
    }

    private fun loadCharacterData(characterId: String) {
        val character = JsonHelper.getCharacterById(requireContext(), characterId)
        if (character != null) {
            viewModel.characterData.value = character
        } else {
            Toast.makeText(requireContext(), "Error: Could not find character", Toast.LENGTH_SHORT).show()
            //Go back if we couldn't find the character
            findNavController().popBackStack()
        }
    }

    private fun setSpinnerSelection(characterClass: String?) {
        if (characterClass.isNullOrBlank()) return
        val classes = resources.getStringArray(R.array.dnd_classes)
        val position = classes.indexOf(characterClass)
        if (position >= 0) binding.spinnerClass.setSelection(position)
    }

    private fun setupClassSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.dnd_classes,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerClass.adapter = adapter

        binding.spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!isUpdateMode) {
                    val selectedClass = parent.getItemAtPosition(position).toString()
                    updateStatsForClass(selectedClass)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateStatsForClass(className: String) {
        //Start from current character or a new one (the ? ensures null safety)
        val character = viewModel.characterData.value ?: Character()

        //Preserve what the user has already typed
        character.name = binding.etName.text?.toString()
        character.level = binding.etLevel.text?.toString()?.toIntOrNull() ?: character.level
        character.hp = binding.etHp.text?.toString()?.toIntOrNull() ?: character.hp
        character.speed = binding.etSpeed.text?.toString()?.toIntOrNull() ?: character.speed
        character.strength = binding.etStrength.text?.toString()?.toIntOrNull() ?: character.strength
        character.dexterity = binding.etDexterity.text?.toString()?.toIntOrNull() ?: character.dexterity
        character.constitution = binding.etConstitution.text?.toString()?.toIntOrNull() ?: character.constitution
        character.intelligence = binding.etIntelligence.text?.toString()?.toIntOrNull() ?: character.intelligence
        character.wisdom = binding.etWisdom.text?.toString()?.toIntOrNull() ?: character.wisdom
        character.charisma = binding.etCharisma.text?.toString()?.toIntOrNull() ?: character.charisma

        if (character.CharClass == className) {
            viewModel.characterData.value = character
            return
        }
        character.CharClass = className

        //switch case that with default class values. Modify this so it fits characters better
        when (className) {
            "Mage" -> {
                character.strength = 8;  character.dexterity = 10; character.constitution = 10
                character.intelligence = 15; character.wisdom = 14; character.charisma = 12
            }
            "Thief" -> {
                character.strength = 10; character.dexterity = 15; character.constitution = 10
                character.intelligence = 12; character.wisdom = 8;  character.charisma = 14
            }
            "Warrior" -> {
                character.strength = 15; character.dexterity = 14; character.constitution = 12
                character.intelligence = 8;  character.wisdom = 10; character.charisma = 10
            }
            "Bullywug" -> {
                character.strength = 12; character.dexterity = 12; character.constitution = 12
                character.intelligence = 10; character.wisdom = 10; character.charisma = 8
            }
            //If nothing, keep values as is.
            else -> {}
        }

        viewModel.characterData.value = character
    }

    private fun saveCharacter() {
        //Get the current character object (either new or loaded)
        val characterToSave = viewModel.characterData.value ?: Character()

        //Name and Class are read-only in update mode.
        characterToSave.name = binding.etName.text.toString()
        characterToSave.CharClass = binding.spinnerClass.selectedItem.toString()
        characterToSave.level = binding.etLevel.text.toString().toIntOrNull() ?: 1
        characterToSave.hp = binding.etHp.text.toString().toIntOrNull() ?: 10
        characterToSave.speed = binding.etSpeed.text.toString().toIntOrNull() ?: 30
        characterToSave.strength = binding.etStrength.text.toString().toIntOrNull() ?: 10
        characterToSave.dexterity = binding.etDexterity.text.toString().toIntOrNull() ?: 10
        characterToSave.constitution = binding.etConstitution.text.toString().toIntOrNull() ?: 10
        characterToSave.intelligence = binding.etIntelligence.text.toString().toIntOrNull() ?: 10
        characterToSave.wisdom = binding.etWisdom.text.toString().toIntOrNull() ?: 10
        characterToSave.charisma = binding.etCharisma.text.toString().toIntOrNull() ?: 10

        val enteredName = characterToSave.name?.trim().orEmpty()
        val enteredClass = characterToSave.CharClass?.trim().orEmpty()

        if (enteredName.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
            return
        }

        //Block duplicate characters (same name and class)
        if (!isUpdateMode) {
            val existing = JsonHelper.readCharacters(requireContext()).any { c ->
                (c.name?.trim()?.lowercase() == enteredName.lowercase()) && (c.CharClass?.trim() == enteredClass)
            }
            if (existing) {
                Toast.makeText(requireContext(), "character already exists", Toast.LENGTH_SHORT).show()
                return
            }
        }

        JsonHelper.saveCharacter(requireContext(), characterToSave)

        //Little popup saying we either updated/saved a character
        val message = if (isUpdateMode) "Character updated!" else "Character saved!"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

        //Navigate back
        findNavController().popBackStack()
    }

    private fun confirmAndDelete() {
        val id = viewModel.characterIdToEdit ?: return
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete character")
            .setMessage("Are you sure you want to delete this character?")
            .setPositiveButton("Delete") { _, _ ->
                val deleted = JsonHelper.deleteCharacter(requireContext(), id)
                if (deleted) {
                    Toast.makeText(requireContext(), "Character deleted", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}