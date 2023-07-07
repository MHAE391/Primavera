package com.m391.primavera.user.father.home.switjha.teacher

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.m391.primavera.R
import com.m391.primavera.user.father.home.switjha.teacher.SelectSubjectsFragment
import com.m391.primavera.databinding.FragmentCreateTeacherBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.NavigationCommand
import kotlinx.coroutines.launch
import java.util.Calendar

class CreateTeacherFragment : BaseFragment() {

    private lateinit var binding: FragmentCreateTeacherBinding
    override val viewModel: CreateTeacherViewModel by activityViewModels()
    private val args: CreateTeacherFragmentArgs by navArgs()
    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_create_teacher, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        calendar = Calendar.getInstance()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.teacherSubjects.setOnClickListener {
            showSelectSubjectsFragment()
        }
        binding.teacherAcademicYears.setOnClickListener {
            showSelectYearsFragment()
        }
        binding.teacherLocation.setOnClickListener {
            showSelectLocationFragment()
        }
        binding.teacherDateOfBarth.setOnClickListener {
            showDatePickerDialog()
        }
        binding.createTeacher.setOnClickListener {
            lifecycleScope.launch {
                val response = viewModel.createTeacherAccount()
                if (response == SUCCESS) {
                    viewModel.showToast.value = "Account Created Successfully"
                    viewModel.navigationCommand.value = NavigationCommand.Back
                } else viewModel.showToast.value = response
            }
        }
    }

    private fun showDatePickerDialog() {
        val years = calendar.get(Calendar.YEAR)
        val months = calendar.get(Calendar.MONTH)
        val days = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
                viewModel.teacherDateOfBarth.value = "$day/${month + 1}/$year"
            },
            years,
            months,
            days
        )

        datePickerDialog.show()
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

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openStream(viewLifecycleOwner, args.fatherUid)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStream(viewLifecycleOwner, args.fatherUid)
        }
    }
}