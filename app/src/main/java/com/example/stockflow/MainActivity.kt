package com.example.stockflow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val dao = db.appDao()
        val session = SessionManager(this)

        // Seed product database if empty
        lifecycleScope.launch(Dispatchers.IO) {
            if (dao.getProductCountOnce() == 0) {
                seedDatabase(dao)
            }
        }

        // If already logged in, skip to Home
        if (session.isLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        setContent {
            StockFlowTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LoginScreen(dao) { email ->
                        session.saveLoginSession(email)
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(dao: AppDao, onLoginSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "StockFlow",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0)
        )
        Text(
            text = "Clothing Inventory",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Username / Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    if (isRegistering) {
                        // Register flow
                        val existing = dao.getUser(email)
                        if (existing != null) {
                            Toast.makeText(context, "User already exists!", Toast.LENGTH_SHORT).show()
                        } else {
                            dao.insertUser(User(email = email, password = password))
                            Toast.makeText(context, "Account created! Please log in.", Toast.LENGTH_SHORT).show()
                            isRegistering = false
                        }
                    } else {
                        // Login flow
                        val user = dao.getUser(email)
                        if (user != null && user.password == password) {
                            onLoginSuccess(user.email)
                        } else {
                            Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
        ) {
            Text(
                text = if (isRegistering) "Sign Up" else "Login",
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        TextButton(onClick = { isRegistering = !isRegistering }) {
            Text(
                text = if (isRegistering) "Back to Login" else "Create New Account",
                color = Color(0xFF1565C0)
            )
        }
    }
}

/** Light blue theme shared by the Compose screens (login and add/edit). */
@Composable
fun StockFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1565C0),
            onPrimary = Color.White,
            background = Color(0xFFF4F6FA),
            onBackground = Color(0xFF1F2937),
            surface = Color.White,
            onSurface = Color(0xFF1F2937),
            onSurfaceVariant = Color(0xFF6B7280)
        ),
        content = content
    )
}
