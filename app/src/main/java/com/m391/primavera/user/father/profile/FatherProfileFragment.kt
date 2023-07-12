package com.m391.primavera.user.father.profile

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.m391.primavera.R
import com.m391.primavera.chat.ChatActivity
import com.m391.primavera.databinding.FragmentFatherProfileBinding
import com.m391.primavera.user.father.teacher.TeacherLocationFragment
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Binding
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.CHATS
import com.m391.primavera.utils.NavigationCommand
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class FatherProfileFragment : BaseFragment() {

    override val viewModel: FatherProfileViewModel by activityViewModels()
    private val args: FatherProfileFragmentArgs by navArgs()
    private lateinit var binding: FragmentFatherProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_father_profile, container, false)
        return binding.root
    }

    private val chooseFatherPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Binding.loadImage(binding.profileImage, uri.toString(), uri.toString())
                viewModel.setNewImage(uri.toString())
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.profileImage.visibility = View.GONE
        if (args.from == CHATS) {
            binding.editProfileImage.visibility = View.GONE
            binding.editProfileLocation.visibility = View.GONE
            binding.addChild.visibility = View.GONE
            binding.update.text = getString(R.string.back_to_chat)
            binding.update.tag = getString(R.string.back_to_chat)
        } else if (args.from != "Home") {
            binding.editProfileImage.visibility = View.GONE
            binding.editProfileLocation.visibility = View.GONE
            binding.addChild.visibility = View.GONE
            binding.update.text = getString(R.string.chat)
            binding.update.tag = getString(R.string.chat)
        }
    }

    override fun onStart() {
        super.onStart()

        binding.editProfileImage.setOnClickListener {
            binding.loadedProfileImage.visibility = View.GONE
            binding.profileImage.visibility = View.VISIBLE
            if (viewModel.newImageUri.value.isNullOrEmpty()) Binding.loadImage(
                binding.profileImage,
                viewModel.fatherInfo.value!!.image,
                viewModel.fatherInfo.value!!.imageUri
            )
            chooseFatherPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.fatherLocation.setOnClickListener {
            if (!checkLocationPermission()) requestLocationPermission()
            else {
                checkGpsEnabled()
                gpsStatus.observe(viewLifecycleOwner, Observer {
                    if (it == "Enabled") showShowFatherLocation()
                })
            }
        }
        binding.editProfileLocation.setOnClickListener {
            if (!checkLocationPermission()) requestLocationPermission()
            else {
                checkGpsEnabled()
                gpsStatus.observe(viewLifecycleOwner, Observer {
                    if (it == "Enabled") showEditFatherLocation()
                })
            }
        }
        binding.update.setOnClickListener { button ->
            if (button.tag != getString(R.string.chat) && button.tag != getString(R.string.back_to_chat)) {
                lifecycleScope.launch {
                    viewModel.updateFatherInfo()
                }
            } else if (button.tag == getString(R.string.back_to_chat)) {
                viewModel.navigationCommand.value = NavigationCommand.Back
            } else {
                if (!viewModel.checkChildFather(viewModel.fatherInfo.value!!.fatherUID)) {
                    val intent = Intent(activity, ChatActivity::class.java)
                    lifecycleScope.launch {
                        viewModel.createConversation(viewModel.fatherInfo.value!!.fatherUID)
                    }
                    intent.putExtra(Constants.TYPE, Constants.FATHER)
                    intent.putExtra(Constants.FATHER_UID, viewModel.fatherInfo.value!!.fatherUID)
                    intent.putExtra(
                        Constants.FATHER_FIRST_NAME,
                        viewModel.fatherInfo.value!!.firstName
                    )
                    startActivity(intent)
                } else viewModel.showToast.value = "Can't Chat With Yourself"

            }
        }
        binding.addChild.setOnClickListener {
            viewModel.navigationCommand.value =
                NavigationCommand.To(FatherProfileFragmentDirections.actionFatherProfileFragmentToAddNewChildFragment())
        }
    }

    private fun showShowFatherLocation() {
        val fragment = FatherShowLocationFragment()
        fragment.show(parentFragmentManager, "FatherShowLocation")
    }

    private fun showEditFatherLocation() {
        val fragment = FatherEditLocationFragment()
        fragment.show(parentFragmentManager, "FatherEditLocation")
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
        PermissionX.init(this@FatherProfileFragment)
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
            }
            .request { _, _, _ ->

            }
    }
}