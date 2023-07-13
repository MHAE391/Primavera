package com.m391.primavera.authentication.otp

import android.app.AlertDialog
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.m391.primavera.Primavera
import com.m391.primavera.R
import com.m391.primavera.authentication.information.InformationActivity
import com.m391.primavera.databinding.FragmentOtpVerificationBinding
import com.m391.primavera.user.father.FatherActivity
import com.m391.primavera.user.teacher.TeacherActivity
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.BOTH
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.NavigationCommand
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
                if (viewModel.response.hasActiveObservers()) viewModel.response.removeObservers(
                    viewLifecycleOwner
                )
                viewModel.response.observe(viewLifecycleOwner, Observer {
                    if (it == Constants.SUCCESSFUL_LOGIN) {
                        when (viewModel.alreadySigned.value) {
                            FATHER -> showFatherActivity()
                            TEACHER -> showTeacherActivity()
                            BOTH -> showChoiceAlert()
                            else -> showInformationActivity()
                        }
                    } else if (it != null) {
                        viewModel.showSnackBar.value = it
                        binding.firstCode.requestFocus()
                    }
                })
            }
        }
        view.addTextChangedListener(textWatcher)
    }


    private fun showChoiceAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Multiple Account")
            .setMessage("Choose Father to open Father Mode\nTeacher to open Teacher Mode")
            .setPositiveButton("Father") { _, _ ->
                lifecycleScope.launch {
                    viewModel.setUserType(FATHER)
                    showFatherActivity()
                }
            }.setNegativeButton("Teacher") { _, _ ->
                lifecycleScope.launch {
                    viewModel.setUserType(TEACHER)
                    showTeacherActivity()
                }
            }.show()
    }

    override fun onPause() {
        super.onPause()
        viewModel.response.removeObservers(viewLifecycleOwner)
    }

    override fun onStart() {
        super.onStart()
        setupEditText()
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
        binding.verify.setOnClickListener {
            viewModel.signInWithPhone()
            if (viewModel.response.hasActiveObservers()) viewModel.response.removeObservers(
                viewLifecycleOwner
            )
            viewModel.response.observe(viewLifecycleOwner, Observer {
                if (it == Constants.SUCCESSFUL_LOGIN) {
                    when (viewModel.alreadySigned.value) {
                        FATHER -> showFatherActivity()
                        TEACHER -> showTeacherActivity()
                        BOTH -> showChoiceAlert()
                        else -> showInformationActivity()
                    }
                } else if (it != null) {
                    viewModel.showSnackBar.value = it
                    binding.firstCode.requestFocus()
                }
            })
        }

    }

    private fun showFatherActivity() {
        startActivity(Intent(activity, FatherActivity::class.java))
        activity?.finish()
    }

    private fun showTeacherActivity() {
        startActivity(Intent(activity, TeacherActivity::class.java))
        activity?.finish()
    }

    private fun showInformationActivity() {
        startActivity(Intent(activity, InformationActivity::class.java))
        activity?.finish()
    }

}