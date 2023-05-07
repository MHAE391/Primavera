package com.m391.primavera.authentication.information.father

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.R
import com.m391.primavera.authentication.information.father.watch.QRCodeScannerFragment
import com.m391.primavera.databinding.FragmentFatherInformationBinding
import com.m391.primavera.user.father.FatherActivity
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Binding
import com.m391.primavera.utils.Constants.SUCCESS
import kotlinx.coroutines.launch

class FatherInformationFragment : BaseFragment() {

    private lateinit var binding: FragmentFatherInformationBinding
    override val viewModel: FatherInformationViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_father_information, container, false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.childWatch.setOnClickListener {
            showQRScanner()
        }
        binding.fatherImage.setOnClickListener {
            chooseFatherPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.childImage.setOnClickListener {
            chooseChildPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.academic_years_array,
            R.layout.spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_item)
            // Apply the adapter to the spinner
            binding.academicYear.adapter = adapter
            binding.academicYear.onItemSelectedListener = spinnerClickListener
        }
        binding.setData.setOnClickListener {
            it.isEnabled = false
            lifecycleScope.launch {
                viewModel.setData()
            }
            it.isEnabled = true
        }
        viewModel.response.observe(viewLifecycleOwner, Observer {
            if (it == SUCCESS) {
                startActivity(Intent(activity, FatherActivity::class.java))
                activity?.finish()
            } else {
                viewModel.showSnackBar.value = it.toString()
            }
        })
    }

    private fun showQRScanner() {
        val fragment = QRCodeScannerFragment()
        fragment.show(parentFragmentManager, "QR")
    }

    private val qrCodeScanLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val qrCodeText = data?.getStringExtra("SCAN_RESULT")
                Toast.makeText(requireContext(), qrCodeText, Toast.LENGTH_SHORT).show()
            } else {
                // The QR code scanning was cancelled or failed, handle the result here
            }
        }
    private val chooseFatherPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Binding.loadImage(binding.fatherImage, uri.toString())
                viewModel.fatherImage.value = uri.toString()
            }
        }

    private val chooseChildPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Binding.loadImage(binding.childImage, uri.toString())
                viewModel.childImage.value = uri.toString()
            }
        }


    private val spinnerClickListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
            viewModel.getChildAcademicYear(parent.getItemAtPosition(pos).toString())
        }

        override fun onNothingSelected(perant: AdapterView<*>?) {}

    }
}