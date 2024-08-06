package uk.ac.tees.mad.D3746064

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.compose.*
import com.google.maps.model.DirectionsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.firebase.database.FirebaseDatabase
import kotlinx.parcelize.Parcelize
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt

data class CarType(
    val type: String = "",
    val pricePerKm: Double = 0.0,
    val imageUrl: String = ""
)

data class RouteInfo(
    val polyline: List<LatLng>,
    val distanceInKm: Double
)


class HomeActivity : ComponentActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var geoApiContext: GeoApiContext

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationPermission()

        val apiKey = getApiKey()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        placesClient = Places.createClient(this)

        geoApiContext = GeoApiContext.Builder()
            .apiKey(apiKey)
            .build()

        setContent {
            HomeScreen(placesClient, geoApiContext)
        }
    }

    private fun getApiKey(): String {
        val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return applicationInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(placesClient: PlacesClient, geoApiContext: GeoApiContext) {
    var fromLocation by remember { mutableStateOf("") }
    var toLocation by remember { mutableStateOf("") }
    var selectedCar by remember { mutableStateOf<CarType?>(null) }
    var fromPredictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var toPredictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var fromLatLng by remember { mutableStateOf<LatLng?>(null) }
    var toLatLng by remember { mutableStateOf<LatLng?>(null) }
    var routeInfo by remember { mutableStateOf<RouteInfo?>(null) }
    var carTypes by remember { mutableStateOf<List<CarType>>(emptyList()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.7128, -74.0060), 12f)
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Fetch car types from Firebase
    LaunchedEffect(Unit) {
        carTypes = fetchCarTypes()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = MapType.NORMAL)
        ) {
            fromLatLng?.let { latLng ->
                Marker(
                    state = MarkerState(position = latLng),
                    title = "From"
                )
            }

            routeInfo?.let { route ->
                Polyline(
                    points = route.polyline,
                    color = Color.Blue,
                    width = 5f
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("BetterCommute", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* TODO: Open profile */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = fromLocation,
                    onValueChange = {
                        fromLocation = it
                        coroutineScope.launch {
                            fromPredictions = getPlacePredictions(placesClient, it)
                        }
                    },
                    label = { Text("From") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
                )
                if (fromPredictions.isNotEmpty()) {
                    LazyColumn {
                        items(fromPredictions) { prediction ->
                            Text(
                                text = prediction.getFullText(null).toString(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        fromLocation = prediction.getFullText(null).toString()
                                        fromPredictions = emptyList()
                                        coroutineScope.launch {
                                            fromLatLng = getPlaceLatLng(placesClient, prediction.placeId)
                                            fromLatLng?.let { latLng ->
                                                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                                            }
                                            routeInfo = null // Clear existing route
                                            selectedCar = null // Clear selected car
                                        }
                                    }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = toLocation,
                    onValueChange = {
                        toLocation = it
                        coroutineScope.launch {
                            toPredictions = getPlacePredictions(placesClient, it)
                        }
                    },
                    label = { Text("To") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
                )
                if (toPredictions.isNotEmpty()) {
                    LazyColumn {
                        items(toPredictions) { prediction ->
                            Text(
                                text = prediction.getFullText(null).toString(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        toLocation = prediction.getFullText(null).toString()
                                        toPredictions = emptyList()
                                        coroutineScope.launch {
                                            toLatLng = getPlaceLatLng(placesClient, prediction.placeId)
                                            if (fromLatLng != null && toLatLng != null) {
                                                routeInfo = getRoute(geoApiContext, fromLatLng!!, toLatLng!!)
                                                // Adjust camera to show the entire route
                                                val bounds = LatLngBounds.builder()
                                                    .include(fromLatLng!!)
                                                    .include(toLatLng!!)
                                                    .build()
                                                cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                                            }
                                            selectedCar = null // Clear selected car
                                        }
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(carTypes) { carType ->
                    CarCard(
                        carType = carType,
                        isSelected = carType == selectedCar,
                        onSelect = { selectedCar = carType },
                        routeDistance = routeInfo?.distanceInKm
                    )
                }
            }

            Button(
                onClick = {
                    if (selectedCar != null && routeInfo != null && fromLatLng != null && toLatLng != null) {
                        val totalPrice = (selectedCar!!.pricePerKm * routeInfo!!.distanceInKm).roundToInt()
                        val bookingDetails = BookingDetails(
                            fromLocation = fromLocation,
                            toLocation = toLocation,
                            fromLat = fromLatLng!!.latitude,
                            fromLng = fromLatLng!!.longitude,
                            toLat = toLatLng!!.latitude,
                            toLng = toLatLng!!.longitude,
                            carType = selectedCar!!.type,
                            distance = routeInfo!!.distanceInKm,
                            pricePerKm = selectedCar!!.pricePerKm,
                            totalPrice = totalPrice
                        )
                        val intent = Intent(context, BookingConfirmationActivity::class.java).apply {
                            putExtra("BOOKING_DETAILS", bookingDetails)
                        }
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = selectedCar != null && routeInfo != null && fromLatLng != null && toLatLng != null
            ) {
                Text("BOOK NOW")
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarCard(carType: CarType, isSelected: Boolean, onSelect: () -> Unit, routeDistance: Double?) {
    val totalPrice = if (routeDistance != null) {
        (carType.pricePerKm * routeDistance).roundToInt()
    } else null

    Card(
        modifier = Modifier
            .width(120.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = carType.imageUrl,
                contentDescription = "Car Image",
                modifier = Modifier
                    .size(60.dp)
                    .padding(bottom = 4.dp),
                contentScale = ContentScale.Fit
            )
            Text(carType.type, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text("$${carType.pricePerKm}/km", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (totalPrice != null) {
                Text("Total: $$totalPrice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

suspend fun fetchCarTypes(): List<CarType> {
    val database = FirebaseDatabase.getInstance()
    val carTypesRef = database.getReference("car_types")

    return try {
        val snapshot = carTypesRef.get().await()
        snapshot.children.mapNotNull { it.getValue(CarType::class.java) }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun getPlacePredictions(placesClient: PlacesClient, query: String): List<AutocompletePrediction> {
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()

    return suspendCancellableCoroutine { continuation ->
        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            continuation.resume(response.autocompletePredictions)
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }
}

// ... (previous code remains the same)

suspend fun getPlaceLatLng(placesClient: PlacesClient, placeId: String): LatLng? {
    val placeFields = listOf(Place.Field.LAT_LNG)
    val request = FetchPlaceRequest.newInstance(placeId, placeFields)

    return suspendCancellableCoroutine { continuation ->
        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            continuation.resume(response.place.latLng)
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }
}

suspend fun getRoute(geoApiContext: GeoApiContext, origin: LatLng, destination: LatLng): RouteInfo {
    return withContext(Dispatchers.IO) {
        try {
            val result: DirectionsResult = DirectionsApi.newRequest(geoApiContext)
                .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .await()

            if (result.routes.isNotEmpty() && result.routes[0].legs.isNotEmpty()) {
                val route = result.routes[0]
                val leg = route.legs[0]
                val polyline = route.overviewPolyline.decodePath().map { LatLng(it.lat, it.lng) }
                val distanceInKm = leg.distance.inMeters / 1000.0

                RouteInfo(polyline, distanceInKm)
            } else {
                RouteInfo(emptyList(), 0.0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            RouteInfo(emptyList(), 0.0)
        }
    }
}


@Parcelize
data class BookingDetails(
    val fromLocation: String,
    val toLocation: String,
    val fromLat: Double,
    val fromLng: Double,
    val toLat: Double,
    val toLng: Double,
    val carType: String,
    val distance: Double,
    val pricePerKm: Double,
    val totalPrice: Int
) : Parcelable



