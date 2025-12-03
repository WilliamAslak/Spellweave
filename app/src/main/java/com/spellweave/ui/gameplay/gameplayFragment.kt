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
import com.spellweave.R
import com.spellweave.data.Character
import com.spellweave.data.SpellSlot
import com.spellweave.databinding.FragmentGameplayBinding
import com.spellweave.util.JsonHelper
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
import com.spellweave.data.remote.ApiClient
import com.spellweave.data.remote.SpellSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameplayFragment : Fragment() {

    private var _binding: FragmentGameplayBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GameplayModel

    private val dndApi = ApiClient.api
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
        setupCastSpellButton()

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

        // stats
        binding.tvStrengthValue.text = "STR: ${c.strength}"
        binding.tvDexterityValue.text = "DEX: ${c.dexterity}"
        binding.tvConstitutionValue.text = "CON: ${c.constitution}"
        binding.tvIntelligenceValue.text = "INT: ${c.intelligence}"
        binding.tvWisdomValue.text = "WIS: ${c.wisdom}"
        binding.tvCharismaValue.text = "CHA: ${c.charisma}"

        renderSpellSlots(c.spellSlots)

        //Enable-disable Cast Spell button
        val hasAvailableSlots = c.spellSlots.any { !it.used }
        binding.btnCastSpell.isEnabled = hasAvailableSlots
        binding.btnCastSpell.alpha = if (hasAvailableSlots) 1f else 0.5f
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
            // Manually trigger a UI update to reflect the reset spell slots
            viewModel.characterData.value?.let { updateUi(it) }
            saveCharacterState()
        }
    }

    private fun setupCastSpellButton() {
        binding.btnCastSpell.setOnClickListener {
            val character = viewModel.characterData.value
            if (character == null) {
                Toast.makeText(requireContext(), "No character loaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val availableSlots = character.spellSlots.filter { !it.used }
            if (availableSlots.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "No spell slots available",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Highest level slot available
            val maxSlotLevel = availableSlots.maxOf { it.level }
            val spellLevels = (1..maxSlotLevel).toList()
            val levelLabels = spellLevels.map { "Level $it" }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Choose Spell Level")
                .setItems(levelLabels) { _, which ->
                    val chosenSpellLevel = spellLevels[which]
                    showSpellSelectionDialog(character, chosenSpellLevel)
                }
                .show()
        }
    }

    private fun showSpellSelectionDialog(character: Character, spellLevel: Int) {
        val className = character.charClass?.lowercase() ?: "wizard"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Get all spells for this class
                val spellList = withContext(Dispatchers.IO) {
                    dndApi.getClassSpells(className)
                }

                val spellsOfLevel: List<SpellSummary> =
                    spellList.results.filter { it.level == spellLevel }

                if (spellsOfLevel.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No level $spellLevel spells available for $className",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val spellNames = spellsOfLevel.map { it.name }.toTypedArray()

                AlertDialog.Builder(requireContext())
                    .setTitle("Choose Spell (Level $spellLevel)")
                    .setItems(spellNames) { _, which ->
                        val chosenSpell = spellsOfLevel[which]
                        showSpellDetailDialog(character, chosenSpell, spellLevel)
                    }.setNegativeButton("Cancel", null)
                    .show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Failed to load spells: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showSpellDetailDialog(character: Character, spellSummary: SpellSummary, spellLevel: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val detail = withContext(Dispatchers.IO) {
                    dndApi.getSpell(spellSummary.index)
                }

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
                    .setPositiveButton("Select Spell Slot") { _, _ ->
                        // After viewing info, you pick the slot
                        showSlotLevelSelectionDialog(character, spellSummary, spellLevel)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Failed to load spell details: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun showSlotLevelSelectionDialog(
        character: Character,
        spell: SpellSummary,
        spellLevel: Int
    ) {
        val availableSlotsByLevel = character.spellSlots
            .filter { !it.used }
            .groupBy { it.level }

        // Only slot levels that can legally cast this spell
        val validSlotLevels = availableSlotsByLevel.keys
            .filter { it >= spellLevel }
            .sorted()

        if (validSlotLevels.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No spell slots of level $spellLevel or higher available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val levelLabels = validSlotLevels.map { level ->
            val count = availableSlotsByLevel[level]?.size ?: 0
            "Use level $level slot ($count available)"
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Select Spell Slot")
            .setItems(levelLabels) { _, which ->
                val chosenSlotLevel = validSlotLevels[which]

                // Mark a slot as used with this spell
                viewModel.consumeSpellSlot(
                    spellIndex = spell.index,
                    spellName = spell.name,
                    slotLevel = chosenSlotLevel,
                    castAtLevel = chosenSlotLevel // upcast level
                )

                // Persist + refresh UI
                saveCharacterState()
                viewModel.characterData.value?.let { updateUi(it) }

                Toast.makeText(
                    requireContext(),
                    "Cast ${spell.name} using a level $chosenSlotLevel slot",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun renderSpellSlots(spellSlots: List<SpellSlot>) {
        val summaryContainer = binding.containerSpellSlotSummary
        summaryContainer.removeAllViews()

        val slotsByLevel = spellSlots.groupBy { it.level }.toSortedMap()

        for ((level, slots) in slotsByLevel) {
            val availableCount = slots.count { !it.used }
            val totalCount = slots.size

            val summaryView = TextView(requireContext()).apply {
                text = "Level $level: $availableCount / $totalCount"
                textSize = 20f
                setPadding(0, 4, 0, 4)
            }
            summaryContainer.addView(summaryView)
        }
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
