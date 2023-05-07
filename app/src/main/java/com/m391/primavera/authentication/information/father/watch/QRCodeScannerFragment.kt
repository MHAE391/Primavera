package com.m391.primavera.authentication.information.father.watch

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.m391.primavera.R
import com.m391.primavera.authentication.information.father.FatherInformationViewModel
import com.m391.primavera.databinding.FragmentQrCodeScannerBinding
import com.m391.primavera.utils.Constants.PERMISSION_CANCEL
import com.m391.primavera.utils.Constants.PERMISSION_LIST
import com.m391.primavera.utils.Constants.PERMISSION_OK
import com.m391.primavera.utils.Constants.PERMISSION_SETTING
import com.permissionx.guolindev.PermissionX

class QRCodeScannerFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentQrCodeScannerBinding
    private val viewModel: QRCodeScannerViewModel by viewModels()
    private val fatherViewModel: FatherInformationViewModel by activityViewModels()
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var captureManager: CaptureManager
    private lateinit var barcodeView: DecoratedBarcodeView
    private val startTimestamp = System.currentTimeMillis()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_qr_code_scanner, container, false)
        binding.lifecycleOwner = this
        binding.fatherViewModel = fatherViewModel
        binding.qrViewModel = viewModel
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
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
        PermissionX.init(this@QRCodeScannerFragment).permissions(Manifest.permission.CAMERA)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList, PERMISSION_LIST, PERMISSION_OK, PERMISSION_CANCEL
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList, PERMISSION_SETTING, PERMISSION_OK, PERMISSION_CANCEL
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
        captureManager.initializeFromIntent(requireActivity().intent, savedInstanceState)
        captureManager.decode()
        barcodeView.decodeSingle(barcodeCallback)
        viewModel.barcodeResult.observe(viewLifecycleOwner)
        { result ->
            Toast.makeText(requireContext(), result.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private val barcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            viewModel.onBarcodeScanned(result?.text!!)
            viewModel.setScanned(result.text)
            fatherViewModel.getChildWatch(result.text)
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
        captureManager.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun onResume() {
        super.onResume()
        if (checkPermission()) {
            barcodeView.resume()
        } else requestPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.onDestroy()
    }
}