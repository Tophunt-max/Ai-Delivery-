package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Parcel
import com.example.ui.DeliveryViewModel

@Composable
fun ParcelsScreen(
    viewModel: DeliveryViewModel,
    onNavigateToParcelDetail: (Int) -> Unit
) {
    val parcels by viewModel.parcels.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Pending, 2: Delivered, 3: Failed
    
    var showManualDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    // Filter parcels based on tab and query
    val filteredParcels = parcels.filter { p ->
        val statusMatches = when (selectedTab) {
            1 -> p.status == "Pending"
            2 -> p.status == "Delivered"
            3 -> p.status == "Failed"
            else -> true
        }
        val queryMatches = p.customerName.contains(searchQuery, ignoreCase = true) ||
                p.parcelId.contains(searchQuery, ignoreCase = true) ||
                p.fullAddress.contains(searchQuery, ignoreCase = true)

        statusMatches && queryMatches
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.CyberDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Customer, ID, or Address...", color = com.example.ui.theme.TextMuted, fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parcel_search_input"),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = com.example.ui.theme.TextPrimary,
                    unfocusedTextColor = com.example.ui.theme.TextPrimary,
                    focusedContainerColor = com.example.ui.theme.CyberSurface,
                    unfocusedContainerColor = com.example.ui.theme.CyberSurface,
                    disabledContainerColor = com.example.ui.theme.CyberSurface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = com.example.ui.theme.TextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = com.example.ui.theme.TextSecondary)
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bulk import and sequence control row (Beautiful cyber pills)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.weight(1f).testTag("csv_import_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.CyberSurfaceGlass),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Bulk Upload", tint = com.example.ui.theme.TextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bulk Import", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.TextPrimary)
                }

                Button(
                    onClick = { viewModel.optimizeRoute() },
                    modifier = Modifier.weight(1f).testTag("optimize_route_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AltRoute, contentDescription = "Optimize Sequence", modifier = Modifier.size(16.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Route Sort", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = { viewModel.clearAllParcels() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = com.example.ui.theme.NeonPink.copy(alpha = 0.12f))
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = com.example.ui.theme.NeonPink)
                }
            }

            if (viewModel.lastOptimizationStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(com.example.ui.theme.CyberSurface.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // High-fidelity Segmented Custom Pill Tabs (Custom Row instead of ScrollableTabRow)
            val tabs = listOf("All", "Pending", "Delivered", "Failed")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(com.example.ui.theme.CyberSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { idx, label ->
                    val isSelected = selectedTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                if (isSelected) {
                                    when (idx) {
                                        2 -> com.example.ui.theme.NeonEmerald.copy(alpha = 0.15f)
                                        3 -> com.example.ui.theme.NeonPink.copy(alpha = 0.15f)
                                        else -> com.example.ui.theme.NeonCyan.copy(alpha = 0.15f)
                                    }
                                } else {
                                    Color.Transparent
                                }
                            )
                            .clickable { selectedTab = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) {
                                when (idx) {
                                    2 -> com.example.ui.theme.NeonEmerald
                                    3 -> com.example.ui.theme.NeonPink
                                    else -> com.example.ui.theme.NeonCyan
                                }
                            } else {
                                com.example.ui.theme.TextSecondary
                            },
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Parcels count readout
            Text(
                text = "FOUND ${filteredParcels.size} PARCELS",
                color = com.example.ui.theme.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Parcel List View
            if (filteredParcels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "No parcels",
                            tint = com.example.ui.theme.TextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No parcels found in this category.",
                            color = com.example.ui.theme.TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Import a CSV dataset or tap the '+' button to manually log some.",
                            color = com.example.ui.theme.TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filteredParcels) { parcel ->
                        ParcelItemRow(
                            parcel = parcel,
                            onItemClick = { onNavigateToParcelDetail(parcel.id) }
                        )
                    }
                }
            }
        }

        // Floating manual entry button (Pulsing glowing FAB)
        FloatingActionButton(
            onClick = { showManualDialog = true },
            containerColor = com.example.ui.theme.NeonEmerald,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .size(56.dp)
                .testTag("add_manual_parcel_fab"),
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Manual Parcel", modifier = Modifier.size(24.dp))
        }

        // MANUAL PARCEL ADD DIALOG
        if (showManualDialog) {
            var custName by remember { mutableStateOf("") }
            var custMobile by remember { mutableStateOf("") }
            var custAddress by remember { mutableStateOf("") }
            var codAmt by remember { mutableStateOf("") }
            var compSelected by remember { mutableStateOf("Amazon") }
            var notes by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showManualDialog = false },
                containerColor = Color(0xFF1E293B),
                titleContentColor = Color.White,
                textContentColor = Color(0xFF94A3B8),
                title = { Text("Manual Parcel Entry", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextField(
                            value = custName,
                            onValueChange = { custName = it },
                            placeholder = { Text("Customer Full Name") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFF0F172A), unfocusedContainerColor = Color(0xFF0F172A), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        TextField(
                            value = custMobile,
                            onValueChange = { custMobile = it },
                            placeholder = { Text("Mobile Number (e.g. +91 98...)") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFF0F172A), unfocusedContainerColor = Color(0xFF0F172A), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        TextField(
                            value = custAddress,
                            onValueChange = { custAddress = it },
                            placeholder = { Text("Full Address / Village landmarks") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFF0F172A), unfocusedContainerColor = Color(0xFF0F172A), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 2
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextField(
                                value = codAmt,
                                onValueChange = { codAmt = it },
                                placeholder = { Text("COD Amount (0 if Prepaid)") },
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFF0F172A), unfocusedContainerColor = Color(0xFF0F172A), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            // Simple company selector
                            Box(modifier = Modifier.weight(1f)) {
                                var expanded by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { expanded = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(compSelected, fontSize = 12.sp)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(Color(0xFF1E293B))
                                ) {
                                    listOf("Amazon", "Flipkart", "Delhivery", "Blue Dart", "Ekart", "DTDC").forEach { comp ->
                                        DropdownMenuItem(
                                            text = { Text(comp, color = Color.White) },
                                            onClick = {
                                                compSelected = comp
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        TextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("Delivery instructions / Landmarks") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFF0F172A), unfocusedContainerColor = Color(0xFF0F172A), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (custName.isNotEmpty() && custAddress.isNotEmpty()) {
                                viewModel.addParcel(
                                    parcelId = "",
                                    name = custName,
                                    mobile = custMobile,
                                    address = custAddress,
                                    cod = codAmt.toDoubleOrNull() ?: 0.0,
                                    company = compSelected,
                                    notes = notes
                                )
                                showManualDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Add Parcel")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualDialog = false }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                }
            )
        }

        // CSV/BULK EXCEL IMPORT DIALOG
        if (showImportDialog) {
            var rawText by remember { mutableStateOf("") }
            val templateText = "Suresh Mandi, +91 90021 54321, near Banyan Tree Village Rampur, 500, Flipkart\n" +
                               "Gopal Soren, +91 88001 76543, behind primary school Sector B, 0, Amazon\n" +
                               "Munni Baskey, +91 76110 43210, opposite yellow tank Village Pipri, 150, Delhivery\n" +
                               "Nadeem Ali, +91 99114 88776, near Masjid Village Chhota Harishpur, 1200, Blue Dart\n" +
                               "Runa Laha, +91 82345 67890, Ward 4 near Temple Village Rampur, 0, Ekart"

            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                containerColor = Color(0xFF1E293B),
                titleContentColor = Color.White,
                textContentColor = Color(0xFF94A3B8),
                title = { Text("Bulk Logistics Import", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Paste comma-separated parcel rows (CSV) below to import instantly:",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "Format: Name, Mobile, Address, COD_Amt, Company",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )

                        TextField(
                            value = rawText,
                            onValueChange = { rawText = it },
                            placeholder = { Text("Paste CSV data here or tap template...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("csv_paste_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { rawText = templateText }
                            ) {
                                Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = "Use Template", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Load 5 Rural Parcels Template", fontSize = 10.sp, color = Color(0xFF38BDF8))
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rawText.isNotEmpty()) {
                                viewModel.importCSVData(rawText)
                                showImportDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Text("Process Import", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                }
            )
        }
    }
}

@Composable
fun ParcelItemRow(
    parcel: Parcel,
    onItemClick: () -> Unit
) {
    // Custom branding colors per courier company
    val badgeColors = when (parcel.company.lowercase()) {
        "amazon" -> Pair(Color(0xFFFF9900), Color.Black)
        "flipkart" -> Pair(Color(0xFF2874F0), Color.White)
        "delhivery" -> Pair(com.example.ui.theme.NeonPink, Color.White)
        "blue dart" -> Pair(com.example.ui.theme.NeonAmber, Color.Black)
        "ekart" -> Pair(com.example.ui.theme.NeonEmerald, Color.White)
        else -> Pair(com.example.ui.theme.TextMuted, Color.White)
    }

    val statusColor = when (parcel.status) {
        "Delivered" -> com.example.ui.theme.NeonEmerald
        "Failed" -> com.example.ui.theme.NeonPink
        else -> com.example.ui.theme.NeonAmber // Pending
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .testTag("parcel_item_card_${parcel.id}"),
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
        Row(modifier = Modifier.fillMaxWidth()) {
            // Status Indicator Left Accent Bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(130.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(statusColor, Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Header: sequence, ID, company badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(com.example.ui.theme.CyberSurfaceGlass),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (parcel.deliverySequence == 999) "#" else parcel.deliverySequence.toString(),
                                color = com.example.ui.theme.NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parcel.parcelId,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.TextPrimary,
                            fontSize = 14.sp
                        )
                    }

                    // Company badge
                    Surface(
                        color = badgeColors.first,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = parcel.company.uppercase(),
                            color = badgeColors.second,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Body: Customer Name and Address
                Text(
                    text = parcel.customerName,
                    color = com.example.ui.theme.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Address",
                        tint = com.example.ui.theme.TextMuted,
                        modifier = Modifier.size(16.dp).offset(y = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = parcel.fullAddress,
                        color = com.example.ui.theme.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer: COD amount vs status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // COD or Prepaid Status
                    if (parcel.codAmount > 0.0) {
                        Surface(
                            color = com.example.ui.theme.NeonAmber.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    listOf(com.example.ui.theme.NeonAmber.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.CurrencyRupee, contentDescription = "COD", tint = com.example.ui.theme.NeonAmber, modifier = Modifier.size(12.dp))
                                Text(
                                    text = "COD: ₹${parcel.codAmount}",
                                    color = com.example.ui.theme.NeonAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = com.example.ui.theme.NeonEmerald.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    listOf(com.example.ui.theme.NeonEmerald.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )
                        ) {
                            Text(
                                text = "PREPAID",
                                color = com.example.ui.theme.NeonEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Status tag
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(statusColor.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                    ) {
                        Text(
                            text = parcel.status.uppercase(),
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
