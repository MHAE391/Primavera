package com.m391.primavera.user.teacher.search

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.m391.primavera.R
import com.m391.primavera.chat.ChatActivity
import com.m391.primavera.databinding.FragmentTeacherChildSearchBinding
import com.m391.primavera.user.father.home.FatherHomeFragmentDirections
import com.m391.primavera.user.father.home.switjha.ChildrenAdapter
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.setupGridRecycler
import com.m391.primavera.utils.setupLinearRecycler
import kotlinx.coroutines.launch

class TeacherChildSearchFragment : BaseFragment() {

    private lateinit var binding: FragmentTeacherChildSearchBinding
    override val viewModel: TeacherChildSearchViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_teacher_child_search,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
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
        val adapter = ChildSearchAdapter {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Child Options")
                .setMessage("Choose chat to open chat with child father  , profile to display child profile")
                .setPositiveButton("Profile") { _, _ ->
                    findNavController().navigate(
                        TeacherChildSearchFragmentDirections.actionTeacherChildSearchFragmentToChildProfileFragment(
                            it.childUID
                        )
                    )
                }.setNegativeButton("Chat") { _, _ ->
                    if (!viewModel.checkChildFather(it.fatherUID)) {
                        val intent = Intent(activity, ChatActivity::class.java)
                        lifecycleScope.launch {
                            viewModel.createConversation(it.fatherUID)
                        }
                        intent.putExtra(Constants.TYPE, Constants.FATHER)
                        intent.putExtra(Constants.FATHER_UID, it.fatherUID)
                        intent.putExtra(Constants.FATHER_FIRST_NAME, it.fatherName)
                        startActivity(intent)
                    } else viewModel.showToast.value = "Your Child"
                }.show()
        }
        binding.usersRecyclerView.setupGridRecycler(adapter)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openStreamChildren(viewLifecycleOwner)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStreamChildren(viewLifecycleOwner)
        }
    }
}