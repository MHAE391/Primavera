package com.m391.primavera.user.father.conversations

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.m391.primavera.R
import com.m391.primavera.chat.ChatActivity
import com.m391.primavera.databinding.FragmentFatherConversationsBinding
import com.m391.primavera.user.father.search.FatherTeacherSearchFragmentDirections
import com.m391.primavera.user.father.search.TeacherAdapter
import com.m391.primavera.utils.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FatherConversationsFragment : BaseFragment() {

    override val viewModel: FatherConversationsViewModel by viewModels()
    private lateinit var binding: FragmentFatherConversationsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_father_conversations,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.addNewConversation.setOnClickListener {
            findNavController().navigate(R.id.action_fatherConversationsFragment_to_fatherTeacherSearchFragment)
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val expandAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.expand_searchview)

        binding.searchView.setOnSearchClickListener {
            it.startAnimation(expandAnimation)
            binding.text.visibility = View.GONE
        }
        binding.searchView.setOnCloseListener {
            binding.text.visibility = View.VISIBLE
            false
        }

        binding.searchView.setOnQueryTextListener(viewModel.searchWatcher)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = ConversationsAdapter {
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra(Constants.TYPE, Constants.TEACHER)
            intent.putExtra(Constants.TEACHER_UID, it.receiverUID)
            intent.putExtra(Constants.FATHER_FIRST_NAME, it.firstName)
            startActivity(intent)
        }
        binding.usersRecyclerView.setupGridRecycler(adapter)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openStreamConversations(viewLifecycleOwner)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStreamConversation(viewLifecycleOwner)
        }
    }
}