package com.m391.primavera.user.father.home.switjha.teacher

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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.m391.primavera.R
import com.m391.primavera.user.father.home.switjha.teacher.SelectSubjectsFragment
import com.m391.primavera.databinding.FragmentCreateTeacherBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.NavigationCommand
import com.permissionx.guolindev.PermissionX
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
            if (gpsStatus.hasActiveObservers()) gpsStatus.removeObservers(viewLifecycleOwner)
        }
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
        PermissionX.init(this@CreateTeacherFragment)
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