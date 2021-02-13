package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint

import android.content.pm.PackageManager

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil

import androidx.navigation.fragment.findNavController

import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment

import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import com.google.android.gms.maps.GoogleMap
import java.util.*

class SelectLocationFragment : BaseFragment() , OnMapReadyCallback{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private val TAG = SelectLocationFragment::class.java.simpleName
    private lateinit var gmap: GoogleMap

    private lateinit var map: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var marker: Marker
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        binding.buttonSave.setOnClickListener {
            onLocationSelected()
        }
//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val dmapView =  childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        dmapView.getMapAsync(this)

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        //val latLng = poi.latLng
        //poi:PointOfInterest
        val latitude = marker.position.latitude.toDouble()
        val longitude = marker.position.longitude.toDouble()

        _viewModel.reminderSelectedLocationStr.value = marker.title
        _viewModel.latitude.value = latitude
        _viewModel.longitude.value = longitude
        findNavController().popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            true
        }
        R.id.hybrid_map -> {
            true
        }
        R.id.satellite_map -> {
            true
        }
        R.id.terrain_map -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            map = p0
        }
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_style))
        setOnMapClick(map)
        enableLocation()
    }
    private fun setOnMapClick(googleMap: GoogleMap) {
        googleMap.setOnMapClickListener { latLong ->
            val snippet = String.format(
                    Locale.getDefault(),
                    getString(R.string.lat_long_snippet),
                    latLong.latitude,
                    latLong.longitude
            )
            googleMap.clear()
            marker = googleMap.addMarker(
                    MarkerOptions()
                            .position(latLong)
                            .title(getString(R.string.dropped_pin))
                            .snippet(snippet)
            )
            marker.showInfoWindow()
            enableSaveButton()
        }
        googleMap.setOnPoiClickListener { poi ->
            googleMap.clear()
            marker = googleMap.addMarker(
                    MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name)
            )
            marker.showInfoWindow()
            enableSaveButton()
        }
    }
    private fun enableLocation() {
        if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not granted, requesting permissions")
            requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
            )
        } else {
            Log.d(TAG, "Location permission granted, requesting location")
            map.isMyLocationEnabled = true
            getLocation(map)
        }

    }
    @SuppressLint("MissingPermission")
    private fun getLocation(googleMap: GoogleMap) {
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            var lastKnownLocation: Location
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.result!!
                    Log.d(TAG, "Last location found: ${lastKnownLocation.latitude}, ${lastKnownLocation.longitude}")
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation.latitude,
                                    lastKnownLocation.longitude), 15.0f))
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.")
                    Log.e(TAG, "Exception: %s", task.exception)
                    val defaultLocation = LatLng(37.4030185, -122.3212949)
                    googleMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, 15.0f))
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    private fun enableSaveButton() {
        binding.buttonSave
                .apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            isEnabled = true
        }
    }


}
