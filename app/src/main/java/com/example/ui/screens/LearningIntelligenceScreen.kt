package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LearningRecord
import com.example.data.CustomerMemory
import com.example.data.BuildingMemory
import com.example.data.AreaIntelligence
import com.example.data.RouteMemory
import com.example.ui.DeliveryViewModel

@Composable
fun LearningIntelligenceScreen(
    viewModel: DeliveryViewModel
) {
    val learningRecords by viewModel.learningRecords.collectAsState()
    val customerMemories by viewModel.customerMemories.collectAsState()
    val buildingMemories by viewModel.buildingMemories.collectAsState()
    val areaIntelligence by viewModel.areaIntelligence.collectAsState()
    val routeMemories by viewModel.routeMemories.collectAsState()

    val aiUsageCount by viewModel.aiUsageCount.collectAsState()
    val databaseHitCount by viewModel.databaseHitCount.collectAsState()
    val cacheHitCount by viewModel.cacheHitCount.collectAsState()
    val successfulPredictions by viewModel.successfulPredictions.collectAsState()
    val failedPredictions by viewModel.failedPredictions.collectAsState()
    val profile by viewModel.profile.collectAsState()

    val totalPredictions = successfulPredictions + failedPredictions
    val predictionAccuracy = if (totalPredictions > 0) (successfulPredictions.toDouble() / totalPredictions * 100) else 85.0
    
    val totalHits = databaseHitCount + cacheHitCount + aiUsageCount
    val dbHitRate = if (totalHits > 0) (databaseHitCount.toDouble() / totalHits * 100) else 75.0
    val cacheHitRate = if (totalHits > 0) (cacheHitCount.toDouble() / totalHits * 100) else 20.0
    
    val totalMemories = customerMemories.size + buildingMemories.size + areaIntelligence.size + routeMemories.size + learningRecords.size
    
    val fuelSaved = profile?.fuelSavedLiters ?: 12.8
    val timeSaved = profile?.timeSavedMinutes ?: 185.0

    var activeTabIndex by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.CyberDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Screen Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = "AI Intel",
                    tint = com.example.ui.theme.NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Self-Learning Engine",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.theme.TextPrimary
                    )
                    Text(
                        text = "Continuous neural optimization via delivery completions",
                        fontSize = 12.sp,
                        color = com.example.ui.theme.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI ADMIN COGNITIVE LEARNING DASHBOARD CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_telemetry_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(
                            com.example.ui.theme.CyberBorder,
                            Color.Transparent
                        )
                    )
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ADMIN SELF-LEARNING & COGNITIVE HUD",
                        color = com.example.ui.theme.NeonPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ROW 1: Learning Progress & Prediction Accuracy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurfaceGlass)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = com.example.ui.theme.NeonEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Accuracy", color = com.example.ui.theme.TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = String.format("%.1f%%", predictionAccuracy),
                                    color = com.example.ui.theme.NeonEmerald,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text("$successfulPredictions success / $failedPredictions missed", color = com.example.ui.theme.TextSecondary, fontSize = 9.sp)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurfaceGlass)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.QueryStats,
                                        contentDescription = null,
                                        tint = com.example.ui.theme.NeonPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Memory Growth", color = com.example.ui.theme.TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "+$totalMemories Rules",
                                    color = com.example.ui.theme.NeonPink,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text("Offline SQLite brain", color = com.example.ui.theme.TextSecondary, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ROW 2: Hit Analysis (DB, Cache, AI)
                    Text(
                        text = "PREDICTION PIPELINE COGNITION (TIERED CAPABILITIES)",
                        color = com.example.ui.theme.TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Database hits progress bar
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1. Long-Term SQL DB Hit Rate", color = com.example.ui.theme.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(String.format("%.1f%% (%d hits)", dbHitRate, databaseHitCount), color = com.example.ui.theme.NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (dbHitRate / 100f).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = com.example.ui.theme.NeonCyan,
                            trackColor = com.example.ui.theme.CyberSurfaceGlass
                        )
                    }

                    // Cache / Fallback hits progress bar
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("2. Cloud D1 / Fallback Cache Rate", color = com.example.ui.theme.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(String.format("%.1f%% (%d hits)", cacheHitRate, cacheHitCount), color = com.example.ui.theme.NeonEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (cacheHitRate / 100f).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = com.example.ui.theme.NeonEmerald,
                            trackColor = com.example.ui.theme.CyberSurfaceGlass
                        )
                    }

                    // AI Call statistics
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("3. Expensive Generative AI calls", color = com.example.ui.theme.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(com.example.ui.theme.NeonAmber)
                            )
                            Text("$aiUsageCount API queries (Minimized)", color = com.example.ui.theme.NeonAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ROW 3: Time Saved & Fuel Saved
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurfaceGlass)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = com.example.ui.theme.NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Time Saved", color = com.example.ui.theme.TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format("%.1f mins", timeSaved),
                                        color = com.example.ui.theme.TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurfaceGlass)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = com.example.ui.theme.NeonAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Fuel Saved", color = com.example.ui.theme.TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format("%.2f Liters", fuelSaved),
                                        color = com.example.ui.theme.TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ADVANCED DEEP SELF-LEARNING BUTTON & CONTROL CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deep_learning_control_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(
                            com.example.ui.theme.NeonCyan.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Neural Synthesis",
                                tint = com.example.ui.theme.NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "NEURAL PATTERN SYNTHESIS",
                                    color = com.example.ui.theme.NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = "Scan past delivery logs for insights",
                                    color = com.example.ui.theme.TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        if (viewModel.isSynthesizingSelfLearning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = com.example.ui.theme.NeonCyan,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Automatically mine customer addresses, delivery notes, and successful patterns. AI will generate custom timing rules and save them directly into the local SQLite memory database to refine future routes.",
                        color = com.example.ui.theme.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.synthesizeSelfLearning() },
                        enabled = !viewModel.isSynthesizingSelfLearning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_synthesis_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.ui.theme.NeonCyan.copy(alpha = 0.15f),
                            contentColor = com.example.ui.theme.NeonCyan,
                            disabledContainerColor = com.example.ui.theme.CyberSurfaceGlass,
                            disabledContentColor = com.example.ui.theme.TextMuted
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(com.example.ui.theme.NeonCyan, com.example.ui.theme.NeonCyan.copy(alpha = 0.2f))
                            )
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (viewModel.isSynthesizingSelfLearning) Icons.Default.HourglassEmpty else Icons.Default.PlayArrow,
                                contentDescription = "Run",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (viewModel.isSynthesizingSelfLearning) "SYNTHESIZING PATTERNS..." else "RUN ACTIVE SELF-LEARNING",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // DISPLAY COMPILED SELF-LEARNING REPORT
                    if (viewModel.selfLearningReport.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(com.example.ui.theme.CyberDark)
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Report icon",
                                            tint = com.example.ui.theme.NeonEmerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "INTELLIGENCE DISCOVERY REPORT",
                                            color = com.example.ui.theme.NeonEmerald,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.selfLearningReport = "" },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Report",
                                            tint = com.example.ui.theme.TextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = viewModel.selfLearningReport,
                                    color = com.example.ui.theme.TextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ECO-DRIVE FUEL EFFICIENCY AND CARBON OFFSET TREND (Feature 3)
            PerformanceOffsetChart()

            Spacer(modifier = Modifier.height(24.dp))

            // Intel DB Lists Header
            Text(
                text = "Long-Term SQL Database Memory",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = com.example.ui.theme.TextPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Select cache category to inspect deep learned routing patterns.",
                fontSize = 11.sp,
                color = com.example.ui.theme.TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Pill-Shaped Custom Cyber Tabs
            val tabLabels = listOf("Area Intel", "Customer Mem", "Building Mem", "Route Learn")
            val tabIcons = listOf(Icons.Default.Map, Icons.Default.Person, Icons.Default.LocationCity, Icons.Default.AltRoute)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tabLabels.forEachIndexed { index, label ->
                    val isSelected = activeTabIndex == index
                    val activeColor = when (index) {
                        0 -> com.example.ui.theme.NeonCyan
                        1 -> com.example.ui.theme.NeonPurple
                        2 -> com.example.ui.theme.NeonPink
                        else -> com.example.ui.theme.NeonEmerald
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) activeColor.copy(alpha = 0.12f) else com.example.ui.theme.CyberSurface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) activeColor else com.example.ui.theme.CyberBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { activeTabIndex = index }
                            .padding(vertical = 10.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = label,
                                tint = if (isSelected) activeColor else com.example.ui.theme.TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label.split(" ").first(),
                                color = if (isSelected) com.example.ui.theme.TextPrimary else com.example.ui.theme.TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // DYNAMIC RENDER BASED ON ACTIVE TAB
            when (activeTabIndex) {
                0 -> {
                    // Area Intelligence Tab
                    if (areaIntelligence.isEmpty() && learningRecords.isEmpty()) {
                        EmptyMemoryPlaceholder("No Area intelligence learned yet. Run synthesis or complete a delivery.")
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 88.dp)
                        ) {
                            areaIntelligence.forEach { area ->
                                AreaIntelligenceRow(area = area)
                            }
                            learningRecords.forEach { record ->
                                LearningRecordRow(record = record)
                            }
                        }
                    }
                }
                1 -> {
                    // Customer Memory Tab
                    if (customerMemories.isEmpty()) {
                        EmptyMemoryPlaceholder("No Customer patterns recorded. AI learns preferences from completed doorstep handovers.")
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 88.dp)
                        ) {
                            customerMemories.forEach { customer ->
                                CustomerMemoryRow(customer = customer)
                            }
                        }
                    }
                }
                2 -> {
                    // Building Memory Tab
                    if (buildingMemories.isEmpty()) {
                        EmptyMemoryPlaceholder("No Complex or Building profiles mapped. Doorstep verification populates this cache.")
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 88.dp)
                        ) {
                            buildingMemories.forEach { building ->
                                BuildingMemoryRow(building = building)
                            }
                        }
                    }
                }
                3 -> {
                    // Route Learning Tab
                    if (routeMemories.isEmpty()) {
                        EmptyMemoryPlaceholder("No optimized route shortcuts recorded yet. Sequence completions map shortcuts automatically.")
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 88.dp)
                        ) {
                            routeMemories.forEach { route ->
                                RouteMemoryRow(route = route)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryMetricItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(com.example.ui.theme.CyberSurfaceGlass),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, color = com.example.ui.theme.TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = com.example.ui.theme.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun LearningRecordRow(record: LearningRecord) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    com.example.ui.theme.CyberBorder,
                    Color.Transparent
                )
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Area, Landmark tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.area,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = com.example.ui.theme.TextPrimary
                )

                Surface(
                    color = com.example.ui.theme.CyberSurfaceGlass,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(com.example.ui.theme.NeonCyan.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Place, contentDescription = "Landmark", tint = com.example.ui.theme.NeonCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(record.landmark, color = com.example.ui.theme.NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: learned insight note
            Text(
                text = record.notes,
                color = com.example.ui.theme.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Preferred time and customer availability probability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preferred time slot
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = "Active Times", tint = com.example.ui.theme.TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.preferredTime,
                        color = com.example.ui.theme.TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Availability Confidence
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Availability: ",
                        color = com.example.ui.theme.TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = String.format("%.0f%%", record.customerAvailability * 100),
                        color = if (record.customerAvailability >= 0.8) com.example.ui.theme.NeonEmerald else com.example.ui.theme.NeonAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulated Performance Ratio Bar (Delivered vs Failed ratio)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Successful Deliveries: ${record.deliveredCount}", fontSize = 9.sp, color = com.example.ui.theme.TextSecondary)
                    Text("Failed retries: ${record.failedCount}", fontSize = 9.sp, color = com.example.ui.theme.TextSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Ratio bar
                val total = (record.deliveredCount + record.failedCount).coerceAtLeast(1)
                val deliveryWeight = record.deliveredCount.toFloat() / total

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(com.example.ui.theme.CyberDark)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(deliveryWeight.coerceAtLeast(0.01f))
                            .background(com.example.ui.theme.NeonEmerald)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((1f - deliveryWeight).coerceAtLeast(0.01f))
                            .background(com.example.ui.theme.NeonPink)
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceOffsetChart() {
    val fuelSavedList = listOf(2.1f, 3.4f, 4.8f, 6.2f, 7.5f) // Trend values
    val co2OffsetList = listOf(5.2f, 8.5f, 12.0f, 15.5f, 18.8f) // Trend values in kg CO2
    val labels = listOf("Run 1", "Run 2", "Run 3", "Run 4", "Run 5")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    com.example.ui.theme.CyberBorder,
                    Color.Transparent
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ECO-DRIVE ANALYTICS HUD",
                        color = com.example.ui.theme.NeonEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Fuel & CO2 Carbon Offset",
                        color = com.example.ui.theme.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Surface(
                    color = com.example.ui.theme.NeonEmerald.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "+18.8 kg CO2 OFFSET",
                        color = com.example.ui.theme.NeonEmerald,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Canvas Chart Drawing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(com.example.ui.theme.CyberDark, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val paddingLeft = 35.dp.toPx()
                    val paddingBottom = 25.dp.toPx()
                    val chartWidth = width - paddingLeft
                    val chartHeight = height - paddingBottom

                    // Draw grid lines
                    val gridLinesCount = 3
                    for (i in 0..gridLinesCount) {
                        val y = chartHeight * (i.toFloat() / gridLinesCount)
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(paddingLeft, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Max values for scaling
                    val maxFuel = 10f
                    val maxCo2 = 25f

                    // Points for Fuel efficiency (Bars) and CO2 Offset (Line)
                    val pointsCount = labels.size
                    val stepX = chartWidth / (pointsCount - 1).coerceAtLeast(1)

                    val co2Points: List<Offset> = co2OffsetList.mapIndexed { idx, value ->
                        Offset(paddingLeft + idx.toFloat() * stepX, chartHeight - (value / maxCo2) * chartHeight)
                    }

                    // Draw Fuel Saved bars
                    fuelSavedList.forEachIndexed { idx, value ->
                        val x = paddingLeft + idx.toFloat() * stepX
                        val barHeight = (value / maxFuel) * chartHeight
                        val barWidth = 14.dp.toPx()
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    com.example.ui.theme.NeonPurple,
                                    com.example.ui.theme.NeonPurple.copy(alpha = 0.2f)
                                )
                            ),
                            topLeft = Offset(x - barWidth / 2, chartHeight - barHeight),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    // Draw CO2 Offset glowing connected line
                    if (co2Points.size > 1) {
                        val linePath = androidx.compose.ui.graphics.Path()
                        linePath.moveTo(co2Points[0].x, co2Points[0].y)
                        for (i in 1 until co2Points.size) {
                            linePath.lineTo(co2Points[i].x, co2Points[i].y)
                        }
                        drawPath(
                            path = linePath,
                            color = com.example.ui.theme.NeonEmerald,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )

                        // Draw glow shadow beneath the line
                        val filledPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(co2Points[0].x, co2Points[0].y)
                            for (i in 1 until co2Points.size) {
                                lineTo(co2Points[i].x, co2Points[i].y)
                            }
                            lineTo(co2Points.last().x, chartHeight)
                            lineTo(co2Points.first().x, chartHeight)
                            close()
                        }
                        drawPath(
                            path = filledPath,
                            brush = Brush.verticalGradient(
                                listOf(
                                    com.example.ui.theme.NeonEmerald.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                    }

                    // Draw CO2 Offset glowing dot points
                    co2Points.forEach { pt: Offset ->
                        drawCircle(
                            color = Color.Black,
                            radius = 6.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = com.example.ui.theme.NeonEmerald,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(com.example.ui.theme.NeonPurple)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Fuel Saved (L)", color = com.example.ui.theme.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.NeonEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CO2 Prevented (kg)", color = com.example.ui.theme.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyMemoryPlaceholder(msg: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(com.example.ui.theme.CyberBorder, Color.Transparent)
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = "Empty Cache",
                tint = com.example.ui.theme.TextMuted,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = msg,
                color = com.example.ui.theme.TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun AreaIntelligenceRow(area: AreaIntelligence) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(com.example.ui.theme.NeonCyan.copy(alpha = 0.4f), Color.Transparent)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = area.areaName,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = com.example.ui.theme.TextPrimary
                )
                Surface(
                    color = com.example.ui.theme.NeonCyan.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "AREA CACHE",
                        color = com.example.ui.theme.NeonCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BulletText("🛣️ Fast Roads Mapped", area.fastRoads)
                BulletText("🚲 Shortcuts Mapped", area.smallShortcuts)
                BulletText("🕒 Peak Traffic Times", area.trafficTiming)
                BulletText("🌧️ Weather Impact Notes", area.rainEffect)
            }

            Spacer(modifier = Modifier.height(14.dp))

            MemoryRatioBar(
                successRate = area.successRate,
                confidence = area.confidenceScore,
                usage = area.usageCount
            )
        }
    }
}

@Composable
fun CustomerMemoryRow(customer: CustomerMemory) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(com.example.ui.theme.NeonPurple.copy(alpha = 0.4f), Color.Transparent)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = customer.customerName,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = com.example.ui.theme.TextPrimary
                )
                Surface(
                    color = com.example.ui.theme.NeonPurple.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "CUSTOMER PROFILE",
                        color = com.example.ui.theme.NeonPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BulletText("🕒 Preferred Window", customer.preferredDeliveryTime)
                BulletText("📝 Common Instructions", customer.commonInstructions)
                BulletText("⏳ Avg Waiting Time", "${customer.averageWaitingTimeMinutes} minutes")
                BulletText("🔐 OTP Verified Rate", if (customer.otpReadiness.lowercase() == "ready") "High (Instant Readiness)" else "Standard")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Successful vs Failed tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Handovers History:",
                    color = com.example.ui.theme.TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    customer.deliverySuccessHistory.split(",").filter { it.isNotEmpty() }.forEach { log ->
                        val isSuccessLog = log == "S"
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSuccessLog) com.example.ui.theme.NeonEmerald.copy(alpha = 0.15f)
                                    else com.example.ui.theme.NeonPink.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = log,
                                color = if (isSuccessLog) com.example.ui.theme.NeonEmerald else com.example.ui.theme.NeonPink,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MemoryRatioBar(
                successRate = customer.successRate,
                confidence = customer.confidenceScore,
                usage = customer.usageCount
            )
        }
    }
}

@Composable
fun BuildingMemoryRow(building: BuildingMemory) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(com.example.ui.theme.NeonPink.copy(alpha = 0.4f), Color.Transparent)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = building.buildingName,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = com.example.ui.theme.TextPrimary
                )
                Surface(
                    color = com.example.ui.theme.NeonPink.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "BUILDING MEMORY",
                        color = com.example.ui.theme.NeonPink,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BulletText("🚪 Best Entrance Gate", building.bestEntrance)
                BulletText("🅿️ Safe Bike Parking", building.parkingLocation)
                BulletText("⚡ Security & Lift", "Speed: ${building.liftSpeed}")
                BulletText("⏱️ Entry-To-Door duration", "${String.format("%.1f", building.averageDeliveryTimeMinutes)} mins avg")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Delivery Difficulty Score:",
                    color = com.example.ui.theme.TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Difficulty",
                            tint = if (i <= building.deliveryDifficultyScore) com.example.ui.theme.NeonAmber else com.example.ui.theme.CyberDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MemoryRatioBar(
                successRate = building.successRate,
                confidence = building.confidenceScore,
                usage = building.usageCount
            )
        }
    }
}

@Composable
fun RouteMemoryRow(route: RouteMemory) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(com.example.ui.theme.NeonEmerald.copy(alpha = 0.4f), Color.Transparent)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = route.routeKey,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = com.example.ui.theme.TextPrimary
                )
                Surface(
                    color = com.example.ui.theme.NeonEmerald.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ROUTE SHORTCUT",
                        color = com.example.ui.theme.NeonEmerald,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("STANDARD ROUTING", color = com.example.ui.theme.TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(route.suggestedRoutePoints, color = com.example.ui.theme.TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("⏱️ ${String.format("%.1f", route.suggestedTimeMinutes)} min | 🛣️ ${route.suggestedDistanceKm} km", color = com.example.ui.theme.TextSecondary, fontSize = 10.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("NEURAL ROUTE SHORTCUT", color = com.example.ui.theme.NeonEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(route.actualRoutePoints, color = com.example.ui.theme.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("⚡ ${String.format("%.1f", route.actualTimeMinutes)} min | 🚀 ${route.actualDistanceKm} km", color = com.example.ui.theme.NeonEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SAVINGS STATS!
            val timeSaved = (route.suggestedTimeMinutes - route.actualTimeMinutes).coerceAtLeast(0.0)
            val distSaved = (route.suggestedDistanceKm - route.actualDistanceKm).coerceAtLeast(0.0)
            val fuelSaved = (route.suggestedFuelLiters - route.actualFuelLiters).coerceAtLeast(0.0)

            Surface(
                color = com.example.ui.theme.NeonEmerald.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SavingsMetric("TIME SAVED", "${String.format("%.1f", timeSaved)} min", com.example.ui.theme.NeonCyan)
                    SavingsMetric("DIST SAVED", "${String.format("%.2f", distSaved)} km", com.example.ui.theme.NeonPurple)
                    SavingsMetric("FUEL SAVED", "${String.format("%.2f", fuelSaved)} L", com.example.ui.theme.NeonEmerald)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            MemoryRatioBar(
                successRate = 100.0,
                confidence = route.confidenceScore,
                usage = route.usageCount
            )
        }
    }
}

@Composable
fun BulletText(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label: ",
            color = com.example.ui.theme.TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = com.example.ui.theme.TextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
fun SavingsMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = com.example.ui.theme.TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun MemoryRatioBar(
    successRate: Double,
    confidence: Double,
    usage: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Confidence: ", fontSize = 10.sp, color = com.example.ui.theme.TextMuted)
                Text("${String.format("%.1f%%", confidence)}", fontSize = 10.sp, color = com.example.ui.theme.NeonCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Text("•  Usage: ", fontSize = 10.sp, color = com.example.ui.theme.TextMuted)
                Text("$usage runs", fontSize = 10.sp, color = com.example.ui.theme.TextSecondary, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Success: ", fontSize = 10.sp, color = com.example.ui.theme.TextMuted)
                Text("${String.format("%.1f%%", successRate)}", fontSize = 10.sp, color = if (successRate >= 80.0) com.example.ui.theme.NeonEmerald else com.example.ui.theme.NeonPink, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Ratio Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(com.example.ui.theme.CyberDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight((successRate / 100.0).toFloat().coerceAtLeast(0.01f))
                    .background(com.example.ui.theme.NeonEmerald)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(((100.0 - successRate) / 100.0).toFloat().coerceAtLeast(0.01f))
                    .background(com.example.ui.theme.NeonPink)
            )
        }
    }
}
