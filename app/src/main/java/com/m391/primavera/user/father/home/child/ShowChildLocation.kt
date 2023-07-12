package com.m391.primavera.user.father.home.child

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
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
import com.m391.primavera.user.father.home.FatherHomeViewModel
import com.m391.primavera.utils.BaseBottomSheetFragment
import com.m391.primavera.utils.BaseViewModel
import timber.log.Timber

class ShowChildLocation : BaseBottomSheetFragment(), OnMapReadyCallback {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentFatherShowLocationBinding
    override val viewModel: FatherHomeViewModel by activityViewModels()


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
        viewModel.watchLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                displayLocationOnMap(location.latitude, location.longitude)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.watchLocation.removeObservers(viewLifecycleOwner)
    }

    private fun displayLocationOnMap(latitude: Double, longitude: Double) {
        val location = LatLng(latitude, longitude)
        map.clear()
        val cameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(FatherLocationFragment.DEFAULT_ZOOM_LEVEL)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        val poiMarker = map.addMarker(
            MarkerOptions().position(location)
                .title(
                    "${viewModel.currentChildInformation.value!!.childName} Location"
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