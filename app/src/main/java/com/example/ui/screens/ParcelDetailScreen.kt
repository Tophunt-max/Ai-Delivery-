package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Parcel
import com.example.ui.DeliveryViewModel

@Composable
fun ParcelDetailScreen(
    parcelId: Int,
    viewModel: DeliveryViewModel,
    onBack: () -> Unit
) {
    val parcels by viewModel.parcels.collectAsState()
    val parcel = parcels.find { it.id == parcelId }

    val predictionState = produceState<com.example.data.DeliveryPrediction?>(initialValue = null, parcel) {
        parcel?.let {
            value = viewModel.predictDeliverySafety(it)
        }
    }

    val scrollState = rememberScrollState()

    var showCameraView by remember { mutableStateOf(false) }
    var photoCapturedPath by remember { mutableStateOf<String?>(null) }

    var showSignaturePad by remember { mutableStateOf(false) }
    var signatureSaved by remember { mutableStateOf(false) }

    var showFailDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    
    var dialerVisible by remember { mutableStateOf(false) }

    if (parcel == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)), contentAlignment = Alignment.Center) {
            Text("Parcel not found.", color = Color.White)
        }
        return
    }

    // Set initial custom note if present
    LaunchedEffect(parcel) {
        noteText = parcel.deliveryNotes
        if (parcel.photoProof != null) {
            photoCapturedPath = parcel.photoProof
        }
        if (parcel.signaturePath != null) {
            signatureSaved = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.CyberDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = com.example.ui.theme.TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delivery Proof Checklist",
                    color = com.example.ui.theme.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Customer Info Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = parcel.parcelId,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Surface(
                            color = Color(0xFF334155),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = parcel.company.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = parcel.customerName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Location layout
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.Place, contentDescription = "Address", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp).offset(y = 2.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = parcel.fullAddress,
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact and Cash stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("COD AMOUNT", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (parcel.codAmount > 0.0) "₹${parcel.codAmount}" else "PREPAID (₹0.00)",
                                color = if (parcel.codAmount > 0.0) Color(0xFFF59E0B) else Color(0xFF10B981),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Call Customer Button
                        Button(
                            onClick = { dialerVisible = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("call_customer_button")
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call Customer", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            predictionState.value?.let { prediction ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF38BDF8), Color(0xFFC084FC))
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = "AI HUD",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SELF-LEARNING AI PREDICTION HUD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF38BDF8),
                                    letterSpacing = 1.sp
                                )
                            }
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "CONFIDENCE: ${String.format("%.0f%%", prediction.confidence)}",
                                    color = Color(0xFF34D399),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Prediction Source
                        Text(
                            text = "Prediction Source: ${prediction.predictionSource}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Split metrics
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 1. Availability Risk
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Availability",
                                    tint = if (prediction.customerUnavailableRisk) Color(0xFFF43F5E) else Color(0xFF34D399),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (prediction.customerUnavailableRisk) "Customer Unavailable Risk: High (Evening preferred)" else "Customer Availability: 95% Confirmed",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }

                            // 2. Waiting Time
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = "Wait time",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Predicted Waiting Time: ${String.format("%.1f", prediction.waitingTimeMinutes)} mins (doorstep handover)",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }

                            // 3. Traffic and Rain Delay
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Traffic,
                                    contentDescription = "Traffic delay",
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val trafficText = "Traffic: +${String.format("%.1f", prediction.trafficDelayMinutes)} mins"
                                val rainText = if (prediction.rainDelayMinutes > 0) " | Waterlogging: +${String.format("%.1f", prediction.rainDelayMinutes)} mins" else ""
                                Text(
                                    text = "$trafficText$rainText",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }

                            // 4. Parking and Lift Speed
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalParking,
                                    contentDescription = "Parking difficulty",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bike Parking: ${prediction.parkingDifficulty} | Elevator Speed: ${prediction.buildingSpeed}",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Late Risk Banner if true
                        if (prediction.lateDeliveryRisk) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Late Risk",
                                        tint = Color(0xFFF43F5E),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "High risk of delay detected! Recommend taking shortcuts mapped in neural memory.",
                                        color = Color(0xFFFDA4AF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Items Check List
            Text(
                text = "Verification Evidence",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            )

            // 1. Photographic Proof Card
            ProofCheckboxItem(
                title = "Photo Proof of Delivery",
                subtitle = "Take a high-quality picture of the package at the customer doorstep.",
                icon = Icons.Default.CameraAlt,
                isCompleted = photoCapturedPath != null,
                onClick = { showCameraView = true },
                actionLabel = if (photoCapturedPath != null) "Retake Photo" else "Launch AI Camera"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Customer Signature Drawing Card
            ProofCheckboxItem(
                title = "Customer Signature Touchpad",
                subtitle = "Let the receiver sign directly on the screen for electronic logging.",
                icon = Icons.Default.Gesture,
                isCompleted = signatureSaved,
                onClick = { showSignaturePad = true },
                actionLabel = if (signatureSaved) "Redo Signature" else "Open Sign Pad"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Optional notes textfield
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DELIVERY INSIGHTS (AI SELF-LEARNING)",
                        color = com.example.ui.theme.NeonPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mention special landmarks (like 'Shiv temple', 'blue house') or shortcuts. The AI will learn these notes to suggest optimal routing pathways next time!",
                        fontSize = 11.sp,
                        color = com.example.ui.theme.TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("e.g. Yellow door house next to primary school, dirt lane", color = com.example.ui.theme.TextMuted, fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = com.example.ui.theme.CyberDark,
                            unfocusedContainerColor = com.example.ui.theme.CyberDark,
                            focusedTextColor = com.example.ui.theme.TextPrimary,
                            unfocusedTextColor = com.example.ui.theme.TextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delivery_notes_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Finalize Confirmation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showFailDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("mark_failed_button")
                ) {
                    Text("Mark Failed", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.deliverParcel(
                            id = parcel.id,
                            notes = noteText,
                            photoUri = photoCapturedPath ?: "simulated_doorstep_photo.jpg",
                            signatureSvg = if (signatureSaved) "saved_signature.svg" else null
                        )
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("confirm_delivery_button")
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Deliver")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Complete Deliver", fontWeight = FontWeight.Bold)
                }
            }
        }

        // SIMULATED DIALER OVERLAY
        if (dialerVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.width(300.dp).padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Calling", tint = Color(0xFF10B981), modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("DIALING CUSTOMER", color = Color(0xFF10B981), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(parcel.customerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(parcel.customerMobile, color = Color(0xFF64748B), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("SIMULATED OUTGOING CALL", color = Color(0xFF475569), fontSize = 10.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { dialerVisible = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("End Simulation", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SIMULATED CAMERA VIEW FINDER
        if (showCameraView) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AI CAMERA VIEW FINDER", color = Color.White, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showCameraView = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Simulated camera crosshair viewfinder
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .background(Color(0xFF1E293B).copy(alpha = 0.3f), shape = RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing crosshairs
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            drawLine(Color.White.copy(alpha = 0.5f), Offset(w * 0.1f, h * 0.5f), Offset(w * 0.4f, h * 0.5f), strokeWidth = 4f)
                            drawLine(Color.White.copy(alpha = 0.5f), Offset(w * 0.6f, h * 0.5f), Offset(w * 0.9f, h * 0.5f), strokeWidth = 4f)
                            drawLine(Color.White.copy(alpha = 0.5f), Offset(w * 0.5f, h * 0.1f), Offset(w * 0.5f, h * 0.4f), strokeWidth = 4f)
                            drawLine(Color.White.copy(alpha = 0.5f), Offset(w * 0.5f, h * 0.6f), Offset(w * 0.5f, h * 0.9f), strokeWidth = 4f)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = "Parcel Target", tint = Color(0xFF38BDF8), modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Align parcel here", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }

                    // Camera Controls
                    Column(
                        modifier = Modifier.padding(bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                photoCapturedPath = "doorstep_package_${parcel.parcelId}.jpg"
                                showCameraView = false
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .testTag("camera_shutter_button")
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Shutter", tint = Color.Black, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("TAP WHITE SHUTTER BUTTON TO CAPTURE PROOF", color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                }
            }
        }

        // SIGNATURE TOUCHPAD DRAWING pad (Feature 2)
        if (showSignaturePad) {
            var pathList = remember { mutableStateListOf<Offset>() }
            var activeFeedbackTag by remember { mutableStateOf("Handed directly") }
            val feedbackTags = listOf("Handed directly", "Left with relative", "Left with neighbor", "Local Shop drop")

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(360.dp)
                        .padding(16.dp),
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
                    Column(
                        modifier = Modifier
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "DIGITAL SIGNATURE PORTAL",
                                color = com.example.ui.theme.NeonPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Acquire customer verification",
                                color = com.example.ui.theme.TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }

                        // Delivery Feedback Quick Tags
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "SELECT DELIVERY STATUS TYPE",
                                color = com.example.ui.theme.TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                feedbackTags.forEach { tag ->
                                    val isSelected = activeFeedbackTag == tag
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) com.example.ui.theme.NeonPurple.copy(alpha = 0.2f) else com.example.ui.theme.CyberSurfaceGlass
                                            )
                                            .clickable { 
                                                activeFeedbackTag = tag 
                                                // Pre-populate note text
                                                noteText = "Delivered successfully. Receipt mode: $tag."
                                            }
                                            .padding(vertical = 6.dp, horizontal = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tag.replace("Left with ", "").replace("Local ", ""),
                                            color = if (isSelected) com.example.ui.theme.NeonPurple else com.example.ui.theme.TextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Signature Drawing Canvas Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(com.example.ui.theme.CyberDark)
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(com.example.ui.theme.NeonCyan.copy(alpha = 0.4f), Color.Transparent)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        pathList.add(change.position)
                                    }
                                }
                                .testTag("signature_canvas_area")
                        ) {
                            if (pathList.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Gesture,
                                            contentDescription = "Sign Here",
                                            tint = com.example.ui.theme.TextMuted,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Draw signature inside this secure panel",
                                            color = com.example.ui.theme.TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (pathList.size > 1) {
                                    val signPath = Path()
                                    signPath.moveTo(pathList[0].x, pathList[0].y)
                                    for (i in 1 until pathList.size) {
                                        signPath.lineTo(pathList[i].x, pathList[i].y)
                                    }
                                    drawPath(
                                        path = signPath,
                                        color = com.example.ui.theme.NeonCyan,
                                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                                    )
                                }
                            }
                        }

                        // Pad control buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { pathList.clear() },
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.CyberSurfaceGlass),
                                border = BorderStroke(1.dp, com.example.ui.theme.TextMuted.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset Pad", color = com.example.ui.theme.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (pathList.isNotEmpty()) {
                                        signatureSaved = true
                                    }
                                    showSignaturePad = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.NeonPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("signature_save_button")
                            ) {
                                Text("Lock Signature", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // FAILED REASON DIALOG
        if (showFailDialog) {
            var selectedReason by remember { mutableStateOf("Customer Unavailable") }
            val reasons = listOf("Customer Unavailable", "Locked House", "Incorrect Address", "Refused COD Payment")

            AlertDialog(
                onDismissRequest = { showFailDialog = false },
                containerColor = Color(0xFF1E293B),
                titleContentColor = Color.White,
                textContentColor = Color(0xFF94A3B8),
                title = { Text("Delivery Exception", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Select the primary reason for delivery failure:", fontSize = 12.sp)
                        reasons.forEach { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedReason = r }
                                    .background(if (selectedReason == r) Color(0xFF334155) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedReason == r,
                                    onClick = { selectedReason = r },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(r, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.failParcel(parcel.id, selectedReason)
                            showFailDialog = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Submit Failure")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFailDialog = false }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                }
            )
        }
    }
}

@Composable
fun ProofCheckboxItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isCompleted: Boolean,
    onClick: () -> Unit,
    actionLabel: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
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
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1.0f),
                verticalAlignment = Alignment.Top
            ) {
                // Circle checkbox indicator icon
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isCompleted) "Completed" else "Incomplete",
                    tint = if (isCompleted) com.example.ui.theme.NeonEmerald else com.example.ui.theme.TextMuted,
                    modifier = Modifier.size(24.dp).offset(y = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = com.example.ui.theme.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = com.example.ui.theme.TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) com.example.ui.theme.CyberSurfaceGlass else com.example.ui.theme.NeonCyan
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 11.sp,
                    color = if (isCompleted) com.example.ui.theme.TextPrimary else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
