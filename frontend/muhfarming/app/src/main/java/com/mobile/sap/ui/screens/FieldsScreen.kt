package com.mobile.sap.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import com.mobile.sap.data.model.CameroonCities
import com.mobile.sap.data.model.CultivationRisk
import com.mobile.sap.data.model.Field
import com.mobile.sap.data.model.HazardSeverity
import com.mobile.sap.ui.theme.*
import com.mobile.sap.ui.components.*
import com.mobile.sap.ui.viewmodel.FieldUiState
import com.mobile.sap.ui.viewmodel.FieldViewModel
import com.mobile.sap.ui.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldsScreen(
    weatherViewModel: WeatherViewModel = viewModel(),
    fieldViewModel: FieldViewModel = viewModel(),
    farmViewModel: com.mobile.sap.ui.viewmodel.FarmViewModel = viewModel(),
    isAdmin: Boolean = false,
    onOpenFarms: () -> Unit = {}
) {
    val context = LocalContext.current
    val location by weatherViewModel.location.collectAsState()
    val fieldUiState by fieldViewModel.uiState.collectAsState()
    val farmUiState by farmViewModel.uiState.collectAsState()
    val farms = (farmUiState as? com.mobile.sap.ui.viewmodel.FarmUiState.Success)?.farms ?: emptyList()
    val selectedField by fieldViewModel.selectedField.collectAsState()
    var showInfoCard by remember { mutableStateOf(true) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var showAddFieldDialog by remember { mutableStateOf(false) }
    var fieldToEdit by remember { mutableStateOf<Field?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var fieldToDelete by remember { mutableStateOf<Field?>(null) }

    // Coordinate selection mode states
    var isSelectingCoordinates by remember { mutableStateOf(false) }
    var selectedCoordinates by remember { mutableStateOf<List<com.mobile.sap.data.model.Coordinate>>(emptyList()) }

    // Farm filter: null = "All farms" (show every field). Otherwise only fields
    // on the selected farm are drawn.
    var selectedFarmFilterId by remember { mutableStateOf<Long?>(null) }

    // Initialize MapLibre
    DisposableEffect(Unit) {
        MapLibre.getInstance(context)
        onDispose { }
    }

    // Auto-hide info card after 2 seconds
    LaunchedEffect(Unit) {
        delay(2000)
        showInfoCard = false
    }

    // Get coordinates for the current location
    val city = CameroonCities.getCityByName(location) ?: CameroonCities.getDefaultCity()
    val position = LatLng(city.latitude, city.longitude)

    // Load all fields from the backend
    LaunchedEffect(Unit) {
        Log.d("FieldsScreen", "Loading all fields from backend")
        fieldViewModel.loadAllFields()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Fields",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onOpenFarms) {
                        Icon(
                            imageVector = Icons.Outlined.Agriculture,
                            contentDescription = "Manage farms",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            var mapView: MapView? by remember { mutableStateOf(null) }
            var fields by remember { mutableStateOf<List<Field>>(emptyList()) }
            var mapStyleLoaded by remember { mutableStateOf(false) }

            // Update fields when UI state or the farm filter changes.
            LaunchedEffect(fieldUiState, selectedFarmFilterId) {
                if (fieldUiState is FieldUiState.Success) {
                    val allFields = (fieldUiState as FieldUiState.Success).fields
                    val newFields = selectedFarmFilterId?.let { farmId ->
                        allFields.filter { it.farmId == farmId }
                    } ?: allFields
                    Log.d("FieldsScreen", "UI State updated with ${newFields.size} fields (filter=$selectedFarmFilterId)")
                    fields = newFields

                    // If map style is already loaded, add fields immediately
                    if (mapStyleLoaded) {
                        mapView?.getMapAsync { map ->
                            map.style?.let { style ->
                                Log.d("FieldsScreen", "Adding fields after state change")
                                removeFieldsFromMap(style)
                                addFieldsToMap(style, fields)
                            }
                        }
                    }
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapView = this
                        onCreate(null)
                        getMapAsync { map ->
                            mapLibreMap = map
                            Log.d("FieldsScreen", "Map initialized, loading style...")

                            // Use OSM Bright style with roads, rivers, buildings visible
                            val styleJson = """
                            {
                              "version": 8,
                              "sources": {
                                "osm": {
                                  "type": "raster",
                                  "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
                                  "tileSize": 256,
                                  "attribution": "© OpenStreetMap contributors"
                                }
                              },
                              "layers": [
                                {
                                  "id": "osm",
                                  "type": "raster",
                                  "source": "osm",
                                  "minzoom": 0,
                                  "maxzoom": 22
                                }
                              ]
                            }
                            """.trimIndent()

                            map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                                Log.d("FieldsScreen", "Style loaded successfully")
                                mapStyleLoaded = true

                                // Set camera position with lower zoom to see fields
                                map.cameraPosition = CameraPosition.Builder()
                                    .target(position)
                                    .zoom(12.0)
                                    .build()

                                Log.d("FieldsScreen", "Camera positioned at $position with zoom 12.0")

                                // Add field polygons immediately if available
                                if (fields.isNotEmpty()) {
                                    Log.d("FieldsScreen", "Fields available (${fields.size}), adding to map now")
                                    addFieldsToMap(style, fields)
                                } else {
                                    Log.w("FieldsScreen", "No fields available yet during style load, will add when fields load")
                                }

                                // Add click listener for field polygons and coordinate selection
                                map.addOnMapClickListener { point ->
                                    Log.d("FieldsScreen", "Map clicked at ${point.latitude}, ${point.longitude}")

                                    if (isSelectingCoordinates) {
                                        // Check if user clicked on an existing marker
                                        val screenPoint = map.projection.toScreenLocation(point)
                                        val markerFeatures = map.queryRenderedFeatures(screenPoint, "selection-markers-layer")

                                        if (markerFeatures.isNotEmpty()) {
                                            // User clicked on a marker - remove that coordinate
                                            val markerIndex = markerFeatures.first().getNumberProperty("index").toInt()
                                            selectedCoordinates = selectedCoordinates.filterIndexed { index, _ -> index != markerIndex }
                                            Log.d("FieldsScreen", "Removed coordinate at index $markerIndex. Total: ${selectedCoordinates.size}")
                                        } else {
                                            // Add new coordinate to selection
                                            val newCoord = com.mobile.sap.data.model.Coordinate(point.latitude, point.longitude)
                                            selectedCoordinates = selectedCoordinates + newCoord
                                            Log.d("FieldsScreen", "Added coordinate: $newCoord. Total: ${selectedCoordinates.size}")
                                        }

                                        // Update markers and preview polygon on map
                                        updateSelectionMarkers(style, selectedCoordinates)
                                    } else {
                                        // Query features at click point — check
                                        // polygon fills and point markers.
                                        val screenPoint = map.projection.toScreenLocation(point)
                                        val features = map.queryRenderedFeatures(
                                            screenPoint, "field-fill-layer", "field-marker-layer"
                                        )

                                        if (features.isNotEmpty()) {
                                            val feature = features.first()
                                            val fieldId = feature.getStringProperty("id")
                                            Log.d("FieldsScreen", "Field clicked: $fieldId")

                                            // Find the field in our list
                                            val clickedField = fields.find { it.id == fieldId }
                                            if (clickedField != null) {
                                                fieldViewModel.selectField(clickedField)
                                            }
                                        } else {
                                            // Clicked outside a field - deselect
                                            fieldViewModel.selectField(null)
                                        }
                                    }

                                    true
                                }
                            }
                        }
                    }
                },
                update = { view ->
                    view.getMapAsync { map ->
                        map.style?.let { style ->
                            // Update fields on map when they change or selection mode changes
                            if (isSelectingCoordinates) {
                                // Hide existing fields during coordinate selection
                                removeFieldsFromMap(style)
                                updateSelectionMarkers(style, selectedCoordinates)
                            } else if (fields.isNotEmpty()) {
                                Log.d("FieldsScreen", "Update triggered with ${fields.size} fields")
                                // Remove selection markers and show fields
                                removeSelectionMarkers(style)
                                addFieldsToMap(style, fields)
                            }
                        } ?: Log.w("FieldsScreen", "Map style not ready yet")
                    }
                }
            )


            // Farm filter chips overlaying the top of the map (hidden while
            // picking coordinates). "All" clears the filter; each chip narrows
            // the visible fields to one farm.
            if (farms.isNotEmpty() && !isSelectingCoordinates) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFarmFilterId == null,
                        onClick = { selectedFarmFilterId = null },
                        label = { Text("All farms") },
                        shape = MaterialTheme.shapes.small,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                    farms.forEach { farm ->
                        FilterChip(
                            selected = selectedFarmFilterId == farm.ID,
                            onClick = {
                                selectedFarmFilterId =
                                    if (selectedFarmFilterId == farm.ID) null else farm.ID
                            },
                            label = { Text(farm.name ?: "Unnamed farm") },
                            shape = MaterialTheme.shapes.small,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Loading indicator
            if (fieldUiState is FieldUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Minimal location info card at the bottom with auto-hide
            AnimatedVisibility(
                visible = showInfoCard && selectedField == null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📍",
                            fontSize = 16.sp
                        )
                        Column {
                            Text(
                                text = city.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val fieldCount = fields.size
                            Text(
                                text = "$fieldCount field${if (fieldCount != 1) "s" else ""} nearby",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Field details card when a field is selected
            selectedField?.let { field ->
                SectionCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    // Header with close and edit buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = field.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAdmin) {
                                IconButton(
                                    onClick = {
                                        fieldToEdit = field
                                        showAddFieldDialog = true
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Field",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        fieldToDelete = field
                                        showDeleteConfirmation = true
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Field",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { fieldViewModel.selectField(null) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Region
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "📍", fontSize = 14.sp)
                        Text(
                            text = field.region,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Cultivation info
                    field.cultivation?.let { cultivation ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Season",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = cultivation.season,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Status",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = cultivation.status,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Cultivation Risk
                    field.cultivationRisk?.let { risk ->
                        val riskColor = when (risk) {
                            CultivationRisk.LOW -> Success
                            CultivationRisk.MEDIUM -> Warning
                            CultivationRisk.HIGH -> Danger
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Risk Level:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            StatusPill(text = risk.name, color = riskColor)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Cultivation Guideline
                    field.cultivationGuideline?.let { guideline ->
                        Text(
                            text = "Guidelines",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = guideline,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Incidents
                    if (field.incidents.isNotEmpty()) {
                        Text(
                            text = "⚠️ Incidents (${field.incidents.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        field.incidents.forEach { incident ->
                            Text(
                                text = "• ${incident.type}: ${incident.description}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Hazards
                    if (field.hazards.isNotEmpty()) {
                        Text(
                            text = "⚡ Hazards (${field.hazards.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Warning
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        field.hazards.forEach { hazard ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = hazard.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "(${hazard.severity})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (hazard.severity) {
                                        HazardSeverity.LOW -> SeverityLow
                                        HazardSeverity.MEDIUM -> SeverityMedium
                                        HazardSeverity.HIGH -> SeverityHigh
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Add Field FAB for administrators
            if (isAdmin && selectedField == null && !isSelectingCoordinates) {
                FloatingActionButton(
                    onClick = {
                        isSelectingCoordinates = true
                        selectedCoordinates = emptyList()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Field"
                    )
                }
            }

            // Coordinate selection UI
            if (isSelectingCoordinates) {
                CoordinateSelectionOverlay(
                    selectedCount = selectedCoordinates.size,
                    onContinue = {
                        showAddFieldDialog = true
                    },
                    onCancel = {
                        isSelectingCoordinates = false
                        selectedCoordinates = emptyList<com.mobile.sap.data.model.Coordinate>()
                        // Trigger map update to restore fields
                        mapView?.getMapAsync { map ->
                            map.style?.let { style ->
                                removeSelectionMarkers(style)
                                addFieldsToMap(style, fields)
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Add/Edit Field Dialog
            if (showAddFieldDialog) {
                AddFieldDialog(
                    existingField = fieldToEdit,
                    preSelectedCoordinates = if (fieldToEdit == null) selectedCoordinates else emptyList(),
                    farms = farms,
                    onAddFarm = { name, onCreated -> farmViewModel.addFarm(name, onCreated) },
                    onDismiss = {
                        showAddFieldDialog = false
                        fieldToEdit = null
                        if (isSelectingCoordinates) {
                            isSelectingCoordinates = false
                            selectedCoordinates = emptyList<com.mobile.sap.data.model.Coordinate>()
                            // Restore field polygons
                            mapView?.getMapAsync { map ->
                                map.style?.let { style ->
                                    removeSelectionMarkers(style)
                                    addFieldsToMap(style, fields)
                                }
                            }
                        }
                    },
                    onAddField = { newField, farmId ->
                        if (fieldToEdit != null) {
                            // Update existing field
                            fieldViewModel.updateField(newField)
                        } else {
                            // Add new field on the chosen farm
                            fieldViewModel.addField(newField, farmId)
                        }
                        showAddFieldDialog = false
                        fieldToEdit = null
                        isSelectingCoordinates = false
                        selectedCoordinates = emptyList<com.mobile.sap.data.model.Coordinate>()
                        // Restore field polygons
                        mapView?.getMapAsync { map ->
                            map.style?.let { style ->
                                removeSelectionMarkers(style)
                                // Fields will be updated automatically via uiState
                            }
                        }
                    }
                )
            }

            // Delete Confirmation Dialog
            if (showDeleteConfirmation && fieldToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteConfirmation = false
                        fieldToDelete = null
                    },
                    title = {
                        Text(
                            text = "Delete Field",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to delete this field? This action cannot be undone.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                fieldToDelete?.let { field ->
                                    fieldViewModel.deleteField(field.id)
                                    fieldViewModel.selectField(null)
                                }
                                showDeleteConfirmation = false
                                fieldToDelete = null
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Delete", fontWeight = FontWeight.Medium)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteConfirmation = false
                            fieldToDelete = null
                        }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large
                )
            }

            DisposableEffect(Unit) {
                onDispose {
                    mapView?.onDestroy()
                }
            }
        }
    }
}

/**
 * Add field polygons to the map using proper GeoJSON with risk-based colors.
 * Fields with 3+ coordinates render as filled polygons; fields with 1-2
 * coordinates can't form a polygon, so they render as point markers (at their
 * average position) so they're still visible and selectable. Fields with no
 * coordinates are skipped (nothing to place).
 */
private fun addFieldsToMap(style: Style, fields: List<Field>) {
    Log.d("FieldsScreen", "addFieldsToMap called with ${fields.size} fields")

    // Remove existing sources and layers
    style.getLayer("field-fill-layer")?.let {
        style.removeLayer(it)
        Log.d("FieldsScreen", "Removed existing fill layer")
    }
    style.getLayer("field-border-layer")?.let {
        style.removeLayer(it)
        Log.d("FieldsScreen", "Removed existing border layer")
    }
    style.getLayer("field-marker-layer")?.let { style.removeLayer(it) }
    style.getSource("fields-source")?.let {
        style.removeSource(it)
        Log.d("FieldsScreen", "Removed existing source")
    }
    style.getSource("field-markers-source")?.let { style.removeSource(it) }

    if (fields.isEmpty()) {
        Log.w("FieldsScreen", "No fields to display")
        return
    }

    // Risk -> color, shared by polygons and markers.
    fun fillColorFor(field: Field): String = when (field.cultivationRisk) {
        CultivationRisk.LOW -> "#107E3E"    // Green
        CultivationRisk.MEDIUM -> "#E76500" // Orange
        CultivationRisk.HIGH -> "#BB0000"   // Red
        null -> "#107E3E"                   // Default green
    }

    fun Feature.tagWith(field: Field) = apply {
        addStringProperty("id", field.id)
        addStringProperty("region", field.region)
        addStringProperty("cropType", field.name)
        addStringProperty("risk", field.cultivationRisk?.name ?: "LOW")
        addStringProperty("fillColor", fillColorFor(field))
    }

    val polygonFields = fields.filter { it.coordinates.size >= 3 }
    val markerFields = fields.filter { it.coordinates.size in 1..2 }
    fields.filter { it.coordinates.isEmpty() }.forEach {
        Log.w("FieldsScreen", "Field ${it.id} has no coordinates; skipping (not drawable)")
    }

    try {
        if (polygonFields.isNotEmpty()) {
            val features = polygonFields.map { field ->
                val points = field.coordinates.map { coord ->
                    Point.fromLngLat(coord.longitude, coord.latitude)
                }.toMutableList()
                points.add(points.first()) // close the ring
                Feature.fromGeometry(Polygon.fromLngLats(listOf(points))).tagWith(field)
            }

            val geoJsonSource = GeoJsonSource("fields-source", FeatureCollection.fromFeatures(features))
            style.addSource(geoJsonSource)

            val fillLayer = FillLayer("field-fill-layer", "fields-source")
            fillLayer.setProperties(
                PropertyFactory.fillColor(
                    org.maplibre.android.style.expressions.Expression.get("fillColor")
                ),
                PropertyFactory.fillOpacity(0.5f)
            )
            style.addLayer(fillLayer)

            val borderLayer = LineLayer("field-border-layer", "fields-source")
            borderLayer.setProperties(
                PropertyFactory.lineColor(
                    org.maplibre.android.style.expressions.Expression.get("fillColor")
                ),
                PropertyFactory.lineWidth(3f),
                PropertyFactory.lineOpacity(0.9f)
            )
            style.addLayer(borderLayer)
            Log.d("FieldsScreen", "Added ${features.size} polygon fields")
        }

        if (markerFields.isNotEmpty()) {
            val markerFeatures = markerFields.map { field ->
                val avgLat = field.coordinates.map { it.latitude }.average()
                val avgLng = field.coordinates.map { it.longitude }.average()
                Feature.fromGeometry(Point.fromLngLat(avgLng, avgLat)).tagWith(field)
            }

            style.addSource(
                GeoJsonSource("field-markers-source", FeatureCollection.fromFeatures(markerFeatures))
            )
            val markerLayer = org.maplibre.android.style.layers.CircleLayer(
                "field-marker-layer", "field-markers-source"
            )
            markerLayer.setProperties(
                PropertyFactory.circleColor(
                    org.maplibre.android.style.expressions.Expression.get("fillColor")
                ),
                PropertyFactory.circleRadius(8f),
                PropertyFactory.circleStrokeColor("#FFFFFF"),
                PropertyFactory.circleStrokeWidth(2f),
                PropertyFactory.circleOpacity(0.9f)
            )
            style.addLayer(markerLayer)
            Log.d("FieldsScreen", "Added ${markerFeatures.size} point-marker fields (<3 coords)")
        }

    } catch (e: Exception) {
        Log.e("FieldsScreen", "Error adding fields to map", e)
        e.printStackTrace()
    }
}

/**
 * Remove field polygons and markers from the map
 */
private fun removeFieldsFromMap(style: Style) {
    style.getLayer("field-fill-layer")?.let { style.removeLayer(it) }
    style.getLayer("field-border-layer")?.let { style.removeLayer(it) }
    style.getLayer("field-marker-layer")?.let { style.removeLayer(it) }
    style.getSource("fields-source")?.let { style.removeSource(it) }
    style.getSource("field-markers-source")?.let { style.removeSource(it) }
}

/**
 * Update selection markers and preview polygon
 */
private fun updateSelectionMarkers(style: Style, coordinates: List<com.mobile.sap.data.model.Coordinate>) {
    // Remove existing selection layers
    removeSelectionMarkers(style)

    if (coordinates.isEmpty()) return

    try {
        // Create marker points
        val markerFeatures = coordinates.mapIndexed { index, coord ->
            Feature.fromGeometry(Point.fromLngLat(coord.longitude, coord.latitude)).apply {
                addNumberProperty("index", index.toDouble())
            }
        }

        // Add marker source
        val markerSource = GeoJsonSource("selection-markers-source", FeatureCollection.fromFeatures(markerFeatures))
        style.addSource(markerSource)

        // Add marker layer (circles)
        val markerLayer = org.maplibre.android.style.layers.CircleLayer("selection-markers-layer", "selection-markers-source")
        markerLayer.setProperties(
            PropertyFactory.circleRadius(10f),
            PropertyFactory.circleColor("#2E7D46"), // Leaf
            PropertyFactory.circleStrokeWidth(3f),
            PropertyFactory.circleStrokeColor("#FFFFFF")
        )
        style.addLayer(markerLayer)

        // If we have 3+ points, draw preview polygon
        if (coordinates.size >= 3) {
            val points = coordinates.map { coord ->
                Point.fromLngLat(coord.longitude, coord.latitude)
            }.toMutableList()

            // Close the polygon
            points.add(points.first())

            val polygon = Polygon.fromLngLats(listOf(points))
            val polygonFeature = Feature.fromGeometry(polygon)

            // Add polygon source
            val polygonSource = GeoJsonSource("selection-polygon-source", FeatureCollection.fromFeatures(listOf(polygonFeature)))
            style.addSource(polygonSource)

            // Add polygon fill layer
            val fillLayer = FillLayer("selection-polygon-fill", "selection-polygon-source")
            fillLayer.setProperties(
                PropertyFactory.fillColor("#2E7D46"),
                PropertyFactory.fillOpacity(0.3f)
            )
            style.addLayer(fillLayer)

            // Add polygon border layer
            val borderLayer = LineLayer("selection-polygon-border", "selection-polygon-source")
            borderLayer.setProperties(
                PropertyFactory.lineColor("#2E7D46"),
                PropertyFactory.lineWidth(3f),
                PropertyFactory.lineOpacity(0.8f)
            )
            style.addLayer(borderLayer)
        }
    } catch (e: Exception) {
        Log.e("FieldsScreen", "Error updating selection markers", e)
    }
}

/**
 * Remove selection markers from the map
 */
private fun removeSelectionMarkers(style: Style) {
    style.getLayer("selection-markers-layer")?.let { style.removeLayer(it) }
    style.getSource("selection-markers-source")?.let { style.removeSource(it) }
    style.getLayer("selection-polygon-fill")?.let { style.removeLayer(it) }
    style.getLayer("selection-polygon-border")?.let { style.removeLayer(it) }
    style.getSource("selection-polygon-source")?.let { style.removeSource(it) }
}

@Composable
fun CoordinateSelectionOverlay(
    selectedCount: Int,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tap the map to select field corners",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap a marker to remove it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$selectedCount point${if (selectedCount != 1) "s" else ""} selected ${if (selectedCount < 3) "(minimum 3 required)" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedCount < 3) Warning else Success,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onContinue,
                    modifier = Modifier.weight(1f),
                    enabled = selectedCount >= 3,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.outline,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Continue", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldDialog(
    existingField: Field? = null,
    preSelectedCoordinates: List<com.mobile.sap.data.model.Coordinate> = emptyList(),
    farms: List<com.mobile.sap.data.api.dto.FarmDto> = emptyList(),
    onAddFarm: (String, (Long) -> Unit) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    onAddField: (Field, Long) -> Unit
) {
    val isEditing = existingField != null
    var region by remember { mutableStateOf(existingField?.region ?: "") }
    var fieldName by remember { mutableStateOf(existingField?.name?.takeIf { it != "Field" } ?: "") }
    var selectedFarmId by remember { mutableStateOf(farms.firstOrNull()?.ID) }
    var guideline by remember { mutableStateOf(existingField?.cultivationGuideline ?: "") }
    var coordinatesText by remember {
        mutableStateOf(
            when {
                existingField != null -> existingField.coordinates.joinToString("; ") { "${it.latitude},${it.longitude}" }
                preSelectedCoordinates.isNotEmpty() -> preSelectedCoordinates.joinToString("; ") { "${it.latitude},${it.longitude}" }
                else -> ""
            }
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coordinatesReadOnly = preSelectedCoordinates.isNotEmpty() || isEditing

    // Inline "add new farm" flow from the farm dropdown.
    var showAddFarm by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Field" else "Add New Field",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Region dropdown (Cameroon's 10 regions)
                var expandedRegion by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedRegion,
                    onExpandedChange = { expandedRegion = it }
                ) {
                    OutlinedTextField(
                        value = region,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Region *", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        shape = MaterialTheme.shapes.small,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRegion) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRegion,
                        onDismissRequest = { expandedRegion = false }
                    ) {
                        com.mobile.sap.data.model.CameroonRegions.names.forEach { regionName ->
                            DropdownMenuItem(
                                text = { Text(regionName, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    region = regionName
                                    errorMessage = null
                                    expandedRegion = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Field name
                OutlinedTextField(
                    value = fieldName,
                    onValueChange = { fieldName = it; errorMessage = null },
                    label = { Text("Field Name *", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Farm dropdown (the caller's farms). Editing keeps the field on
                // its current farm — the picker only applies when creating.
                if (!isEditing) {
                    var expandedFarm by remember { mutableStateOf(false) }
                    val selectedFarmName = farms.firstOrNull { it.ID == selectedFarmId }?.name
                        ?: "Select a farm"
                    ExposedDropdownMenuBox(
                        expanded = expandedFarm,
                        onExpandedChange = { expandedFarm = it }
                    ) {
                        OutlinedTextField(
                            value = selectedFarmName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Farm *", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            shape = MaterialTheme.shapes.small,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFarm) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFarm,
                            onDismissRequest = { expandedFarm = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Add new farm", color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    expandedFarm = false
                                    showAddFarm = true
                                }
                            )
                            if (farms.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No farms — create one in the Farms tab", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { expandedFarm = false }
                                )
                            } else {
                                farms.forEach { farm ->
                                    DropdownMenuItem(
                                        text = { Text(farm.name ?: "Unnamed farm", color = MaterialTheme.colorScheme.onSurface) },
                                        onClick = {
                                            selectedFarmId = farm.ID
                                            errorMessage = null
                                            expandedFarm = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Guideline
                OutlinedTextField(
                    value = guideline,
                    onValueChange = { guideline = it },
                    label = { Text("Notes", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Coordinates
                OutlinedTextField(
                    value = coordinatesText,
                    onValueChange = { if (!coordinatesReadOnly) { coordinatesText = it; errorMessage = null } },
                    label = {
                        Text(
                            text = if (coordinatesReadOnly) "Coordinates (from map selection) *" else "Coordinates (lat,lng; lat,lng; ...) *",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    readOnly = coordinatesReadOnly,
                    enabled = !coordinatesReadOnly,
                    placeholder = { if (!coordinatesReadOnly) Text("3.850,11.500; 3.851,11.502; ...", color = MaterialTheme.colorScheme.outline) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
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
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Validate required fields
                            if (region.isBlank() || fieldName.isBlank() || coordinatesText.isBlank()) {
                                errorMessage = "Please fill all required fields"
                                return@Button
                            }
                            val farmId = if (isEditing) 0L else selectedFarmId
                            if (!isEditing && farmId == null) {
                                errorMessage = "Please select a farm"
                                return@Button
                            }

                            // Parse coordinates
                            try {
                                val coordinates = coordinatesText.split(";")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }
                                    .map { coord: String ->
                                        val parts = coord.split(",").map { it.trim().toDouble() }
                                        if (parts.size != 2) throw IllegalArgumentException()
                                        com.mobile.sap.data.model.Coordinate(latitude = parts[0], longitude = parts[1])
                                    }

                                if (coordinates.size < 3) {
                                    errorMessage = "At least 3 coordinates required"
                                    return@Button
                                }

                                // Create or update field object
                                val newField = com.mobile.sap.data.model.Field(
                                    id = existingField?.id ?: java.util.UUID.randomUUID().toString(),
                                    name = fieldName.trim(),
                                    region = region,
                                    coordinates = coordinates,
                                    cultivationGuideline = guideline.ifBlank { null },
                                    createdAt = existingField?.createdAt,
                                    updatedAt = existingField?.updatedAt
                                )

                                onAddField(newField, farmId ?: 0L)
                            } catch (e: Exception) {
                                errorMessage = "Invalid coordinates format"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(if (isEditing) "Save Changes" else "Add Field", fontWeight = FontWeight.Medium)
                    }
                }
        }
    }

    if (showAddFarm) {
        var newFarmName by remember { mutableStateOf("") }
        val addFarmSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAddFarm = false },
            sheetState = addFarmSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "New farm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newFarmName,
                    onValueChange = { newFarmName = it },
                    label = { Text("Farm name", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                PrimaryButton(
                    text = "Add",
                    onClick = {
                        val name = newFarmName.trim()
                        if (name.isNotEmpty()) {
                            onAddFarm(name) { newId ->
                                // Auto-select the freshly created farm.
                                selectedFarmId = newId
                                errorMessage = null
                            }
                            showAddFarm = false
                        }
                    },
                    enabled = newFarmName.isNotBlank()
                )
            }
        }
    }
}
