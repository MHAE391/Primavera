package com.m391.primavera.user.father.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentFatherTeacherSearchBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants.SEARCH
import com.m391.primavera.utils.NavigationCommand
import com.m391.primavera.utils.setupGridRecycler
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FatherTeacherSearchFragment : BaseFragment() {

    private lateinit var binding: FragmentFatherTeacherSearchBinding
    override val viewModel: FatherTeacherSearchViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_father_teacher_search,
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
        val adapter = TeacherAdapter {
            viewModel.navigationCommand.postValue(
                NavigationCommand.To(
                    FatherTeacherSearchFragmentDirections.actionFatherTeacherSearchFragmentToTeacherProfileFragment(
                        it.teacherId,
                        SEARCH
                    )
                )
            )
        }
        binding.usersRecyclerView.setupGridRecycler(adapter)
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStream(viewLifecycleOwner)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openOnlineStream(viewLifecycleOwner)
        }
    }

}