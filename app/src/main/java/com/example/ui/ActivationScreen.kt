package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
import com.example.viewmodel.EcuLabViewModel

/**
 * Hardware-Bound Device ID & Activation Key Screen for ECU LAB.
 *
 * Customer Workflow:
 * 1. Customer sees their unique Device ID (e.g., AK-7F39-C812).
 * 2. Customer copies or sends their Device ID via WhatsApp / Email (`akautodiagnostics4@gmail.com`) after UPI payment.
 * 3. You generate their unique Unlock Key using the built-in Admin Key Generator (PIN: 9791) and send it to them.
 * 4. Customer enters the Activation Key once to permanently unlock the app on their phone.
 */
@Composable
fun ActivationScreen(
    deviceId: String,
    activationError: String?,
    onActivateSubmit: (String) -> Unit,
    onClearError: () -> Unit,
    onOpenAdminGenerator: () -> Unit
) {
    val context = LocalContext.current
    var activationKeyInput by remember { mutableStateOf("") }
    var logoTapCount by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF102038),
                        DeepObsidian,
                        Color(0xFF05070A)
                    )
                )
            )
            .imePadding()
            .testTag("activation_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular AK ECU Signal Lab Logo Badge (Tap 5 times or use Admin button below to open Admin Key Generator)
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(listOf(CyanGlow, ElectricBlue)),
                        shape = CircleShape
                    )
                    .clickable {
                        logoTapCount++
                        if (logoTapCount >= 5) {
                            logoTapCount = 0
                            onOpenAdminGenerator()
                        }
                    }
                    .testTag("activation_logo_badge"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_ak_ecu_logo_1791276792510),
                    contentDescription = "ECU LAB Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ECU LAB",
                style = MaterialTheme.typography.displayMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp
            )

            Text(
                text = "DEVICE LICENSE ACTIVATION",
                style = MaterialTheme.typography.labelLarge,
                color = CyanGlow,
                letterSpacing = 1.6.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step 1: Device ID Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STEP 1: YOUR DEVICE ID",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Text(
                            text = "UNIQUE ID",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Text(
                        text = "After completing your UPI payment, send this Device ID on WhatsApp or Email to receive your permanent Unlock Key:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    // Highlighted Device ID Box with Copy button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepObsidian)
                            .border(1.5.dp, CyanGlow.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Device ID",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = deviceId,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                modifier = Modifier.testTag("device_id_text")
                            )
                        }

                        IconButton(
                            onClick = {
                                copyToClipboard(context, "ECU LAB Device ID", deviceId)
                                Toast.makeText(context, "Device ID copied: $deviceId", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricBlue.copy(alpha = 0.2f))
                                .testTag("copy_device_id_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Device ID",
                                tint = CyanGlow
                            )
                        }
                    }

                    // Share on WhatsApp / Send via Email buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val message = "Hello AK Auto Diagnostics,\nI have completed the payment for ECU LAB.\nMy Device ID is: $deviceId\nPlease send my Activation Unlock Key."
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, message)
                                }
                                try {
                                    context.startActivity(Intent.createChooser(shareIntent, "Send Device ID via WhatsApp / Message"))
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SignalGreen),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("share_whatsapp_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = SignalGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SEND ON WHATSAPP",
                                style = MaterialTheme.typography.labelMedium,
                                color = SignalGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:akautodiagnostics4@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "ECU LAB Activation Request - Device ID: $deviceId")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Hello AK Auto Diagnostics,\n\nMy ECU LAB Device ID is: $deviceId\n\nPlease send my Activation Code / Unlock Key."
                                    )
                                }
                                try {
                                    context.startActivity(emailIntent)
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("send_email_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EMAIL DEVICE ID",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Enter Activation Code Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = SignalGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STEP 2: ENTER ACTIVATION KEY",
                            style = MaterialTheme.typography.labelLarge,
                            color = SignalGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }

                    OutlinedTextField(
                        value = activationKeyInput,
                        onValueChange = {
                            activationKeyInput = it.uppercase()
                            if (activationError != null) onClearError()
                        },
                        label = { Text("Activation Code / Unlock Key") },
                        placeholder = { Text("XXXX-XXXX-XXXX", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Unlock Key",
                                tint = CyanGlow
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onActivateSubmit(activationKeyInput) }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DeepObsidian,
                            unfocusedContainerColor = DeepObsidian,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = CyanGlow,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_key_input")
                    )

                    if (activationError != null) {
                        Text(
                            text = activationError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = StopRed,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("activation_error_text")
                        )
                    }

                    Button(
                        onClick = { onActivateSubmit(activationKeyInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("activate_unlock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACTIVATE & PERMANENTLY UNLOCK",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Support & Admin Key Generator Footer
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardDarkSurface.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AK Auto Diagnostics Support",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "akautodiagnostics4@gmail.com",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanGlow
                        )
                    }

                    TextButton(
                        onClick = onOpenAdminGenerator,
                        modifier = Modifier.testTag("open_admin_keygen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Key Generator",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ADMIN KEYGEN",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Built-in Admin Key Generator Dialog (Protected by Admin PIN `9791`).
 * Allows you (`akautodiagnostics`) to paste any customer's Device ID and instantly generate
 * their permanent Activation Unlock Key, or unlock your own phone in one tap.
 */
@Composable
fun AdminKeyGeneratorDialog(
    currentDeviceId: String,
    onVerifyAdminPin: (String) -> Boolean,
    onGenerateKeyForDevice: (String) -> String,
    onUnlockThisDeviceDirectly: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var isPinVerified by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    var customerDeviceId by remember { mutableStateOf(currentDeviceId) }
    var generatedKey by remember {
        mutableStateOf(onGenerateKeyForDevice(currentDeviceId))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDarkSurface,
        titleContentColor = TextWhite,
        textContentColor = TextSecondary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = CyanGlow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isPinVerified) "Admin Unlock Key Generator" else "Admin Security PIN",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (!isPinVerified) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter your AK Auto Diagnostics Admin PIN to open the Customer Activation Key Generator.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            pinError = null
                        },
                        label = { Text("Admin PIN") },
                        placeholder = { Text("Enter Admin PIN", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CyanGlow
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (onVerifyAdminPin(pinInput)) {
                                    isPinVerified = true
                                } else {
                                    pinError = "Incorrect Admin PIN"
                                }
                            }
                        ),
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
                            .testTag("admin_pin_input")
                    )

                    if (pinError != null) {
                        Text(
                            text = pinError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = StopRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter the customer's Device ID (sent via WhatsApp/Email after UPI payment) to generate their permanent Unlock Key:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = customerDeviceId,
                        onValueChange = {
                            customerDeviceId = it.uppercase()
                            generatedKey = if (it.isNotBlank()) {
                                onGenerateKeyForDevice(it)
                            } else {
                                ""
                            }
                        },
                        label = { Text("Customer Device ID") },
                        placeholder = { Text("e.g. AK-7F39-C812", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii
                        ),
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
                            .testTag("admin_customer_device_id_input")
                    )

                    if (generatedKey.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SignalGreenBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SignalGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "GENERATED UNLOCK KEY:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SignalGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = generatedKey,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp,
                                        modifier = Modifier.testTag("generated_unlock_key_text")
                                    )
                                    IconButton(
                                        onClick = {
                                            copyToClipboard(context, "ECU LAB Unlock Key", generatedKey)
                                            Toast.makeText(context, "Unlock Key copied: $generatedKey", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DeepObsidian)
                                            .testTag("copy_generated_key_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Unlock Key",
                                            tint = SignalGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Share key back to customer via WhatsApp
                        OutlinedButton(
                            onClick = {
                                val msg = "AK Auto Diagnostics - ECU LAB\nDevice ID: ${customerDeviceId.trim()}\nYour Permanent Activation Key: $generatedKey"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, msg)
                                }
                                try {
                                    context.startActivity(Intent.createChooser(shareIntent, "Send Unlock Key to Customer"))
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SHARE UNLOCK KEY TO CUSTOMER",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))

                    // Instant unlock for admin's own phone
                    Button(
                        onClick = onUnlockThisDeviceDirectly,
                        colors = ButtonDefaults.buttonColors(containerColor = SignalGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("admin_unlock_this_device_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = DeepObsidian,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "UNLOCK THIS PHONE NOW",
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepObsidian,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isPinVerified) {
                Button(
                    onClick = {
                        if (onVerifyAdminPin(pinInput)) {
                            isPinVerified = true
                        } else {
                            pinError = "Incorrect Admin PIN"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.testTag("verify_admin_pin_button")
                ) {
                    Text("OPEN GENERATOR", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonDarkSurface)
                ) {
                    Text("DONE", color = TextWhite)
                }
            }
        },
        dismissButton = {
            if (!isPinVerified) {
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        }
    )
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}
