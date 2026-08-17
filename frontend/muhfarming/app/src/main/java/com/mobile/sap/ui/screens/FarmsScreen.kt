package com.mobile.sap.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.sap.data.api.dto.FarmDto
import com.mobile.sap.ui.components.EmptyState
import com.mobile.sap.ui.components.PrimaryButton
import com.mobile.sap.ui.viewmodel.FarmUiState
import com.mobile.sap.ui.viewmodel.FarmViewModel

/**
 * Dedicated Farms management: list the caller's farms and create / rename /
 * delete them. Fields are attached to a farm (see the field form's farm
 * picker), so a farmer manages farms here first.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmsScreen(
    onBack: () -> Unit = {},
    viewModel: FarmViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FarmDto?>(null) }
    var pendingDelete by remember { mutableStateOf<FarmDto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Farms", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showEditor = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add farm")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is FarmUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is FarmUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                is FarmUiState.Success -> {
                    if (state.farms.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Icons.Outlined.Agriculture,
                                title = "No farms yet",
                                subtitle = "Tap + to create your first farm."
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.farms, key = { it.ID }) { farm ->
                                FarmRow(
                                    farm = farm,
                                    onEdit = { editing = farm; showEditor = true },
                                    onDelete = { pendingDelete = farm }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        FarmEditorSheet(
            initial = editing,
            onDismiss = { showEditor = false },
            onConfirm = { name ->
                val target = editing
                if (target == null) viewModel.addFarm(name)
                else viewModel.updateFarm(target.ID, name)
                showEditor = false
            }
        )
    }

    pendingDelete?.let { farm ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete farm") },
            text = { Text("Delete \"${farm.name ?: "farm"}\"? Its fields may be affected.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteFarm(farm.ID)
                    pendingDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FarmRow(
    farm: FarmDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = farm.name ?: "Unnamed farm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FarmEditorSheet(
    initial: FarmDto?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(initial?.name ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = if (initial == null) "New farm" else "Rename farm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Farm name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Save",
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            )
        }
    }
}
