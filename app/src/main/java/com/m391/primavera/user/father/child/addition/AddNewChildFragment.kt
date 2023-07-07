package com.m391.primavera.user.father.child.addition

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.R
import com.m391.primavera.authentication.information.father.watch.QRCodeScannerFragment
import com.m391.primavera.databinding.FragmentAddNewChildBinding
import com.m391.primavera.user.father.FatherActivity
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Binding
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.NavigationCommand
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Calendar

class AddNewChildFragment : BaseFragment() {

    private lateinit var binding: FragmentAddNewChildBinding
    override val viewModel: AddNewChildViewModel by activityViewModels()
    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_new_child, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

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
        binding.save.setOnClickListener {
            it.isEnabled = false
            lifecycleScope.launch {
                val response = viewModel.createNewChild()
                if (response == SUCCESS) {
                    viewModel.showToast.value = "Child Created Successfully"
                    viewModel.navigationCommand.value = NavigationCommand.Back
                } else viewModel.showToast.value = response
            }
            it.isEnabled = true
        }

    }


    override fun onPause() {
        super.onPause()
    }

    private fun showQRScanner() {
        val fragment = WatchQRCode()
        fragment.show(parentFragmentManager, "QR")
    }

}