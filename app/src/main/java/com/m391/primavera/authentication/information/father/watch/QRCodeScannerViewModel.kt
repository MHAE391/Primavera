package com.m391.primavera.authentication.information.father.watch

import android.app.Application
import android.graphics.Bitmap
import com.m391.primavera.utils.BaseViewModel
import android.util.SparseArray
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer

class QRCodeScannerViewModel(app: Application) : BaseViewModel(app) {
    private val _barcodeResult = MutableLiveData<String>()
    val barcodeResult: LiveData<String>
        get() = _barcodeResult

    fun onBarcodeScanned(barcode: String) {
        _barcodeResult.postValue(barcode)
    }
}