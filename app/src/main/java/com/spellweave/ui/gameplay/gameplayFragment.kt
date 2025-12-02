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
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.spellweave.data.remote.ApiClient
import com.spellweave.data.remote.SpellSummary
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
        binding.tvClassLevel.text = "${c.charClass ?: "Class"} • Lv ${c.level}"

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
        binding.btnTakeDamage.setOnClickListener {
            val amount = binding.etHpChange.text.toString().toIntOrNull()
            if (amount == null) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateHp(-amount)
            saveCharacterState()
            binding.etHpChange.text?.clear()
        }

        binding.btnHeal.setOnClickListener {
            val amount = binding.etHpChange.text.toString().toIntOrNull()
            if (amount == null) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateHp(amount)
            saveCharacterState()
            binding.etHpChange.text?.clear()
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
                val character = viewModel.characterData.value ?: return@setOnClickListener
                val currentSlot = character.spellSlots.getOrNull(index) ?: return@setOnClickListener

                if (currentSlot.used) {
                    // If already used, simply un-use it
                    viewModel.toggleSpellSlotUsed(index)
                    viewModel.characterData.value?.let { updated ->
                        applySlotStyle(card, updated.spellSlots[index].used)
                        saveCharacterState()
                    }
                } else {
                    // If unused, run the "cast spell" flow
                    startCastSpellFlow(slotIndex = index, slot = currentSlot, character = character, card = card)
                }
            }

            grid.addView(slotView)
        }
    }
    private fun startCastSpellFlow(slotIndex: Int, slot: SpellSlot, character: Character, card: MaterialCardView) {
        val maxLevel = slot.level
        if (maxLevel <= 0) return

        // Available levels to cast (slot level and down)
        val levels = (1..maxLevel).toList().reversed()
        val levelLabels = levels.map { "Cast as level $it" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Choose spell level")
            .setItems(levelLabels) { _, which ->
                val chosenLevel = levels[which]
                pickSpellForLevel(slotIndex, chosenLevel, character, card)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun pickSpellForLevel(slotIndex: Int, chosenLevel: Int, character: Character, card: MaterialCardView) {
        val classIndex = character.charClass?.lowercase() ?: run {
            Toast.makeText(requireContext(), "Character class missing", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Get all spells for that class
                val spellList = ApiClient.api.getClassSpells(classIndex)

                // Only keep spells of the chosen level
                val spellsForLevel = spellList.results.filter { it.level == chosenLevel }

                if (spellsForLevel.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No level $chosenLevel spells for ${character.charClass}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val spellNames = spellsForLevel.map { it.name }.toTypedArray()

                AlertDialog.Builder(requireContext())
                    .setTitle("Choose spell (level $chosenLevel)")
                    .setItems(spellNames) { _, which ->
                        val chosenSpell = spellsForLevel[which]
                        showSpellDetailAndConsumeSlot(slotIndex, chosenSpell, card, chosenLevel)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to load spells", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSpellDetailAndConsumeSlot(slotIndex: Int, summary: SpellSummary, card: MaterialCardView, castAtLevel: Int) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val detail = ApiClient.api.getSpell(summary.index)

                    val desc = detail.desc?.joinToString("\n\n") ?: "No description available."
                    val higher = detail.higher_level?.joinToString("\n") ?: ""
                    val infoBuilder = StringBuilder()

                    infoBuilder.append(desc)

                    if (higher.isNotBlank()) {
                        infoBuilder.append("\n\nAt Higher Levels:\n")
                        infoBuilder.append(higher)
                    }

                    infoBuilder.append("\n\nRange: ${detail.range ?: "—"}")
                    infoBuilder.append("\nDuration: ${detail.duration ?: "—"}")
                    infoBuilder.append("\nCasting time: ${detail.casting_time ?: "—"}")
                    infoBuilder.append("\nConcentration: ${if (detail.concentration == true) "Yes" else "No"}")
                    infoBuilder.append("\nRitual: ${if (detail.ritual == true) "Yes" else "No"}")

                    AlertDialog.Builder(requireContext())
                        .setTitle(detail.name)
                        .setMessage(infoBuilder.toString())
                        .setPositiveButton("Use slot") { _, _ ->
                            viewModel.markSlotUsedWithSpell(
                                index = slotIndex,
                                spellIndex = summary.index,
                                spellName = detail.name,
                                castAtLevel = castAtLevel
                            )

                            viewModel.characterData.value?.let { updated ->
                                applySlotStyle(card, updated.spellSlots[slotIndex].used)
                                saveCharacterState()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Failed to load spell details", Toast.LENGTH_SHORT).show()
                }
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
