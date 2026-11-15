package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.bluetooth.BtConnectionState
import com.example.data.VehicleCatalog
import com.example.data.VehicleCategoryType
import com.example.data.VehicleModel
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TitaniumSilver
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.brushedMetalPanel
import com.example.ui.theme.carbonFiberBackground

/**
 * Step 1 inside ECU STIMULATION:
 * Displays VEHICLE CATEGORY options: CAR, LCV, HCV, BUS.
 * - LCV is fully active (5 brands + vehicle lineup + cranking/sensor controls).
 * - CAR, HCV, BUS open their Brand Logo directory with "COMING SOON" ONLY.
 */
@Composable
fun EcuCategorySelectionScreen(
    btState: BtConnectionState,
    onBack: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    onSelectCategory: (VehicleCategoryType) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .testTag("ecu_category_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp)
        ) {
            SubScreenTopBar(
                title = "VEHICLE CATEGORY",
                btState = btState,
                onBack = onBack,
                onOpenBluetoothSheet = onOpenBluetoothSheet,
                backTestTag = "category_back_button"
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(VehicleCategoryType.entries) { category ->
                    CategoryOptionCard(
                        category = category,
                        onClick = { onSelectCategory(category) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryOptionCard(
    category: VehicleCategoryType,
    onClick: () -> Unit
) {
    val (previewDrawable, iconVector, accentColor) = when (category) {
        VehicleCategoryType.CAR -> Triple(
            R.drawable.img_car_front_glow_1794742208220,
            Icons.Default.DirectionsCar,
            WarningAmber
        )
        VehicleCategoryType.LCV -> Triple(
            R.drawable.img_lcv_pickup_bolero_1794745518078,
            Icons.Default.LocalShipping,
            SignalGreen
        )
        VehicleCategoryType.HCV -> Triple(
            R.drawable.img_vehicle_truck_bench_1794742209701,
            Icons.Default.LocalShipping,
            WarningAmber
        )
        VehicleCategoryType.BUS -> Triple(
            R.drawable.img_category_bus_1794747200488,
            Icons.Default.DirectionsBus,
            WarningAmber
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brushedMetalPanel(
                cornerRadius = 16.dp,
                borderColor = if (category.isComingSoon) BorderSubtle else SignalGreen.copy(alpha = 0.8f),
                borderWidth = if (category.isComingSoon) 1.dp else 1.5.dp,
                elevated = !category.isComingSoon
            )
            .clickable { onClick() }
            .testTag("category_card_${category.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 112.dp, height = 78.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = previewDrawable),
                    contentDescription = category.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(RecessedWell.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (category.isComingSoon) RecessedWell else SignalGreenBg)
                            .border(
                                1.dp,
                                if (category.isComingSoon) WarningAmber.copy(alpha = 0.7f) else SignalGreen,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (category.isComingSoon) "COMING SOON" else "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (category.isComingSoon) GoldAccent else SignalGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (category.isComingSoon) {
                        "View Brand Lineup (Coming Soon)"
                    } else {
                        "Tata • Mahindra • Ashok Leyland • Force • BharatBenz"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (category.isComingSoon) TextMuted else CyanGlow,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open ${category.title}",
                tint = if (category.isComingSoon) TextSecondary else CyanGlow,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Shown for CAR, HCV, and BUS categories:
 * Displays all brands with brand logos and ONLY "COMING SOON" (no stimulation controls).
 */
@Composable
fun ComingSoonBrandsScreen(
    category: VehicleCategoryType,
    btState: BtConnectionState,
    onBack: () -> Unit,
    onOpenBluetoothSheet: () -> Unit
) {
    val brands = VehicleCatalog.comingSoonBrandsByCategory[category].orEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .testTag("coming_soon_brands_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp)
        ) {
            SubScreenTopBar(
                title = "${category.title} BRANDS",
                btState = btState,
                onBack = onBack,
                onOpenBluetoothSheet = onOpenBluetoothSheet,
                backTestTag = "coming_soon_back_button"
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .brushedMetalPanel(
                                cornerRadius = 14.dp,
                                borderColor = WarningAmber.copy(alpha = 0.75f),
                                elevated = true
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Coming Soon",
                                tint = GoldAccent,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "${category.title} STIMULATION • COMING SOON",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${category.subtitle} ECU profiles are under development and will be unlocked in an upcoming update.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                items(brands) { brand ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .brushedMetalPanel(cornerRadius = 14.dp, borderColor = BorderSubtle)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("coming_soon_brand_${brand.lowercase().replace(" ", "_")}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RecessedWell)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                VehicleBrandBadge(brand = brand, sizeDp = 34.dp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = brand,
                                style = MaterialTheme.typography.titleLarge,
                                color = TitaniumSilver,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RecessedWell)
                                .border(1.dp, WarningAmber.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "COMING SOON",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 2 inside LCV:
 * Displays the 5 LCV Brands with custom Brand Logos:
 * TATA, MAHINDRA, ASHOK LEYLAND, FORCE, BHARATBENZ.
 */
@Composable
fun LcvBrandsScreen(
    selectedLcvBrand: String,
    btState: BtConnectionState,
    onBack: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    onSelectBrand: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .testTag("lcv_brands_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp)
        ) {
            SubScreenTopBar(
                title = "SELECT LCV BRAND",
                btState = btState,
                onBack = onBack,
                onOpenBluetoothSheet = onOpenBluetoothSheet,
                backTestTag = "lcv_brands_back_button"
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(VehicleCatalog.lcvBrands) { brand ->
                    val vehiclesCount = VehicleCatalog.vehiclesForLcvBrand(brand).size
                    val isSelected = brand.equals(selectedLcvBrand, ignoreCase = true)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .brushedMetalPanel(
                                cornerRadius = 14.dp,
                                borderColor = if (isSelected) CyanGlow else BorderSubtle,
                                borderWidth = if (isSelected) 1.5.dp else 1.dp,
                                elevated = true
                            )
                            .clickable { onSelectBrand(brand) }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                            .testTag("lcv_brand_card_${brand.lowercase().replace(" ", "_")}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(RecessedWell)
                                    .border(
                                        1.2.dp,
                                        if (isSelected) CyanGlow else BorderSubtle,
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                VehicleBrandBadge(brand = brand, sizeDp = 40.dp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = brand,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "$vehiclesCount LCV Vehicles Available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CyanGlow
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Select $brand",
                            tint = CyanGlow,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Step 3 inside LCV:
 * Displays vehicles for the selected brand ONE BY ONE with prominent vehicle studio image,
 * Brand Logo, Model Name, and ECU System (NEVER shows crank tooth pattern).
 */
@Composable
fun LcvVehiclesScreen(
    brand: String,
    selectedVehicle: VehicleModel,
    btState: BtConnectionState,
    onBack: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    onSelectVehicle: (VehicleModel) -> Unit
) {
    val brandVehicles = VehicleCatalog.vehiclesForLcvBrand(brand)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .testTag("lcv_vehicles_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp)
        ) {
            SubScreenTopBar(
                title = "$brand LCV",
                btState = btState,
                onBack = onBack,
                onOpenBluetoothSheet = onOpenBluetoothSheet,
                backTestTag = "lcv_vehicles_back_button"
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("lcv_vehicle_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(brandVehicles, key = { it.id }) { vehicle ->
                    val isSelected = vehicle.id == selectedVehicle.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .brushedMetalPanel(
                                cornerRadius = 16.dp,
                                borderColor = if (isSelected) CyanGlow else BorderSubtle,
                                borderWidth = if (isSelected) 1.6.dp else 1.dp,
                                elevated = true
                            )
                            .clickable { onSelectVehicle(vehicle) }
                            .testTag("vehicle_card_${vehicle.id}")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(155.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = vehiclePreviewDrawable(vehicle)),
                                    contentDescription = "${vehicle.brand} ${vehicle.modelName}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color(0xFF061024).copy(alpha = 0.88f)
                                                )
                                            )
                                        )
                                )

                                // Brand Logo Overlay in top-left corner
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(RecessedWell.copy(alpha = 0.88f))
                                        .border(1.dp, CyanGlow.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    VehicleBrandBadge(brand = vehicle.brand, sizeDp = 22.dp)
                                    Text(
                                        text = vehicle.brand,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TitaniumSilver,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected Vehicle",
                                        tint = SignalGreen,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(10.dp)
                                            .size(26.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = vehicle.modelName,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${vehicle.category} • ${vehicle.ecuSystem}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElectricBlue.copy(alpha = 0.22f))
                                        .border(1.dp, CyanGlow.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "SELECT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanGlow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shared compact sub-screen top bar with Back Arrow, Screen Title, and Small Bluetooth Icon.
 */
@Composable
fun SubScreenTopBar(
    title: String,
    btState: BtConnectionState,
    onBack: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    backTestTag: String = "subscreen_back_button",
    trailingContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag(backTestTag)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            trailingContent()
            SmallBluetoothStatusIcon(
                btState = btState,
                onClick = onOpenBluetoothSheet
            )
        }
    }
}
