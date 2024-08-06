package uk.ac.tees.mad.D3746064
import android.content.Intent
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

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContent {
            LoginScreen(
                onSignInClick = { email, password -> handleSignIn(email, password) },
                onSignUpClick = { navigateToSignUp() },
                onGoogleSignInClick = { handleGoogleSignIn() },
                onFacebookSignInClick = { handleFacebookSignIn() }
            )
        }
    }

    private fun handleSignIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    Toast.makeText(this, "Authentication successful.", Toast.LENGTH_SHORT).show()
                    // Navigate to main activity or home screen
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, CreateAccountActivity::class.java))
    }

    private fun handleGoogleSignIn() {
        // Implement Google sign-in
        Toast.makeText(this, "Unable to Login", Toast.LENGTH_SHORT).show()
        // You would typically:
        // 1. Configure Google Sign-In
        // 2. Start the sign-in intent
        // 3. Handle the result in onActivityResult
    }

    private fun handleFacebookSignIn() {
        // Implement Facebook sign-in
        Toast.makeText(this, "Unable to Login", Toast.LENGTH_SHORT).show()
        // You would typically:
        // 1. Configure Facebook SDK
        // 2. Call LoginManager.getInstance().logInWithReadPermissions()
        // 3. Handle the result in onActivityResult
    }
}

// Define colors
val DeepsYellow = Color(0xFFFFA500)
val DeepsBlack = Color(0xFF000000)
val DeepsWhite = Color(0xFFFFFFFF)

@Composable
fun LoginScreen(
    onSignInClick: (String, String) -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onFacebookSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
            AuthButton(text = "Sign In") { onSignInClick(email, password) }
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
fun AuthButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DeepsYellow)
    ) {
        Text(text, color = DeepsBlack, fontSize = 16.sp)
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