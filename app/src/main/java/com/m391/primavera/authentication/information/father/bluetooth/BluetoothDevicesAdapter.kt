package com.m391.primavera.authentication.information.father.bluetooth

import com.m391.primavera.R
import com.m391.primavera.utils.BaseRecyclerViewAdapter
import com.m391.primavera.utils.models.BluetoothDeviceModel

class BluetoothDevicesAdapter(callBack: (device: BluetoothDeviceModel) -> Unit) :
    BaseRecyclerViewAdapter<BluetoothDeviceModel>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.bluetooth_device
}