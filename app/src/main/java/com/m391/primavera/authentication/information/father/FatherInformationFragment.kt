package com.m391.primavera.authentication.information.father

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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.m391.primavera.R
import com.m391.primavera.authentication.information.father.bluetooth.BluetoothFragment
import com.m391.primavera.databinding.FragmentFatherInformationBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Binding

class FatherInformationFragment : BaseFragment() {

    private lateinit var binding: FragmentFatherInformationBinding
    override val viewModel: FatherInformationViewModel by activityViewModels()

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
            showBluetoothSheet()
        }
        binding.fatherImage.setOnClickListener {
            chooseFatherPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.childImage.setOnClickListener {
            chooseChildPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

    private fun showBluetoothSheet() {
        val bluetoothFragment = BluetoothFragment()
        bluetoothFragment.show(parentFragmentManager, "Bluetooth")
    }
}