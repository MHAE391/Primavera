package com.m391.primavera.user.teacher.home.switjha.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.R
import com.m391.primavera.authentication.AuthenticationActivity
import com.m391.primavera.databinding.FragmentTeacherEditProfileBinding
import com.m391.primavera.user.father.FatherActivity

import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Binding
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.NavigationCommand
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.dialog.permissionMapOnQ
import kotlinx.coroutines.launch

class TeacherEditProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentTeacherEditProfileBinding
    override val viewModel: TeacherEditProfileViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_teacher_edit_profile,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.profileImage.visibility = View.GONE

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.teacherSubjects.setOnClickListener {
            showAcademicSubjectsFragment()
        }
        binding.teacherAcademicYears.setOnClickListener {
            showAcademicYearsFragment()
        }
        binding.editProfileImage.setOnClickListener {
            binding.loadedProfileImage.visibility = View.GONE
            binding.profileImage.visibility = View.VISIBLE
            if (viewModel.teacherNewImage.value.isNullOrEmpty())
                Binding.loadImage(
                    binding.profileImage,
                    viewModel.teacherInfo.value!!.image,
                    viewModel.teacherInfo.value!!.imageUri
                )
            chooseTeacherPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.save.setOnClickListener {
            lifecycleScope.launch {
                val response = viewModel.updateTeacherInformation()
                if (response == SUCCESS) viewModel.showToast.value =
                    "Information Updated Successfully"
                else viewModel.showToast.value = response
            }
        }
        binding.teacherLocation.setOnClickListener {
            if (!checkLocationPermission()) requestLocationPermission()
            else {
                checkGpsEnabled()
                gpsStatus.observe(viewLifecycleOwner, Observer {
                    if (it == "Enabled") showShowLocationFragment()
                })
            }
        }
        binding.editProfileLocation.setOnClickListener {
            if (!checkLocationPermission()) requestLocationPermission()
            else {
                checkGpsEnabled()
                gpsStatus.observe(viewLifecycleOwner, Observer {
                    if (it == "Enabled") showEditLocationFragment()
                })
            }
        }
        binding.deleteTeacher.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Delete Teacher")
                .setMessage("Choose Yes to delete Teacher Account , No to back")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {
                        val response = viewModel.deleteTeacherAccount(viewLifecycleOwner)
                        if (response == FATHER) {
                            val deleteResponse = viewModel.deleteAccount()
                            if (deleteResponse == SUCCESS) {
                                startActivity(Intent(activity, FatherActivity::class.java))
                                requireActivity().finish()
                                viewModel.showToast.value = "Account Deleted"
                            } else viewModel.showToast.value = deleteResponse
                        } else if (response == TEACHER) {
                            val deleteResponse = viewModel.logOut()
                            if (deleteResponse == SUCCESS) {
                                startActivity(Intent(activity, AuthenticationActivity::class.java))
                                requireActivity().finish()
                                viewModel.showToast.value = "Account Deleted"
                            } else viewModel.showToast.value = deleteResponse
                        }
                        viewModel.showLoading.value = false
                    }
                }.setNegativeButton("No") { _, _ ->

                }.show()
        }
    }

    private fun showAcademicSubjectsFragment() {
        val fragment = AcademicSubjectsFragment()
        fragment.show(parentFragmentManager, "AcademicSubjects")
    }

    private fun showShowLocationFragment() {

        val fragment = ShowLocationFragment()
        fragment.show(parentFragmentManager, "ShowLocation")
    }

    private fun showEditLocationFragment() {
        val fragment = EditLocationFragment()
        fragment.show(parentFragmentManager, "EditLocation")
    }

    private fun showAcademicYearsFragment() {
        val fragment = AcademicYearsFragment()
        fragment.show(parentFragmentManager, "AcademicYears")
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
            if (gpsStatus.hasActiveObservers()) gpsStatus.removeObservers(viewLifecycleOwner)
        }
    }

    private val chooseTeacherPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Binding.loadImage(binding.profileImage, uri.toString(), uri.toString())
                viewModel.teacherNewImage.value = uri.toString()
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
        PermissionX.init(this@TeacherEditProfileFragment)
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