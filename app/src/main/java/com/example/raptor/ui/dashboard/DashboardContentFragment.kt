package com.example.raptor.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.raptor.R
import com.example.raptor.models.DeliveryOrder
import com.example.raptor.models.DriverLocation
import com.example.raptor.network.NetworkModule
import com.example.raptor.repositories.OrderRepository
import com.example.raptor.ui.orders.CreateOrderActivity
import com.example.raptor.ui.orders.OrderDetailActivity
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
 * Dashboard Content Fragment
 * Displays map, orders, drivers, and flashcard for accepted orders
 */
class DashboardContentFragment : Fragment(), OnMapReadyCallback {

    private lateinit var loadingContainer: View
    private lateinit var errorContainer: View
    private lateinit var emptyContainer: View
    private lateinit var orderCardsContainer: View
    private lateinit var activeOrdersList: LinearLayout
    private lateinit var emptyOrdersContainer: View
    private lateinit var activeOrderCardContainer: ViewGroup
    private lateinit var flashcardContainer: FrameLayout
    private lateinit var retryButton: Button
    private lateinit var errorMessage: TextView
    private lateinit var activeOrdersCount: TextView

    // Map
    private var googleMap: GoogleMap? = null
    private var driverMarkers: MutableMap<String, Marker> = mutableMapOf()
    private var routePolyline: Polyline? = null
    private var routePolylineBackground: Polyline? = null
    private var destinationMarker: Marker? = null
    private var driverLocations: MutableMap<String, DriverLocation> = mutableMapOf()
    private var locationUpdateJob: Job? = null

    // Accepted order tracking
    private var trackedOrder: DeliveryOrder? = null
    private var trackedDriverEmail: String? = null
    private var showFlashcard: Boolean = false

    private val orderRepository = OrderRepository()
    private var customerEmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerEmail = arguments?.getString("customer_email")

        // Initialize views
        loadingContainer = view.findViewById(R.id.loadingContainer)
        errorContainer = view.findViewById(R.id.errorContainer)
        emptyContainer = view.findViewById(R.id.emptyContainer)
        orderCardsContainer = view.findViewById(R.id.orderCardsContainer)
        activeOrdersList = view.findViewById(R.id.activeOrdersList)
        emptyOrdersContainer = view.findViewById(R.id.emptyOrdersContainer)
        activeOrderCardContainer = view.findViewById(R.id.activeOrderCardContainer)
        flashcardContainer = view.findViewById(R.id.flashcardContainer)
        retryButton = view.findViewById(R.id.retryButton)
        errorMessage = view.findViewById(R.id.errorMessage)
        activeOrdersCount = view.findViewById(R.id.activeOrdersCount)

        // Setup map
        setupMap()

        // Setup retry button
        retryButton.setOnClickListener {
            loadDashboardData()
        }

        // Setup empty orders container (create order button)
        emptyOrdersContainer.setOnClickListener {
            openCreateOrder()
        }

