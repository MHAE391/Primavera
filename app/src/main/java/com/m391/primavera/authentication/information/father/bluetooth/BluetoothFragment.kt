package com.m391.primavera.authentication.information.father.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.m391.primavera.R
import com.m391.primavera.authentication.information.father.FatherInformationViewModel
import com.m391.primavera.databinding.FragmentBluetoothBinding
import com.m391.primavera.utils.Constants.PERMISSION_CANCEL
import com.m391.primavera.utils.Constants.PERMISSION_LIST
import com.m391.primavera.utils.Constants.PERMISSION_OK
import com.m391.primavera.utils.Constants.PERMISSION_SETTING
import com.m391.primavera.utils.setup
import com.permissionx.guolindev.PermissionX

class BluetoothFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentBluetoothBinding
    private val bluetoothViewModel: BluetoothFragmentViewModel by viewModels()
    private val fatherViewModel: FatherInformationViewModel by activityViewModels()
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bluetooth, container, false)
        binding.lifecycleOwner = this
        binding.fatherViewModel = fatherViewModel
        binding.bluetoothViewModel = bluetoothViewModel
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    override fun onPause() {
        super.onPause()
        if (checkPermissions()) bluetoothAdapter.cancelDiscovery()
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()
        bluetoothViewModel.setBluetoothStatus(false)
        if (checkPermissions()) {
            if (bluetoothAdapter.isEnabled) enableBluetooth()
        }
        binding.switchBluetoothStatus.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    if (!checkPermissions()) {
                        requestPermissions()
                    } else {
                        enableBluetooth()
                    }
                }
                false -> {
                    if (checkPermissions()) bluetoothAdapter.cancelDiscovery()
                    else {
                        requestPermissions()
                    }
                }
            }

        }

        setupRecyclerView()
    }

    private lateinit var adapter: BluetoothDevicesAdapter
    private fun setupRecyclerView() {
        adapter = BluetoothDevicesAdapter {
            fatherViewModel.getChildWatch(it.name, it.address)
            this.dismiss()
        }
        binding.usersRecyclerView.setup(adapter)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels / 2
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val layout = binding.bottom
        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
    }

    private val requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                binding.switchBluetoothStatus.isChecked = true
                bluetoothViewModel.setBluetoothStatus(true)
                discoverDevices()
            } else {
                bluetoothViewModel.setBluetoothStatus(false)
                binding.switchBluetoothStatus.isChecked = false
            }
        }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissions(): Boolean {
        return PermissionX.isGranted(
            requireContext(), Manifest.permission.BLUETOOTH
        ) && PermissionX.isGranted(
            requireContext(), Manifest.permission.BLUETOOTH_SCAN
        ) && PermissionX.isGranted(
            requireContext(), Manifest.permission.BLUETOOTH_ADMIN
        ) && PermissionX.isGranted(
            requireContext(), Manifest.permission.BLUETOOTH_CONNECT
        ) && PermissionX.isGranted(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) && PermissionX.isGranted(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermissions() {
        PermissionX.init(this@BluetoothFragment).permissions(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                deniedList, PERMISSION_LIST, PERMISSION_OK, PERMISSION_CANCEL
            )
        }.onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(
                deniedList, PERMISSION_SETTING, PERMISSION_OK, PERMISSION_CANCEL
            )
        }.request { allGranted, _, _ ->
            if (allGranted) {
                enableBluetooth()
            } else {
                bluetoothViewModel.setBluetoothStatus(false)
                this.dismiss()
            }
        }
    }

    private fun enableBluetooth() {
        //binding.switchBluetoothStatus.isChecked = false
        requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    @SuppressLint("MissingPermission")
    private fun discoverDevices() {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        requireActivity().registerReceiver(bluetoothViewModel.discoverDevicesReceiver, filter)
        bluetoothAdapter.startDiscovery()
    }


}