package com.mobile.sap.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.sap.data.api.dto.CultivationDto
import com.mobile.sap.data.api.dto.CultivationGuidelineDto
import com.mobile.sap.data.api.dto.CultivationGuidelineRequest
import com.mobile.sap.data.api.dto.CultivationRiskDto
import com.mobile.sap.data.api.dto.CultivationRiskRequest
import com.mobile.sap.data.api.dto.HazardDto
import com.mobile.sap.data.repository.CultivationGuide
import com.mobile.sap.data.repository.GuidelineView
import com.mobile.sap.data.repository.RiskView
import com.mobile.sap.ui.components.EmptyState
import com.mobile.sap.ui.components.NoRippleIconButton
import com.mobile.sap.ui.components.SectionLabel
import com.mobile.sap.ui.theme.*
import com.mobile.sap.ui.viewmodel.CultivationUiState
import com.mobile.sap.ui.viewmodel.CultivationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.WeekFields

/**
 * Read-only cultivation guide: browse each cultivation's guidelines (with
 * fertilizer) and risks (with hazard). Only an admin can author cultivations,
 * guidelines, risks, and hazards — the backend enforces this (403 for
 * farmers), so the add controls are hidden unless [isAdmin]. Authoring uses
 * bottom sheets (icon + text rows, Save pinned at the bottom) rather than
 * dialogs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivationGuideScreen(
    isAdmin: Boolean = false,
    viewModel: CultivationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cultivations by viewModel.cultivations.collectAsState()
    val hazards by viewModel.hazards.collectAsState()

    // Which authoring bottom sheet (if any) is open for *adding*.
    var sheet by remember { mutableStateOf<AuthoringSheet?>(null) }
    var showAdminMenu by remember { mutableStateOf(false) }

    // Item currently being *edited* (admin only). Non-null opens the matching
    // sheet prefilled; Save routes to the update path instead of create.
    var editingCultivation by remember { mutableStateOf<CultivationDto?>(null) }
    var editingHazard by remember { mutableStateOf<HazardDto?>(null) }
    var editingGuideline by remember { mutableStateOf<CultivationGuidelineDto?>(null) }
    var editingRisk by remember { mutableStateOf<CultivationRiskDto?>(null) }

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
                title = { Text("Cultivation Guide", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAdminMenu = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is CultivationUiState.Loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                is CultivationUiState.Error ->
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                is CultivationUiState.Success -> {
                    if (state.guides.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Icons.Outlined.Grass,
                                title = "No cultivation guides",
                                subtitle = if (isAdmin)
                                    "Tap + to add guidelines, risks, and hazards."
                                else
                                    "No cultivation guides available for your crops yet."
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.guides, key = { it.cultivation.ID }) { guide ->
                                CultivationCard(
                                    guide = guide,
                                    isAdmin = isAdmin,
                                    onEditCultivation = { editingCultivation = guide.cultivation },
                                    onEditGuideline = { editingGuideline = it },
                                    onEditRisk = { editingRisk = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Admin: pick what to add (icon + text rows in a bottom sheet).
    if (showAdminMenu) {
        AdminAddMenuSheet(
            onDismiss = { showAdminMenu = false },
            onPick = { picked -> showAdminMenu = false; sheet = picked }
        )
    }

    when (sheet) {
        AuthoringSheet.Hazard -> HazardSheet(
            onDismiss = { sheet = null },
            onSave = { name, desc ->
                viewModel.addHazard(name, desc)
                sheet = null
            }
        )

        AuthoringSheet.Guideline -> GuidelineSheet(
            cultivations = cultivations,
            onAddCultivation = { name, weeks, cb -> viewModel.addCultivation(name, weeks, cb) },
            onDismiss = { sheet = null },
            onSave = { id, req ->
                if (id == null) viewModel.addGuideline(req) else viewModel.updateGuideline(id, req)
                sheet = null
            }
        )

        AuthoringSheet.Risk -> RiskSheet(
            cultivations = cultivations,
            hazards = hazards,
            onAddCultivation = { name, weeks, cb -> viewModel.addCultivation(name, weeks, cb) },
            onAddHazard = { name, desc, cb -> viewModel.addHazard(name, desc, cb) },
            onDismiss = { sheet = null },
            onSave = { id, req ->
                if (id == null) viewModel.addRisk(req) else viewModel.updateRisk(id, req)
                sheet = null
            }
        )

        null -> Unit
    }

    // ---- Edit sheets (admin only) — reuse the authoring sheets, prefilled. ----

    editingCultivation?.let { c ->
        AddCultivationSheet(
            initial = c,
            onDismiss = { editingCultivation = null },
            onSave = { name, weeks ->
                viewModel.updateCultivation(c.ID, name, weeks)
                editingCultivation = null
            }
        )
    }

    editingHazard?.let { h ->
        HazardSheet(
            initial = h,
            onDismiss = { editingHazard = null },
            onSave = { name, desc ->
                viewModel.updateHazard(h.ID, name, desc)
                editingHazard = null
            }
        )
    }

    editingGuideline?.let { g ->
        GuidelineSheet(
            initial = g,
            cultivations = cultivations,
            onAddCultivation = { name, weeks, cb -> viewModel.addCultivation(name, weeks, cb) },
            onDismiss = { editingGuideline = null },
            onSave = { id, req ->
                if (id == null) viewModel.addGuideline(req) else viewModel.updateGuideline(id, req)
                editingGuideline = null
            }
        )
    }

    editingRisk?.let { r ->
        RiskSheet(
            initial = r,
            cultivations = cultivations,
            hazards = hazards,
            onAddCultivation = { name, weeks, cb -> viewModel.addCultivation(name, weeks, cb) },
            onAddHazard = { name, desc, cb -> viewModel.addHazard(name, desc, cb) },
            onDismiss = { editingRisk = null },
            onSave = { id, req ->
                if (id == null) viewModel.addRisk(req) else viewModel.updateRisk(id, req)
                editingRisk = null
            }
        )
    }
}

private enum class AuthoringSheet { Hazard, Guideline, Risk }

// ---- Read-only display ----

@Composable
private fun CultivationCard(
    guide: CultivationGuide,
    isAdmin: Boolean = false,
    onEditCultivation: () -> Unit = {},
    onEditGuideline: (CultivationGuidelineDto) -> Unit = {},
    onEditRisk: (CultivationRiskDto) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = guide.cultivation.name ?: "Cultivation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val weeks = guide.cultivation.estTimeToHarvestWeeks
                    if (weeks > 0) {
                        Text(
                            "~$weeks weeks to harvest",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isAdmin) {
                    NoRippleIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit cultivation",
                        onClick = onEditCultivation
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    SectionLabel("Guidelines")
                    Spacer(Modifier.height(4.dp))
                    if (guide.guidelines.isEmpty()) {
                        Text(
                            "None",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        guide.guidelines.forEach { view ->
                            GuidelineRow(
                                view = view,
                                onEdit = if (isAdmin) ({ onEditGuideline(view.guideline) }) else null
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))

                    SectionLabel("Risks")
                    Spacer(Modifier.height(4.dp))
                    if (guide.risks.isEmpty()) {
                        Text(
                            "None",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        guide.risks.forEach { view ->
                            RiskRow(
                                view = view,
                                onEdit = if (isAdmin) ({ onEditRisk(view.risk) }) else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuidelineRow(view: GuidelineView, onEdit: (() -> Unit)? = null) {
    val g = view.guideline
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = buildString {
                    append(g.type?.takeIf { it.isNotBlank() } ?: "Guideline")
                    if (g.weekFrom > 0 || g.weekTo > 0) append("  ·  week ${g.weekFrom}–${g.weekTo}")
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            g.instructions?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            view.fertilizerName?.let {
                Text(
                    "Fertilizer: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        if (onEdit != null) {
            NoRippleIconButton(
                icon = Icons.Default.Edit,
                contentDescription = "Edit guideline",
                onClick = onEdit
            )
        }
    }
}

@Composable
private fun RiskRow(view: RiskView, onEdit: (() -> Unit)? = null) {
    val r = view.risk
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = view.hazard?.name ?: "Risk",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (r.weekFrom > 0 || r.weekTo > 0) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "week ${r.weekFrom}–${r.weekTo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            view.hazard?.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            r.solution?.takeIf { it.isNotBlank() }?.let {
                Text(
                    "Solution: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = Success
                )
            }
        }
        if (onEdit != null) {
            NoRippleIconButton(
                icon = Icons.Default.Edit,
                contentDescription = "Edit risk",
                onClick = onEdit
            )
        }
    }
}

// ---- Admin authoring bottom sheets ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminAddMenuSheet(
    onDismiss: () -> Unit,
    onPick: (AuthoringSheet) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            SheetTitle("Add to guide")
            SheetActionRow(Icons.Outlined.WarningAmber, "New hazard") { onPick(AuthoringSheet.Hazard) }
            SheetActionRow(Icons.Outlined.MenuBook, "New guideline") { onPick(AuthoringSheet.Guideline) }
            SheetActionRow(Icons.Outlined.BugReport, "New risk") { onPick(AuthoringSheet.Risk) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HazardSheet(
    initial: HazardDto? = null,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    AuthoringSheetScaffold(
        title = if (initial != null) "Edit hazard" else "New hazard",
        onDismiss = onDismiss,
        saveEnabled = name.isNotBlank() && desc.isNotBlank(),
        onSave = { onSave(name.trim(), desc.trim()) }
    ) {
        SheetTextField(
            value = name,
            onValueChange = { name = it },
            label = "Name",
            singleLine = true
        )
        SheetTextField(
            value = desc,
            onValueChange = { desc = it },
            label = "Description"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuidelineSheet(
    initial: CultivationGuidelineDto? = null,
    cultivations: List<CultivationDto>,
    onAddCultivation: (String, Int, (Long) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Long?, CultivationGuidelineRequest) -> Unit
) {
    var type by remember { mutableStateOf(initial?.type ?: "") }
    var instructions by remember { mutableStateOf(initial?.instructions ?: "") }
    var weekFrom by remember { mutableStateOf(initial?.weekFrom ?: 0) }
    var weekTo by remember { mutableStateOf(initial?.weekTo ?: 0) }
    var cultivationId by remember { mutableStateOf(initial?.cultivationId ?: cultivations.firstOrNull()?.ID) }
    var showAddCultivation by remember { mutableStateOf(false) }

    AuthoringSheetScaffold(
        title = if (initial != null) "Edit guideline" else "New guideline",
        onDismiss = onDismiss,
        saveEnabled = cultivationId != null,
        onSave = {
            val cid = cultivationId ?: return@AuthoringSheetScaffold
            onSave(
                initial?.ID,
                CultivationGuidelineRequest(
                    type = type.trim().ifBlank { null },
                    weekFrom = weekFrom,
                    weekTo = weekTo,
                    instructions = instructions.trim().ifBlank { null },
                    cultivationId = cid
                )
            )
        }
    ) {
        LabeledPicker(
            label = "Cultivation",
            options = cultivations.map { it.ID to (it.name ?: "Cultivation") },
            selectedId = cultivationId,
            onSelect = { cultivationId = it },
            addLabel = "Add cultivation",
            onAdd = { showAddCultivation = true }
        )
        SheetTextField(
            value = type,
            onValueChange = { type = it },
            label = "Type",
            singleLine = true
        )
        SheetTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = "Instructions"
        )
        WeekRangeField(weekFrom, weekTo) { from, to -> weekFrom = from; weekTo = to }
    }

    if (showAddCultivation) {
        AddCultivationSheet(
            onDismiss = { showAddCultivation = false },
            onSave = { name, weeks ->
                onAddCultivation(name, weeks) { newId -> cultivationId = newId }
                showAddCultivation = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RiskSheet(
    initial: CultivationRiskDto? = null,
    cultivations: List<CultivationDto>,
    hazards: List<HazardDto>,
    onAddCultivation: (String, Int, (Long) -> Unit) -> Unit,
    onAddHazard: (String, String, (Long) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Long?, CultivationRiskRequest) -> Unit
) {
    var solution by remember { mutableStateOf(initial?.solution ?: "") }
    var weekFrom by remember { mutableStateOf(initial?.weekFrom ?: 0) }
    var weekTo by remember { mutableStateOf(initial?.weekTo ?: 0) }
    var cultivationId by remember { mutableStateOf(initial?.cultivationId ?: cultivations.firstOrNull()?.ID) }
    var hazardId by remember { mutableStateOf(initial?.hazardId ?: hazards.firstOrNull()?.ID) }
    var showAddCultivation by remember { mutableStateOf(false) }
    var showAddHazard by remember { mutableStateOf(false) }

    AuthoringSheetScaffold(
        title = if (initial != null) "Edit risk" else "New risk",
        onDismiss = onDismiss,
        saveEnabled = cultivationId != null && hazardId != null,
        onSave = {
            val cid = cultivationId ?: return@AuthoringSheetScaffold
            val hid = hazardId ?: return@AuthoringSheetScaffold
            onSave(
                initial?.ID,
                CultivationRiskRequest(
                    weekFrom = weekFrom,
                    weekTo = weekTo,
                    solution = solution.trim().ifBlank { null },
                    cultivationId = cid,
                    hazardId = hid
                )
            )
        }
    ) {
        LabeledPicker(
            label = "Cultivation",
            options = cultivations.map { it.ID to (it.name ?: "Cultivation") },
            selectedId = cultivationId,
            onSelect = { cultivationId = it },
            addLabel = "Add cultivation",
            onAdd = { showAddCultivation = true }
        )
        LabeledPicker(
            label = "Hazard",
            options = hazards.map { it.ID to (it.name ?: "Hazard") },
            selectedId = hazardId,
            onSelect = { hazardId = it },
            addLabel = "Add new hazard",
            onAdd = { showAddHazard = true }
        )
        SheetTextField(
            value = solution,
            onValueChange = { solution = it },
            label = "Solution"
        )
        WeekRangeField(weekFrom, weekTo) { from, to -> weekFrom = from; weekTo = to }
    }

    if (showAddCultivation) {
        AddCultivationSheet(
            onDismiss = { showAddCultivation = false },
            onSave = { name, weeks ->
                onAddCultivation(name, weeks) { newId -> cultivationId = newId }
                showAddCultivation = false
            }
        )
    }
    if (showAddHazard) {
        HazardSheet(
            onDismiss = { showAddHazard = false },
            onSave = { name, desc ->
                onAddHazard(name, desc) { newId -> hazardId = newId }
                showAddHazard = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCultivationSheet(
    initial: CultivationDto? = null,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var weeks by remember { mutableStateOf(initial?.estTimeToHarvestWeeks?.takeIf { it > 0 }?.toString() ?: "") }
    AuthoringSheetScaffold(
        title = if (initial != null) "Edit cultivation" else "Add cultivation",
        onDismiss = onDismiss,
        saveEnabled = name.isNotBlank(),
        onSave = { onSave(name.trim(), weeks.toIntOrNull() ?: 0) }
    ) {
        SheetTextField(
            value = name,
            onValueChange = { name = it },
            label = "Name",
            singleLine = true
        )
        SheetTextField(
            value = weeks,
            onValueChange = { input -> weeks = input.filter { it.isDigit() } },
            label = "Weeks to harvest",
            singleLine = true
        )
    }
}

// ---- Reusable sheet building blocks ----

/**
 * Common scaffold for an authoring bottom sheet, following M3 guidance: a
 * title, scrollable content, and a single full-width filled button pinned at
 * the very bottom. Dismissal is via swipe-down / scrim tap (no explicit
 * cancel button).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthoringSheetScaffold(
    title: String,
    onDismiss: () -> Unit,
    saveEnabled: Boolean,
    saveLabel: String = "Save",
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
            SheetTitle(title)
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
            Button(
                onClick = onSave,
                enabled = saveEnabled,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .heightIn(min = 54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    saveLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SheetTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

/** A rounded outlined text field with consistent brand styling for sheets. */
@Composable
private fun SheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun SheetActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
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
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (addLabel != null && onAdd != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            addLabel,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
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

/**
 * A read-only field that opens the M3 date-range picker; the chosen start and
 * end dates are each converted to their ISO week-of-year (what the backend
 * stores). Shows "Week X – Week Y" once picked, or a hint when unset.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekRangeField(
    weekFrom: Int,
    weekTo: Int,
    onWeeks: (Int, Int) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val display = when {
        weekFrom > 0 && weekTo > 0 -> "Week $weekFrom – Week $weekTo"
        weekFrom > 0 -> "Week $weekFrom"
        else -> ""
    }
    OutlinedTextField(
        value = display,
        onValueChange = {},
        readOnly = true,
        label = { Text("Weeks (from – to)") },
        placeholder = { Text("Select date range") },
        shape = RoundedCornerShape(12.dp),
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Pick date range",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier.fillMaxWidth().clickable { showPicker = true }
    )

    if (showPicker) {
        val rangeState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val from = rangeState.selectedStartDateMillis?.let { millisToIsoWeek(it) } ?: 0
                    val to = rangeState.selectedEndDateMillis?.let { millisToIsoWeek(it) } ?: from
                    onWeeks(from, to)
                    showPicker = false
                }) { Text("OK", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(state = rangeState, modifier = Modifier.weight(1f))
        }
    }
}

/** Convert an epoch-millis date (UTC, as the Material picker returns) to its ISO week-of-year. */
private fun millisToIsoWeek(millis: Long): Int {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
    return date.get(WeekFields.ISO.weekOfWeekBasedYear()).coerceIn(1, 53)
}
