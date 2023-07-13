package com.m391.primavera.user.teacher.home.switjha

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.m391.primavera.R
import com.m391.primavera.authentication.AuthenticationActivity
import com.m391.primavera.databinding.FragmentTeacherSwitchBinding
import com.m391.primavera.user.father.FatherActivity
import com.m391.primavera.user.teacher.TeacherActivity
import com.m391.primavera.user.teacher.home.TeacherHomeFragmentDirections
import com.m391.primavera.utils.BaseBottomSheetFragment
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.NavigationCommand
import kotlinx.coroutines.launch


class TeacherSwitchFragment : BaseBottomSheetFragment() {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    override val viewModel: TeacherSwitchViewModel by viewModels()
    private lateinit var binding: FragmentTeacherSwitchBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_teacher_switch, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    private fun setupBottomSheet(view: View) {
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels / 5
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val layout = binding.bottom
        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels / 6
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheet(view)
    }

    override fun onStart() {
        super.onStart()
        binding.arrowBtn.setOnClickListener {
            this.dismiss()
        }
        binding.fatherSwitch.setOnClickListener {
            lifecycleScope.launch {
                viewModel.showLoading.value = true
                if (viewModel.switchTeacherFather()) {
                    startActivity(Intent(activity, FatherActivity::class.java))
                    activity?.finish()
                    viewModel.setCurrentUserType()
                } else {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Father Account")
                        .setMessage("You don't have father account press Yes if you want to create one and No if don't")
                        .setPositiveButton("Yes") { _, _ ->
                            this@TeacherSwitchFragment.dismiss()
                            viewModel.navigationCommand.value =
                                NavigationCommand.To(TeacherHomeFragmentDirections.actionTeacherHomeFragmentToCreateFatherFragment())
                        }.setNegativeButton("No") { dialog, _ ->
                            dialog.cancel() // check again in case user changes their mind
                        }.show()
                }
                viewModel.showLoading.value = false
            }
        }
        binding.teacherProfile.setOnClickListener {
            this@TeacherSwitchFragment.dismiss()
            viewModel.navigationCommand.value =
                NavigationCommand.To(TeacherHomeFragmentDirections.actionTeacherHomeFragmentToTeacherEditProfileFragment())
        }
        binding.logOut.setOnClickListener {
            lifecycleScope.launch {
                viewModel.showLoading.value = true
                viewModel.logout()
                startActivity(Intent(activity, AuthenticationActivity::class.java))
                activity?.finish()
                viewModel.showLoading.value = false
            }
        }
    }
}