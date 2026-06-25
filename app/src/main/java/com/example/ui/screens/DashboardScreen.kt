package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeliveryViewModel

@Composable
fun DashboardScreen(
    viewModel: DeliveryViewModel,
    onNavigateToParcels: () -> Unit,
    onNavigateToRoute: () -> Unit,
    onNavigateToLearning: () -> Unit
) {
    val parcels by viewModel.parcels.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val scrollState = rememberScrollState()

    val total = parcels.size
    val delivered = parcels.count { it.status == "Delivered" }
    val pending = parcels.count { it.status == "Pending" }
    val failed = parcels.count { it.status == "Failed" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.CyberDark)
    ) {
        // Futuristic abstract glow backdrops
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(com.example.ui.theme.NeonPurple.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.85f, 120.dp.toPx()),
                    radius = 350.dp.toPx()
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(com.example.ui.theme.NeonCyan.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(80.dp.toPx(), size.height * 0.45f),
                    radius = 320.dp.toPx()
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HELLO, ${profile?.name?.uppercase() ?: "RAMESH"}",
                        fontSize = 11.sp,
                        color = com.example.ui.theme.TextSecondary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "AI Logistics Desk",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.theme.TextPrimary
                    )
                }

                // AI Pulse Badge with Neon Ring Glow
                Surface(
                    color = com.example.ui.theme.NeonEmerald.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                com.example.ui.theme.NeonEmerald,
                                com.example.ui.theme.NeonEmerald.copy(alpha = 0.3f)
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(com.example.ui.theme.NeonEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI ENGINE ACTIVE",
                            color = com.example.ui.theme.NeonEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Daily Assistant Briefing Card (Glassmorphic Container + Left Neon Accent Bar)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_briefing_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(
                            com.example.ui.theme.NeonPurple.copy(alpha = 0.3f),
                            com.example.ui.theme.CyberBorder.copy(alpha = 0.1f)
                        )
                    )
                )
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Left vertical gradient glow strip
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        com.example.ui.theme.NeonPurple,
                                        com.example.ui.theme.NeonCyan
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "AI Co-pilot",
                                    tint = com.example.ui.theme.NeonPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI CO-PILOT BRIEFING",
                                    color = com.example.ui.theme.NeonPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp
                                )
                            }

                            // Audio indicator if speaking
                            if (viewModel.voiceSpeaking) {
                                AnimatedVoiceBars()
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = viewModel.activeBriefing,
                            color = com.example.ui.theme.TextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (viewModel.voiceSpeaking) "AI Voice active..." else "Tap to play neural voice summary",
                                color = com.example.ui.theme.TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.refreshBriefing() },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = com.example.ui.theme.CyberSurfaceGlass
                                    ),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Regenerate Info",
                                        tint = com.example.ui.theme.TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                FloatingActionButton(
                                    onClick = { viewModel.speakBriefing() },
                                    containerColor = if (viewModel.voiceSpeaking) com.example.ui.theme.NeonPink else com.example.ui.theme.NeonCyan,
                                    contentColor = Color.Black,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("speak_briefing_fab"),
                                    shape = CircleShape,
                                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                                ) {
                                    Icon(
                                        imageVector = if (viewModel.voiceSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                        contentDescription = "Read Aloud",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI CO-PILOT HUD CONFIGURATION PANEL (Feature 1 & Feature 4)
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
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
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SettingsSuggest,
                                contentDescription = "Config HUD",
                                tint = com.example.ui.theme.NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CO-PILOT ADVANCED CONTROLS",
                                color = com.example.ui.theme.TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Weather state indicator badge
                        Surface(
                            color = when (viewModel.weatherCondition) {
                                "Monsoon Rain" -> com.example.ui.theme.NeonCyan.copy(alpha = 0.12f)
                                "Dense Fog" -> com.example.ui.theme.NeonPink.copy(alpha = 0.12f)
                                "Intense Heat" -> com.example.ui.theme.NeonAmber.copy(alpha = 0.12f)
                                else -> com.example.ui.theme.NeonEmerald.copy(alpha = 0.12f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${viewModel.weatherTemp}°C | ${viewModel.weatherCondition.uppercase()}",
                                color = when (viewModel.weatherCondition) {
                                    "Monsoon Rain" -> com.example.ui.theme.NeonCyan
                                    "Dense Fog" -> com.example.ui.theme.NeonPink
                                    "Intense Heat" -> com.example.ui.theme.NeonAmber
                                    else -> com.example.ui.theme.NeonEmerald
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // WEATHER TOGGLE
                    Text(
                        text = "SIMULATE MICRO-CLIMATE WEATHER RADAR",
                        color = com.example.ui.theme.TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val weatherOptions = listOf(
                            Triple("Clear Sky", Icons.Default.WbSunny, com.example.ui.theme.NeonEmerald),
                            Triple("Monsoon Rain", Icons.Default.Umbrella, com.example.ui.theme.NeonCyan),
                            Triple("Dense Fog", Icons.Default.Cloud, com.example.ui.theme.NeonPink),
                            Triple("Intense Heat", Icons.Default.Thermostat, com.example.ui.theme.NeonAmber)
                        )
                        weatherOptions.forEach { (cond, icon, color) ->
                            val isSelected = viewModel.weatherCondition == cond
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) color.copy(alpha = 0.15f) else com.example.ui.theme.CyberSurfaceGlass
                                    )
                                    .clickable { viewModel.updateWeather(cond) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = cond,
                                        tint = if (isSelected) color else com.example.ui.theme.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cond.split(" ").first(),
                                        color = if (isSelected) color else com.example.ui.theme.TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⚠️ ALERT: ${viewModel.weatherWarning}",
                        color = when (viewModel.weatherCondition) {
                            "Monsoon Rain" -> com.example.ui.theme.NeonCyan
                            "Dense Fog" -> com.example.ui.theme.NeonPink
                            "Intense Heat" -> com.example.ui.theme.NeonAmber
                            else -> com.example.ui.theme.TextMuted
                        },
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // LANGUAGE DIALECT SELECTOR
                    Text(
                        text = "AI CO-PILOT DIALECT & LANGUAGE MODE",
                        color = com.example.ui.theme.TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(com.example.ui.theme.CyberSurfaceGlass)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val languages = listOf("English", "Hinglish", "Bhojpuri", "Hindi")
                        languages.forEach { lang ->
                            val isSelected = viewModel.aiLanguageMode == lang
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) com.example.ui.theme.NeonPurple.copy(alpha = 0.2f) else Color.Transparent
                                    )
                                    .clickable { viewModel.changeLanguageMode(lang) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lang,
                                    color = if (isSelected) com.example.ui.theme.NeonPurple else com.example.ui.theme.TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Visual Route Progress Bar Card (Futuristic Neon Style)
            if (parcels.isNotEmpty()) {
                val totalCount = parcels.size
                val deliveredCount = parcels.count { it.status == "Delivered" }
                val remainingCount = parcels.count { it.status == "Pending" }
                val progressFraction = if (totalCount > 0) deliveredCount.toFloat() / totalCount else 0f
                val animatedProgress by animateFloatAsState(
                    targetValue = progressFraction,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
                    label = "DashboardProgressAnim"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_progress_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(
                            listOf(
                                com.example.ui.theme.NeonEmerald.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = "Active Route Progress",
                                    tint = com.example.ui.theme.NeonEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACTIVE ROUTE COMPLETED",
                                    color = com.example.ui.theme.NeonEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp
                                )
                            }
                            Text(
                                text = String.format("%.0f%%", progressFraction * 100),
                                color = com.example.ui.theme.NeonEmerald,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar Track with Neon Glow Indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedProgress)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                com.example.ui.theme.NeonCyan,
                                                com.example.ui.theme.NeonEmerald
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Remaining Stops: $remainingCount",
                                color = com.example.ui.theme.TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$deliveredCount / $totalCount Delivered",
                                color = com.example.ui.theme.TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Main Metrics Grid
            Text(
                text = "Today's Deliveries",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = com.example.ui.theme.TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        title = "DELIVERED",
                        value = "$delivered / $total",
                        subtitle = "Pending: $pending",
                        icon = Icons.Default.CheckCircle,
                        iconColor = com.example.ui.theme.NeonEmerald
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        title = "FUEL SAVED",
                        value = String.format("%.1f L", profile?.fuelSavedLiters ?: 12.8),
                        subtitle = "CO2 footprint down",
                        icon = Icons.Default.LocalGasStation,
                        iconColor = com.example.ui.theme.NeonAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        title = "TIME SAVED",
                        value = String.format("%.0f Mins", profile?.timeSavedMinutes ?: 185.0),
                        subtitle = "Sequence gain",
                        icon = Icons.Default.Timelapse,
                        iconColor = com.example.ui.theme.NeonCyan
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        title = "AI SCORE",
                        value = String.format("%.1f%%", profile?.aiEfficiencyScore ?: 94.5),
                        subtitle = "Logistics precision",
                        icon = Icons.Default.QueryStats,
                        iconColor = com.example.ui.theme.NeonPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Route Optimization FAB / Quick Callout Card with Neon border
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(
                            com.example.ui.theme.NeonPurple,
                            com.example.ui.theme.NeonCyan.copy(alpha = 0.4f)
                        )
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = "Sequence Optimization",
                            color = com.example.ui.theme.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Optimize your route dynamically to group the $pending remaining parcels via shortest neural paths.",
                            color = com.example.ui.theme.TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        if (viewModel.lastOptimizationStatus.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewModel.lastOptimizationStatus.contains("Optimized")) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = "Status Icon",
                                    tint = if (viewModel.lastOptimizationStatus.contains("Optimized")) com.example.ui.theme.NeonEmerald else com.example.ui.theme.NeonAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = viewModel.lastOptimizationStatus,
                                    color = if (viewModel.lastOptimizationStatus.contains("Optimized")) com.example.ui.theme.NeonEmerald else com.example.ui.theme.TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { viewModel.optimizeRoute() },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.NeonPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dashboard_optimize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AltRoute,
                            contentDescription = "Optimize",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Optimize", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // CLOUDFLARE WORKER AUTOMATIC BACKGROUND SYNCHRONIZER
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(
                            com.example.ui.theme.NeonCyan.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Cloudflare Worker Link",
                                tint = com.example.ui.theme.NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CLOUDFLARE WORKER & D1 DATALINK",
                                color = com.example.ui.theme.TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        // State badge
                        Surface(
                            color = if (viewModel.isBackgroundSyncEnabled) com.example.ui.theme.NeonEmerald.copy(alpha = 0.15f) else com.example.ui.theme.NeonAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (viewModel.isBackgroundSyncEnabled) "AUTO-SYNC ACTIVE" else "AUTO-SYNC PAUSED",
                                color = if (viewModel.isBackgroundSyncEnabled) com.example.ui.theme.NeonEmerald else com.example.ui.theme.NeonAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Node Address Input
                    Text(
                        text = "CLOUDFLARE WORKER ROUTE ENDPOINT",
                        color = com.example.ui.theme.TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = viewModel.cloudServerAddress,
                        onValueChange = { viewModel.updateServerAddress(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_endpoint_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = com.example.ui.theme.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = com.example.ui.theme.CyberDark,
                            unfocusedContainerColor = com.example.ui.theme.CyberDark,
                            focusedBorderColor = com.example.ui.theme.NeonCyan,
                            unfocusedBorderColor = com.example.ui.theme.CyberBorder.copy(alpha = 0.4f),
                            cursorColor = com.example.ui.theme.NeonCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Background auto-sync toggle switch row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Automatic Background Sync",
                                color = com.example.ui.theme.TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto-saves SQL to D1 & photos to R2",
                                color = com.example.ui.theme.TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = viewModel.isBackgroundSyncEnabled,
                            onCheckedChange = { viewModel.toggleBackgroundSync(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = com.example.ui.theme.NeonCyan,
                                checkedTrackColor = com.example.ui.theme.NeonCyan.copy(alpha = 0.4f),
                                uncheckedThumbColor = com.example.ui.theme.TextMuted,
                                uncheckedTrackColor = com.example.ui.theme.CyberDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status Message and Loading Indicator
                    if (viewModel.syncState == "SYNCING") {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = com.example.ui.theme.NeonCyan,
                            trackColor = com.example.ui.theme.CyberDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = viewModel.lastSyncStatusText,
                        color = if (viewModel.isBackgroundSyncEnabled) com.example.ui.theme.TextPrimary else com.example.ui.theme.TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (viewModel.lastSyncTimestamp != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val formattedTime = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.getDefault()).format(java.util.Date(viewModel.lastSyncTimestamp!!))
                        Text(
                            text = "Last synced at: $formattedTime",
                            color = com.example.ui.theme.NeonEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fetch cloud assignments
                    Button(
                        onClick = { viewModel.triggerCloudReset() },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.CyberSurfaceGlass),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.TextMuted.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = viewModel.syncState != "SYNCING"
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Pull fresh assignments",
                            tint = com.example.ui.theme.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Pull Cloudflare D1 Assignments",
                            color = com.example.ui.theme.TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation shortcuts (Styled with premium Glassmorphism-style buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToParcels,
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.CyberSurface),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                com.example.ui.theme.CyberBorder.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Parcels",
                        tint = com.example.ui.theme.NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Parcels", color = com.example.ui.theme.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToRoute,
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.CyberSurface),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                com.example.ui.theme.CyberBorder.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = "Route",
                        tint = com.example.ui.theme.NeonEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deliver", color = com.example.ui.theme.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToLearning,
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.CyberSurface),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                com.example.ui.theme.CyberBorder.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "AI Intel",
                        tint = com.example.ui.theme.NeonPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Learn", color = com.example.ui.theme.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Loader Overlay
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = com.example.ui.theme.NeonCyan)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color
) {
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
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = com.example.ui.theme.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = com.example.ui.theme.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = com.example.ui.theme.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AnimatedVoiceBars() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val height1 by infiniteTransition.animateValue(
        initialValue = 4.dp,
        targetValue = 18.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val height2 by infiniteTransition.animateValue(
        initialValue = 16.dp,
        targetValue = 6.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val height3 by infiniteTransition.animateValue(
        initialValue = 6.dp,
        targetValue = 22.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier.height(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(modifier = Modifier.size(3.dp, height1).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFF38BDF8)))
        Box(modifier = Modifier.size(3.dp, height2).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFF38BDF8)))
        Box(modifier = Modifier.size(3.dp, height3).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFF38BDF8)))
    }
}
