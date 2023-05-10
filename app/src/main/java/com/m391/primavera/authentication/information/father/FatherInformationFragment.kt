package com.m391.primavera.authentication.information.father

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.m391.primavera.R
import com.m391.primavera.authentication.information.father.location.FatherLocationFragment
import com.m391.primavera.authentication.information.father.watch.QRCodeScannerFragment
import com.m391.primavera.databinding.FragmentFatherInformationBinding
import com.m391.primavera.notification.Notification
import com.m391.primavera.user.father.FatherActivity
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Binding
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.SUCCESS
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

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

    private lateinit var locationManager: LocationManager
    private val gpsStatus = MutableLiveData<String>()
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {}

        override fun onProviderEnabled(provider: String) {
            super.onProviderEnabled(provider)
            gpsStatus.value = "Enabled"
        }

        override fun onProviderDisabled(provider: String) {
            super.onProviderDisabled(provider)
            gpsStatus.value = "Disabled"
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkGpsEnabled() {
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 0, 0f, locationListener
        )
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is not enabled, show a dialog to prompt the user to enable it
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Enable GPS")
                .setMessage("GPS is required for this app. Do you want to enable it?")
                .setPositiveButton("Yes") { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }.setNegativeButton("No") { dialog, _ ->
                    dialog.cancel()
                    checkGpsEnabled() // check again in case user changes their mind
                }.show()
        } else gpsStatus.value = "Enabled"
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        PermissionX.init(this@FatherInformationFragment)
            .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    Constants.PERMISSION_LIST,
                    Constants.PERMISSION_OK,
                    Constants.PERMISSION_CANCEL
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    Constants.PERMISSION_SETTING,
                    Constants.PERMISSION_OK,
                    Constants.PERMISSION_CANCEL
                )
            }.request { allGranted, _, _ ->
                if (!allGranted) {
                    checkGpsEnabled()
                    gpsStatus.observe(viewLifecycleOwner, Observer {
                        if (it == "Enabled") showSelectLocation()
                    })
                }
            }
    }


    override fun onStart() {
        super.onStart()
        binding.fatherLocation.setOnClickListener {
            if (!checkLocationPermission()) requestLocationPermission()
            else {
                checkGpsEnabled()
                gpsStatus.observe(viewLifecycleOwner, Observer {
                    if (it == "Enabled") showSelectLocation()
                })
            }
        }
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
            requireContext(), R.array.academic_years_array, R.layout.spinner_item
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

    private fun showSelectLocation() {
        gpsStatus.removeObservers(viewLifecycleOwner)
        val fragment = FatherLocationFragment()
        fragment.show(parentFragmentManager, "Location")
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