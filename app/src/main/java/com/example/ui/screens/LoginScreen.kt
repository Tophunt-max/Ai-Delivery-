package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Animation for pulsing glow rings on nodes
    val infiniteTransition = rememberInfiniteTransition(label = "nodePulse")
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.CyberDark)
    ) {
        // High-fidelity background neon gradient glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(com.example.ui.theme.NeonCyan.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(0f, 150.dp.toPx()),
                    radius = 450.dp.toPx()
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(com.example.ui.theme.NeonPurple.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width, size.height * 0.7f),
                    radius = 400.dp.toPx()
                )
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .navigationBarsPadding()
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Brand Header Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 48.dp)
                ) {
                    Text(
                        text = "DELIV-AI SYSTEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = com.example.ui.theme.NeonCyan,
                        letterSpacing = 5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "The Intelligent Rural\nLogistics Co-Pilot",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.theme.TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 38.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Optimal path-finding, real-time feedback loop, and self-learning rural shortcuts tracker.",
                        fontSize = 14.sp,
                        color = com.example.ui.theme.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        lineHeight = 20.sp
                    )
                }

                // Beautiful custom drawn canvas vector graphic (smart path HUD map card)
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .padding(12.dp)
                        .background(com.example.ui.theme.CyberSurface, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Inner futuristic grid lines
                        val gridCount = 6
                        val gridStepX = width / gridCount
                        val gridStepY = height / gridCount
                        for (i in 1 until gridCount) {
                            drawLine(
                                color = com.example.ui.theme.CyberBorder.copy(alpha = 0.1f),
                                start = Offset(i * gridStepX, 0f),
                                end = Offset(i * gridStepX, height),
                                strokeWidth = 1.5f
                            )
                            drawLine(
                                color = com.example.ui.theme.CyberBorder.copy(alpha = 0.1f),
                                start = Offset(0f, i * gridStepY),
                                end = Offset(width, i * gridStepY),
                                strokeWidth = 1.5f
                            )
                        }

                        // Outer card high-fidelity sleek border
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    com.example.ui.theme.CyberBorder,
                                    com.example.ui.theme.CyberBorder.copy(alpha = 0.1f)
                                )
                            ),
                            size = Size(width, height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(48f),
                            style = Stroke(width = 3f)
                        )

                        // Path trace
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(width * 0.15f, height * 0.8f)
                            quadraticTo(width * 0.35f, height * 0.45f, width * 0.55f, height * 0.7f)
                            cubicTo(width * 0.7f, height * 0.85f, width * 0.8f, height * 0.35f, width * 0.85f, height * 0.2f)
                        }
                        
                        // Path glowing glow outline
                        drawPath(
                            path = path,
                            color = com.example.ui.theme.NeonCyan.copy(alpha = 0.25f),
                            style = Stroke(width = 14f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        // Path sharp inner line
                        drawPath(
                            path = path,
                            color = com.example.ui.theme.NeonCyan,
                            style = Stroke(width = 6f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )

                        // Pulsing wave animations behind node landmarks
                        drawCircle(
                            color = com.example.ui.theme.NeonPink.copy(alpha = pulseAlpha),
                            radius = 16f * pulseRatio,
                            center = Offset(width * 0.15f, height * 0.8f)
                        )
                        drawCircle(
                            color = com.example.ui.theme.NeonEmerald.copy(alpha = pulseAlpha),
                            radius = 20f * pulseRatio,
                            center = Offset(width * 0.55f, height * 0.7f)
                        )
                        drawCircle(
                            color = com.example.ui.theme.NeonCyan.copy(alpha = pulseAlpha),
                            radius = 16f * pulseRatio,
                            center = Offset(width * 0.85f, height * 0.2f)
                        )

                        // Central core solid nodes
                        drawCircle(com.example.ui.theme.NeonPink, radius = 10f, center = Offset(width * 0.15f, height * 0.8f)) // Start Hub
                        drawCircle(com.example.ui.theme.NeonEmerald, radius = 12f, center = Offset(width * 0.55f, height * 0.7f))  // Temple/Village Cluster
                        drawCircle(com.example.ui.theme.NeonCyan, radius = 10f, center = Offset(width * 0.85f, height * 0.2f)) // Selected Destination
                    }

                    // Floating overlays representing smart logistics HUD
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Routing",
                        tint = com.example.ui.theme.NeonAmber,
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-12).dp, y = 12.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.ElectricBike,
                        contentDescription = "Courier",
                        tint = com.example.ui.theme.NeonEmerald,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = 12.dp, y = (-12).dp)
                    )
                }

                // Bottom Login Action Panel with modern high contrast white glassmorphic card
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .clickable { onLoginSuccess() }
                            .testTag("google_signin_button"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Custom high quality Google logo vector path
                            Canvas(modifier = Modifier.size(22.dp)) {
                                val radius = size.width / 2
                                drawCircle(
                                    color = Color(0xFFEA4335), // Red arc slice
                                    radius = radius,
                                    style = Stroke(width = 5.5f)
                                )
                                drawRect(
                                    color = Color(0xFF4285F4),
                                    topLeft = Offset(radius, radius - 2.5f),
                                    size = Size(radius, 5f)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Sign in with Google",
                                color = com.example.ui.theme.CyberDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Authorized couriers only. The system logs navigation metrics, coordinates, and local self-learned landmark tags to optimize regional logistics routes.",
                        fontSize = 11.sp,
                        color = com.example.ui.theme.TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
