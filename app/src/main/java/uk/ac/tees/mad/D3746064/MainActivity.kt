package uk.ac.tees.mad.D3746064

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            FirebaseApp.initializeApp(this)
            auth = FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to initialize Firebase", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
            return
        }

        setContent {
            MaterialTheme {
                MainScreen(
                    onAnimationEnd = {
                        checkAuthAndRedirect()
                    }
                )
            }
        }
    }

    private fun checkAuthAndRedirect() {
        val intent = if (auth.currentUser != null) {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        intent.putExtra("showAuth", "yes")
        startActivity(intent)
        finish()
    }
}

@Composable
fun MainScreen(onAnimationEnd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo and Title at the top
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logopng),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(120.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Better Commute",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "Your Ride, Your Way",
                    fontSize = 18.sp,
                    color = Color(0xFF666666)
                )
            }

            // Animated city and car
            Box(modifier = Modifier.weight(1f)) {
                IntroScreen(onAnimationEnd)
            }
        }
    }
}

@Composable
fun IntroScreen(onAnimationEnd: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()

    val carPosition by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val cityOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(Unit) {
        delay(3000) // Run the animation for 3 seconds
        onAnimationEnd()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CityBackground(cityOffset)
        Road()
        Car(carPosition)
    }
}

@Composable
fun CityBackground(offset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val buildingColor = Color(0xFFE0E0E0)
        val windowColor = Color(0xFFCCCCCC)

        // Draw moving buildings
        for (i in 0..8) {
            val buildingWidth = size.width / 4
            val buildingHeight = (size.height * (0.3f + (i % 3) * 0.1f))
            val buildingX = (i * buildingWidth + offset) % (size.width + buildingWidth) - buildingWidth

            drawRect(
                color = buildingColor,
                topLeft = Offset(buildingX, size.height - buildingHeight),
                size = androidx.compose.ui.geometry.Size(buildingWidth, buildingHeight)
            )

            // Draw windows
            for (row in 0..4) {
                for (col in 0..2) {
                    drawRect(
                        color = windowColor,
                        topLeft = Offset(buildingX + col * 30 + 15, size.height - buildingHeight + row * 40 + 20),
                        size = androidx.compose.ui.geometry.Size(20f, 30f)
                    )
                }
            }
        }
    }
}

@Composable
fun Road() {
    val roadColor = Color.Black
    val lineColor = Color(0xFFAAAAAA)

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val roadHeight = size.height / 4
        val roadY = size.height - roadHeight

        drawRect(
            color = roadColor,
            topLeft = Offset(0f, roadY),
            size = size.copy(height = roadHeight)
        )

        val lineWidth = 50f
        val lineSpacing = 100f
        var startX = 0f

        while (startX < size.width) {
            drawLine(
                color = lineColor,
                start = Offset(startX, roadY + roadHeight / 2),
                end = Offset(startX + lineWidth, roadY + roadHeight / 2),
                strokeWidth = 10f
            )
            startX += lineWidth + lineSpacing
        }
    }
}

@Composable
fun Car(xPosition: Float) {
    val carColor = Color(0xFFE74C3C)
    val wheelColor = Color(0xFF333333)

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val carWidth = 120f
        val carHeight = 70f
        val roadHeight = size.height / 4
        val carY = size.height - roadHeight - carHeight + 10f

        val path = Path().apply {
            moveTo(xPosition, carY + carHeight * 0.7f)
            lineTo(xPosition + carWidth * 0.2f, carY + carHeight * 0.7f)
            lineTo(xPosition + carWidth * 0.3f, carY + carHeight * 0.3f)
            lineTo(xPosition + carWidth * 0.7f, carY + carHeight * 0.3f)
            lineTo(xPosition + carWidth * 0.8f, carY + carHeight * 0.7f)
            lineTo(xPosition + carWidth, carY + carHeight * 0.7f)
            lineTo(xPosition + carWidth, carY + carHeight)
            lineTo(xPosition, carY + carHeight)
            close()
        }

        drawPath(path, carColor)

        // Wheels
        drawCircle(wheelColor, radius = 18f, center = Offset(xPosition + carWidth * 0.25f, carY + carHeight))
        drawCircle(wheelColor, radius = 18f, center = Offset(xPosition + carWidth * 0.75f, carY + carHeight))
    }
}