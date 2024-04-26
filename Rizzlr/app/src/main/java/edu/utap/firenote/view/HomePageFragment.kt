package edu.utap.firenote.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import edu.utap.firenote.AuthWrap
import edu.utap.firenote.ModernViewModel
import edu.utap.firenote.R
import edu.utap.firenote.databinding.HomepageBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomePageFragment : Fragment(R.layout.homepage) {
    private val viewModel : ModernViewModel by activityViewModels()

    private lateinit var btnGetMatched: Button
    private lateinit var btnSeeStats: Button
    private lateinit var btnSeeSettings: Button
    private lateinit var tvWelcome: TextView

    private var _binding: HomepageBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var activateTimer = false
    private var endDate : Date? = Date()

    var matchLock : Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.homepage, container, false)
        _binding = HomepageBinding.bind(view)

        // Initialize views
        btnGetMatched = view.findViewById(R.id.btnGetMatched)
        btnSeeStats = view.findViewById(R.id.btnSeeStats)
        btnSeeSettings = view.findViewById(R.id.btnSeeSettings)
        tvWelcome = view.findViewById(R.id.tvWelcome)

        CoroutineScope(Dispatchers.Main).launch {
            // Perform asynchronous tasks here
            // For example, make a network request
            detectTime()
        }

        return view
    }

    private fun toggleChatElements(state : Int) {
        binding.totalChatRV.visibility = state
        binding.chatBtnLayout.visibility = state
        binding.chatTime.visibility = state
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup welcome message
        AuthWrap.observeUser().observe(viewLifecycleOwner) {
            if(it?.name != "User logged out") {
                tvWelcome.text = it?.name
            } else {
                tvWelcome.text = "Welcome to Rizzlr!"
            }
        }

        viewModel.observeProfile().observe(viewLifecycleOwner) {
            if(it.queueStatus == 0) {
                btnGetMatched.text = "CLICK HERE TO GET MATCHED"
                btnGetMatched.isClickable = true
                toggleChatElements(View.GONE)
                activateTimer = false

            } else if(it.queueStatus == 1) {
                btnGetMatched.text = "IN QUEUE"
                btnGetMatched.isClickable = false
                toggleChatElements(View.GONE)
                activateTimer = false
            } else if(it.queueStatus == 2) {
                btnGetMatched.text = "YOU HAVE BEEN MATCHED"
                btnGetMatched.isClickable = false
                toggleChatElements(View.VISIBLE)

                activateTimer = true
                endDate = it.chatEndDate

            } else if(it.queueStatus == 3) {
                btnGetMatched.text = "SELECT THE BETTER PARTNER"
                btnGetMatched.isClickable = false
                toggleChatElements(View.VISIBLE)
                binding.chatTime.visibility = View.INVISIBLE

                activateTimer = false

                viewModel.deleteChat(it.chatRoomID)
            }

            binding.chat1.text = it.chatPartnerName1
            binding.chat2.text = it.chatPartnerName2

            binding.maxRating.text = "Peak Rating: " + it.maxRating.toString()
            binding.currentRating.text = "Current Rating: " + it.rating.toString()
        }

        binding.chat1.setOnClickListener() {
            if(viewModel.getProfile().queueStatus == 3) {
                viewModel.adjustRatings(viewModel.getProfile().chatPartnerID1, 1)
                viewModel.adjustRatings(viewModel.getProfile().chatPartnerID2, -1)

                viewModel.setQueueStatus(0)
            } else if(viewModel.getProfile().queueStatus == 2){
                val action = HomePageFragmentDirections.actionNavigationHomepageToChat(
                    viewModel.getProfile().chatPartnerID1, viewModel.getProfile().chatPartnerName1)
                findNavController().navigate(action)
            }

        }
        binding.chat2.setOnClickListener() {
            if(viewModel.getProfile().queueStatus == 3) {
                viewModel.setQueueStatus(0)
                viewModel.adjustRatings(viewModel.getProfile().chatPartnerID2, 1)
                viewModel.adjustRatings(viewModel.getProfile().chatPartnerID1, -1)
            } else if(viewModel.getProfile().queueStatus == 2){
                val action = HomePageFragmentDirections.actionNavigationHomepageToChat(
                    viewModel.getProfile().chatPartnerID2, viewModel.getProfile().chatPartnerName2)
                findNavController().navigate(action)
            }

        }

        viewModel.observeMatchmaker().observe(viewLifecycleOwner) {
            if(!matchLock) {
                viewModel.matchmakerUpdate(it)
                matchLock = true
            }
        }

        // Set click listeners
        btnGetMatched.setOnClickListener {
            // Navigate to matching fragment
            //modernViewModel.setQueueStatus(1)
            if(matchLock) {
                matchLock = false
                viewModel.queryMatchmaker()
            }
        }

        btnSeeStats.setOnClickListener {
            // Navigate to stats fragment
            val p = viewModel.getProfile()
            val action = HomePageFragmentDirections.actionNavigationHomepageToStats(
                            p.rating.toString(), p.maxRating.toString(), p.received, p.sent)
            findNavController().navigate(action)
        }

        btnSeeSettings.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_homepage_to_settings)
        }

        val adapter = MessageAdapter(viewModel)
        viewModel.observeChatAllReceived().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        val rv = binding.totalChatRV
        val itemDecor = DividerItemDecoration(rv.context, LinearLayoutManager.VERTICAL)
        binding.totalChatRV.addItemDecoration(itemDecor)
        binding.totalChatRV.adapter = adapter

    }

    private suspend fun detectTime() {
        var currentMillis = System.currentTimeMillis()
        // End color of replayButton is  red
        val delayMillis = 1000L // Time step for updates
        // XML button

        while (coroutineContext.isActive) {
            if(activateTimer && endDate != null) {
                val currentDate = Date(currentMillis)
                val chatStr = "Now: " + currentDate + "\n End: " + endDate
                binding.chatTime.text = chatStr

                currentMillis = System.currentTimeMillis()
                if(currentDate.after(endDate)) {
                    viewModel.setQueueStatus(3)
                    activateTimer = false
                }
            }
            delay(delayMillis)

        }
    }
}