        // Initial load
        showLoadingState()
        loadDashboardData()
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Set default location (Amsterdam center)
        val amsterdam = LatLng(52.3676, 4.9041)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(amsterdam, 12f))

        // Enable location button if permission granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }

        // Load driver locations
        loadDriverLocations()

        // Start location updates
        startLocationUpdates()
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                // Load orders for customer
                customerEmail?.let { email ->
                    val result = orderRepository.getOrdersByCustomer(email)
                    val orders = result.getOrNull()

                    if (orders != null && orders.isNotEmpty()) {
                        // Check for accepted orders
                        val acceptedOrders = orders.filter { 
                            it.status.lowercase() == "assigned" || 
                            it.status.lowercase() == "in_progress" ||
                            it.status.lowercase() == "inprogress"
                        }

                        if (acceptedOrders.isNotEmpty()) {
                            val latestAccepted = acceptedOrders.first()
                            showAcceptedOrderFlashcard(latestAccepted)
                            trackOrderOnMap(latestAccepted)
                        }

                        showContent()
                        populateOrderCards(orders)
                    } else {
                        showEmptyState()
                    }
                } ?: run {
                    showEmptyState()
                }
            } catch (e: Exception) {
                showErrorState(e.message ?: "Onbekende fout")
            }
        }
    }

    private fun showAcceptedOrderFlashcard(order: DeliveryOrder) {
        if (showFlashcard) return // Already showing

        showFlashcard = true
        trackedOrder = order
        trackedDriverEmail = order.assignedDriverEmail

        // Inflate flashcard
        val flashcardView = LayoutInflater.from(context).inflate(R.layout.flashcard_accepted_order, null)
        
        // Populate flashcard
        flashcardView.findViewById<TextView>(R.id.flashcardDriverName).text = 
            order.assignedDriverEmail ?: "Bezorger"
        flashcardView.findViewById<TextView>(R.id.flashcardDestination).text = order.destinationAddress

        // Setup buttons
        flashcardView.findViewById<Button>(R.id.flashcardViewDetailsButton).setOnClickListener {
            val intent = Intent(context, OrderDetailActivity::class.java).apply {
                putExtra("order_id", order.orderId)
                putExtra("customer_email", customerEmail)
            }
            startActivity(intent)
        }

        flashcardView.findViewById<Button>(R.id.flashcardDismissButton).setOnClickListener {
            hideFlashcard()
        }

        // Add flashcard to container
        flashcardContainer.removeAllViews()
        flashcardContainer.addView(flashcardView)

        // Animate in
        val slideIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
        flashcardView.startAnimation(slideIn)
        flashcardContainer.visibility = View.VISIBLE
    }

    private fun hideFlashcard() {
        showFlashcard = false
        flashcardContainer.visibility = View.GONE
        flashcardContainer.removeAllViews()
    }

    private fun trackOrderOnMap(order: DeliveryOrder) {
        trackedOrder = order
        trackedDriverEmail = order.assignedDriverEmail

        // Update map with route
        updateMapWithRoute()
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
                // Silent fail - will retry
            } catch (e: IOException) {
                // Silent fail - will retry
            }
        }
    }

    private fun updateMapMarkers() {
        googleMap?.let { map ->
            // Clear existing markers
            driverMarkers.values.forEach { it.remove() }
            driverMarkers.clear()
            routePolyline?.remove()
            routePolylineBackground?.remove()
            destinationMarker?.remove()

            // Add driver markers
            driverLocations.forEach { (email, location) ->
                val position = LatLng(location.latitude, location.longitude)
                val isTracked = email == trackedDriverEmail

                val marker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("Bezorger")
                        .snippet(if (isTracked) "Wordt getrackt" else "Beschikbaar")
                )

                if (marker != null) {
                    driverMarkers[email] = marker
                }
            }

            // Update route if tracking order
            if (trackedOrder != null && trackedDriverEmail != null) {
                updateMapWithRoute()
            }
        }
    }

    private fun updateMapWithRoute() {
        trackedOrder?.let { order ->
            trackedDriverEmail?.let { driverEmail ->
                val driverLocation = driverLocations[driverEmail]
                if (driverLocation != null) {
                    showRouteForOrder(order, driverLocation)
                }
            }
        }
    }

    private fun showRouteForOrder(order: DeliveryOrder, driverLocation: DriverLocation) {
        lifecycleScope.launch {
            try {
                val geocoder = Geocoder(requireContext())

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
                        destinationMarker?.remove()
                        destinationMarker = map.addMarker(
                            MarkerOptions()
                                .position(targetLatLng)
                                .title(if (order.pickedUp == true) "Bestemming" else "Ophaaladres")
                        )

                        // Draw route line with white background and gold foreground (matching iOS)
                        routePolylineBackground?.remove()
                        routePolyline?.remove()
                        
                        // White background line (wider)
                        val backgroundOptions = PolylineOptions()
                            .add(LatLng(driverLocation.latitude, driverLocation.longitude))
                            .add(targetLatLng)
                            .color(ContextCompat.getColor(requireContext(), android.R.color.white))
                            .width(10f)
                            .zIndex(1f)
                        
                        routePolylineBackground = map.addPolyline(backgroundOptions)
                        
                        // Gold foreground line (narrower, on top)
                        val goldOptions = PolylineOptions()
                            .add(LatLng(driverLocation.latitude, driverLocation.longitude))
                            .add(targetLatLng)
                            .color(ContextCompat.getColor(requireContext(), R.color.gold))
                            .width(8f)
                            .zIndex(2f)
                        
                        routePolyline = map.addPolyline(goldOptions)

                        // Focus camera on route
                        val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                            .include(LatLng(driverLocation.latitude, driverLocation.longitude))
                            .include(targetLatLng)
                            .build()

                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    }
                }
            } catch (e: Exception) {
                // Geocoding failed - silently continue
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

    private fun populateOrderCards(orders: List<DeliveryOrder>) {
        // Clear existing cards
        activeOrdersList.removeAllViews()
        activeOrderCardContainer.removeAllViews()

        // Filter active orders
        val activeOrders = orders.filter { 
            it.status.lowercase() == "assigned" || 
            it.status.lowercase() == "in_progress" ||
            it.status.lowercase() == "inprogress"
        }

        // Show first order as active order card (if exists and not showing flashcard)
        if (activeOrders.isNotEmpty() && !showFlashcard) {
            val firstOrder = activeOrders[0]
            val activeCard = createActiveOrderCard(firstOrder)
            activeOrderCardContainer.addView(activeCard)
        }

        // Show remaining orders in list
        val remainingOrders = if (activeOrders.size > 1) activeOrders.subList(1, activeOrders.size) else emptyList()
        
        if (remainingOrders.isNotEmpty()) {
            activeOrdersList.visibility = View.VISIBLE
            emptyOrdersContainer.visibility = View.GONE
            
            // Update count badge in bottom sheet
            activeOrdersCount.text = remainingOrders.size.toString()
            activeOrdersCount.visibility = View.VISIBLE
            
            remainingOrders.forEach { order ->
                val orderCard = createOrderCardItem(order)
                activeOrdersList.addView(orderCard)
            }
        } else {
            activeOrdersList.visibility = View.GONE
            emptyOrdersContainer.visibility = View.VISIBLE
            activeOrdersCount.visibility = View.GONE
        }
    }

    private fun createActiveOrderCard(order: DeliveryOrder): View {
        val inflater = LayoutInflater.from(context)
        val cardView = inflater.inflate(R.layout.item_active_order_card, null)

        // Populate card with order data
        cardView.findViewById<TextView>(R.id.driverName).text = order.assignedDriverEmail ?: "Bezorger"
        cardView.findViewById<TextView>(R.id.driverNameFull).text = order.assignedDriverEmail ?: "Bezorger"
        cardView.findViewById<TextView>(R.id.orderId).text = "Order #${order.orderId.take(8)}"
        cardView.findViewById<TextView>(R.id.destinationAddress).text = order.destinationAddress
        cardView.findViewById<TextView>(R.id.statusText).text = getStatusText(order.status)
        
        // Set status color and badge background
        val statusIndicator = cardView.findViewById<View>(R.id.statusIndicator)
        val statusBadge = cardView.findViewById<LinearLayout>(R.id.statusBadge)
        val statusText = cardView.findViewById<TextView>(R.id.statusText)
        
        when (order.status.lowercase()) {
            "assigned" -> {
                statusIndicator.setBackgroundResource(R.drawable.status_indicator)
                statusBadge.setBackgroundResource(R.drawable.status_badge_background_blue)
                statusText.setTextColor(resources.getColor(R.color.primary_blue, null))
            }
            "inprogress", "in-progress" -> {
                statusIndicator.setBackgroundResource(R.drawable.status_indicator_gold)
                statusBadge.setBackgroundResource(R.drawable.status_badge_background_gold)
                statusText.setTextColor(resources.getColor(R.color.gold, null))
            }
            else -> {
                statusIndicator.setBackgroundResource(R.drawable.status_indicator)
                statusBadge.setBackgroundResource(R.drawable.status_badge_background)
                statusText.setTextColor(resources.getColor(R.color.primary_blue, null))
            }
        }

        // Setup chat button
        cardView.findViewById<View>(R.id.chatButton).setOnClickListener {
            // TODO: Open chat
        }

        return cardView
    }

    private fun createOrderCardItem(order: DeliveryOrder): View {
        return createActiveOrderCard(order)
    }

    private fun getStatusText(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "Wachtend"
            "assigned" -> "Geaccepteerd"
            "inprogress", "in-progress" -> "Onderweg"
            "completed" -> "Voltooid"
            "cancelled" -> "Geannuleerd"
            else -> status.capitalize()
        }
    }

    private fun openCreateOrder() {
        val intent = Intent(context, CreateOrderActivity::class.java)
        startActivity(intent)
    }

    private fun showLoadingState() {
        loadingContainer.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        emptyContainer.visibility = View.GONE
        orderCardsContainer.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.VISIBLE
        emptyContainer.visibility = View.GONE
        orderCardsContainer.visibility = View.GONE
        errorMessage.text = message
    }

    private fun showEmptyState() {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.GONE
        emptyContainer.visibility = View.VISIBLE
        orderCardsContainer.visibility = View.GONE
    }

    private fun showContent() {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.GONE
        emptyContainer.visibility = View.GONE
        orderCardsContainer.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationUpdateJob?.cancel()
    }
}
