package com.m391.primavera.user.teacher.home.switjha.father

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentWatchQrCodeBinding
import com.m391.primavera.utils.Constants
import com.permissionx.guolindev.PermissionX

class WatchQRCodeScanner : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentWatchQrCodeBinding
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private var captureManager: CaptureManager? = null
    private var barcodeView: DecoratedBarcodeView? = null
    private val startTimestamp = System.currentTimeMillis()
    private val viewModel: CreateFatherViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_watch_qr_code, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        if (!checkPermission()) {
            requestPermission()
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheet(view)
        if (checkPermission()) {
            setupCamera(savedInstanceState)
        } else {
            requestPermission()
        }

    }

    private fun requestPermission() {
        PermissionX.init(this@WatchQRCodeScanner).permissions(Manifest.permission.CAMERA)
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
                    this.dismiss()
                }
            }
    }

    private fun setupBottomSheet(view: View) {
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels / 2
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val layout = binding.bottom
        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupCamera(savedInstanceState: Bundle?) {
        barcodeView = binding.barcodeView
        captureManager = CaptureManager(requireActivity(), barcodeView)
        captureManager!!.initializeFromIntent(requireActivity().intent, savedInstanceState)
        captureManager!!.decode()
        barcodeView!!.decodeSingle(barcodeCallback)
    }

    private val barcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            viewModel.getChildWatch(result!!.text)
            dismiss()
        }

        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
            if (System.currentTimeMillis() - startTimestamp >= 30_000) {
                Toast.makeText(requireContext(), "Unable To Scan Code", Toast.LENGTH_SHORT)
                    .show()
                dismiss()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (checkPermission() && captureManager != null) captureManager!!.onSaveInstanceState(
            outState
        )
    }

    override fun onPause() {
        super.onPause()
        if (checkPermission() && barcodeView != null) {
            barcodeView!!.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermission() && barcodeView != null) {
            barcodeView!!.resume()
        } else requestPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (checkPermission() && captureManager != null) captureManager!!.onDestroy()
    }
}