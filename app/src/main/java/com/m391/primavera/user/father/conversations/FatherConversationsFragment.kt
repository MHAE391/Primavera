package com.m391.primavera.user.father.conversations

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentFatherConversationsBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.BaseViewModel

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

}