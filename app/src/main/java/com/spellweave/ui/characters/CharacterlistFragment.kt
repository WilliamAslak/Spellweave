package com.spellweave.ui.characterlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.spellweave.databinding.FragmentCharacterlistBinding
import com.spellweave.R


class CharacterlistFragment : Fragment() {

    private var _binding: FragmentCharacterlistBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CharacterlistModel
    private lateinit var characterAdapter: CharacterAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(CharacterlistModel::class.java)
        _binding = FragmentCharacterlistBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.text.observe(viewLifecycleOwner) {
            binding.textCharacterlist.text = it
        }

        setupRecyclerView()

        //Toggle empty list state
        viewModel.characterList.observe(viewLifecycleOwner) { characters ->
            characterAdapter.updateData(characters)

            val isEmpty = characters.isNullOrEmpty()
            binding.rvCharacterList.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }

        binding.btnEmptyCreate.setOnClickListener {
            val args = Bundle().apply { putString("characterId", null) }
            findNavController().navigate(
                R.id.action_nav_characterlist_to_nav_charactercreator,
                args
            )
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCharacters(requireContext())
    }

    private fun setupRecyclerView() {
        characterAdapter = CharacterAdapter(emptyList()) { character ->
            //pass the id of the character to the creator
            val bundle = bundleOf("characterId" to character.id)
            findNavController().navigate(R.id.action_nav_characterlist_to_nav_gameplay, bundle)

        }
        binding.rvCharacterList.adapter = characterAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}