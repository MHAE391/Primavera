package com.m391.primavera.user.father.child.profile.display

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.m391.primavera.R
import com.m391.primavera.chat.ChatActivity
import com.m391.primavera.databinding.FragmentChildProfileBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChildProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentChildProfileBinding
    override val viewModel: ChildProfileViewModel by viewModels()
    private val args: ChildProfileFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_child_profile, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.fatherInfo.setOnClickListener {
            findNavController().navigate(
                ChildProfileFragmentDirections.actionChildProfileFragmentToFatherProfileFragment(
                    "Chat", viewModel.childInfo.value!!.fatherUID
                )
            )
        }
        binding.chat.setOnClickListener {
            if (!viewModel.checkChildFather(viewModel.childInfo.value!!.fatherUID)) {
                val intent = Intent(activity, ChatActivity::class.java)
                lifecycleScope.launch {
                    viewModel.createConversation(viewModel.childInfo.value!!.fatherUID)
                }
                intent.putExtra(Constants.TYPE, Constants.FATHER)
                intent.putExtra(Constants.FATHER_UID, viewModel.childInfo.value!!.fatherUID)
                intent.putExtra(Constants.FATHER_FIRST_NAME, viewModel.childInfo.value!!.fatherName)
                startActivity(intent)
            } else viewModel.showToast.value = "Your Child"

        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            viewModel.openStream(viewLifecycleOwner, args.childUid)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStream(viewLifecycleOwner, args.childUid)
        }
    }
}