package uk.ac.tees.mad.D3746064

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class BookingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BookingsScreen()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this,ProfileActivity::class.java))
        finish()
    }
}

data class Booking(
    val id: String = "",
    val userId: String = "",
    val fromLat: Double = 0.0,
    val fromLng: Double = 0.0,
    val toLat: Double = 0.0,
    val toLng: Double = 0.0,
    val carType: String = "",
    val distance: Double = 0.0,
    val pricePerKm: Double = 0.0,
    val totalPrice: Double = 0.0,
    val driverName: String = "",
    val driverMobile: String = "",
    val timestamp: Long = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen() {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    var context = LocalContext.current

    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(key1 = Unit) {
        val database = FirebaseDatabase.getInstance().reference
        currentUser?.uid?.let { userId ->
            database.child("bookings").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val bookingsList = mutableListOf<Booking>()
                        for (bookingSnapshot in snapshot.children) {
                            val booking = bookingSnapshot.getValue(Booking::class.java)
                            booking?.let { bookingsList.add(it) }
                        }
                        bookings = bookingsList.sortedByDescending { it.timestamp }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Trips", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, ProfileActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF3F7F9))
        ) {
            items(bookings) { booking ->
                BookingItem(booking) {
                    selectedBooking = booking
                    showBottomSheet = true
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            selectedBooking?.let { booking ->
                TripDetailsBottomSheet(booking)
            }
        }
    }
}

@Composable
fun BookingItem(booking: Booking, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(booking.timestamp)),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Trip distance: ${String.format("%.2f", booking.distance)} km")
                }
            }
            Text(
                text = "$${String.format("%.2f", booking.totalPrice)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TripDetailsBottomSheet(booking: Booking) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng((booking.fromLat + booking.toLat) / 2, (booking.fromLng + booking.toLng) / 2),
                        10f
                    )
                }
            ) {
                Marker(
                    state = MarkerState(position = LatLng(booking.fromLat, booking.fromLng)),
                    title = "Start"
                )
                Marker(
                    state = MarkerState(position = LatLng(booking.toLat, booking.toLng)),
                    title = "End"
                )
                Polyline(
                    points = listOf(
                        LatLng(booking.fromLat, booking.fromLng),
                        LatLng(booking.toLat, booking.toLng)
                    ),
                    color = Color.Blue,
                    width = 5f
                )
            }
        }

        // Trip details
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Trip Details", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow("Date", SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(booking.timestamp)))
            DetailRow("Car Type", booking.carType)
            DetailRow("Distance", "${String.format("%.2f", booking.distance)} km")
            DetailRow("Price per km", "$${String.format("%.2f", booking.pricePerKm)}")
            DetailRow("Total Price", "$${String.format("%.2f", booking.totalPrice)}", isTotal = true)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Driver Information", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            DetailRow("Name", booking.driverName)
            DetailRow("Phone", booking.driverMobile)
        }
    }
}

