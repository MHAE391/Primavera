package com.m391.primavera.authentication.otp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.m391.primavera.R
import com.m391.primavera.authentication.information.InformationActivity
import com.m391.primavera.databinding.FragmentOtpVerificationBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants
import kotlinx.coroutines.launch

class OTPVerificationFragment : BaseFragment() {
    private lateinit var binding: FragmentOtpVerificationBinding
    override val viewModel: OTPVerificationViewModel by viewModels()
    private val args: OTPVerificationFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_otp_verification, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setupEditText()
        return binding.root
    }

    private fun setupEditText() {
        setTextWatcher(binding.firstCode, binding.secondCode)
        setTextWatcher(binding.secondCode, binding.thirdCode)
        setTextWatcher(binding.thirdCode, binding.fourthCode)
        setTextWatcher(binding.fourthCode, binding.fifthCode)
        setTextWatcher(binding.fifthCode, binding.sixthCode)
        setLastTextWatcher(binding.sixthCode)
    }

    private fun setTextWatcher(view: EditText, secondView: EditText) {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                view.requestFocus()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                if (!view.text.isNullOrEmpty()) secondView.requestFocus()
                else view.requestFocus()
            }
        }
        view.addTextChangedListener(textWatcher)
    }

    private fun resendOTPCodeVisibility() {
        binding.resendOTPCode.visibility = View.GONE
        binding.resendText.visibility = View.GONE
        binding.resendOTPCode.isEnabled = false
        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            binding.resendOTPCode.visibility = View.VISIBLE
            binding.resendText.visibility = View.VISIBLE
            binding.resendOTPCode.isEnabled = true
        }, 60000)
    }

    private fun setLastTextWatcher(view: EditText) {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                viewModel.signInWithPhone()
                viewModel.response.observe(viewLifecycleOwner, Observer {
                    if (it == Constants.SUCCESSFUL_LOGIN) {
                        startActivity(Intent(activity, InformationActivity::class.java))
                        activity!!.finish()
                    } else if (it != null) {
                        viewModel.showSnackBar.value = it
                        viewModel.resetData()
                        binding.firstCode.requestFocus()
                    }

                })
            }
        }
        view.addTextChangedListener(textWatcher)
    }

    override fun onStart() {
        super.onStart()
        viewModel.setupData(
            args.phoneNumber, args.resendToken, args.storedVerificationId
        )
        binding.firstCode.requestFocus()
        resendOTPCodeVisibility()
        binding.resendOTPCode.setOnClickListener {
            lifecycleScope.launch {
                viewModel.resendOTPCode(requireActivity())
                resendOTPCodeVisibility()
            }
        }
    }

}