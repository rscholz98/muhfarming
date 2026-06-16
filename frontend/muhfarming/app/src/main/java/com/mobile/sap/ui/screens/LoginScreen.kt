package com.mobile.sap.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.sap.ui.theme.*

@Composable
fun LoginScreen(
    onAdminLogin: () -> Unit,
    onFarmerLogin: () -> Unit
) {
    var showAdminDialog by remember { mutableStateOf(false) }

    if (showAdminDialog) {
        AdminLoginDialog(
            onDismiss = { showAdminDialog = false },
            onLoginSuccess = {
                showAdminDialog = false
                onAdminLogin()
            }
        )
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
            // App Logo/Title
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
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Role Selection Title
            Text(
                text = "Select Your Role",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = FioriBlack,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Admin Card
            RoleCard(
                icon = Icons.Outlined.AdminPanelSettings,
                title = "Administrator",
                description = "Manage system, users, and configurations",
                onClick = { showAdminDialog = true },
                backgroundColor = FioriBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Farmer Card
            RoleCard(
                icon = Icons.Outlined.Agriculture,
                title = "Farmer",
                description = "Monitor weather and manage pests",
                onClick = onFarmerLogin,
                backgroundColor = FioriSuccess
            )
        }
    }
}

@Composable
private fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AdminPanelSettings,
                    contentDescription = "Admin",
                    tint = FioriBlue,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Administrator Login",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FioriBlack
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = null
                    },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FioriBlue,
                        focusedLabelColor = FioriBlue,
                        unfocusedBorderColor = FioriGray,
                        unfocusedLabelColor = FioriDarkGray,
                        focusedTextColor = FioriBlack,
                        unfocusedTextColor = FioriBlack
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    singleLine = true,
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FioriBlue,
                        focusedLabelColor = FioriBlue,
                        unfocusedBorderColor = FioriGray,
                        unfocusedLabelColor = FioriDarkGray,
                        focusedTextColor = FioriBlack,
                        unfocusedTextColor = FioriBlack
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = FioriError,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username == "admin_alex" && password == "k1ngAdm1n.567") {
                        onLoginSuccess()
                    } else {
                        errorMessage = "Invalid username or password"
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FioriBlue,
                    contentColor = FioriWhite,
                    disabledContainerColor = FioriGray,
                    disabledContentColor = FioriDarkGray
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = username.isNotBlank() && password.isNotBlank()
            ) {
                Text(
                    text = "Login",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = FioriDarkGray
                )
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = FioriWhite,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun RoleCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    backgroundColor: androidx.compose.ui.graphics.Color
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FioriWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(12.dp),
                color = backgroundColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize(),
                    tint = backgroundColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FioriBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = FioriDarkGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
