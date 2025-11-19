package com.spellweave.ui.gameplay

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.spellweave.R
import com.spellweave.data.Character
import com.spellweave.data.SpellSlot
import com.spellweave.databinding.FragmentGameplayBinding
import com.spellweave.util.JsonHelper
import androidx.core.os.bundleOf

class GameplayFragment : Fragment() {

    private var _binding: FragmentGameplayBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GameplayModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(GameplayModel::class.java)
        _binding = FragmentGameplayBinding.inflate(inflater, container, false)
        val root = binding.root

        val characterIdArg = arguments?.getString("characterId")
        if (characterIdArg == null) {
            Toast.makeText(requireContext(), "No character ID", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return root
        }

        viewModel.characterId = characterIdArg
        viewModel.loadCharacter(requireContext())

        setupObservers()
        setupHpButtons()
        setupLongRestButton()
        setupUpdateCharacterButton()

        return root
    }

    private fun setupUpdateCharacterButton() {
        binding.btnUpdateCharacter.setOnClickListener {
            val id = viewModel.characterId
            if (id == null) {
                Toast.makeText(requireContext(), "No character ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = bundleOf("characterId" to id)
            // Navigate to the existing CharactercreatorFragment
            findNavController().navigate(R.id.action_nav_gameplay_to_nav_charactercreator, bundle)
        }
    }

    private fun setupObservers() {
        viewModel.characterData.observe(viewLifecycleOwner) { character ->
            if (character != null) {
                updateUi(character)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(c: Character) {
        binding.tvName.text = c.name ?: "Unnamed"
        binding.tvClassLevel.text = "${c.CharClass ?: "Class"} • Lv ${c.level}"

        val currentHp = c.currentHp ?: c.hp
        binding.tvHpValue.text = "$currentHp / ${c.hp}"

        // NEW: stats
        binding.tvStrengthValue.text = "STR: ${c.strength}"
        binding.tvDexterityValue.text = "DEX: ${c.dexterity}"
        binding.tvConstitutionValue.text = "CON: ${c.constitution}"
        binding.tvIntelligenceValue.text = "INT: ${c.intelligence}"
        binding.tvWisdomValue.text = "WIS: ${c.wisdom}"
        binding.tvCharismaValue.text = "CHA: ${c.charisma}"

        renderSpellSlots(c.spellSlots)
    }

    private fun setupHpButtons() {
        binding.btnHpMinus.setOnClickListener {
            viewModel.updateHp(-1)
            saveCharacterState()
        }

        binding.btnHpPlus.setOnClickListener {
            viewModel.updateHp(1)
            saveCharacterState()
        }
    }

    private fun setupLongRestButton() {
        binding.btnLongRest.setOnClickListener {
            viewModel.longRest()
            saveCharacterState()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderSpellSlots(spellSlots: List<SpellSlot>) {
        val grid = binding.gridSpellSlotsGameplay
        grid.removeAllViews()

        // Use the underlying order (already sorted in ViewModel.loadCharacter)
        spellSlots.forEachIndexed { index, slot ->
            val slotView = layoutInflater.inflate(
                R.layout.item_spell_slot_gameplay,
                grid,
                false
            )

            val card = slotView as MaterialCardView
            val tvLevel = slotView.findViewById<TextView>(R.id.tv_slot_level)
            tvLevel.text = "Lvl ${slot.level}"

            applySlotStyle(card, slot.used)

            card.setOnClickListener {
                viewModel.toggleSpellSlotUsed(index)
                // After toggling, re-read from ViewModel and save
                viewModel.characterData.value?.let { updated ->
                    applySlotStyle(card, updated.spellSlots[index].used)
                    saveCharacterState()
                }
            }

            grid.addView(slotView)
        }
    }

    private fun applySlotStyle(card: MaterialCardView, used: Boolean) {
        // Simple gray-out effect when used
        card.alpha = if (used) 0.4f else 1.0f
    }

    private fun saveCharacterState() {
        val c = viewModel.characterData.value ?: return
        JsonHelper.saveCharacter(requireContext(), c)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCharacter(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
