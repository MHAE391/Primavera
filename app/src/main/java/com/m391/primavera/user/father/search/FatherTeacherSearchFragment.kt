package com.m391.primavera.user.father.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentFatherTeacherSearchBinding
import com.m391.primavera.utils.BaseFragment
import kotlinx.coroutines.launch

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