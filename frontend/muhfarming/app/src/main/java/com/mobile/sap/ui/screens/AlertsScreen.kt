package com.mobile.sap.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.sap.data.api.dto.CultivationRiskDto
import com.mobile.sap.data.api.dto.IncidentDto
import com.mobile.sap.data.api.dto.IncidentRequest
import com.mobile.sap.data.model.CameroonRegions
import com.mobile.sap.data.repository.AlertView
import com.mobile.sap.ui.components.*
import com.mobile.sap.ui.theme.*
import com.mobile.sap.ui.viewmodel.AlertUiState
import com.mobile.sap.ui.viewmodel.AlertViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Alerts tab. Lists the caller's alerts (each an incident affecting one of
 * their fields) and lets the user filter locally by region. The filter
 * defaults to the regions of the user's own fields; an "All" chip selects or
 * clears every region at once. Creating alerts (and incidents) is admin-only
 * (backend 403s otherwise), so the add controls are hidden unless [isAdmin].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    isAdmin: Boolean = false,
    viewModel: AlertViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedRegionIds by viewModel.selectedRegionIds.collectAsState()
    val fields by viewModel.fields.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val cultivationRisks by viewModel.cultivationRisks.collectAsState()

    var showCreate by remember { mutableStateOf(false) }
    // Alert currently being edited / pending deletion (admin only).
    var editingAlert by remember { mutableStateOf<AlertView?>(null) }
    var pendingDelete by remember { mutableStateOf<AlertView?>(null) }

    // Surface create success / failure as snackbars.
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Alerts", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { viewModel.refreshReferenceData(); showCreate = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create alert")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            val selected = selectedRegionIds ?: emptySet()
            val allRegionIds = CameroonRegions.regions.map { it.id }.toSet()
            val allSelected = selected.containsAll(allRegionIds)

            // Small section title above the filter chips.
            SectionLabel(
                text = "Regions",
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 2.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
                // "All" chip: selects every region, or clears them when all are on.
                FilterChip(
                    selected = allSelected,
                    onClick = {
                        if (allSelected) viewModel.clearRegions() else viewModel.selectAllRegions()
                    },
                    label = { Text("All") },
                    shape = MaterialTheme.shapes.small,
                    colors = chipColors
                )
                CameroonRegions.regions.forEach { region ->
                    FilterChip(
                        selected = region.id in selected,
                        onClick = { viewModel.toggleRegion(region.id) },
                        label = { Text(region.name) },
                        shape = MaterialTheme.shapes.small,
                        colors = chipColors
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            when (val state = uiState) {
                is AlertUiState.Loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                is AlertUiState.Error ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                is AlertUiState.Success -> {
                    // Client-side region filter: show an alert if its field's region
                    // is selected, or if it has no resolvable region.
                    val visible = state.alerts.filter { av ->
                        av.regionId == null || av.regionId in selected
                    }
                    if (visible.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Icons.Outlined.Notifications,
                                title = "No alerts",
                                subtitle = "No alerts for the selected regions."
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(visible, key = { it.alert.ID }) { av ->
                                AlertCard(
                                    av = av,
                                    isAdmin = isAdmin,
                                    onEdit = { viewModel.refreshReferenceData(); editingAlert = av },
                                    onDelete = { pendingDelete = av }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateAlertSheet(
            fields = fields,
            incidents = incidents,
            cultivationRisks = cultivationRisks,
            onAddIncident = { req, cb -> viewModel.createIncident(req, cb) },
            onDismiss = { showCreate = false },
            onConfirm = { fieldId, incidentId ->
                viewModel.createAlert(fieldId, incidentId)
                showCreate = false
            }
        )
    }

    editingAlert?.let { av ->
        CreateAlertSheet(
            initial = av,
            fields = fields,
            incidents = incidents,
            cultivationRisks = cultivationRisks,
            onAddIncident = { req, cb -> viewModel.createIncident(req, cb) },
            onDismiss = { editingAlert = null },
            onConfirm = { fieldId, incidentId ->
                viewModel.updateAlert(av.alert.ID, fieldId, incidentId)
                editingAlert = null
            }
        )
    }

    pendingDelete?.let { av ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove alert?") },
            text = {
                Text(
                    "This alert for “${av.fieldName ?: "this field"}” will be removed. " +
                        "This can't be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAlert(av.alert.ID)
                    pendingDelete = null
                }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

/** Maps an incident priority string to its severity accent color. */
private fun priorityColor(priority: String?): Color = when (priority?.trim()?.lowercase()) {
    "high" -> SeverityHigh
    "medium" -> SeverityMedium
    "low" -> SeverityLow
    else -> SeverityMedium
}

@Composable
private fun AlertCard(
    av: AlertView,
    isAdmin: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val regionName = av.regionId?.let { CameroonRegions.nameForId(it) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val priority = av.incident?.priority?.takeIf { it.isNotBlank() }
                if (priority != null) {
                    StatusPill(text = priority, color = priorityColor(priority))
                } else {
                    StatusPill(text = "Alert", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.weight(1f))
                av.incident?.date?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isAdmin) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit alert",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove alert",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            av.incident?.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = buildString {
                    append("Field: ${av.fieldName ?: "—"}")
                    regionName?.let { append("   •   $it") }
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ---- Admin authoring bottom sheets ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAlertSheet(
    initial: AlertView? = null,
    fields: List<com.mobile.sap.data.api.dto.FieldDto>,
    incidents: List<IncidentDto>,
    cultivationRisks: List<CultivationRiskDto>,
    onAddIncident: (IncidentRequest, (Long) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    var fieldId by remember { mutableStateOf(initial?.alert?.fieldId ?: fields.firstOrNull()?.ID) }
    var incidentId by remember { mutableStateOf(initial?.alert?.incidentId ?: incidents.firstOrNull()?.ID) }
    var showAddIncident by remember { mutableStateOf(false) }

    AlertSheetScaffold(
        title = if (initial != null) "Edit alert" else "Create alert",
        onDismiss = onDismiss,
        saveEnabled = fieldId != null && incidentId != null,
        saveLabel = if (initial != null) "Save" else "Create",
        onSave = {
            val f = fieldId ?: return@AlertSheetScaffold
            val i = incidentId ?: return@AlertSheetScaffold
            onConfirm(f, i)
        }
    ) {
        LabeledPicker(
            label = "Field",
            options = fields.map { it.ID to (it.name ?: "Field ${it.ID}") },
            selectedId = fieldId,
            onSelect = { fieldId = it }
        )
        LabeledPicker(
            label = "Incident",
            options = incidents.map { it.ID to (it.description?.take(40) ?: "Incident ${it.ID}") },
            selectedId = incidentId,
            onSelect = { incidentId = it },
            addLabel = "Add incident",
            onAdd = { showAddIncident = true }
        )
    }

    if (showAddIncident) {
        AddIncidentSheet(
            cultivationRisks = cultivationRisks,
            onDismiss = { showAddIncident = false },
            onSave = { req ->
                onAddIncident(req) { newId -> incidentId = newId }
                showAddIncident = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIncidentSheet(
    cultivationRisks: List<CultivationRiskDto>,
    onDismiss: () -> Unit,
    onSave: (IncidentRequest) -> Unit
) {
    var date by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var description by remember { mutableStateOf("") }
    var riskId by remember { mutableStateOf(cultivationRisks.firstOrNull()?.ID) }
    var regionId by remember { mutableStateOf(CameroonRegions.regions.firstOrNull()?.id) }

    // An incident must reference a cultivation-risk. Without any risks there is
    // nothing to attach to, so block saving and explain how to add one.
    val hasRisks = cultivationRisks.isNotEmpty()

    AlertSheetScaffold(
        title = "Add incident",
        onDismiss = onDismiss,
        saveEnabled = hasRisks && riskId != null && regionId != null,
        saveLabel = "Save",
        onSave = {
            val rid = riskId ?: return@AlertSheetScaffold
            val reg = regionId ?: return@AlertSheetScaffold
            onSave(
                IncidentRequest(
                    date = date.trim().ifBlank { null },
                    priority = priority.trim().ifBlank { null },
                    description = description.trim().ifBlank { null },
                    cultivationRiskId = rid,
                    regionId = reg
                )
            )
        }
    ) {
        IncidentDateField(date = date, onDateChange = { date = it })
        LabeledStringPicker(
            label = "Priority",
            options = listOf("High", "Medium", "Low"),
            selected = priority,
            onSelect = { priority = it }
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        )
        if (hasRisks) {
            LabeledPicker(
                label = "Cultivation risk",
                options = cultivationRisks.map { it.ID to (it.solution?.take(40)?.ifBlank { null } ?: "Risk ${it.ID}") },
                selectedId = riskId,
                onSelect = { riskId = it }
            )
        } else {
            Text(
                text = "No cultivation risks yet. Add a risk in the Cultivation Guide tab " +
                    "before creating an incident.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LabeledPicker(
            label = "Region",
            options = CameroonRegions.regions.map { it.id to it.name },
            selectedId = regionId,
            onSelect = { regionId = it }
        )
    }
}

/**
 * Read-only date field that opens an M3 [DatePickerDialog] on tap and stores the
 * selection as an ISO-8601 date string (yyyy-MM-dd), which is what the backend
 * expects for an incident's date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncidentDateField(date: String, onDateChange: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    OutlinedTextField(
        value = date,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text("Date") },
        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Pick date") },
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val iso = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                            .format(DateTimeFormatter.ISO_LOCAL_DATE)
                        onDateChange(iso)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ---- Reusable sheet building blocks ----

/**
 * Common scaffold for an authoring bottom sheet, following M3 guidance: a
 * title, scrollable content, and a single full-width filled button pinned at
 * the very bottom. Dismissal is via swipe-down / scrim tap.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertSheetScaffold(
    title: String,
    onDismiss: () -> Unit,
    saveEnabled: Boolean,
    saveLabel: String,
    onSave: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = saveLabel,
                onClick = onSave,
                enabled = saveEnabled,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** A dropdown picker over plain string options (e.g. a fixed priority list). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledStringPicker(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}
/**
 * A dropdown picker with a label. When [onAdd] is provided, a highlighted
 * "add" item is shown at the top of the menu for inline creation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledPicker(
    label: String,
    options: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    addLabel: String? = null,
    onAdd: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.firstOrNull { it.first == selectedId }?.second ?: "Select"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (addLabel != null && onAdd != null) {
                DropdownMenuItem(
                    text = { Text(addLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = { expanded = false; onAdd() }
                )
                HorizontalDivider()
            }
            options.forEach { (id, name) ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onSelect(id); expanded = false })
            }
        }
    }
}
