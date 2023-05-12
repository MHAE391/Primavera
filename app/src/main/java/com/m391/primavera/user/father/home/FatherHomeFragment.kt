package com.m391.primavera.user.father.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentFatherHomeBinding
import com.m391.primavera.utils.BaseFragment


class FatherHomeFragment : BaseFragment() {

    private lateinit var binding: FragmentFatherHomeBinding
    override val viewModel: FatherHomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_father_home, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.navigateToConversations.setOnClickListener {
            findNavController().navigate(R.id.action_fatherHomeFragment_to_fatherConversationsFragment)
        }

        return binding.root
    }

}