package com.m391.primavera.authentication.information.father.location

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.m391.primavera.R
import com.m391.primavera.authentication.information.father.FatherInformationViewModel
import com.m391.primavera.databinding.FragmentFatherLocationBinding
import timber.log.Timber
import java.util.*


class FatherLocationFragment : BottomSheetDialogFragment(), OnMapReadyCallback {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var binding: FragmentFatherLocationBinding
    private val locationViewModel: FatherLocationViewModel by viewModels()
    private val fatherViewModel: FatherInformationViewModel by activityViewModels()

    private lateinit var map: GoogleMap
    private var markerPosition: PointOfInterest? = null
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheet(view)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun moveToSelectedPosition() {
        if (fatherViewModel.selectedLatitude.value != null && fatherViewModel.selectedLongitude.value != null) {
            val userLocation = LatLng(
                fatherViewModel.selectedLatitude.value!!.toDouble(),
                fatherViewModel.selectedLongitude.value!!.toDouble()
            )
            map.clear()
            val cameraPosition = CameraPosition.Builder()
                .target(userLocation)
                .zoom(DEFAULT_ZOOM_LEVEL)
                .build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            val poiMarker = map.addMarker(
                MarkerOptions().position(userLocation)
                    .title(getString(R.string.my_selected_location))
            )
            poiMarker?.showInfoWindow()
        }
    }

    private fun setMap() {
        setMapStyle(map)
        setPoiClick(map)
        setMapClick(map)
        enableMyLocation()
        moveToSelectedPosition()
        binding.save.setOnClickListener {
            if (markerPosition != null) {
                locationViewModel.setLocation(
                    markerPosition!!.latLng.longitude, markerPosition!!.latLng.latitude
                )
                fatherViewModel.setLocation(
                    markerPosition!!.latLng.longitude, markerPosition!!.latLng.latitude
                )
                if (markerPosition!!.name == getString(R.string.dropped_pin)) fatherViewModel.setLocationString(
                    markerPosition!!.latLng.toString()
                )
                else fatherViewModel.setLocationString(markerPosition!!.name)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Choose Your Position", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMap()
    }

    private fun setMapClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            map.clear()
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                latLng.latitude,
                latLng.longitude
            )
            val poiMarker = map.addMarker(
                MarkerOptions().position(latLng).title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )

            poiMarker?.showInfoWindow()
            markerPosition = PointOfInterest(
                latLng, getString(R.string.my_selected_location), getString(R.string.dropped_pin)
            )

        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name)
            )
            poiMarker?.showInfoWindow()
            markerPosition = poi
        }
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

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        map.setOnMyLocationButtonClickListener {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    map.clear()
                    val cameraPosition = CameraPosition.Builder()
                        .target(userLocation)
                        .zoom(DEFAULT_ZOOM_LEVEL)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    val poiMarker = map.addMarker(
                        MarkerOptions().position(userLocation).title("My Location")
                    )
                    poiMarker?.showInfoWindow()
                    markerPosition = PointOfInterest(
                        userLocation,
                        getString(R.string.my_location),
                        getString(R.string.dropped_pin)
                    )
                }
            }
            true
        }

    }

    private fun setupBottomSheet(view: View) {
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels * 4 / 5
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val layout = binding.bottom
        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
        behavior.isDraggable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_father_location, container, false)
        binding.lifecycleOwner = this
        binding.fatherViewModel = fatherViewModel
        binding.locationViewModel = locationViewModel
        return binding.root
    }

    companion object {
        const val DEFAULT_ZOOM_LEVEL = 15f
    }
}