package com.example.parkedcarfinder

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.example.parkedcarfinder.databinding.ActivityMapsBinding

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var parkLocation: LatLng? = null

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private var marker: Marker? = null

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLocation()
            } else {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
        }

        Log.d("MapsFragment", "1. onViewCreated() called. parkLocation: ${parkLocation?.toString() ?: "not set"}")

        prepareViewModel(parkLocation.toString())
    }

    private fun prepareViewModel(parkLoc: String) {
        moveCarIconToCurrentLocation()
        val locationViewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)
        locationViewModel.location.observe(viewLifecycleOwner, { updateText(it) })

        view?.findViewById<Button>(R.id.parked_button)?.setOnClickListener {
            parkLocation?.let {
                locationViewModel.setParkedLocation(parkLocation.toString())
            }
//            updateText(locationViewModel.location.value ?: parkLocation.toString())
            updateText(parkLocation.toString())
            Log.d("MapsFragment", "11. setOnClickListener called. parkLocation:${parkLocation?.toString() ?: "not set"}")
        }
    }


    @SuppressLint("StringFormatMatches")
    private fun updateText(parkLoc: String) {
        view?.findViewById<TextView>(R.id.activity_fragment_split_maps)?.text = getString(R.string.parking_location, parkLoc)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap = googleMap.apply {
            setOnMapClickListener { latLng ->
                addOrMoveSelectedPositionMarker(latLng)
            }
        }
        when {
            hasLocationPermission() -> getLocation()
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
            else -> requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        Log.d("MapsFragment", "2. getLocation() called.")
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                parkLocation = userLocation

                updateMapLocation(userLocation)
                addMarkerAtLocation(userLocation, "Your Location")
                Log.d("MapsFragment", "3. User location obtained: $userLocation")
            }
        }
    }

    private fun moveCarIconToCurrentLocation() {
        if (hasLocationPermission()) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                return
            }

            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = LatLng(it.latitude, it.longitude)



                    // Ensure mMap is initialized before accessing it
                    if (::mMap.isInitialized) {
                        // Check if a marker already exists
                        if (marker == null) {
                            // Create a new marker at the user's location if no marker exists
                            marker = addMarkerAtLocation(userLocation, "Park Here", getBitmapDescriptorFromVector(R.drawable.car_parking_48))
                            Log.d("MapsFragment", "4. Marker created at: $userLocation")
                        } else {
                            // Move the existing marker to the user's location
                            marker?.position = userLocation
                            updateMapLocation(userLocation)
                            Log.d("MapsFragment", "5. Marker moved to: $userLocation")
                        }

                        // Update parkLocation with the marker's geographical location
                        parkLocation = marker?.position
                        Log.d("MapsFragment", "6. parkLocation updated to: $parkLocation")
                    } else {
                        Log.e("MapsFragment", "7. mMap is not initialized")
                    }
                }
            }
        } else {
            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun addOrMoveSelectedPositionMarker(latLng: LatLng) {
        if (marker == null) {
            marker = addMarkerAtLocation(latLng, "Park Here", getBitmapDescriptorFromVector(R.drawable.car_parking_48))
        } else {
            marker?.position = latLng
        }

        // Update parkLocation with the marker's geographical location
        parkLocation = marker?.position
        Log.d("MapsFragment", "8. Marker position updated in addOrMoveSelectedPositionMarker to: $parkLocation")
    }

    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun addMarkerAtLocation(location: LatLng, title: String, markerIcon: BitmapDescriptor? = null) =
        mMap.addMarker(
            MarkerOptions().title(title).position(location).apply {
                markerIcon?.let { icon(markerIcon) }
            }
        )

    private fun getBitmapDescriptorFromVector(@DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor? {
        val bitmap = ContextCompat.getDrawable(requireContext(), vectorDrawableResourceId)?.let { vectorDrawable ->
            vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
            val drawableWithTint = DrawableCompat.wrap(vectorDrawable)
            DrawableCompat.setTint(drawableWithTint, Color.RED)
            val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawableWithTint.draw(canvas)
            bitmap
        } ?: return null
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun showPermissionRationale(positiveAction: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Location permission")
            .setMessage("We need your permission to find your current position")
            .setPositiveButton(android.R.string.ok) { _, _ -> positiveAction() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create().show()
    }
}
