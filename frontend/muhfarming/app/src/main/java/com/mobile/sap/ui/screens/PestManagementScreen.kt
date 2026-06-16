package com.mobile.sap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.sap.data.model.PestInfo
import com.mobile.sap.ui.theme.*
import com.mobile.sap.ui.viewmodel.PestUiState
import com.mobile.sap.ui.viewmodel.PestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PestManagementScreen(
    viewModel: PestViewModel = viewModel(),
    isAdmin: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddPestDialog by remember { mutableStateOf(false) }
    var pestToEdit by remember { mutableStateOf<PestInfo?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var pestToDelete by remember { mutableStateOf<PestInfo?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pests",
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        letterSpacing = 0.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FioriBlue,
                    titleContentColor = FioriWhite
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddPestDialog = true },
                    containerColor = FioriBlue,
                    contentColor = FioriWhite
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Pest"
                    )
                }
            }
        },
        containerColor = FioriLightGray
    ) { paddingValues ->
        when (val state = uiState) {
            is PestUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FioriBlue)
                }
            }
            is PestUiState.Success -> {
                PestListContent(
                    pests = state.pests,
                    isAdmin = isAdmin,
                    onEditPest = { pest ->
                        pestToEdit = pest
                        showAddPestDialog = true
                    },
                    onDeletePest = { pest ->
                        pestToDelete = pest
                        showDeleteConfirmation = true
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is PestUiState.Error -> {
                ErrorStateWithRetry(
                    message = state.message,
                    onRetry = { viewModel.loadPestInfo() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        // Add/Edit Pest Dialog
        if (showAddPestDialog) {
            AddPestDialog(
                existingPest = pestToEdit,
                onDismiss = {
                    showAddPestDialog = false
                    pestToEdit = null
                },
                onAddPest = { newPest, originalName ->
                    if (pestToEdit != null) {
                        // Update existing pest
                        viewModel.updatePest(newPest, originalName ?: "")
                    } else {
                        // Add new pest
                        viewModel.addPest(newPest)
                    }
                    showAddPestDialog = false
                    pestToEdit = null
                }
            )
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmation && pestToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmation = false
                    pestToDelete = null
                },
                title = {
                    Text(
                        text = "Delete Pest",
                        fontWeight = FontWeight.Bold,
                        color = FioriBlack
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete ${pestToDelete?.pestName}? This action cannot be undone.",
                        color = FioriDarkGray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            pestToDelete?.let { pest ->
                                viewModel.deletePest(pest.pestName)
                            }
                            showDeleteConfirmation = false
                            pestToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FioriError,
                            contentColor = FioriWhite
                        )
                    ) {
                        Text("Delete", fontWeight = FontWeight.Medium)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteConfirmation = false
                        pestToDelete = null
                    }) {
                        Text("Cancel", color = FioriDarkGray, fontWeight = FontWeight.Medium)
                    }
                },
                containerColor = FioriWhite,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun PestListContent(
    pests: List<PestInfo>,
    isAdmin: Boolean,
    onEditPest: (PestInfo) -> Unit,
    onDeletePest: (PestInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            InfoCard()
        }

        items(pests) { pest ->
            PestInfoCard(
                pest = pest,
                isAdmin = isAdmin,
                onEditClick = { onEditPest(pest) },
                onDeleteClick = { onDeletePest(pest) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FioriInfo.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ℹ️",
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 14.dp)
            )
            Column {
                Text(
                    text = "Pest Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = FioriBlack,
                    fontSize = 14.sp,
                    letterSpacing = 0.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Monitor by season and severity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FioriDarkGray.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun PestInfoCard(
    pest: PestInfo,
    isAdmin: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FioriWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pest.pestName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = FioriBlack,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeverityBadge(severity = pest.severity)
                    if (isAdmin) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Pest",
                                tint = FioriBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Pest",
                                tint = FioriError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = pest.pestDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = FioriDarkGray.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            SeasonTag(season = pest.season)
        }
    }
}

@Composable
fun SeverityBadge(severity: String) {
    val (backgroundColor, textColor) = when (severity.lowercase()) {
        "high" -> SeverityHigh to FioriWhite
        "medium" -> SeverityMedium to FioriWhite
        "low" -> SeverityLow to FioriBlack
        else -> FioriGray to FioriBlack
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = severity,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun SeasonTag(season: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = FioriBlue.copy(alpha = 0.08f)
    ) {
        Text(
            text = season,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            color = FioriBlue,
            fontSize = 12.sp,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ErrorStateWithRetry(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🐛",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Unable to Load Pest Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FioriBlack
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = FioriError
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = FioriBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Retry",
                    color = FioriWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPestDialog(
    existingPest: PestInfo? = null,
    onDismiss: () -> Unit,
    onAddPest: (PestInfo, String?) -> Unit
) {
    val isEditing = existingPest != null
    val originalName = existingPest?.pestName
    var pestName by remember { mutableStateOf(existingPest?.pestName ?: "") }
    var pestDescription by remember { mutableStateOf(existingPest?.pestDescription ?: "") }
    var season by remember { mutableStateOf(existingPest?.season ?: "") }
    var severity by remember { mutableStateOf(existingPest?.severity ?: "LOW") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = FioriWhite),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Pest Information" else "Add Pest Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FioriBlack
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = FioriDarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pest Name
                OutlinedTextField(
                    value = pestName,
                    onValueChange = { pestName = it; errorMessage = null },
                    label = { Text("Pest Name *", color = FioriDarkGray) },
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

                Spacer(modifier = Modifier.height(12.dp))

                // Pest Description
                OutlinedTextField(
                    value = pestDescription,
                    onValueChange = { pestDescription = it; errorMessage = null },
                    label = { Text("Description *", color = FioriDarkGray) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FioriBlue,
                        focusedLabelColor = FioriBlue,
                        unfocusedBorderColor = FioriGray,
                        unfocusedLabelColor = FioriDarkGray,
                        focusedTextColor = FioriBlack,
                        unfocusedTextColor = FioriBlack
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Season
                OutlinedTextField(
                    value = season,
                    onValueChange = { season = it; errorMessage = null },
                    label = { Text("Season *", color = FioriDarkGray) },
                    placeholder = { Text("e.g., Rainy Season, Dry Season", color = FioriGray) },
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

                Spacer(modifier = Modifier.height(12.dp))

                // Severity Dropdown
                var expandedSeverity by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedSeverity,
                    onExpandedChange = { expandedSeverity = it }
                ) {
                    OutlinedTextField(
                        value = severity,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Severity *", color = FioriDarkGray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSeverity) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FioriBlue,
                            focusedLabelColor = FioriBlue,
                            unfocusedBorderColor = FioriGray,
                            unfocusedLabelColor = FioriDarkGray,
                            focusedTextColor = FioriBlack,
                            unfocusedTextColor = FioriBlack,
                            disabledTextColor = FioriBlack
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSeverity,
                        onDismissRequest = { expandedSeverity = false }
                    ) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { sev ->
                            DropdownMenuItem(
                                text = { Text(sev, color = FioriBlack) },
                                onClick = {
                                    severity = sev
                                    expandedSeverity = false
                                }
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = FioriError,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = FioriDarkGray, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Validate required fields
                            if (pestName.isBlank() || pestDescription.isBlank() || season.isBlank()) {
                                errorMessage = "Please fill all required fields"
                                return@Button
                            }

                            // Create pest info object
                            val newPest = PestInfo(
                                pestName = pestName,
                                pestDescription = pestDescription,
                                season = season,
                                severity = severity
                            )

                            onAddPest(newPest, originalName)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FioriBlue,
                            contentColor = FioriWhite
                        )
                    ) {
                        Text(if (isEditing) "Save Changes" else "Add Pest", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
