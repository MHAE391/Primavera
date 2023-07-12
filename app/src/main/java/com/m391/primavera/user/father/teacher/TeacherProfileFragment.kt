package com.m391.primavera.user.father.teacher

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
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.m391.primavera.R
import com.m391.primavera.authentication.information.teacher.SelectTeacherLocationFragment
import com.m391.primavera.chat.ChatActivity
import com.m391.primavera.databinding.FragmentTeacherProfileBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.CHATS
import com.m391.primavera.utils.Constants.CHILD
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.FATHER_FIRST_NAME
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.Constants.TEACHER_UID
import com.m391.primavera.utils.Constants.TYPE
import com.m391.primavera.utils.NavigationCommand
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.Serializable

class TeacherProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentTeacherProfileBinding
    override val viewModel: TeacherProfileViewModel by activityViewModels()
    private val args: TeacherProfileFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_teacher_profile, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        if (args.from == CHATS) {
            binding.chat.text = getString(R.string.back_to_chat)
        }
        binding.chat.setOnClickListener {
            if (args.from != CHATS) {
                val intent = Intent(activity, ChatActivity::class.java)
                lifecycleScope.launch {
                    viewModel.createConversation()
                }
                intent.putExtra(TYPE, TEACHER)
                intent.putExtra(TEACHER_UID, viewModel.teacherData.value!!.teacherId)
                intent.putExtra(FATHER_FIRST_NAME, viewModel.teacherData.value!!.firstName)
                startActivity(intent)
            } else {
                viewModel.navigationCommand.postValue(NavigationCommand.Back)
            }
        }
        binding.teacherLocation.setOnClickListener {
            if (!checkLocationPermission()) requestLocationPermission()
            else {
                checkGpsEnabled()
                gpsStatus.observe(viewLifecycleOwner, Observer {
                    if (it == "Enabled") {
                        val fragment = TeacherLocationFragment()
                        fragment.show(parentFragmentManager, "ShowLocation")
                    }
                })
            }
        }
        binding.teacherRate.setOnClickListener {
            showRatingDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openStream(viewLifecycleOwner, args.teacherUid)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStream(viewLifecycleOwner, args.teacherUid)
            if (gpsStatus.hasActiveObservers()) gpsStatus.removeObservers(viewLifecycleOwner)
        }
    }

    private fun showRatingDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rating, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.rateBar)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating.toDouble()
            lifecycleScope.launch {
                viewModel.rateTeacher(rating)
            }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
        PermissionX.init(this@TeacherProfileFragment)
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
