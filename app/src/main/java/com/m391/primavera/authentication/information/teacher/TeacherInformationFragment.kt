package com.m391.primavera.authentication.information.teacher

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.R
import com.m391.primavera.database.server.TeacherInformation
import com.m391.primavera.databinding.FragmentTeacherInformationBinding
import com.m391.primavera.user.father.FatherActivity
import com.m391.primavera.user.teacher.TeacherActivity
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Binding
import com.m391.primavera.utils.Constants
import kotlinx.coroutines.launch

class TeacherInformationFragment : BaseFragment() {

    private lateinit var binding: FragmentTeacherInformationBinding
    override val viewModel: TeacherInformationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_teacher_information,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    private val chooseTeacherPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Binding.loadImage(binding.teacherImage, uri.toString())
                viewModel.teacherImage.value = uri.toString()
            }
        }

    override fun onStart() {
        super.onStart()
        binding.selectYears.setOnClickListener {
            showSelectYearsFragment()
        }
        binding.selectSubjects.setOnClickListener {
            showSelectSubjectsFragment()
        }
        binding.selectLocation.setOnClickListener {
            showSelectLocationFragment()
        }
        binding.teacherImage.setOnClickListener {
            chooseTeacherPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        viewModel.response.observe(viewLifecycleOwner, Observer {
            if (it == Constants.SUCCESS) {
                startActivity(Intent(activity, TeacherActivity::class.java))
                activity?.finish()
            } else {
                viewModel.showSnackBar.value = it.toString()
            }
        })
        binding.setData.setOnClickListener {
            lifecycleScope.launch {
                viewModel.setData()
            }
        }
    }

    private fun showSelectSubjectsFragment() {
        val fragment = SelectSubjectsFragment()
        fragment.show(parentFragmentManager, "SelectSubjects")
    }

    private fun showSelectLocationFragment() {
        val fragment = SelectTeacherLocationFragment()
        fragment.show(parentFragmentManager, "SelectLocation")
    }

    private fun showSelectYearsFragment() {
        val fragment = SelectYearsFragment()
        fragment.show(parentFragmentManager, "SelectYears")
    }
}