package edu.utap.firenote.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import edu.utap.firenote.ModernViewModel
import edu.utap.firenote.R
import edu.utap.firenote.databinding.FragmentChatBinding

class ChatFragment :
    Fragment(R.layout.fragment_chat) {
    private val viewModel: ModernViewModel by activityViewModels()
    private val args: ChatFragmentArgs by navArgs()

    // No need for onCreateView because we passed R.layout to Fragment constructor
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentChatBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated")
        // Create new note
        // Don't need action, because sending default argument
        binding.fab.setOnClickListener {
            //findNavController().navigate(R.id.action_navigation_home_to_navigation_note_edit)
            viewModel.createMessage(binding.textCreate.text.toString(), args.recipientID)
            binding.textCreate.text.clear()
        }

        // Long press to edit.
        val adapter = MessageAdapter(viewModel)
        if(args.recipientID == viewModel.getProfile().chatPartnerID1) {
            viewModel.observeChatWithPerson1().observe(viewLifecycleOwner) {
                Log.d("MY NOTE", it.toString())
                adapter.submitList(it)
            }
        } else {
            viewModel.observeChatWithPerson2().observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
        }

        viewModel.observeProfile().observe(viewLifecycleOwner) {
            if(it.queueStatus != 2) {
                findNavController().popBackStack()
            }
        }
        val nameStr = "Chatting with " + args.recipientName
        binding.partnerName.text = nameStr

        val rv = binding.messageListRV
        val itemDecor = DividerItemDecoration(rv.context, LinearLayoutManager.VERTICAL)
        binding.messageListRV.addItemDecoration(itemDecor)
        binding.messageListRV.adapter = adapter
    }
}