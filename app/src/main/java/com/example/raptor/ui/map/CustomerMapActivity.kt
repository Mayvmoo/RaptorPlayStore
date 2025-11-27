package com.example.raptor.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.raptor.R
import com.example.raptor.models.DeliveryOrder
import com.example.raptor.models.DriverLocation
import com.example.raptor.network.NetworkModule
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Customer Map Activity
 * Komt overeen met iOS CustomerMapView
 */
class CustomerMapActivity : AppCompatActivity(), OnMapReadyCallback {
    
    private var googleMap: GoogleMap? = null
    private var driverMarkers: MutableMap<String, Marker> = mutableMapOf()
    private var routePolyline: Polyline? = null
    private var destinationMarker: Marker? = null
    
    private var trackedOrder: DeliveryOrder? = null
    private var trackedDriverEmail: String? = null
    private var driverLocations: MutableMap<String, DriverLocation> = mutableMapOf()
    
    private var locationUpdateJob: Job? = null
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupMap()
        } else {
            Toast.makeText(this, "Locatie toestemming nodig voor kaart", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_map)
        
        trackedOrder = intent.getSerializableExtra("tracked_order") as? DeliveryOrder
        trackedDriverEmail = intent.getStringExtra("tracked_driver_email")
        
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        
        // Check location permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupMap()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Enable location button
        try {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
        } catch (e: SecurityException) {
            // Permission not granted
        }
        
        // Set default location (Amsterdam)
        val amsterdam = LatLng(52.3676, 4.9041)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(amsterdam, 12f))
        
        // Load driver locations
        loadDriverLocations()
        
        // Start location updates
        startLocationUpdates()
    }
    
    private fun setupMap() {
        googleMap?.let { map ->
            try {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }
    }
    
    private fun loadDriverLocations() {
        lifecycleScope.launch {
            try {
                val response = NetworkModule.apiService.getDriverLocations()
                if (response.isSuccessful && response.body() != null) {
                    val locations = response.body()!!
                    driverLocations.clear()
                    locations.forEach { location ->
                        driverLocations[location.driverEmail] = location
                    }
                    updateMapMarkers()
                }
            } catch (e: HttpException) {
                Toast.makeText(this@CustomerMapActivity, "Kon driver locaties niet ophalen", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(this@CustomerMapActivity, "Netwerk fout", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateMapMarkers() {
        googleMap?.let { map ->
            // Clear existing markers
            driverMarkers.values.forEach { it.remove() }
            driverMarkers.clear()
            routePolyline?.remove()
            destinationMarker?.remove()
            
            // Add driver markers
            driverLocations.forEach { (email, location) ->
                val position = LatLng(location.latitude, location.longitude)
                val isTracked = email == trackedDriverEmail
                
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("Bezorger: $email")
                        .snippet(if (isTracked) "Wordt getrackt" else "Beschikbaar")
                )
                
                if (marker != null) {
                    driverMarkers[email] = marker
                }
            }
            
            // If tracking an order, show route
            trackedOrder?.let { order ->
                trackedDriverEmail?.let { driverEmail ->
                    val driverLocation = driverLocations[driverEmail]
                    if (driverLocation != null) {
                        showRouteForOrder(order, driverLocation)
                    }
                }
            }
        }
    }
    
    private fun showRouteForOrder(order: DeliveryOrder, driverLocation: DriverLocation) {
        lifecycleScope.launch {
            try {
                val geocoder = Geocoder(this@CustomerMapActivity)
                
                // Determine target address based on pickup status
                val targetAddress = if (order.pickedUp == true) {
                    order.destinationAddress
                } else {
                    order.senderAddress
                }
                
                // Geocode target address
                val addresses = geocoder.getFromLocationName(targetAddress, 1)
                if (addresses?.isNotEmpty() == true) {
                    val targetLocation = addresses[0]
                    val targetLatLng = LatLng(
                        targetLocation.latitude,
                        targetLocation.longitude
                    )
                    
                    // Add destination marker
                    googleMap?.let { map ->
                        destinationMarker = map.addMarker(
                            MarkerOptions()
                                .position(targetLatLng)
                                .title(if (order.pickedUp == true) "Bestemming" else "Ophaaladres")
                        )
                        
                        // Draw route line (simplified - in production use Directions API)
                        val routeOptions = PolylineOptions()
                            .add(LatLng(driverLocation.latitude, driverLocation.longitude))
                            .add(targetLatLng)
                            .color(ContextCompat.getColor(this@CustomerMapActivity, android.R.color.holo_blue_dark))
                            .width(8f)
                        
                        routePolyline = map.addPolyline(routeOptions)
                        
                        // Focus camera on route
                        val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                            .include(LatLng(driverLocation.latitude, driverLocation.longitude))
                            .include(targetLatLng)
                            .build()
                        
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomerMapActivity, "Kon route niet tonen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startLocationUpdates() {
        locationUpdateJob?.cancel()
        locationUpdateJob = lifecycleScope.launch {
            while (true) {
                delay(15000) // 15 seconds
                loadDriverLocations()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        locationUpdateJob?.cancel()
    }
}

