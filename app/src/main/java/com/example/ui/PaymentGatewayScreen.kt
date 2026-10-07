package com.example.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.payment.SubscriptionPlan
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ButtonDarkSurface
import com.example.ui.theme.CardDarkSurface
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenBg
import com.example.ui.theme.StopRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber

/**
 * Interactive Payment & License Activation Modal Sheet.
 * Supports:
 * 1. Official Stripe Android PaymentSheet SDK (`com.stripe:stripe-android`)
 * 2. Official Razorpay Standard Checkout (UPI, Debit/Credit Cards, NetBanking, Wallets)
 * 3. Direct UPI App Deep-Link (Google Pay / PhonePe / Paytm / BHIM)
 * 4. Device-bound SHA-256 License Activation Key unlock
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewayBottomSheet(
    isProUnlocked: Boolean,
    activePlanTitle: String,
    lastPaymentId: String?,
    lastPaymentDate: String?,
    deviceHardwareCode: String,
    selectedPlan: SubscriptionPlan,
    paymentError: String?,
    razorpayCheckoutHtml: String?,
    onSelectPlan: (SubscriptionPlan) -> Unit,
    onPayWithStripe: (SubscriptionPlan) -> Unit,
    onPayWithRazorpay: (SubscriptionPlan) -> Unit,
    onPayWithUpiIntent: (SubscriptionPlan) -> Unit,
    onRedeemActivationKey: (String) -> Unit,
    onRazorpayWebSuccess: (String) -> Unit,
    onRazorpayWebError: (String) -> Unit,
    onCloseRazorpayWeb: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var activationKeyInput by remember { mutableStateOf("") }
    var showKeySection by remember { mutableStateOf(false) }

    if (razorpayCheckoutHtml != null) {
        RazorpayWebCheckoutDialog(
            htmlContent = razorpayCheckoutHtml,
            onPaymentSuccess = onRazorpayWebSuccess,
            onPaymentError = onRazorpayWebError,
            onDismiss = onCloseRazorpayWeb
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidian,
        contentColor = TextWhite,
        modifier = Modifier.testTag("payment_gateway_bottom_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(ElectricBlue, CyanGlow)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = DeepObsidian,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ECU LAB PRO GATEWAY",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Secured by Stripe & Razorpay 256-Bit PCI-DSS Checkout",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanGlow
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_payment_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Current License Status Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isProUnlocked) SignalGreenBg else CardDarkSurface,
                    border = BorderStroke(
                        1.dp,
                        if (isProUnlocked) SignalGreen else ElectricBlue.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isProUnlocked) Icons.Default.Verified else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isProUnlocked) SignalGreen else WarningAmber,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isProUnlocked) "PRO LICENSE ACTIVE: $activePlanTitle" else "CURRENT PLAN: STANDARD ACCESS",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isProUnlocked) SignalGreen else TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isProUnlocked && lastPaymentId != null) {
                                        "Ref: $lastPaymentId • Activated $lastPaymentDate"
                                    } else {
                                        "Select a plan below to unlock commercial ECU features & lifetime updates"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Subscription Plan Cards
            item {
                Text(
                    text = "SELECT SUBSCRIPTION PLAN",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanGlow,
                    letterSpacing = 1.4.sp
                )
            }

            items(SubscriptionPlan.entries) { plan ->
                val isSelected = plan == selectedPlan
                SubscriptionPlanCard(
                    plan = plan,
                    isSelected = isSelected,
                    onClick = { onSelectPlan(plan) }
                )
            }

            // Error or Configuration Notice
            if (paymentError != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StopRed.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, StopRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_error_banner")
                    ) {
                        Text(
                            text = paymentError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Primary Checkout Action Buttons (Razorpay + Stripe + Direct UPI App)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onPayWithRazorpay(selectedPlan) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("pay_with_razorpay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PAY ₹${selectedPlan.priceInr} VIA RAZORPAY (UPI / CARDS)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { onPayWithStripe(selectedPlan) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CyanGlow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("pay_with_stripe_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = CyanGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PAY ₹${selectedPlan.priceInr} VIA STRIPE PAYMENTSHEET",
                            style = MaterialTheme.typography.labelLarge,
                            color = CyanGlow,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { onPayWithUpiIntent(selectedPlan) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SignalGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("pay_with_upi_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = SignalGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PAY ₹${selectedPlan.priceInr} DIRECT VIA UPI APP (GPAY / PHONEPE)",
                            style = MaterialTheme.typography.labelLarge,
                            color = SignalGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Device ID & Offline License Key Activation Section
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardDarkSurface,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showKeySection = !showKeySection }
                                .testTag("toggle_license_key_section"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = CyanGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "HAVE AN ACTIVATION KEY?",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Device Hardware ID: $deviceHardwareCode",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanGlow
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("ECU LAB Device ID", deviceHardwareCode))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Device ID",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (showKeySection) {
                            OutlinedTextField(
                                value = activationKeyInput,
                                onValueChange = { activationKeyInput = it },
                                label = { Text("Enter License Activation Key") },
                                placeholder = { Text("e.g. PRO-XXXX-XXXX", color = TextMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DeepObsidian,
                                    unfocusedContainerColor = DeepObsidian,
                                    focusedBorderColor = ElectricBlue,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("activation_key_input")
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { onRedeemActivationKey(activationKeyInput) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SignalGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("activate_key_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = DeepObsidian,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ACTIVATE LICENSE",
                                        color = DeepObsidian,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:akautodiagnostics4@gmail.com")
                                            putExtra(
                                                Intent.EXTRA_SUBJECT,
                                                "ECU LAB Activation Key Request ($deviceHardwareCode)"
                                            )
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Hello AK Auto Diagnostics,\n\nPlease share the activation key for my ECU LAB app.\nDevice ID: $deviceHardwareCode\nSelected Plan: ${selectedPlan.title} (₹${selectedPlan.priceInr})\n"
                                            )
                                        }
                                        try {
                                            context.startActivity(emailIntent)
                                        } catch (_: Exception) {}
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("REQUEST KEY", color = CyanGlow, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun RazorpayWebCheckoutDialog(
    htmlContent: String,
    onPaymentSuccess: (String) -> Unit,
    onPaymentError: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidian,
        title = {
            Text(
                text = "Razorpay Secure Checkout",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                            addJavascriptInterface(
                                object {
                                    @JavascriptInterface
                                    fun onPaymentSuccess(paymentId: String) {
                                        onPaymentSuccess(paymentId)
                                    }

                                    @JavascriptInterface
                                    fun onPaymentError(desc: String) {
                                        onPaymentError(desc)
                                    }

                                    @JavascriptInterface
                                    fun onPaymentCancelled() {
                                        onDismiss()
                                    }
                                },
                                "AndroidBridge"
                            )
                            loadDataWithBaseURL(
                                "https://checkout.razorpay.com",
                                htmlContent,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) CyanGlow else BorderSubtle
    val containerColor = if (isSelected) ButtonDarkSurface else CardDarkSurface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("plan_card_${plan.id}"),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) ElectricBlue else DeepObsidian)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = plan.badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) TextWhite else CyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = plan.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${plan.priceInr}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isSelected) CyanGlow else TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "INR",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            plan.features.forEach { feature ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isSelected) SignalGreen else CyanGlow,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
