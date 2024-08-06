package uk.ac.tees.mad.D3746064

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import uk.ac.tees.mad.D3746064.data.DriverDetails
import uk.ac.tees.mad.D3746064.fragments.TaxiArrivedFragment
import uk.ac.tees.mad.D3746064.services.DriverApiService

class BookingConfirmationActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bookingDetails = intent.getParcelableExtra<BookingDetails>("BOOKING_DETAILS")

        setContent {
            MaterialTheme {
                BookingConfirmationScreen(bookingDetails, ::showTaxiArrivedFragment)
            }
        }
    }

    private fun showTaxiArrivedFragment() {
        val fragment = TaxiArrivedFragment()
        fragment.setOnDismissCallback {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        fragment.show(supportFragmentManager, "TaxiArrivedFragment")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreen(
    bookingDetails: BookingDetails?,
    showTaxiArrivedFragment: () -> Unit
) {
    var showCancelRideButton by remember { mutableStateOf(true) }
    var driverDetails by remember { mutableStateOf<DriverDetails?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableStateOf(0f) }

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        delay(20000) // 20 seconds
        showCancelRideButton = false
    }

    // Fetch driver details
    LaunchedEffect(key1 = Unit) {
        try {
            driverDetails = DriverApiService.driverApi.getDriverDetails()
            Log.d("API_RESPONSE", "Driver details: $driverDetails")
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error fetching driver details", e)
            errorMessage = "Error: ${e.message}"
        }
    }

    // Simulating driver approach
    LaunchedEffect(driverDetails) {
        driverDetails?.let { driver ->
            val totalTimeInMillis = driver.time * 60 * 1000 // Convert minutes to milliseconds
            val updateInterval = 1000L // Update every second
            val steps = totalTimeInMillis / updateInterval

            for (i in 1..steps.toInt()) {
                delay(updateInterval)
                progress = i.toFloat() / steps.toFloat()
            }
            showTaxiArrivedFragment()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F7F9))
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("BetterCommute", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { /* TODO: Open profile */ }) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.White)
        )

        bookingDetails?.let { details ->
            // Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(LatLng(details.fromLat, details.fromLng), 12f)
                    }
                ) {
                    Marker(
                        state = MarkerState(position = LatLng(details.fromLat, details.fromLng)),
                        title = "Pickup Location"
                    )
                    Marker(
                        state = MarkerState(position = LatLng(details.toLat, details.toLng)),
                        title = "Destination"
                    )
                    Polyline(
                        points = listOf(
                            LatLng(details.fromLat, details.fromLng),
                            LatLng(details.toLat, details.toLng)
                        ),
                        color = Color.Blue
                    )
                }
            }

            // Taxi Ordered Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Taxi Ordered", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    driverDetails?.let { driver ->
                        Text("Driver: ${driver.name}", color = Color.White)
                        Text("Phone: ${driver.mobileNumber}", color = Color.White)
                    } ?: Text("Fetching driver details...", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = animateFloatAsState(progress).value,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Yellow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    driverDetails?.let { driver ->
                        Text("Arriving in ${driver.time - (driver.time * progress).toInt()} minutes", color = Color.White)
                    }
                }
            }

            // Trip Details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow("Car Type", details.carType)
                    DetailRow("Distance", "${String.format("%.2f", details.distance)} km")
                    DetailRow("Price per km", "$${String.format("%.2f", details.pricePerKm)}")
                    DetailRow("Total Price", "$${details.totalPrice}", isTotal = true)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            Button(
                onClick = {
                    driverDetails?.mobileNumber?.let { number ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Call Driver")
            }
        } ?: Text("Booking details not available")

        // Display error message if any
        errorMessage?.let { error ->
            Text(error, color = Color.Red, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = if (isTotal) Color.Black else Color.Gray)
        Text(value, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal)
    }
}