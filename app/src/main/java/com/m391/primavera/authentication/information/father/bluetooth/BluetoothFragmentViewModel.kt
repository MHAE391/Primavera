package com.m391.primavera.authentication.information.father.bluetooth

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.models.BluetoothDeviceModel
import okhttp3.internal.notifyAll


class BluetoothFragmentViewModel(val app: Application) : BaseViewModel(app) {
    private val _devices = MutableLiveData<List<BluetoothDeviceModel>>()
    val devices: LiveData<List<BluetoothDeviceModel>> = _devices
    private val _bluetoothStatus = MutableLiveData<String>()
    val bluetoothStatus: LiveData<String> = _bluetoothStatus

    @SuppressLint("MissingPermission")
    fun searchDevices(devices: List<BluetoothDevice>?) {
        val devicesList = ArrayList<BluetoothDeviceModel>()
        for (device in devices!!) {
            if (!device.name.isNullOrEmpty()) {
                devicesList.add(BluetoothDeviceModel(device.name, device.address))
            }
        }
        _devices.value = devicesList

    }

    private fun startSearch() {
        showLoading.value = true
    }

    private fun endSearch() {
        showLoading.value = false
    }

    fun setBluetoothStatus(status: Boolean) {
        if (status) _bluetoothStatus.value = "ON"
        else _bluetoothStatus.value = "OFF"
    }

    private val devicesSet = HashSet<BluetoothDevice>()
    val discoverDevicesReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(p0: Context?, intent: Intent?) {
            when (intent!!.action.toString()) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    startSearch()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    endSearch()
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    devicesSet.add(device!!)
                    searchDevices(devicesSet.toList())
                }
            }
        }

        override fun peekService(myContext: Context?, service: Intent?): IBinder {
            return super.peekService(myContext, service)
        }
    }
}