package com.m391.primavera.chat.father

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.m391.primavera.R
import com.m391.primavera.chat.ChatActivityViewModel
import com.m391.primavera.databinding.FragmentFatherTeacherChatBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.CHILD
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.Constants.TYPE

class FatherTeacherChatFragment : BaseFragment() {

    private lateinit var binding: FragmentFatherTeacherChatBinding
    override val viewModel: ChatActivityViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (activity?.intent!!.extras!!.getString(TYPE)) {
            TEACHER -> {
                val teacherUid = activity?.intent!!.extras!!.getString(Constants.TEACHER_UID)
                viewModel.setChatSenderAndReceiver(teacherUid!!)
            }
            CHILD -> {
                findNavController().navigate(R.id.action_fatherTeacherChatFragment_to_fatherChildChatFragment)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_father_teacher_chat,
            container,
            false
        )
        binding.lifecycleOwner = this
        viewModel._receiverUid.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        })
        return binding.root
    }
}