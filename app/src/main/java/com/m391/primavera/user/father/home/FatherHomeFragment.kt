package com.m391.primavera.user.father.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.m391.primavera.R
import com.m391.primavera.chat.ChatActivity
import com.m391.primavera.database.server.Authentication
import com.m391.primavera.databinding.FragmentFatherHomeBinding
import com.m391.primavera.user.father.home.switjha.FatherSwitchFragment
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants
import kotlinx.coroutines.launch


class FatherHomeFragment : BaseFragment() {

    private lateinit var binding: FragmentFatherHomeBinding
    override val viewModel: FatherHomeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                requireContext().startActivity(intent)
            }
        }

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
        binding.childImage.setOnClickListener {
            val fragment = FatherSwitchFragment()
            fragment.show(parentFragmentManager, "FatherSwitchFragment")
        }
        binding.childChat.setOnClickListener {
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra(Constants.TYPE, Constants.CHILD)
            intent.putExtra(Constants.CHILD_UID, viewModel.currentChildUID.value)
            intent.putExtra(
                Constants.CHILD_NAME,
                viewModel.currentChildInformation.value!!.childName
            )
            startActivity(intent)
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openStreamFather(viewLifecycleOwner)
            viewModel.openStreamChild(viewLifecycleOwner)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStreamChild(viewLifecycleOwner)
            viewModel.closeStreamFather(viewLifecycleOwner)
        }
    }

}