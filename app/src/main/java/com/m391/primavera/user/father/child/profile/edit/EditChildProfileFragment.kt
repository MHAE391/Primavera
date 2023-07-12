package com.m391.primavera.user.father.child.profile.edit

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.m391.primavera.Primavera
import com.m391.primavera.R
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.databinding.FragmentEditChildProfileBinding
import com.m391.primavera.user.father.home.FatherHomeFragmentDirections
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Binding
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.NavigationCommand
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class EditChildProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentEditChildProfileBinding
    override val viewModel: EditChildProfileViewModel by activityViewModels()
    private val args: EditChildProfileFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_edit_child_profile,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.profileImage.visibility = View.GONE

        return binding.root
    }

    private val spinnerClickListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
            viewModel.getChildAcademicYear(parent.getItemAtPosition(pos).toString())
        }

        override fun onNothingSelected(perant: AdapterView<*>?) {}

    }

    override fun onStart() {
        super.onStart()
        ArrayAdapter.createFromResource(
            requireContext(), R.array.academic_years_array, R.layout.spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_item)
            // Apply the adapter to the spinner
            binding.academicYear.adapter = adapter
            binding.academicYear.onItemSelectedListener = spinnerClickListener
        }
        binding.editProfileImage.setOnClickListener {
            binding.loadedProfileImage.visibility = View.GONE
            binding.profileImage.visibility = View.VISIBLE
            if (viewModel.childNewImage.value.isNullOrEmpty())
                Binding.loadImage(
                    binding.profileImage,
                    viewModel.childInfo.value!!.image,
                    viewModel.childInfo.value!!.imageUri
                )
            chooseChildPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.changeChildAcademicYear.setOnClickListener {
            binding.childAcademicYears.visibility = View.VISIBLE
            binding.storedAcademicYear.visibility = View.GONE
        }
        binding.childWatch.setOnClickListener {
            showQRScanner()
        }
        binding.changeChildWatch.setOnClickListener {
            showQRScanner()
        }
        binding.save.setOnClickListener {
            lifecycleScope.launch {
                if (viewModel.updateChildInformation() == SUCCESS) {
                    binding.childAcademicYears.visibility = View.GONE
                    binding.storedAcademicYear.visibility = View.VISIBLE
                    viewModel.showToast.postValue("Information Updated")
                }
            }
        }
        binding.deleteChild.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Delete Child")
                .setMessage("Choose Yes to delete child  , No to back")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {
                        val response = viewModel.deleteChildFromFatherList()
                        if (response == SUCCESS) {
                            viewModel.showToast.value = "Child Deleted Successfully"
                            viewModel.navigationCommand.value = NavigationCommand.Back
                            viewModel.deleteChildInformation()
                        } else viewModel.showToast.value = response
                    }
                }.setNegativeButton("No") { _, _ ->

                }.show()
        }
    }


    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openStream(viewLifecycleOwner, args.childUid)
        }
    }

    override fun onPause() {
        super.onPause()

        lifecycleScope.launch {
            viewModel.closeStream(viewLifecycleOwner, args.childUid)
        }
    }

    private val chooseChildPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Binding.loadImage(binding.profileImage, uri.toString(), uri.toString())
                viewModel.childNewImage.value = uri.toString()
            }
        }

    private fun showQRScanner() {
        val fragment = WatchQRCode()
        fragment.show(parentFragmentManager, "QR")
    }

}