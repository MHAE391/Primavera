package com.m391.primavera.user.teacher.home

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
import com.m391.primavera.databinding.FragmentTeacherHomeBinding
import com.m391.primavera.databinding.FragmentTeacherProfileBinding
import com.m391.primavera.user.teacher.home.switjha.TeacherSwitchFragment
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.setupGridRecycler
import kotlinx.coroutines.launch

class TeacherHomeFragment : BaseFragment() {

    override val viewModel: TeacherConversationViewModel by viewModels()
    private lateinit var binding: FragmentTeacherHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_teacher_home, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
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

        binding.addNewConversation.setOnClickListener {
            findNavController().navigate(R.id.action_teacherHomeFragment_to_teacherChildSearchFragment)
        }
        binding.searchView.setOnQueryTextListener(viewModel.searchWatcher)
        binding.teacherFatherSwitch.setOnClickListener {
            val fragment = TeacherSwitchFragment()
            fragment.show(parentFragmentManager, "Teacher Switch Fragment")
        }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = ConversationsAdapter {
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra(Constants.TYPE, Constants.FATHER)
            intent.putExtra(Constants.FATHER_UID, it.receiverUID)
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