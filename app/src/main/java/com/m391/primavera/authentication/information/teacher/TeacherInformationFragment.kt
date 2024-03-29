package com.m391.primavera.authentication.information.teacher

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import android.widget.DatePicker
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentTeacherInformationBinding
import com.m391.primavera.user.teacher.TeacherActivity
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Binding
import com.m391.primavera.utils.Constants
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.launch
import java.util.Calendar

class TeacherInformationFragment : BaseFragment() {

    private lateinit var binding: FragmentTeacherInformationBinding
    override val viewModel: TeacherInformationViewModel by activityViewModels()
    private lateinit var calendar: Calendar

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
        calendar = Calendar.getInstance()
        return binding.root
    }

    private val chooseTeacherPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Binding.loadImage(binding.teacherImage, uri.toString(), uri.toString())
                viewModel.teacherImage.value = uri.toString()
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

    override fun onStart() {
        super.onStart()
        binding.teacherAcademicYears.setOnClickListener {
            showSelectYearsFragment()
        }
        binding.teacherSubjects.setOnClickListener {
            showSelectSubjectsFragment()
        }
        binding.teacherLocation.setOnClickListener {
            if (!checkLocationPermission()) requestLocationPermission()
            else {
                checkGpsEnabled()
                gpsStatus.observe(viewLifecycleOwner, Observer {
                    if (it == "Enabled") showSelectLocationFragment()
                })
            }
        }
        binding.teacherDateOfBarth.setOnClickListener {
            showDatePickerDialog()
        }
        binding.teacherImage.setOnClickListener {
            chooseTeacherPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.setData.setOnClickListener {
            it.isEnabled = false
            lifecycleScope.launch {
                val response = viewModel.setData()
                if (response == Constants.SUCCESS) {
                    startActivity(Intent(activity, TeacherActivity::class.java))
                    activity?.finish()
                } else {
                    viewModel.showSnackBar.value = response
                }
            }
            it.isEnabled = true
        }
    }

    private fun showSelectSubjectsFragment() {
        val fragment = SelectSubjectsFragment()
        fragment.show(parentFragmentManager, "SelectSubjects")
    }

    private fun showSelectLocationFragment() {
        gpsStatus.removeObservers(viewLifecycleOwner)
        val fragment = SelectTeacherLocationFragment()
        fragment.show(parentFragmentManager, "SelectLocation")
    }

    private fun showSelectYearsFragment() {
        val fragment = SelectYearsFragment()
        fragment.show(parentFragmentManager, "SelectYears")
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

    override fun onPause() {
        super.onPause()
        if (gpsStatus.hasActiveObservers()) gpsStatus.removeObservers(viewLifecycleOwner)

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
        PermissionX.init(this@TeacherInformationFragment)
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
            }.request { _, _, _ ->

            }
    }

}