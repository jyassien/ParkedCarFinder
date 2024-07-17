import androidx.core.app.ActivityCompat
import com.example.parkedcarfinder.Manifest
import com.example.parkedcarfinder.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap

private lateinit var fusedLocationClient: FusedLocationProviderClient
private lateinit var mMap: GoogleMap
private lateinit var btnParkedHere: Button
private val REQUEST_LOCATION_PERMISSION = 1

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_maps)

    btnParkedHere = findViewById(R.id.btn_parked_here)

    // Initialize the location client
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    val supportFragmentManager = null
    val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)

    btnParkedHere.setOnClickListener {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            getLastLocation()
        }
    }
}

override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        mMap.isMyLocationEnabled = true
    } else {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
    }
}

@SuppressLint("MissingPermission")
private fun getLastLocation() {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLocation = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                mMap.addMarker(MarkerOptions().position(currentLocation).title("I'm parked here"))
            }
        }
}

override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_LOCATION_PERMISSION) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        } else {
            // Show an explanation to the user
        }
    }
}
