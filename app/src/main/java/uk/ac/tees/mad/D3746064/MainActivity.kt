package uk.ac.tees.mad.D3746064

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.colorResource
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
        startActivity(intent)
        finish()
    }
}

@Composable
fun MainScreen(onAnimationEnd: () -> Unit) {
    val lightYellow = colorResource(id = R.color.light_yellow)
    val mediumYellow = colorResource(id = R.color.medium_yellow)
    val darkYellow = colorResource(id = R.color.dark_yellow)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.White
            )
    ) {
        Column {
            // Title at the top
            Text(
                text = "Better Commute",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 8.dp)
                    .align(Alignment.CenterHorizontally),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.road_color)
            )

            // Existing animation content
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
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(Unit) {
        delay(2000)
        onAnimationEnd()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Road()
        Car(carPosition)
    }
}

@Composable
fun Road() {
    val roadColor = colorResource(id = R.color.road_color)
    val white = colorResource(id = R.color.white)

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
                color = white,
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
    val carColor = colorResource(id = R.color.car_color)
    val black = colorResource(id = R.color.black)

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val carWidth = 100f
        val carHeight = 60f
        val roadHeight = size.height / 4
        val carY = size.height - roadHeight - carHeight

        val path = Path().apply {
            moveTo(xPosition, carY + carHeight * 0.8f)
            lineTo(xPosition + carWidth * 0.2f, carY + carHeight * 0.8f)
            lineTo(xPosition + carWidth * 0.3f, carY + carHeight * 0.4f)
            lineTo(xPosition + carWidth * 0.7f, carY + carHeight * 0.4f)
            lineTo(xPosition + carWidth * 0.8f, carY + carHeight * 0.8f)
            lineTo(xPosition + carWidth, carY + carHeight * 0.8f)
            lineTo(xPosition + carWidth, carY + carHeight)
            lineTo(xPosition, carY + carHeight)
            close()
        }

        drawPath(path, carColor)

        // Wheels
        drawCircle(black, radius = 15f, center = Offset(xPosition + carWidth * 0.25f, carY + carHeight))
        drawCircle(black, radius = 15f, center = Offset(xPosition + carWidth * 0.75f, carY + carHeight))
    }
}