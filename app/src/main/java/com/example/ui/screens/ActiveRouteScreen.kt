package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ActionType
import com.example.ai.GeminiClient
import com.example.data.Parcel
import com.example.data.SavedLandmark
import com.example.ui.DeliveryViewModel
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import android.content.Context
import kotlinx.coroutines.launch

import androidx.compose.foundation.lazy.*

data class RuralLandmark(
    val name: String,
    val description: String,
    val intelTip: String,
    val latitude: Double,
    val longitude: Double,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// Global helper for Haversine distance calculation in kilometers
fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth's radius in kilometers
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

@Composable
fun ActiveRouteScreen(
    viewModel: DeliveryViewModel,
    onNavigateToParcelDetail: (Int) -> Unit
) {
    val parcels by viewModel.parcels.collectAsState()
    val learningRecords by viewModel.learningRecords.collectAsState()
    val savedLandmarks by viewModel.savedLandmarks.collectAsState()
    val scope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current

    // Permission launcher for location services
    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.startLocationTracking(context)
        } else {
            viewModel.speakText("Location permissions were denied. Google Maps GPS tracking disabled.")
        }
    }

    LaunchedEffect(Unit) {
        // Automatically request permissions and start location tracking
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val pendingParcels = parcels.filter { it.status == "Pending" }
    val nextParcel = pendingParcels.minByOrNull { it.deliverySequence }

    var showMicPanel by remember { mutableStateOf(false) }
    var activeIntelTip by remember { mutableStateOf("Tap a parcel node to query AI village landmarks...") }
    var queryLoading by remember { mutableStateOf(false) }

    // MAP INTERACTIVES: Zoom, Pan and Sizes
    var mapMode by remember { mutableStateOf("gmaps") } // "gmaps", "radar" or "osm"
    var zoomScale by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var googleMapType by remember { mutableStateOf(com.google.maps.android.compose.MapType.NORMAL) }
    var leafletMapStyleType by remember { mutableStateOf("dark") } // "dark", "classic", "satellite"
    var isListExpanded by remember { mutableStateOf(false) }
    var showRouteSequencerDialog by remember { mutableStateOf(false) }

    var selectedLandmark by remember { mutableStateOf<RuralLandmark?>(null) }
    var selectedParcel by remember { mutableStateOf<Parcel?>(null) }
    var selectedSavedLandmark by remember { mutableStateOf<SavedLandmark?>(null) }
    var lastTappedCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // Save Landmark Dialog States
    var showSaveLandmarkDialog by remember { mutableStateOf(false) }
    var landmarkName by remember { mutableStateOf("") }
    var landmarkLat by remember { mutableStateOf("") }
    var landmarkLng by remember { mutableStateOf("") }
    var landmarkColor by remember { mutableStateOf("#F59E0B") }

    // SIMULATION STATE ENGINE
    var isSimulating by remember { mutableStateOf(false) }
    var simSpeed by remember { mutableStateOf(1f) } // 1x, 2x, 5x speed
    var simProgress by remember { mutableStateOf(0f) } // 0f to 1f between points
    var currentSimNodeIndex by remember { mutableStateOf(0) }

    // Recompute simulation points based on active pending route
    val waypoints = remember(pendingParcels) {
        val list = mutableListOf<Pair<Double, Double>>()
        // Start Hub at central Bihar rural point
        list.add(25.602 to 85.132)
        pendingParcels.forEach { p ->
            list.add((p.latitude ?: 25.61) to (p.longitude ?: 85.14))
        }
        list
    }

    // Courier lat/lng based on simulation status or fallback to active next parcel
    val riderLatLng = remember(waypoints, currentSimNodeIndex, simProgress, isSimulating, nextParcel, viewModel.realLocation) {
        if (!isSimulating) {
            val realLoc = viewModel.realLocation
            if (realLoc != null) {
                realLoc
            } else if (nextParcel != null) {
                // Keep rider slightly shifted from target or at a central position
                (nextParcel.latitude ?: 25.61) - 0.005 to (nextParcel.longitude ?: 85.14) - 0.005
            } else {
                25.602 to 85.132
            }
        } else {
            if (waypoints.isEmpty()) {
                25.602 to 85.132
            } else if (currentSimNodeIndex + 1 < waypoints.size) {
                val start = waypoints[currentSimNodeIndex]
                val end = waypoints[currentSimNodeIndex + 1]
                val lat = start.first + (end.first - start.first) * simProgress
                val lng = start.second + (end.second - start.second) * simProgress
                lat to lng
            } else {
                waypoints.last()
            }
        }
    }

    // Infinite pulse animations for hazard circles and sequence highlights
    val infiniteTransition = rememberInfiniteTransition(label = "hazard_pulse_anim")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 54f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Landmark list anchored geographically in Bihar delivery context
    val landmarks = remember {
        listOf(
            RuralLandmark(
                name = "Shiv Mandir Junction",
                description = "Ancient brick temple and major village gateway.",
                intelTip = "⚠️ LOCAL TIP: High pedestrian traffic near steps. Vehicles cannot pass temple gates, use lane bypass.",
                latitude = 25.62,
                longitude = 85.135,
                color = Color(0xFFF59E0B), // Amber
                icon = Icons.Default.Place
            ),
            RuralLandmark(
                name = "Rampur Primary School",
                description = "Government school serving local communities.",
                intelTip = "💡 LOCAL TIP: Paved access is exclusively via the north entrance. Avoid playground path during school hours.",
                latitude = 25.625,
                longitude = 85.17,
                color = Color(0xFF10B981), // Emerald
                icon = Icons.Default.School
            ),
            RuralLandmark(
                name = "Banyan Tree Gathering",
                description = "Centuries old banyan tree; local community spot.",
                intelTip = "💡 LOCAL TIP: Best landmark for offline addresses. Tappu's tea stall is right here and has Wi-Fi.",
                latitude = 25.598,
                longitude = 85.148,
                color = Color(0xFF84CC16), // Lime Green
                icon = Icons.Default.Info
            ),
            RuralLandmark(
                name = "Sone Canal Bridge",
                description = "Narrow bridge crossing the canal.",
                intelTip = "⚠️ FLOOD ALERT: Bridge planks get submerged in heavy monsoon. Verify route with local farmers.",
                latitude = 25.615,
                longitude = 85.158,
                color = Color(0xFF38BDF8), // Light Blue
                icon = Icons.Default.Warning
            ),
            RuralLandmark(
                name = "Gram Panchayat Bhawan",
                description = "Village administrative centroid.",
                intelTip = "💡 LOCAL TIP: Public high-speed Wi-Fi hotspot active here under BharatNet scheme. Good backup spot.",
                latitude = 25.602,
                longitude = 85.178,
                color = Color(0xFFA855F7), // Purple
                icon = Icons.Default.Home
            )
        )
    }

    // Fetch village intelligence when the next parcel loads
    LaunchedEffect(nextParcel) {
        if (nextParcel != null) {
            queryLoading = true
            activeIntelTip = GeminiClient.getVillageIntelligence(nextParcel, learningRecords)
            queryLoading = false
        } else {
            activeIntelTip = "Great work! All parcels delivered on this route."
        }
    }

    // Drive GPS coordinate simulation progress smoothly
    LaunchedEffect(isSimulating, currentSimNodeIndex, simSpeed, waypoints) {
        if (isSimulating && waypoints.size > 1) {
            while (true) {
                kotlinx.coroutines.delay((60 / simSpeed).toLong())
                simProgress += 0.015f
                if (simProgress >= 1.0f) {
                    simProgress = 0f
                    val nextIndex = currentSimNodeIndex + 1
                    if (nextIndex < waypoints.size - 1) {
                        currentSimNodeIndex = nextIndex
                    } else {
                        // Loop back to start
                        currentSimNodeIndex = 0
                    }
                }
            }
        }
    }

    // Keep simulation indices safe if pending parcels set shrinks
    LaunchedEffect(pendingParcels.size) {
        currentSimNodeIndex = 0
        simProgress = 0f
    }

    // Automatically update simulation intel tips as the rider moves
    LaunchedEffect(currentSimNodeIndex, isSimulating) {
        if (isSimulating && pendingParcels.isNotEmpty()) {
            val approachingP = pendingParcels.getOrNull(currentSimNodeIndex)
            if (approachingP != null) {
                val payMethodStr = if (approachingP.codAmount > 0.0) "COD" else "Prepaid"
                activeIntelTip = "SIMULATOR GPS: Approaching ${approachingP.customerName}'s sector. Collect target ₹${approachingP.codAmount} ($payMethodStr). Roads ahead are passable."
            }
        }
    }

    // Pixel width/height based on measured layout constraints
    val width = if (canvasSize.width > 0) canvasSize.width.toFloat() else 1000f
    val height = if (canvasSize.height > 0) canvasSize.height.toFloat() else 1500f

    // Coordinate helper formula mapping Lat/Lng bounds dynamically into canvas boundaries
    val getCanvasCoords = { lat: Double, lng: Double ->
        val x = (width * 0.15f + (lng - 85.13) * width * 15f).toFloat()
        val y = (height * 0.25f + (25.63 - lat) * height * 15f).toFloat()
        Offset(x, y)
    }

    // Inverse coordinate formula translating Canvas pixel back to Lat/Lng
    val getCoordsFromCanvas = { offset: Offset ->
        val lng = 85.13 + (offset.x - width * 0.15f) / (width * 15f)
        val lat = 25.63 - (offset.y - height * 0.25f) / (height * 15f)
        lat to lng
    }

    // Reverse matrix calculation to translate screen touch back into Canvas coordinate space
    val getCanvasTapSpace = { tap: Offset ->
        val cx = (tap.x - panOffset.x - (width / 2f) * (1f - zoomScale)) / zoomScale
        val cy = (tap.y - panOffset.y - (height / 2f) * (1f - zoomScale)) / zoomScale
        Offset(cx, cy)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.CyberDark)
            .onSizeChanged { canvasSize = it }
    ) {
        if (mapMode == "radar") {
            // High fidelity Custom Drawn Interactive Delivery Map Grid
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("route_map_canvas")
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(1f, 4f)
                        val maxPanX = width * 0.6f * (zoomScale - 1f)
                        val maxPanY = height * 0.6f * (zoomScale - 1f)
                        panOffset = Offset(
                            x = (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                            y = (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val canvasTap = getCanvasTapSpace(tapOffset)

                        // 1. Check if user tapped a parcel pin (within 45px radius)
                        var tappedP: Parcel? = null
                        parcels.forEach { p ->
                            val pinPos = getCanvasCoords(p.latitude ?: 25.61, p.longitude ?: 85.14)
                            if ((pinPos - canvasTap).getDistance() < 45f) {
                                tappedP = p
                            }
                        }

                        // 2. Check if user tapped a rural landmark (within 50px radius)
                        var tappedL: RuralLandmark? = null
                        landmarks.forEach { l ->
                            val landmarkPos = getCanvasCoords(l.latitude, l.longitude)
                            if ((landmarkPos - canvasTap).getDistance() < 50f) {
                                tappedL = l
                            }
                        }

                        // 2b. Check if user tapped a saved local landmark (within 50px radius)
                        var tappedSL: SavedLandmark? = null
                        savedLandmarks.forEach { sl ->
                            val landmarkPos = getCanvasCoords(sl.latitude, sl.longitude)
                            if ((landmarkPos - canvasTap).getDistance() < 50f) {
                                tappedSL = sl
                            }
                        }

                        // Trigger visual states based on selections
                        if (tappedP != null) {
                            selectedParcel = tappedP
                            selectedLandmark = null
                            selectedSavedLandmark = null
                            lastTappedCoords = null
                            activeIntelTip = "Focusing on delivery target: ${tappedP!!.customerName} in village hub."
                        } else if (tappedL != null) {
                            selectedLandmark = tappedL
                            selectedParcel = null
                            selectedSavedLandmark = null
                            lastTappedCoords = null
                            activeIntelTip = tappedL!!.intelTip
                        } else if (tappedSL != null) {
                            selectedSavedLandmark = tappedSL
                            selectedParcel = null
                            selectedLandmark = null
                            lastTappedCoords = null
                            activeIntelTip = tappedSL!!.historicalFact
                        } else {
                            // Tap on empty space closes detail cards and records tapped coordinates
                            selectedParcel = null
                            selectedLandmark = null
                            selectedSavedLandmark = null
                            val tappedCoords = getCoordsFromCanvas(canvasTap)
                            // Keep them within Bihar simulation bounds
                            if (tappedCoords.first in 25.55..25.65 && tappedCoords.second in 85.10..85.20) {
                                lastTappedCoords = tappedCoords
                                activeIntelTip = "Tapped coordinate: (lat: ${String.format("%.4f", tappedCoords.first)}, lng: ${String.format("%.4f", tappedCoords.second)}). Tap 'Save Tapped Location' to document with Gemini."
                            } else {
                                lastTappedCoords = null
                            }
                        }
                    }
                }
        ) {
            // Apply Pinch and Drag transformations to the entire Canvas drawing
            withTransform({
                translate(left = panOffset.x, top = panOffset.y)
                scale(scaleX = zoomScale, scaleY = zoomScale, pivot = Offset(width / 2f, height / 2f))
            }) {
                // 1. Draw rural background grids
                val gridSpacing = 80.dp.toPx()
                for (x in 0..(width / gridSpacing).toInt()) {
                    drawLine(
                        color = Color(0xFF1E293B).copy(alpha = 0.5f),
                        start = Offset(x * gridSpacing, 0f),
                        end = Offset(x * gridSpacing, height),
                        strokeWidth = 2f
                    )
                }
                for (y in 0..(height / gridSpacing).toInt()) {
                    drawLine(
                        color = Color(0xFF1E293B).copy(alpha = 0.5f),
                        start = Offset(0f, y * gridSpacing),
                        end = Offset(width, y * gridSpacing),
                        strokeWidth = 2f
                    )
                }

                // 2. Draw organic flowing Blue River canal
                val riverPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(width * -0.1f, height * 0.65f)
                    cubicTo(
                        width * 0.35f, height * 0.58f,
                        width * 0.52f, height * 0.32f,
                        width * 1.1f, height * 0.44f
                    )
                }
                drawPath(
                    path = riverPath,
                    color = Color(0xFF0284C7).copy(alpha = 0.25f), // Sky-blue water bed
                    style = Stroke(width = 36f)
                )
                drawPath(
                    path = riverPath,
                    color = Color(0xFF38BDF8).copy(alpha = 0.5f),
                    style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 20f)))
                )

                // 3. Draw Green Orchard & Forest patches on the map
                // Mango Orchard (Top Left)
                drawCircle(Color(0xFF0F766E).copy(alpha = 0.15f), radius = 130f, center = Offset(width * 0.18f, height * 0.16f))
                drawCircle(Color(0xFF115E59).copy(alpha = 0.10f), radius = 90f, center = Offset(width * 0.23f, height * 0.12f))

                // Banyan Forest Block (Bottom Center)
                drawCircle(Color(0xFF166534).copy(alpha = 0.14f), radius = 110f, center = Offset(width * 0.38f, height * 0.76f))
                drawCircle(Color(0xFF15803D).copy(alpha = 0.08f), radius = 140f, center = Offset(width * 0.44f, height * 0.79f))

                // 4. Draw Secondary unpaved shortcuts / dirt roads (Dashed Amber)
                val dirtShortcut = androidx.compose.ui.graphics.Path().apply {
                    moveTo(width * 0.12f, height * 0.22f)
                    quadraticTo(width * 0.32f, height * 0.28f, width * 0.42f, height * 0.52f)
                    quadraticTo(width * 0.52f, height * 0.78f, width * 0.88f, height * 0.82f)
                }
                drawPath(
                    path = dirtShortcut,
                    color = Color(0xFFD97706).copy(alpha = 0.2f),
                    style = Stroke(width = 10f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
                )

                // 5. Write territorial labels inside native canvas
                drawContext.canvas.nativeCanvas.apply {
                    val labelPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(80, 226, 232, 240) // White translucent
                        textSize = 26f
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText("RAMPUR TERRITORY", width * 0.25f, height * 0.2f, labelPaint)
                    drawText("PIPRI SOUTH SECTOR", width * 0.45f, height * 0.88f, labelPaint)
                    drawText("BISHNUPUR BORDER", width * 0.82f, height * 0.45f, labelPaint)
                }

                // 6. Draw dynamic hazard alerts (Flashing waterlogging circle near River Bridge)
                val bridgeCoords = getCanvasCoords(25.615, 85.158)
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = bridgeCoords
                )

                // 7. Draw Landmarks
                landmarks.forEach { l ->
                    val coords = getCanvasCoords(l.latitude, l.longitude)
                    val isSelected = selectedLandmark?.name == l.name

                    // Landmark glowing outer circle
                    drawCircle(
                        color = l.color.copy(alpha = if (isSelected) 0.35f else 0.15f),
                        radius = if (isSelected) 55f else 40f,
                        center = coords
                    )
                    // Inner dot
                    drawCircle(
                        color = l.color,
                        radius = 8f,
                        center = coords
                    )
                }

                // 7b. Draw Saved Landmarks
                savedLandmarks.forEach { sl ->
                    val coords = getCanvasCoords(sl.latitude, sl.longitude)
                    val isSelected = selectedSavedLandmark?.id == sl.id
                    val color = try {
                        Color(android.graphics.Color.parseColor(sl.colorHex))
                    } catch (e: Exception) {
                        Color(0xFF38BDF8) // Default to Sky Blue for user landmarks
                    }

                    // Landmark glowing outer circle
                    drawCircle(
                        color = color.copy(alpha = if (isSelected) 0.45f else 0.2f),
                        radius = if (isSelected) 55f else 40f,
                        center = coords
                    )
                    // Inner dot
                    drawCircle(
                        color = color,
                        radius = 10f,
                        center = coords
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = coords
                    )
                }

                // 8. Draw Optimized Connection Route Line
                if (pendingParcels.size >= 2) {
                    val routePath = androidx.compose.ui.graphics.Path()
                    pendingParcels.forEachIndexed { idx, p ->
                        val coords = getCanvasCoords(p.latitude ?: 25.61, p.longitude ?: 85.14)
                        if (idx == 0) {
                            routePath.moveTo(coords.x, coords.y)
                        } else {
                            routePath.lineTo(coords.x, coords.y)
                        }
                    }
                    drawPath(
                        path = routePath,
                        color = Color(0xFF10B981).copy(alpha = 0.75f),
                        style = Stroke(width = 6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f)))
                    )
                }

                // 9. Draw Parcel Pin Markers
                parcels.forEach { p ->
                    val coords = getCanvasCoords(p.latitude ?: 25.61, p.longitude ?: 85.14)
                    val isNextTarget = p.id == nextParcel?.id
                    val isTapped = selectedParcel?.id == p.id

                    val pinColor = when (p.status) {
                        "Delivered" -> Color(0xFF10B981) // Green
                        "Failed" -> Color(0xFFEF4444) // Red
                        else -> if (isNextTarget) Color(0xFFF43F5E) else Color(0xFFF59E0B) // Pink vs Amber
                    }

                    // Highlight next target or tapped marker
                    if (isNextTarget || isTapped) {
                        drawCircle(
                            color = pinColor.copy(alpha = if (isTapped) 0.45f else 0.25f),
                            radius = if (isTapped) 32f else 22f,
                            center = coords
                        )
                    }

                    // Primary base pin shape
                    drawCircle(pinColor, radius = 13f, center = coords)
                    drawCircle(Color.White, radius = 5f, center = coords)
                }

                // 10. Draw Live Courier Blue Dot (Simulated rider position)
                val riderOffset = getCanvasCoords(riderLatLng.first, riderLatLng.second)
                // Pulse halo
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                    radius = 36f,
                    center = riderOffset
                )
                // Solid rider body
                drawCircle(
                    color = Color(0xFF38BDF8),
                    radius = 12f,
                    center = riderOffset
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = riderOffset
                )
            }
        }
    } else if (mapMode == "gmaps") {
            // Native Google Maps SDK Live Map Display
            Box(modifier = Modifier.fillMaxSize()) {
                RealTimeGoogleMap(
                    pendingParcels = pendingParcels,
                    landmarks = landmarks,
                    savedLandmarks = savedLandmarks,
                    riderLatLng = riderLatLng,
                    isSimulating = isSimulating,
                    mapType = googleMapType,
                    routeType = viewModel.routeType
                )
            }
    } else {
            // OpenStreetMap/Leaflet Live Map API
            Box(modifier = Modifier.fillMaxSize()) {
                RealTimeLeafletMap(
                    pendingParcels = pendingParcels,
                    landmarks = landmarks,
                    savedLandmarks = savedLandmarks,
                    riderLatLng = riderLatLng,
                    isSimulating = isSimulating,
                    mapStyleType = leafletMapStyleType,
                    routeType = viewModel.routeType
                )
            }
        }

        // TOP TELEMETRY PANEL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Offline indicators
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.WifiOff, contentDescription = "Offline Cache", tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Offline Route Cache Active", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable {
                        showRouteSequencerDialog = true
                    }.testTag("route_config_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val routeLabel = when (viewModel.routeType) {
                            "express" -> "Express: Priority COD"
                            "flood" -> "Monsoon: Flood Bypass"
                            else -> "Eco: Shortest Path"
                        }
                        Icon(imageVector = Icons.Default.Directions, contentDescription = "Route Engine", tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(routeLabel, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Inventory, contentDescription = "Pending count", tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${pendingParcels.size} Pending Parcels", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable {
                        mapMode = when (mapMode) {
                            "gmaps" -> "radar"
                            "radar" -> "osm"
                            else -> "gmaps"
                        }
                    }.testTag("map_mode_toggle_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (mapMode) {
                            "gmaps" -> Icons.Default.Map
                            "radar" -> Icons.Default.Hub
                            else -> Icons.Default.Language
                        }
                        val text = when (mapMode) {
                            "gmaps" -> "Google Maps Active"
                            "radar" -> "Radar Grid Active"
                            else -> "OSM Leaflet Map"
                        }
                        Icon(imageVector = icon, contentDescription = "Toggle Map Mode", tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Map Style Pill (Only visible when not Radar)
                if (mapMode != "radar") {
                    Surface(
                        color = Color(0xFF1E293B).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable {
                            if (mapMode == "gmaps") {
                                googleMapType = when (googleMapType) {
                                    com.google.maps.android.compose.MapType.NORMAL -> com.google.maps.android.compose.MapType.SATELLITE
                                    com.google.maps.android.compose.MapType.SATELLITE -> com.google.maps.android.compose.MapType.TERRAIN
                                    com.google.maps.android.compose.MapType.TERRAIN -> com.google.maps.android.compose.MapType.HYBRID
                                    else -> com.google.maps.android.compose.MapType.NORMAL
                                }
                            } else {
                                leafletMapStyleType = when (leafletMapStyleType) {
                                    "dark" -> "classic"
                                    "classic" -> "satellite"
                                    else -> "dark"
                                }
                            }
                        }.testTag("map_style_toggle_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val label = if (mapMode == "gmaps") {
                                when (googleMapType) {
                                    com.google.maps.android.compose.MapType.NORMAL -> "Style: Normal"
                                    com.google.maps.android.compose.MapType.SATELLITE -> "Style: Satellite"
                                    com.google.maps.android.compose.MapType.TERRAIN -> "Style: Terrain"
                                    else -> "Style: Hybrid"
                                }
                            } else {
                                when (leafletMapStyleType) {
                                    "dark" -> "Style: Dark Canvas"
                                    "classic" -> "Style: Classic OSM"
                                    else -> "Style: Satellite"
                                }
                            }
                            Icon(imageVector = Icons.Default.Layers, contentDescription = "Toggle Style", tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SIMULATION & ZOOM PANEL (TOP RIGHT)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Recenter map view when scale is shifted
                if (zoomScale > 1f || panOffset != Offset.Zero) {
                    FilledIconButton(
                        onClick = {
                            zoomScale = 1f
                            panOffset = Offset.Zero
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CenterFocusStrong, contentDescription = "Center View", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Simulation trigger pill
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { isSimulating = !isSimulating },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Simulate",
                                tint = if (isSimulating) Color(0xFFEF4444) else Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (isSimulating) {
                            TextButton(
                                onClick = {
                                    simSpeed = when (simSpeed) {
                                        1f -> 2f
                                        2f -> 5f
                                        else -> 1f
                                    }
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.widthIn(max = 32.dp)
                            ) {
                                Text(
                                    text = "${simSpeed.toInt()}X",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "SIM ROUTE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // GPS LIVE SIMULATION HEADS UP DISPLAY
        AnimatedVisibility(
            visible = isSimulating,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.9f),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF10B981)))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("GPS ROUTE SIMULATOR IS ACTIVE", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        val approachingP = pendingParcels.getOrNull(currentSimNodeIndex)
                        if (approachingP != null) {
                            Text(
                                text = "Approaching ${approachingP.customerName} (#${approachingP.deliverySequence})",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "COD Collect: ₹${approachingP.codAmount} | Speed: ${simSpeed.toInt()}x",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        } else {
                            Text("Rider is traversing rural sectors...", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // DYNAMIC VOICE AI FEEDBACK BANNER (If speaking)
        if (viewModel.voiceSpeaking) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 160.dp, start = 16.dp, end = 16.dp),
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7))))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Speaking Feedback", tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = viewModel.voiceResponseText,
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // BOTTOM CONTEXT PANEL & MIC CONTROLS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Overlay card: displays selected Landmark, selected Parcel, or Default Next Target
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rural_intelligence_overlay"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurfaceGlass),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(
                            com.example.ui.theme.CyberBorder,
                            Color.Transparent
                        )
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {

                    // CASE 1a: SAVED LANDMARK DETAILS CARD
                    if (selectedSavedLandmark != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val color = try {
                                    Color(android.graphics.Color.parseColor(selectedSavedLandmark!!.colorHex))
                                } catch (e: Exception) {
                                    Color(0xFF38BDF8)
                                }
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = selectedSavedLandmark!!.name,
                                    tint = color,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedSavedLandmark!!.name.uppercase(),
                                    color = color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                            IconButton(
                                onClick = { selectedSavedLandmark = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedSavedLandmark!!.description,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📜 AI HISTORICAL & DESCRIPTIVE INFO:",
                            color = Color(0xFFA855F7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedSavedLandmark!!.historicalFact,
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val lCoords = getCanvasCoords(selectedSavedLandmark!!.latitude, selectedSavedLandmark!!.longitude)
                                    zoomScale = 1.8f
                                    panOffset = Offset(
                                        x = (width / 2f - lCoords.x) * 1.8f,
                                        y = (height / 2f - lCoords.y) * 1.8f
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF334155),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CenterFocusStrong, contentDescription = "Center Map", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Center Map", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.deleteLandmark(selectedSavedLandmark!!.id)
                                    selectedSavedLandmark = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                                    contentColor = Color(0xFFEF4444)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete", fontSize = 11.sp)
                            }
                        }
                    }

                    // CASE 1: LANDMARK DETAILS CARD
                    else if (selectedLandmark != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = selectedLandmark!!.icon,
                                    contentDescription = selectedLandmark!!.name,
                                    tint = selectedLandmark!!.color,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedLandmark!!.name.uppercase(),
                                    color = selectedLandmark!!.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                            IconButton(
                                onClick = { selectedLandmark = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedLandmark!!.description,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedLandmark!!.intelTip,
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val lCoords = getCanvasCoords(selectedLandmark!!.latitude, selectedLandmark!!.longitude)
                                zoomScale = 1.8f
                                panOffset = Offset(
                                    x = (width / 2f - lCoords.x) * 1.8f,
                                    y = (height / 2f - lCoords.y) * 1.8f
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = selectedLandmark!!.color.copy(alpha = 0.2f), contentColor = selectedLandmark!!.color),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.CenterFocusStrong, contentDescription = "Center Landmark", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Center Landmark on Map", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // CASE 2: TAPPED PARCEL DETAILS CARD
                    else if (selectedParcel != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Inventory, contentDescription = "Parcel Profile", tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TAPPED PARCEL DATA", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            }
                            IconButton(
                                onClick = { selectedParcel = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${selectedParcel!!.customerName} (Seq #${selectedParcel!!.deliverySequence})",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedParcel!!.fullAddress,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("PAY METHOD", color = Color(0xFF64748B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(if (selectedParcel!!.codAmount > 0.0) "COD" else "Prepaid", color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("COLLECT AMOUNT", color = Color(0xFF64748B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text("₹${selectedParcel!!.codAmount}", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToParcelDetail(selectedParcel!!.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Launch, contentDescription = "Open Detail", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Profile", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val pCoords = getCanvasCoords(selectedParcel!!.latitude ?: 25.61, selectedParcel!!.longitude ?: 85.14)
                                    zoomScale = 1.8f
                                    panOffset = Offset(
                                        x = (width / 2f - pCoords.x) * 1.8f,
                                        y = (height / 2f - pCoords.y) * 1.8f
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CenterFocusStrong, contentDescription = "Focus", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Center Pin", fontSize = 11.sp)
                            }
                        }
                    }

                    // CASE 3: DEFAULT ACTIVE NEXT DELIVERY CARD
                    else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Hub, contentDescription = "Village Intel", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("VILLAGE INTELLIGENCE LEARNING", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            }

                            if (nextParcel != null) {
                                Text(
                                    text = "SEQ: #${nextParcel.deliverySequence}",
                                    color = Color(0xFFF43F5E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (nextParcel != null) {
                            Text(
                                text = "Next: ${nextParcel.customerName} (${nextParcel.company})",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = nextParcel.fullAddress,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (queryLoading) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF38BDF8))
                            } else {
                                Text(
                                    text = activeIntelTip,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onNavigateToParcelDetail(nextParcel.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Deliver", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Deliver", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val pCoords = getCanvasCoords(nextParcel.latitude ?: 25.61, nextParcel.longitude ?: 85.14)
                                        zoomScale = 1.8f
                                        panOffset = Offset(
                                            x = (width / 2f - pCoords.x) * 1.8f,
                                            y = (height / 2f - pCoords.y) * 1.8f
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.CenterFocusStrong, contentDescription = "Focus Next", modifier = Modifier.size(16.dp), tint = Color(0xFF38BDF8))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Focus Next", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        } else {
                            Text(
                                text = "No active deliveries pending right now. Tap 'Parcels' to import a CSV list or add a manual entry to get routing sequences!",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Action FABs & Map Tapped pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (lastTappedCoords != null) {
                    Surface(
                        color = Color(0xFF0F172A).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable {
                            landmarkLat = String.format("%.6f", lastTappedCoords!!.first)
                            landmarkLng = String.format("%.6f", lastTappedCoords!!.second)
                            landmarkName = ""
                            showSaveLandmarkDialog = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.AddLocation, contentDescription = "Add Tapped", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Tapped Location", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save Current Location Landmark Button
                    FloatingActionButton(
                        onClick = {
                            landmarkLat = String.format("%.6f", riderLatLng.first)
                            landmarkLng = String.format("%.6f", riderLatLng.second)
                            landmarkName = ""
                            showSaveLandmarkDialog = true
                        },
                        containerColor = Color(0xFF10B981), // Emerald Accent
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("save_landmark_fab")
                    ) {
                        Icon(imageVector = Icons.Default.AddLocationAlt, contentDescription = "Save Landmark Here")
                    }

                    // Voice AI Assistant Floating Mic button
                    FloatingActionButton(
                        onClick = { showMicPanel = true },
                        containerColor = Color(0xFFA855F7), // Purple AI accent
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("route_voice_assistant_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Assistant")
                    }
                }
            }
        }

        // SAVE LANDMARK DIALOG
        if (showSaveLandmarkDialog) {
            AlertDialog(
                onDismissRequest = { showSaveLandmarkDialog = false },
                containerColor = Color(0xFF1E293B),
                titleContentColor = Color.White,
                textContentColor = Color(0xFF94A3B8),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AddLocation, contentDescription = "Save Landmark", tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Local Landmark", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Mark and name this landmark. Gemini AI will analyze its location to generate historical and tactical local intelligence.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        TextField(
                            value = landmarkName,
                            onValueChange = { landmarkName = it },
                            label = { Text("Landmark Name") },
                            placeholder = { Text("e.g. Rampur Old Well, Hanuman Mandir") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("landmark_name_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = landmarkLat,
                                onValueChange = { landmarkLat = it },
                                label = { Text("Latitude") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            TextField(
                                value = landmarkLng,
                                onValueChange = { landmarkLng = it },
                                label = { Text("Longitude") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Accent Color selections
                        Text(
                            text = "Choose Landmark Accent Color:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val colors = listOf(
                                "#F59E0B" to "Amber",
                                "#10B981" to "Emerald",
                                "#38BDF8" to "Sky Blue",
                                "#A855F7" to "Purple",
                                "#F43F5E" to "Pink"
                            )
                            colors.forEach { (hex, label) ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .clickable { landmarkColor = hex }
                                        .border(
                                            width = if (landmarkColor == hex) 3.dp else 0.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val latVal = landmarkLat.toDoubleOrNull()
                            val lngVal = landmarkLng.toDoubleOrNull()
                            if (landmarkName.isNotEmpty() && latVal != null && lngVal != null) {
                                viewModel.saveLandmark(
                                    name = landmarkName,
                                    latitude = latVal,
                                    longitude = lngVal,
                                    colorHex = landmarkColor
                                )
                                showSaveLandmarkDialog = false
                                landmarkName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Save & Query Gemini")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveLandmarkDialog = false }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                }
            )
        }

        // ROUTE SEQUENCER & STOP MANAGER DIALOG
        if (showRouteSequencerDialog) {
            AlertDialog(
                onDismissRequest = { showRouteSequencerDialog = false },
                containerColor = Color(0xFF1E293B),
                titleContentColor = Color.White,
                textContentColor = Color(0xFF94A3B8),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Directions, contentDescription = "Route Sequencer", tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Route Optimizer & Sequence", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Select active routing optimization algorithm:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val activeType = viewModel.routeType
                            val modes = listOf(
                                Triple("eco", "Eco", "mode_eco"),
                                Triple("express", "Express", "mode_express"),
                                Triple("flood", "Monsoon", "mode_flood")
                            )
                            modes.forEach { (type, label, testTagStr) ->
                                Button(
                                    onClick = { viewModel.optimizeRouteType(type) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeType == type) Color(0xFF10B981) else Color(0xFF334155)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag(testTagStr)
                                ) {
                                    Text(label, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF334155))

                        Text(
                            text = "Step-by-Step Delivery Stops List:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (pendingParcels.isEmpty()) {
                            Text(
                                "No pending parcels to display sequence for.",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            val sortedPending = pendingParcels.sortedBy { it.deliverySequence }
                            
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                itemsIndexed(sortedPending) { index, parcel ->
                                    val dist = if (index == 0) {
                                        calculateHaversineDistance(
                                            riderLatLng.first, riderLatLng.second,
                                            parcel.latitude ?: 25.61, parcel.longitude ?: 85.14
                                        )
                                    } else {
                                        val prev = sortedPending[index - 1]
                                        calculateHaversineDistance(
                                            prev.latitude ?: 25.61, prev.longitude ?: 85.14,
                                            parcel.latitude ?: 25.61, parcel.longitude ?: 85.14
                                        )
                                    }

                                    Surface(
                                        color = Color(0xFF0F172A),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "#${parcel.deliverySequence}",
                                                            color = Color(0xFF10B981),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = parcel.customerName,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${parcel.company} • COD: ₹${parcel.codAmount}",
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 10.sp
                                                )
                                                Text(
                                                    text = String.format("+ %.2f km leg distance", dist),
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        if (index > 0) {
                                                            val prevParcel = sortedPending[index - 1]
                                                            viewModel.swapParcelSequences(parcel.id, prevParcel.id)
                                                        }
                                                    },
                                                    enabled = index > 0,
                                                    modifier = Modifier.size(24.dp).testTag("swap_up_${parcel.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowUpward,
                                                        contentDescription = "Move Up",
                                                        tint = if (index > 0) Color.White else Color(0xFF334155),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (index < sortedPending.size - 1) {
                                                            val nextParcel = sortedPending[index + 1]
                                                            viewModel.swapParcelSequences(parcel.id, nextParcel.id)
                                                        }
                                                    },
                                                    enabled = index < sortedPending.size - 1,
                                                    modifier = Modifier.size(24.dp).testTag("swap_down_${parcel.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowDownward,
                                                        contentDescription = "Move Down",
                                                        tint = if (index < sortedPending.size - 1) Color.White else Color(0xFF334155),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showRouteSequencerDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Done")
                    }
                }
            )
        }

        // MIC PANEL VOICE ASSISTANT DIALOG
        if (showMicPanel) {
            var manualVoiceText by remember { mutableStateOf("") }
            val voiceTemplates = listOf(
                "Show next parcel.",
                "Call customer.",
                "Navigate to next delivery.",
                "How many parcels are remaining?"
            )

            AlertDialog(
                onDismissRequest = { showMicPanel = false },
                containerColor = Color(0xFF1E293B),
                titleContentColor = Color.White,
                textContentColor = Color(0xFF94A3B8),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Mic Triggered", tint = Color(0xFFA855F7))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Voice AI Assistant", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Say or select a delivery command to activate voice assistant navigation:",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        // Command quick templates
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            voiceTemplates.forEach { template ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.handleVoiceCommand(template) { _, _ -> }
                                            showMicPanel = false
                                        },
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "\"$template\"",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF334155))

                        Text(
                            text = "Or enter custom spoken command:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        TextField(
                            value = manualVoiceText,
                            onValueChange = { manualVoiceText = it },
                            placeholder = { Text("e.g. Navigate to next") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.testTag("voice_command_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (manualVoiceText.isNotEmpty()) {
                                viewModel.handleVoiceCommand(manualVoiceText) { _, _ -> }
                                showMicPanel = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                    ) {
                        Text("Speak")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMicPanel = false }) {
                        Text("Close", color = Color(0xFF64748B))
                    }
                }
            )
        }
    }
}

@Composable
fun RealTimeLeafletMap(
    pendingParcels: List<Parcel>,
    landmarks: List<RuralLandmark>,
    savedLandmarks: List<SavedLandmark>,
    riderLatLng: Pair<Double, Double>,
    isSimulating: Boolean,
    mapStyleType: String,
    routeType: String
) {
    var webViewRef by remember { mutableStateOf<android.webkit.WebView?>(null) }

    // Re-trigger JS updates whenever riderLatLng or isSimulating changes
    LaunchedEffect(webViewRef, riderLatLng, isSimulating) {
        webViewRef?.let { wv ->
            wv.evaluateJavascript(
                "updateRiderPosition(${riderLatLng.first}, ${riderLatLng.second}, $isSimulating)",
                null
            )
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            android.webkit.WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = android.webkit.WebViewClient()
                
                // Inject all markers initially on page load to be completely race-condition free
                val startHubLat = 25.602
                val startHubLng = 85.132

                val parcelsJson = "[" + pendingParcels.joinToString(",") { p ->
                    """{"customerName":"${p.customerName.replace("\"", "\\\"")}","fullAddress":"${p.fullAddress.replace("\"", "\\\"")}","latitude":${p.latitude ?: 25.61},"longitude":${p.longitude ?: 85.14},"codAmount":${p.codAmount}}"""
                } + "]"

                val landmarksJson = "[" + landmarks.joinToString(",") { l ->
                    """{"name":"${l.name.replace("\"", "\\\"")}","intelTip":"${l.intelTip.replace("\"", "\\\"")}","latitude":${l.latitude},"longitude":${l.longitude}}"""
                } + "]"

                val savedLandmarksJson = "[" + savedLandmarks.joinToString(",") { sl ->
                    """{"name":"${sl.name.replace("\"", "\\\"")}","intelTip":"${sl.historicalFact.replace("\"", "\\\"")}","latitude":${sl.latitude},"longitude":${sl.longitude}}"""
                } + "]"

                val tileUrl = when (mapStyleType) {
                    "classic" -> "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    "satellite" -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
                    else -> "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
                }

                val statusText = when (routeType) {
                    "express" -> "Priority COD Express Active"
                    "flood" -> "Flood Bypass Active"
                    else -> "Eco-Route Shortest Path Active"
                }

                val htmlString = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                        <style>
                            body { padding: 0; margin: 0; background: #0b0f19; }
                            html, body, #map { height: 100%; width: 100vw; }
                            .leaflet-popup-content-wrapper {
                                background: #0f172a;
                                color: #f8fafc;
                                border: 1px solid #10b981;
                                border-radius: 12px;
                                font-family: sans-serif;
                            }
                            .leaflet-popup-tip {
                                background: #0f172a;
                            }
                            .leaflet-control-attribution {
                                display: none !important;
                            }
                        </style>
                    </head>
                    <body>
                        <div id="map"></div>
                        <script>
                            var map = L.map('map', { zoomControl: false }).setView([${riderLatLng.first}, ${riderLatLng.second}], 14);
                            
                            L.tileLayer('$tileUrl', {
                                subdomains: 'abcd',
                                maxZoom: 20
                            }).addTo(map);

                            L.control.zoom({ position: 'topright' }).addTo(map);

                            var parcelIcon = L.divIcon({
                                html: '<div style="background-color: #f43f5e; width: 14px; height: 14px; border-radius: 50%; border: 2px solid white; box-shadow: 0 0 10px #f43f5e;"></div>',
                                className: 'custom-div-icon',
                                iconSize: [14, 14],
                                iconAnchor: [7, 7]
                            });

                            var landmarkIcon = L.divIcon({
                                html: '<div style="background-color: #a855f7; width: 16px; height: 16px; border-radius: 50%; border: 2.5px solid #0f172a; box-shadow: 0 0 12px #a855f7;"></div>',
                                className: 'custom-div-icon',
                                iconSize: [16, 16],
                                iconAnchor: [8, 8]
                            });

                            var savedLandmarkIcon = L.divIcon({
                                html: '<div style="background-color: #38bdf8; width: 16px; height: 16px; border-radius: 50%; border: 2.5px solid white; box-shadow: 0 0 12px #38bdf8;"></div>',
                                className: 'custom-div-icon',
                                iconSize: [16, 16],
                                iconAnchor: [8, 8]
                            });

                            var hubIcon = L.divIcon({
                                html: '<div style="background-color: #ef4444; width: 18px; height: 18px; border-radius: 3px; border: 2.5px solid white; box-shadow: 0 0 12px #ef4444; display: flex; align-items: center; justify-content: center; font-size: 10px; color: white;">🏠</div>',
                                className: 'custom-div-icon',
                                iconSize: [18, 18],
                                iconAnchor: [9, 9]
                            });

                            var riderIcon = L.divIcon({
                                html: '<div style="background-color: #10b981; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 15px #10b981; display: flex; align-items: center; justify-content: center; font-size: 14px; color: white;">🚴</div>',
                                className: 'custom-div-icon',
                                iconSize: [24, 24],
                                iconAnchor: [12, 12]
                            });

                            var startHubLat = $startHubLat;
                            var startHubLng = $startHubLng;
                            L.marker([startHubLat, startHubLng], { icon: hubIcon }).bindPopup('<b>BIHAR CENTRAL ROUTE HUB</b>').addTo(map);

                            var parcels = $parcelsJson;
                            var latlngs = [[startHubLat, startHubLng]];
                            parcels.forEach(function(p) {
                                if (p.latitude && p.longitude) {
                                    latlngs.push([p.latitude, p.longitude]);
                                    L.marker([p.latitude, p.longitude], { icon: parcelIcon })
                                        .bindPopup('<b>' + p.customerName + '</b><br/>' + p.fullAddress + '<br/><b>COD: ₹' + p.codAmount + '</b>')
                                        .addTo(map);
                                }
                            });

                            if (latlngs.length > 1) {
                                L.polyline(latlngs, { color: '#10b981', weight: 4, opacity: 0.8, dashArray: '8, 8' }).addTo(map);
                            }

                            var landmarks = $landmarksJson;
                            landmarks.forEach(function(l) {
                                L.marker([l.latitude, l.longitude], { icon: landmarkIcon })
                                    .bindPopup('<b>' + l.name + '</b><br/>' + l.intelTip)
                                    .addTo(map);
                            });

                            var savedLandmarks = $savedLandmarksJson;
                            savedLandmarks.forEach(function(sl) {
                                L.marker([sl.latitude, sl.longitude], { icon: savedLandmarkIcon })
                                    .bindPopup('<b>' + sl.name + '</b><br/>' + sl.intelTip)
                                    .addTo(map);
                            });

                            var riderMarker = L.marker([${riderLatLng.first}, ${riderLatLng.second}], { icon: riderIcon })
                                .bindPopup('<b>COURIER RIDER</b><br/>$statusText')
                                .addTo(map);

                            function updateRiderPosition(lat, lng, isSimulating) {
                                if (riderMarker) {
                                    riderMarker.setLatLng([lat, lng]);
                                    if (isSimulating) {
                                        map.panTo([lat, lng]);
                                    }
                                }
                            }
                        </script>
                    </body>
                    </html>
                """.trimIndent()

                loadDataWithBaseURL("https://openstreetmap.org", htmlString, "text/html", "UTF-8", null)
                webViewRef = this
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun RealTimeGoogleMap(
    pendingParcels: List<Parcel>,
    landmarks: List<RuralLandmark>,
    savedLandmarks: List<SavedLandmark>,
    riderLatLng: Pair<Double, Double>,
    isSimulating: Boolean,
    mapType: com.google.maps.android.compose.MapType,
    routeType: String
) {
    val riderPosition = remember(riderLatLng) {
        LatLng(riderLatLng.first, riderLatLng.second)
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(riderPosition, 14f)
    }
    
    LaunchedEffect(riderPosition) {
        cameraPositionState.animate(
            com.google.android.gms.maps.CameraUpdateFactory.newLatLng(riderPosition)
        )
    }

    val mapProperties = remember(mapType) {
        MapProperties(
            isMyLocationEnabled = false,
            mapType = mapType
        )
    }
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = uiSettings
    ) {
        // 1. Central dispatch hub
        Marker(
            state = MarkerState(position = LatLng(25.602, 85.132)),
            title = "BIHAR CENTRAL ROUTE HUB",
            snippet = "Starting dispatch hub"
        )

        // 2. Active courier position
        val statusText = when (routeType) {
            "express" -> "Priority COD Express Active"
            "flood" -> "Flood Bypass Active"
            else -> "Eco-Route Shortest Path Active"
        }
        Marker(
            state = MarkerState(position = riderPosition),
            title = "COURIER RIDER",
            snippet = if (isSimulating) "Simulating $statusText" else "Active Location Tracking - $statusText"
        )

        // 3. Pending parcel locations
        pendingParcels.forEach { p ->
            val lat = p.latitude ?: 25.61
            val lng = p.longitude ?: 85.14
            Marker(
                state = MarkerState(position = LatLng(lat, lng)),
                title = p.customerName,
                snippet = "${p.fullAddress} (COD: ₹${p.codAmount})"
            )
        }

        // 4. Learning intelligence landmark tips
        landmarks.forEach { l ->
            Marker(
                state = MarkerState(position = LatLng(l.latitude, l.longitude)),
                title = l.name,
                snippet = "AI Tip: ${l.intelTip}"
            )
        }

        // 5. User-saved customized landmark points
        savedLandmarks.forEach { sl ->
            Marker(
                state = MarkerState(position = LatLng(sl.latitude, sl.longitude)),
                title = sl.name,
                snippet = sl.historicalFact
            )
        }

        // 6. Connecting lines representation
        val routePoints = remember(pendingParcels, riderPosition) {
            val list = mutableListOf<LatLng>()
            list.add(LatLng(25.602, 85.132))
            pendingParcels.forEach { p ->
                list.add(LatLng(p.latitude ?: 25.61, p.longitude ?: 85.14))
            }
            list
        }
        
        if (routePoints.size > 1) {
            Polyline(
                points = routePoints,
                color = Color(0xFF10B981),
                width = 8f
            )
        }
    }
}
