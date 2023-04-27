package com.m391.primavera.authentication.information.teacher

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentTeacherInformationBinding
import com.m391.primavera.utils.BaseFragment

class TeacherInformationFragment : BaseFragment() {

    private lateinit var binding: FragmentTeacherInformationBinding
    override val viewModel: TeacherInformationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_teacher_information,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

}