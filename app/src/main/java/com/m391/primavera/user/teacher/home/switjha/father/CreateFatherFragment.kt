package com.m391.primavera.user.teacher.home.switjha.father

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentCreateFatherBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Binding
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.NavigationCommand
import kotlinx.coroutines.launch
import java.util.Calendar

class CreateFatherFragment : BaseFragment() {

    private lateinit var binding: FragmentCreateFatherBinding
    override val viewModel: CreateFatherViewModel by activityViewModels()
    private lateinit var calendar: Calendar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_create_father, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        ArrayAdapter.createFromResource(
            requireContext(), R.array.academic_years_array, R.layout.spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_item)
            // Apply the adapter to the spinner
            binding.academicYear.adapter = adapter
            binding.academicYear.onItemSelectedListener = spinnerClickListener
        }
        calendar = Calendar.getInstance()
        return binding.root
    }


    private val chooseChildPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Binding.loadImage(binding.childImage, uri.toString(), uri.toString())
                viewModel.childImage.value = uri.toString()
            }
        }


    private val spinnerClickListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
            viewModel.getChildAcademicYear(parent.getItemAtPosition(pos).toString())
        }

        override fun onNothingSelected(perant: AdapterView<*>?) {}

    }

    override fun onStart() {
        super.onStart()
        binding.childDateOfBarth.setOnClickListener {
            showDatePickerDialog()
        }
        binding.childImage.setOnClickListener {
            chooseChildPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.childWatch.setOnClickListener {
            showQRScanner()
        }
        binding.fatherLocation.setOnClickListener {
            showSelectLocation()
        }
        binding.createFather.setOnClickListener {
            lifecycleScope.launch {
                val response = viewModel.createFatherAccount()
                if (response == SUCCESS) {
                    viewModel.showToast.value = "Account Created Successfully"
                    viewModel.navigationCommand.value = NavigationCommand.Back
                } else viewModel.showToast.value = response
            }
        }
    }

    private fun showSelectLocation() {
        val fragment = SelectFatherLocation()
        fragment.show(parentFragmentManager, "FatherLocation")
    }

    private fun showQRScanner() {
        val fragment = WatchQRCodeScanner()
        fragment.show(parentFragmentManager, "QR")
    }

    private fun showDatePickerDialog() {
        val years = calendar.get(Calendar.YEAR)
        val months = calendar.get(Calendar.MONTH)
        val days = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
                viewModel.childDateOfBarth.value = "$day/${month + 1}/$year"
            },
            years,
            months,
            days
        )

        datePickerDialog.show()
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
}