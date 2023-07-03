package com.m391.primavera.user.father.profile

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.m391.primavera.R
import com.m391.primavera.authentication.information.father.location.FatherLocationFragment
import com.m391.primavera.databinding.FragmentFatherShowLocationBinding
import timber.log.Timber

class FatherShowLocationFragment : BottomSheetDialogFragment(), OnMapReadyCallback {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private val viewModel: FatherProfileViewModel by activityViewModels()
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentFatherShowLocationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_father_show_location,
            container,
            false
        )
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }

    private fun setupBottomSheet(view: View) {
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels * 4 / 5
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val layout = binding.bottom
        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
        behavior.isDraggable = false
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheet(view)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        displayLocationOnMap(
            viewModel.fatherInfo.value!!.latitude.toDouble(),
            viewModel.fatherInfo.value!!.longitude.toDouble()
        )
    }

    private fun displayLocationOnMap(latitude: Double, longitude: Double) {
        val location = LatLng(latitude, longitude)

        val cameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(FatherLocationFragment.DEFAULT_ZOOM_LEVEL)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        val poiMarker = map.addMarker(
            MarkerOptions().position(location)
                .title(
                    getString(R.string.father_location)
                )
        )
        poiMarker?.showInfoWindow()
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
            if (!success) {
                Timber.tag("ads").e("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Timber.tag("ads").e(e, "Can't find style. Error: ")
        }
    }

}