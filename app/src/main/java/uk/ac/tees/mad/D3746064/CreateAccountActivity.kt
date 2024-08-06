package uk.ac.tees.mad.D3746064

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

class CreateAccountActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            CreateAccountScreen(
                onSignUpClick = { email, password -> handleSignUp(email, password) },
                onSignInClick = { finish() } // Go back to LoginActivity
            )
        }
    }

    private fun handleSignUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    Toast.makeText(this, "Account created successfully.", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    // If sign up fails, display a message to the user.
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        // The email already exists, try to sign in
                        handleExistingAccount(email, password)
                    } else {
                        Toast.makeText(this, "Account creation failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun handleExistingAccount(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Logged in to existing account.", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    Toast.makeText(this, "Failed to log in to existing account: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}

@Composable
fun CreateAccountScreen(
    onSignUpClick: (String, String) -> Unit,
    onSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
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
            EmailTextField(email = email, onEmailChange = { email = it })
            Spacer(modifier = Modifier.height(16.dp))
            PasswordTextField(password = password, onPasswordChange = { password = it })
            Spacer(modifier = Modifier.height(24.dp))
            AuthButton(text = "Sign Up") { onSignUpClick(email, password) }
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





