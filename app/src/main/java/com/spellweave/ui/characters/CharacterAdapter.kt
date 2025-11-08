package com.spellweave.ui.characterlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spellweave.R
import com.spellweave.data.Character
import com.spellweave.databinding.ListItemCharacterBinding

class CharacterAdapter(
    private var characters: List<Character>,
    private val onClickListener: (Character) -> Unit
) : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {


    inner class CharacterViewHolder(private val binding: ListItemCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: Character) {
            val name = character.name ?: "Unnamed"
            val lvl = character.level ?: 1
            val clazz = character.CharClass ?: "Adventurer"

            binding.tvCharacterName.text = name
            binding.tvCharacterDetails.text = "Level $lvl $clazz"

            binding.ivClassLogo.setImageResource(classIconFor(clazz))

            binding.root.setOnClickListener { onClickListener(character) }
        }
        //Apply images for each class
        private fun classIconFor(clazz: String): Int = when (clazz) {
            "Warrior" -> R.drawable.ic_class_warrior
            "Mage" -> R.drawable.ic_class_mage
            "Thief" -> R.drawable.ic_class_thief
            "Bullywug" -> R.drawable.ic_class_bullywug
            else -> R.drawable.ic_class_default
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = ListItemCharacterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(characters[position])
    }

    override fun getItemCount(): Int = characters.size

    fun updateData(newCharacters: List<Character>) {
        characters = newCharacters
        notifyDataSetChanged()
    }


}