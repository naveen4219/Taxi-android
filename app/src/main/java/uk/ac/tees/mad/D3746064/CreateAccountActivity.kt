package uk.ac.tees.mad.D3746064

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase

class CreateAccountActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setContent {
            var isLoading by remember { mutableStateOf(false) }

            CreateAccountScreen(
                onSignUpClick = { name, email, countryCode, mobile, password ->
                    isLoading = true
                    handleSignUp(name, email, countryCode, mobile, password) {
                        isLoading = false
                    }
                },
                onSignInClick = { navigateToLogin() },
                isLoading = isLoading
            )
        }
    }

    private fun handleSignUp(name: String, email: String, countryCode: String, mobile: String, password: String, onComplete: () -> Unit) {
        if (!validateInputs(name, email, mobile, password)) {
            onComplete()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        val userRef = database.reference.child("users").child(userId)
                        val userData = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "countryCode" to countryCode,
                            "mobile" to mobile
                        )
                        userRef.setValue(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created successfully.", Toast.LENGTH_SHORT).show()
                                logoutAndNavigateToLogin()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                onComplete()
                            }
                    }
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        handleExistingAccount(email, password)
                    } else {
                        Toast.makeText(this, "Account creation failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                    onComplete()
                }
            }
    }

    private fun validateInputs(name: String, email: String, mobile: String, password: String): Boolean {
        if (name.length < 3) {
            Toast.makeText(this, "Name must be at least 3 characters long", Toast.LENGTH_SHORT).show()
            return false
        }
        if (mobile.length != 10 || !mobile.all { it.isDigit() }) {
            Toast.makeText(this, "Mobile number must be 10 digits", Toast.LENGTH_SHORT).show()
            return false
        }
        // Add more validations as needed (e.g., email format, password strength)
        return true
    }

    private fun handleExistingAccount(email: String, password: String) {
        // ... (same as before)
    }

    private fun logoutAndNavigateToLogin() {
        auth.signOut()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@Composable
fun CreateAccountScreen(
    onSignUpClick: (String, String, String, String, String) -> Unit,
    onSignInClick: () -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+44") } // Default to UK
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Image(
                painter = painterResource(id = R.drawable.logopng),
                contentDescription = "Deeps Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Create Account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                CountryCodeDropdown(
                    selectedCode = countryCode,
                    onCodeSelected = { countryCode = it },
                    modifier = Modifier.weight(0.3f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { if (it.length <= 10) mobile = it },
                    label = { Text("Mobile") },
                    singleLine = true,
                    modifier = Modifier.weight(0.7f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSignUpClick(name, email, countryCode, mobile, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Sign Up")
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", color = Color.Black)
                TextButton(onClick = onSignInClick) {
                    Text("Sign In", color = Color(0xFFFFA500), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CountryCodeDropdown(
    selectedCode: String,
    onCodeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val countryCodes = listOf("+44", "+91") // UK, India

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedCode,
            onValueChange = {},
            readOnly = true,
            label = { Text("Code") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, "Expand")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            countryCodes.forEach { code ->
                DropdownMenuItem(
                    text = { Text(code) },
                    onClick = {
                        onCodeSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}