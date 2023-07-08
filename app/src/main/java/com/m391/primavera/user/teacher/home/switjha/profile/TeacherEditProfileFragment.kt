package com.m391.primavera.user.teacher.home.switjha.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.R
import com.m391.primavera.authentication.AuthenticationActivity
import com.m391.primavera.databinding.FragmentTeacherEditProfileBinding

import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Binding
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.NavigationCommand
import com.permissionx.guolindev.dialog.permissionMapOnQ
import kotlinx.coroutines.launch

class TeacherEditProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentTeacherEditProfileBinding
    override val viewModel: TeacherEditProfileViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_teacher_edit_profile,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.profileImage.visibility = View.GONE
        binding.teacherSubjects.setOnClickListener {
            showAcademicSubjectsFragment()
        }
        binding.teacherAcademicYears.setOnClickListener {
            showAcademicYearsFragment()
        }
        binding.editProfileImage.setOnClickListener {
            binding.loadedProfileImage.visibility = View.GONE
            binding.profileImage.visibility = View.VISIBLE
            Binding.loadImage(
                binding.profileImage,
                viewModel.teacherInfo.value!!.image,
                viewModel.teacherInfo.value!!.imageUri
            )
            chooseTeacherPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.save.setOnClickListener {
            lifecycleScope.launch {
                val response = viewModel.updateTeacherInformation()
                if (response == SUCCESS) viewModel.showToast.value =
                    "Information Updated Successfully"
                else viewModel.showToast.value = response
            }
        }
        binding.teacherLocation.setOnClickListener {
            showShowLocationFragment()
        }
        binding.editProfileLocation.setOnClickListener {
            showEditLocationFragment()
        }
        binding.deleteTeacher.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Delete Teacher")
                .setMessage("Choose Yes to delete Teacher Account , No to back")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {
                        val response = viewModel.deleteTeacherAccount()
                        if (response == SUCCESS) {
                            startActivity(Intent(activity, AuthenticationActivity::class.java))
                            viewModel.showToast.value = "Account Deleted"
                            requireActivity().finish()
                        } else viewModel.showToast.value = response
                    }
                }.setNegativeButton("No") { _, _ ->

                }.show()
        }
    }

    private fun showAcademicSubjectsFragment() {
        val fragment = AcademicSubjectsFragment()
        fragment.show(parentFragmentManager, "AcademicSubjects")
    }

    private fun showShowLocationFragment() {
        val fragment = ShowLocationFragment()
        fragment.show(parentFragmentManager, "ShowLocation")
    }

    private fun showEditLocationFragment() {
        val fragment = EditLocationFragment()
        fragment.show(parentFragmentManager, "EditLocation")
    }

    private fun showAcademicYearsFragment() {
        val fragment = AcademicYearsFragment()
        fragment.show(parentFragmentManager, "AcademicYears")
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openStream(viewLifecycleOwner)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStream(viewLifecycleOwner)
        }
    }

    private val chooseTeacherPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Binding.loadImage(binding.profileImage, uri.toString(), uri.toString())
                viewModel.teacherNewImage.value = uri.toString()
            }
        }

}