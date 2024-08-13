package uk.ac.tees.mad.D3746064

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class FaqActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FAQScreen()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen() {
    val context = LocalContext.current
    val faqs = listOf(
        Pair("How do I book a parking space?", "To book a parking space, go to the 'Book' section in the app, select your desired location, date, and time, then follow the prompts to complete your booking."),
        Pair("Can I cancel my booking?", "Yes, you can cancel your booking up to 24 hours before the scheduled time. Go to 'My Bookings' and select the booking you wish to cancel."),
        Pair("What payment methods do you accept?", "We accept all major credit and debit cards, as well as PayPal."),
        Pair("Is my payment information secure?", "Yes, we use industry-standard encryption to protect your payment information."),
        Pair("What if I arrive late to my booking?", "If you're running late, please contact our customer support. We'll do our best to accommodate you, but we can't guarantee availability beyond your booked time."),
        Pair("How do I extend my parking time?", "If you need to extend your parking time, you can do so through the app by going to 'My Bookings' and selecting 'Extend Time' on your current booking, subject to availability."),
        Pair("What if I leave earlier than my booking end time?", "If you leave earlier than your booked time, unfortunately, we cannot offer refunds for unused time. However, you can always end your booking early through the app."),
        Pair("Is there a mobile app available?", "Yes, our mobile app is available for both iOS and Android devices. You can download it from the App Store or Google Play Store."),
        Pair("What if I can't find a parking space at my booked location?", "If you're unable to find a parking space at your booked location, please contact our customer support immediately. We'll assist you in finding an alternative or provide a refund if necessary."),
        Pair("How do I contact customer support?", "You can contact our customer support team through the 'Help' section in the app, or by emailing support@parkingapp.com. We aim to respond to all inquiries within 24 hours.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FAQ") },
                navigationIcon = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, ProfileActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            items(faqs) { (question, answer) ->
                FAQItem(question, answer)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = !expanded },
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = question, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Text(text = answer, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}