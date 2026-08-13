package com.mobile.sap.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.sap.ui.theme.*
import com.mobile.sap.ui.viewmodel.AuthUiState
import com.mobile.sap.ui.viewmodel.AuthViewModel

/**
 * Real username/password login backed by the muhfarming backend. Supports both
 * signing in to an existing account and self-service farmer sign-up. The role
 * (Admin/Farmer) is taken from the backend token response, not chosen here.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (role: String) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }

    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message

    LaunchedEffect(uiState) {
        (uiState as? AuthUiState.Success)?.let { onLoginSuccess(it.role) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FioriLightGray
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MuhFarming",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = FioriBlue,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Smart Agriculture Management",
                fontSize = 16.sp,
                color = FioriDarkGray,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Text(
                text = if (isSignUp) "Create your account" else "Sign in to continue",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = FioriBlack,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FioriBlue,
                focusedLabelColor = FioriBlue,
                unfocusedBorderColor = FioriGray,
                unfocusedLabelColor = FioriDarkGray,
                focusedTextColor = FioriBlack,
                unfocusedTextColor = FioriBlack
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; authViewModel.resetError() },
                label = { Text("Username") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; authViewModel.resetError() },
                label = { Text("Password") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = FioriDarkGray
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = FioriError,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isSignUp) authViewModel.signup(username, password)
                    else authViewModel.login(username, password)
                },
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FioriBlue,
                    contentColor = FioriWhite,
                    disabledContainerColor = FioriGray,
                    disabledContentColor = FioriDarkGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(22.dp),
                        color = FioriWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isSignUp) "Sign Up" else "Login",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    isSignUp = !isSignUp
                    authViewModel.resetError()
                },
                enabled = !isLoading,
                colors = ButtonDefaults.textButtonColors(contentColor = FioriBlue)
            ) {
                Text(
                    text = if (isSignUp) "Already have an account? Sign in"
                    else "New farmer? Create an account",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
