package com.m391.primavera.user.father.home.switjha

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.m391.primavera.R
import com.m391.primavera.authentication.AuthenticationActivity
import com.m391.primavera.chat.ChatActivity
import com.m391.primavera.databinding.FragmentFatherSwitchBinding
import com.m391.primavera.user.father.FatherActivity
import com.m391.primavera.user.father.conversations.ConversationsAdapter
import com.m391.primavera.user.father.home.FatherHomeFragment
import com.m391.primavera.user.father.home.FatherHomeFragmentDirections
import com.m391.primavera.user.father.home.FatherHomeViewModel
import com.m391.primavera.user.teacher.TeacherActivity
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.NavigationCommand
import com.m391.primavera.utils.setupGridRecycler
import com.m391.primavera.utils.setupLinearRecycler
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class FatherSwitchFragment : BottomSheetDialogFragment() {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var binding: FragmentFatherSwitchBinding
    private val viewModel: FatherHomeViewModel by activityViewModels()
    private val fatherSwitchViewModel: FatherSwitchViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fatherSwitchViewModel.setupUIDS(
            viewModel.fatherInformation.value!!.children,
            viewModel.currentChildUID.value!!
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_father_switch, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.switchViewModel = fatherSwitchViewModel
        return binding.root
    }

    override fun onStart() {
        super.onStart()


        binding.arrowBtn.setOnClickListener {
            this@FatherSwitchFragment.dismiss()
        }
        binding.fatherTeacherSwitch.setOnClickListener {
            lifecycleScope.launch {
                fatherSwitchViewModel.showLoading.value = true
                if (fatherSwitchViewModel.switchFatherTeacher()) {
                    startActivity(Intent(activity, TeacherActivity::class.java))
                    activity?.finish()
                    fatherSwitchViewModel.setCurrentUserType()
                } else {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Teacher Account")
                        .setMessage("You don't have teacher account press Yes if you want to create one and No if don't")
                        .setPositiveButton("Yes") { _, _ ->
                            this@FatherSwitchFragment.dismiss()
                            viewModel.navigationCommand.value =
                                NavigationCommand.To(
                                    FatherHomeFragmentDirections.actionFatherHomeFragmentToCreateTeacherFragment(
                                        viewModel.fatherInformation.value!!.fatherUID
                                    )
                                )
                        }.setNegativeButton("No") { dialog, _ ->
                            dialog.cancel() // check again in case user changes their mind
                        }.show()
                }
                fatherSwitchViewModel.showLoading.value = false
            }
        }
        setupRecyclerView()

        binding.logOut.setOnClickListener {
            lifecycleScope.launch {
                fatherSwitchViewModel.showLoading.value = true
                fatherSwitchViewModel.logout()
                startActivity(Intent(activity, AuthenticationActivity::class.java))
                activity?.finish()
                fatherSwitchViewModel.showLoading.value = false
            }
        }
    }


    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            fatherSwitchViewModel.getChildren(viewLifecycleOwner)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            fatherSwitchViewModel.closeObservers(viewLifecycleOwner)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheet(view)
    }

    private fun setupBottomSheet(view: View) {
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels / 3
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val layout = binding.bottomFatherSwitch
        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels / 3
    }

    private fun setupRecyclerView() {
        val adapter = ChildrenAdapter {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Child Options")
                .setMessage("Choose Profile to display child Profile , Switch to switch to this child")
                .setPositiveButton("Profile") { _, _ ->
                    this@FatherSwitchFragment.dismiss()
                    viewModel.navigationCommand.value = NavigationCommand.To(
                        FatherHomeFragmentDirections.actionFatherHomeFragmentToEditChildProfileFragment(
                            it.childUID
                        )
                    )
                }.setNegativeButton("Switch") { _, _ ->
                    lifecycleScope.launch {
                        if (it.childUID == fatherSwitchViewModel.currentChild.value) {
                            Toast.makeText(requireContext(), "Already Signed", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            viewModel.changeCurrentChild(it.childUID)
                            val intent = Intent(activity, FatherActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            requireActivity().finish()
                            this@FatherSwitchFragment.dismiss()
                        }
                    }
                }.show()
        }
        binding.usersRecyclerView.setupLinearRecycler(adapter)
    }

}