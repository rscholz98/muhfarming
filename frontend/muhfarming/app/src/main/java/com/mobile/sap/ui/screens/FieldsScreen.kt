package com.mobile.sap.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import com.mobile.sap.data.model.CameroonCities
import com.mobile.sap.data.model.CultivationRisk
import com.mobile.sap.data.model.Field
import com.mobile.sap.data.model.HazardSeverity
import com.mobile.sap.ui.theme.*
import com.mobile.sap.ui.viewmodel.FieldUiState
import com.mobile.sap.ui.viewmodel.FieldViewModel
import com.mobile.sap.ui.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldsScreen(
    weatherViewModel: WeatherViewModel = viewModel(),
    fieldViewModel: FieldViewModel = viewModel(),
    isAdmin: Boolean = false
) {
    val context = LocalContext.current
    val location by weatherViewModel.location.collectAsState()
    val fieldUiState by fieldViewModel.uiState.collectAsState()
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

    // Load all fields (mocked data - no backend yet)
    LaunchedEffect(Unit) {
        Log.d("FieldsScreen", "Loading all fields from mocked data")
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
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            letterSpacing = 0.sp
                        )
                        Text(
                            text = location,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            letterSpacing = 0.sp,
                            color = FioriWhite.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FioriBlue,
                    titleContentColor = FioriWhite
                )
            )
        },
        containerColor = FioriLightGray
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            var mapView: MapView? by remember { mutableStateOf(null) }
            var fields by remember { mutableStateOf<List<Field>>(emptyList()) }
            var mapStyleLoaded by remember { mutableStateOf(false) }

            // Update fields when UI state changes
            LaunchedEffect(fieldUiState) {
                if (fieldUiState is FieldUiState.Success) {
                    val newFields = (fieldUiState as FieldUiState.Success).fields
                    Log.d("FieldsScreen", "UI State updated with ${newFields.size} fields")
                    fields = newFields

                    // If map style is already loaded, add fields immediately
                    if (mapStyleLoaded) {
                        mapView?.getMapAsync { map ->
                            map.style?.let { style ->
                                Log.d("FieldsScreen", "Adding fields after state change")
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
                                        // Query features at click point
                                        val screenPoint = map.projection.toScreenLocation(point)
                                        val features = map.queryRenderedFeatures(screenPoint, "field-fill-layer")

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


            // Loading indicator
            if (fieldUiState is FieldUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = FioriBlue
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
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = FioriWhite.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FioriBlack
                            )
                            val fieldCount = if (fieldUiState is FieldUiState.Success) {
                                (fieldUiState as FieldUiState.Success).fields.size
                            } else {
                                0
                            }
                            Text(
                                text = "$fieldCount field${if (fieldCount != 1) "s" else ""} nearby",
                                fontSize = 11.sp,
                                color = FioriDarkGray.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Field details card when a field is selected
            selectedField?.let { field ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FioriWhite
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Header with close and edit buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = field.cultivation?.cropType ?: "Field",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = FioriBlack
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
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Field",
                                            tint = FioriBlue
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            fieldToDelete = field
                                            showDeleteConfirmation = true
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Field",
                                            tint = FioriError
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { fieldViewModel.selectField(null) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = FioriDarkGray
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
                                fontSize = 14.sp,
                                color = FioriDarkGray
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = FioriGray)
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
                                        fontSize = 12.sp,
                                        color = FioriDarkGray.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = cultivation.season,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = FioriBlack
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Status",
                                        fontSize = 12.sp,
                                        color = FioriDarkGray.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = cultivation.status,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = FioriBlue
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Cultivation Risk
                        field.cultivationRisk?.let { risk ->
                            val riskColor = when (risk) {
                                CultivationRisk.LOW -> FioriSuccess
                                CultivationRisk.MEDIUM -> FioriWarning
                                CultivationRisk.HIGH -> FioriError
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Risk Level:",
                                    fontSize = 14.sp,
                                    color = FioriDarkGray
                                )
                                Surface(
                                    color = riskColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = risk.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = riskColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Cultivation Guideline
                        field.cultivationGuideline?.let { guideline ->
                            Text(
                                text = "Guidelines",
                                fontSize = 12.sp,
                                color = FioriDarkGray.copy(alpha = 0.7f)
                            )
                            Text(
                                text = guideline,
                                fontSize = 13.sp,
                                color = FioriBlack,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Incidents
                        if (field.incidents.isNotEmpty()) {
                            Text(
                                text = "⚠️ Incidents (${field.incidents.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FioriError
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            field.incidents.forEach { incident ->
                                Text(
                                    text = "• ${incident.type}: ${incident.description}",
                                    fontSize = 12.sp,
                                    color = FioriDarkGray,
                                    lineHeight = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Hazards
                        if (field.hazards.isNotEmpty()) {
                            Text(
                                text = "⚡ Hazards (${field.hazards.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FioriWarning
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            field.hazards.forEach { hazard ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 12.sp,
                                        color = FioriDarkGray
                                    )
                                    Text(
                                        text = hazard.name,
                                        fontSize = 12.sp,
                                        color = FioriDarkGray
                                    )
                                    Text(
                                        text = "(${hazard.severity})",
                                        fontSize = 11.sp,
                                        color = when (hazard.severity) {
                                            HazardSeverity.LOW -> FioriSuccess
                                            HazardSeverity.MEDIUM -> FioriWarning
                                            HazardSeverity.HIGH -> FioriError
                                        }
                                    )
                                }
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
                    containerColor = FioriBlue,
                    contentColor = FioriWhite
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
                    onAddField = { newField ->
                        if (fieldToEdit != null) {
                            // Update existing field
                            fieldViewModel.updateField(newField)
                        } else {
                            // Add new field
                            fieldViewModel.addField(newField)
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
                            color = FioriBlack
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to delete this field? This action cannot be undone.",
                            color = FioriDarkGray
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
                            fieldToDelete = null
                        }) {
                            Text("Cancel", color = FioriDarkGray, fontWeight = FontWeight.Medium)
                        }
                    },
                    containerColor = FioriWhite,
                    shape = RoundedCornerShape(20.dp)
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
 * Add field polygons to the map using proper GeoJSON with risk-based colors
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
    style.getSource("fields-source")?.let {
        style.removeSource(it)
        Log.d("FieldsScreen", "Removed existing source")
    }

    if (fields.isEmpty()) {
        Log.w("FieldsScreen", "No fields to display")
        return
    }

    try {
        // Create GeoJSON features from fields
        val features = fields.mapIndexed { index, field ->
            Log.d("FieldsScreen", "Processing field $index: ${field.id} with ${field.coordinates.size} coordinates")

            // Convert coordinates to Point list for Polygon
            val points = field.coordinates.map { coord ->
                Point.fromLngLat(coord.longitude, coord.latitude)
            }.toMutableList()

            // Close the polygon by adding first point at the end
            if (points.isNotEmpty()) {
                points.add(points.first())
                Log.d("FieldsScreen", "Field ${field.id} polygon closed with ${points.size} points")
            }

            // Create Polygon geometry
            val polygon = Polygon.fromLngLats(listOf(points))

            // Determine color based on risk level
            val riskLevel = field.cultivationRisk?.name ?: "LOW"
            val fillColor = when (field.cultivationRisk) {
                CultivationRisk.LOW -> "#107E3E"    // Green
                CultivationRisk.MEDIUM -> "#E76500" // Orange
                CultivationRisk.HIGH -> "#BB0000"   // Red
                null -> "#107E3E"                   // Default green
            }

            // Create Feature with properties including color
            Feature.fromGeometry(polygon).apply {
                addStringProperty("id", field.id)
                addStringProperty("region", field.region)
                addStringProperty("cropType", field.cultivation?.cropType ?: "Unknown")
                addStringProperty("risk", riskLevel)
                addStringProperty("fillColor", fillColor)
            }
        }

        Log.d("FieldsScreen", "Created ${features.size} features")

        // Create FeatureCollection
        val featureCollection = FeatureCollection.fromFeatures(features)

        // Add GeoJSON source
        val geoJsonSource = GeoJsonSource("fields-source", featureCollection)
        style.addSource(geoJsonSource)
        Log.d("FieldsScreen", "Added GeoJSON source")

        // Add fill layer for field polygons with data-driven color
        val fillLayer = FillLayer("field-fill-layer", "fields-source")
        fillLayer.setProperties(
            PropertyFactory.fillColor(
                org.maplibre.android.style.expressions.Expression.get("fillColor")
            ),
            PropertyFactory.fillOpacity(0.5f)
        )
        style.addLayer(fillLayer)
        Log.d("FieldsScreen", "Added fill layer with risk-based colors")

        // Add border layer for field boundaries with matching color
        val borderLayer = LineLayer("field-border-layer", "fields-source")
        borderLayer.setProperties(
            PropertyFactory.lineColor(
                org.maplibre.android.style.expressions.Expression.get("fillColor")
            ),
            PropertyFactory.lineWidth(3f),
            PropertyFactory.lineOpacity(0.9f)
        )
        style.addLayer(borderLayer)
        Log.d("FieldsScreen", "Added border layer - fields should now be visible with risk colors!")

    } catch (e: Exception) {
        Log.e("FieldsScreen", "Error adding fields to map", e)
        e.printStackTrace()
    }
}

/**
 * Remove field polygons from the map
 */
private fun removeFieldsFromMap(style: Style) {
    style.getLayer("field-fill-layer")?.let { style.removeLayer(it) }
    style.getLayer("field-border-layer")?.let { style.removeLayer(it) }
    style.getSource("fields-source")?.let { style.removeSource(it) }
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
            PropertyFactory.circleColor("#1B5EBE"), // FioriBlue
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
                PropertyFactory.fillColor("#1B5EBE"),
                PropertyFactory.fillOpacity(0.3f)
            )
            style.addLayer(fillLayer)

            // Add polygon border layer
            val borderLayer = LineLayer("selection-polygon-border", "selection-polygon-source")
            borderLayer.setProperties(
                PropertyFactory.lineColor("#1B5EBE"),
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FioriWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tap the map to select field corners",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = FioriBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap a marker to remove it",
                fontSize = 13.sp,
                color = FioriDarkGray,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$selectedCount point${if (selectedCount != 1) "s" else ""} selected ${if (selectedCount < 3) "(minimum 3 required)" else ""}",
                fontSize = 14.sp,
                color = if (selectedCount < 3) FioriWarning else FioriSuccess,
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
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = FioriDarkGray
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FioriGray)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onContinue,
                    modifier = Modifier.weight(1f),
                    enabled = selectedCount >= 3,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FioriBlue,
                        contentColor = FioriWhite,
                        disabledContainerColor = FioriGray,
                        disabledContentColor = FioriDarkGray
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
    onDismiss: () -> Unit,
    onAddField: (Field) -> Unit
) {
    val isEditing = existingField != null
    var region by remember { mutableStateOf(existingField?.region ?: "") }
    var cropType by remember { mutableStateOf(existingField?.cultivation?.cropType ?: "") }
    var season by remember { mutableStateOf(existingField?.cultivation?.season ?: "") }
    var status by remember { mutableStateOf(existingField?.cultivation?.status ?: "") }
    var guideline by remember { mutableStateOf(existingField?.cultivationGuideline ?: "") }
    var riskLevel by remember { mutableStateOf(existingField?.cultivationRisk?.name ?: "LOW") }
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
                        text = if (isEditing) "Edit Field" else "Add New Field",
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

                // Region field
                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it; errorMessage = null },
                    label = { Text("Region *", color = FioriDarkGray) },
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

                // Crop Type
                OutlinedTextField(
                    value = cropType,
                    onValueChange = { cropType = it },
                    label = { Text("Crop Type *", color = FioriDarkGray) },
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

                // Season and Status in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = season,
                        onValueChange = { season = it },
                        label = { Text("Season *", color = FioriDarkGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FioriBlue,
                            focusedLabelColor = FioriBlue,
                            unfocusedBorderColor = FioriGray,
                            unfocusedLabelColor = FioriDarkGray,
                            focusedTextColor = FioriBlack,
                            unfocusedTextColor = FioriBlack
                        )
                    )
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("Status *", color = FioriDarkGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FioriBlue,
                            focusedLabelColor = FioriBlue,
                            unfocusedBorderColor = FioriGray,
                            unfocusedLabelColor = FioriDarkGray,
                            focusedTextColor = FioriBlack,
                            unfocusedTextColor = FioriBlack
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Risk Level Dropdown
                var expandedRisk by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedRisk,
                    onExpandedChange = { expandedRisk = it }
                ) {
                    OutlinedTextField(
                        value = riskLevel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Risk Level", color = FioriDarkGray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRisk) },
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
                        expanded = expandedRisk,
                        onDismissRequest = { expandedRisk = false }
                    ) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { risk ->
                            DropdownMenuItem(
                                text = { Text(risk, color = FioriBlack) },
                                onClick = {
                                    riskLevel = risk
                                    expandedRisk = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Guideline
                OutlinedTextField(
                    value = guideline,
                    onValueChange = { guideline = it },
                    label = { Text("Cultivation Guideline", color = FioriDarkGray) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
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

                // Coordinates
                OutlinedTextField(
                    value = coordinatesText,
                    onValueChange = { if (!coordinatesReadOnly) { coordinatesText = it; errorMessage = null } },
                    label = {
                        Text(
                            text = if (coordinatesReadOnly) "Coordinates (from map selection) *" else "Coordinates (lat,lng; lat,lng; ...) *",
                            color = FioriDarkGray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    readOnly = coordinatesReadOnly,
                    enabled = !coordinatesReadOnly,
                    placeholder = { if (!coordinatesReadOnly) Text("3.850,11.500; 3.851,11.502; ...", color = FioriGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FioriBlue,
                        focusedLabelColor = FioriBlue,
                        unfocusedBorderColor = FioriGray,
                        unfocusedLabelColor = FioriDarkGray,
                        focusedTextColor = FioriBlack,
                        unfocusedTextColor = FioriBlack,
                        disabledTextColor = FioriDarkGray,
                        disabledBorderColor = FioriGray,
                        disabledLabelColor = FioriDarkGray
                    )
                )

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
                            if (region.isBlank() || cropType.isBlank() || season.isBlank() ||
                                status.isBlank() || coordinatesText.isBlank()) {
                                errorMessage = "Please fill all required fields"
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
                                    region = region,
                                    coordinates = coordinates,
                                    cultivation = com.mobile.sap.data.model.Cultivation(
                                        cropType = cropType,
                                        season = season,
                                        status = status
                                    ),
                                    cultivationGuideline = guideline.ifBlank { null },
                                    cultivationRisk = com.mobile.sap.data.model.CultivationRisk.valueOf(riskLevel),
                                    incidents = existingField?.incidents ?: emptyList(),
                                    hazards = existingField?.hazards ?: emptyList(),
                                    fertilizers = existingField?.fertilizers ?: emptyList(),
                                    createdAt = existingField?.createdAt,
                                    updatedAt = existingField?.updatedAt
                                )

                                onAddField(newField)
                            } catch (e: Exception) {
                                errorMessage = "Invalid coordinates format"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FioriBlue,
                            contentColor = FioriWhite
                        )
                    ) {
                        Text(if (isEditing) "Save Changes" else "Add Field", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
