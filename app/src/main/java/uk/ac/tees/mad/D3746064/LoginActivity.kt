package uk.ac.tees.mad.D3746064

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        setContent {
            var isLoading by remember { mutableStateOf(false) }

            LoginScreen(
                onSignInClick = { email, password ->
                    isLoading = true
                    handleSignIn(email, password) { isLoading = false }
                },
                onSignUpClick = { navigateToSignUp() },
                onGoogleSignInClick = { handleGoogleSignIn() },
                onFacebookSignInClick = { handleFacebookSignIn() },
                isLoading = isLoading
            )
        }
    }

    private fun handleSignIn(email: String, password: String, onComplete: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        fetchUserDataAndSave(it.uid)
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            }
    }

    private fun fetchUserDataAndSave(userId: String) {
        val userRef = database.reference.child("users").child(userId)
        userRef.get().addOnSuccessListener { dataSnapshot ->
            val userData = dataSnapshot.value as? Map<String, Any>
            userData?.let { data ->
                saveUserData(data)
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } ?: run {
                Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData(userData: Map<String, Any>) {
        with(sharedPreferences.edit()) {
            putString("name", userData["name"] as? String ?: "")
            putString("email", userData["email"] as? String ?: "")
            putString("countryCode", userData["countryCode"] as? String ?: "")
            putString("mobile", userData["mobile"] as? String ?: "")
            apply()
        }
    }

    private fun navigateToMainActivity() {
        var intent :Intent = Intent(this, MainActivity::class.java)
        intent.putExtra("showAuth","yes")

        startActivity(intent)
        finish()
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, CreateAccountActivity::class.java))
    }

    private fun handleGoogleSignIn() {
        Toast.makeText(this, "Google Sign-In not implemented", Toast.LENGTH_SHORT).show()
    }

    private fun handleFacebookSignIn() {
        Toast.makeText(this, "Facebook Sign-In not implemented", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

@Composable
fun LoginScreen(
    onSignInClick: (String, String) -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onFacebookSignInClick: () -> Unit,
    isLoading: Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Define colors
    val DeepsYellow = MaterialTheme.colorScheme.primary
    val DeepsBlack = Color(0xFF000000)
    val DeepsWhite = Color(0xFFFFFFFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepsWhite)
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
            Text("Sign In", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepsBlack)
            Spacer(modifier = Modifier.height(48.dp))
            EmailTextField(email = email, onEmailChange = { email = it })
            Spacer(modifier = Modifier.height(16.dp))
            PasswordTextField(password = password, onPasswordChange = { password = it })
            Spacer(modifier = Modifier.height(24.dp))
            AuthButton(
                text = "Sign In",
                onClick = { onSignInClick(email, password) },
                isLoading = isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Forgot Password?", color = DeepsYellow)
            Spacer(modifier = Modifier.height(24.dp))
            SocialSignInButtons(onGoogleClick = onGoogleSignInClick, onFacebookClick = onFacebookSignInClick)
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account?", color = DeepsBlack)
                TextButton(onClick = onSignUpClick) {
                    Text("Sign Up", color = DeepsYellow, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailTextField(email: String, onEmailChange: (String) -> Unit) {
    val DeepsYellow = MaterialTheme.colorScheme.primary
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = DeepsYellow,
            focusedLabelColor = DeepsYellow
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(password: String, onPasswordChange: (String) -> Unit) {
    val DeepsYellow = MaterialTheme.colorScheme.primary
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = DeepsYellow,
            focusedLabelColor = DeepsYellow
        )
    )
}

@Composable
fun AuthButton(text: String, onClick: () -> Unit, isLoading: Boolean) {
    val DeepsYellow = MaterialTheme.colorScheme.primary
    val DeepsBlack = Color(0xFF000000)
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DeepsYellow),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = DeepsBlack
            )
        } else {
            Text(text, color = DeepsBlack, fontSize = 16.sp)
        }
    }
}

@Composable
fun SocialSignInButtons(onGoogleClick: () -> Unit, onFacebookClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SocialButton(
            text = "Google",
            icon = R.drawable.google,
            onClick = onGoogleClick,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        SocialButton(
            text = "Facebook",
            icon = R.drawable.facebookicon,
            onClick = onFacebookClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SocialButton(
    text: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val DeepsBlack = Color(0xFF000000)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepsBlack)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(text)
        }
    }
}