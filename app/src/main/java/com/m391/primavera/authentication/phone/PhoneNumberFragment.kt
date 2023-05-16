package com.m391.primavera.authentication.phone

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.m391.primavera.R
import com.m391.primavera.authentication.AuthenticationActivity
import com.m391.primavera.database.server.Authentication
import com.m391.primavera.databinding.FragmentPhoneNumberBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants
import java.util.concurrent.TimeUnit

class PhoneNumberFragment : BaseFragment() {
    private lateinit var binding: FragmentPhoneNumberBinding
    override val viewModel: PhoneNumberViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_phone_number, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.getCode.setOnClickListener {
           it.isEnabled = false
            if (viewModel.sendOTPCode(requireActivity()) == Constants.DONE) {
                it.isEnabled = true
            }

        }

    }
}