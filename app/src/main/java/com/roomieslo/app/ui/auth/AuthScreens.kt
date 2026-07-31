package com.roomieslo.app.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.roomieslo.app.ui.navigation.Destinations

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            navController.navigate(Destinations.LISTINGS) {
                popUpTo(Destinations.LOGIN) { inclusive = true }
            }
        }
    }
    LoginContent(
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onSignIn = { email, password -> viewModel.signIn(email, password) },
        onRegisterClick = { navController.navigate(Destinations.REGISTER) }
    )
}

@Composable
private fun LoginContent(
    isLoading: Boolean,
    errorMessage: String?,
    onSignIn: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("RoomieSlo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Poisci sostanovalca, ki mu res ustrezas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-naslov") },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Geslo") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        if (errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onSignIn(email, password) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Prijava")
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onRegisterClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("se nimas racuna? Registriraj se") }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginContent(isLoading = false, errorMessage = null, onSignIn = { _, _ -> }, onRegisterClick = {})
}

@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            navController.navigate(Destinations.ACADEMIC_VERIFICATION)
        }
    }
    RegisterContent(
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onSignUp = { name, email, password -> viewModel.signUp(name, email, password) }
    )
}

@Composable
private fun RegisterContent(
    isLoading: Boolean,
    errorMessage: String?,
    onSignUp: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Ustvari racun", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ime in priimek") },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-naslov (@student.uni-lj.si ali osebni)") },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Geslo") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        if (errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onSignUp(name, email, password) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Nadaljuj")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    RegisterContent(isLoading = false, errorMessage = null, onSignUp = { _, _, _ -> })
}

@Composable
fun AcademicVerificationScreen(
    navController: NavHostController,
    viewModel: AcademicVerificationViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) viewModel.upload(uri) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Preverjanje akademskega statusa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Nalozi potrdilo o vpisu (vpisnico), da drugi uporabniki vidijo, da si preverjen tuji student. To omogoca preverjanje statusa tudi pred uradnim zacetkom semestra.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Description, contentDescription = null)
                Spacer(Modifier.height(8.dp))
                Text(
                    when {
                        state.isUploading -> "Nalagam..."
                        state.isUploaded -> "Nalozeno: ${state.fileName ?: "vpisnica"} ✓"
                        state.fileName != null -> state.fileName
                        else -> "Ni izbrane datoteke"
                    }
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { picker.launch("*/*") }, enabled = !state.isUploading) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                    Text("  Izberi datoteko (PDF/JPG/PNG)")
                }
                if (state.errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate(Destinations.LIFESTYLE_QUESTIONNAIRE) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (state.isUploaded) "Nadaljuj" else "Poslji v preverjanje") }
        TextButton(onClick = { navController.navigate(Destinations.LIFESTYLE_QUESTIONNAIRE) }) {
            Text("Preskoci za zdaj")
        }
    }
}
