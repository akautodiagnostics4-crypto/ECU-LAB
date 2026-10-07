package com.example.payment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.example.BuildConfig
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Subscription / License Tiers available in ECU LAB.
 */
enum class SubscriptionPlan(
    val id: String,
    val title: String,
    val subtitle: String,
    val priceInr: Int,
    val durationDays: Int,
    val badgeText: String,
    val features: List<String>
) {
    MonthlyPro(
        id = "ecu_lab_monthly",
        title = "Monthly Pro Pass",
        subtitle = "30 Days Full Multi-Vehicle Access",
        priceInr = 499,
        durationDays = 30,
        badgeText = "STARTER",
        features = listOf(
            "Unlock All 9 Vehicle Brands (Mahindra, Tata, Ashok Leyland, Maruti, Hyundai, Force, Eicher, BharatBenz)",
            "Full Real-Time Signal Generation (RPM, Rail, ECT, Speed, Boost MAP & MAF)",
            "Unlimited Custom Bench Presets"
        )
    ),
    YearlyWorkshop(
        id = "ecu_lab_yearly",
        title = "1-Year Workshop License",
        subtitle = "365 Days Commercial Workshop Access",
        priceInr = 1499,
        durationDays = 365,
        badgeText = "MOST POPULAR",
        features = listOf(
            "Full Commercial HCV & Bus ECU Profiles (BharatBenz, Eicher Pro, Ashok Leyland, Tata)",
            "Priority Support via akautodiagnostics4@gmail.com",
            "All Sensor Channels + Cam Sync & Injector Load Pulse Control"
        )
    ),
    LifetimeMaster(
        id = "ecu_lab_lifetime",
        title = "Lifetime Master Unlock",
        subtitle = "Permanent Single-Device License",
        priceInr = 2999,
        durationDays = 36500, // 100 years
        badgeText = "BEST VALUE",
        features = listOf(
            "Permanent Lifetime Access on This Device — Never Expires",
            "All Current & Future ECU Models Unlocked",
            "Offline Device License Key Included"
        )
    )
}

data class PaymentReceipt(
    val paymentId: String,
    val planId: String,
    val planTitle: String,
    val amountInr: Int,
    val method: String,
    val timestampFormatted: String
)

/**
 * Manages Stripe Android SDK (`PaymentConfiguration` + `PaymentSheet`),
 * Razorpay Standard WebView Checkout, Direct UPI Deep-Link checkout,
 * and Device-bound SHA-256 License Key verification.
 */
class RazorpayPaymentManager(private val context: Context) {

    companion object {
        private const val TAG = "PaymentGatewayMgr"
        private const val PREFS_PAYMENT = "ecu_lab_payment_security_prefs"
        private const val KEY_IS_PRO_UNLOCKED = "is_pro_unlocked"
        private const val KEY_ACTIVE_PLAN_ID = "active_plan_id"
        private const val KEY_ACTIVE_PLAN_TITLE = "active_plan_title"
        private const val KEY_LAST_PAYMENT_ID = "last_payment_id"
        private const val KEY_LAST_PAYMENT_DATE = "last_payment_date"
        private const val KEY_EXPIRY_EPOCH_MS = "expiry_epoch_ms"

        private const val LICENSE_SECRET_SALT = "AK_AUTO_DIAGNOSTICS_ECU_LAB_9791_SALT"
    }

    private val prefs = context.getSharedPreferences(PREFS_PAYMENT, Context.MODE_PRIVATE)

    init {
        initializeStripeIfConfigured()
    }

    fun initializeStripeIfConfigured(): Boolean {
        val publishableKey = BuildConfig.STRIPE_PUBLISHABLE_KEY
        if (isStripeKeyConfigured()) {
            try {
                PaymentConfiguration.init(context.applicationContext, publishableKey)
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Stripe init warning: ${e.message}")
            }
        }
        return false
    }

    fun isStripeKeyConfigured(): Boolean {
        val key = BuildConfig.STRIPE_PUBLISHABLE_KEY
        return key.isNotBlank() &&
            key != "STRIPE_PUBLISHABLE_KEY_DEFAULT_VALUE" &&
            (key.startsWith("pk_live_") || key.startsWith("pk_test_"))
    }

    fun isRazorpayKeyConfigured(): Boolean {
        val key = BuildConfig.RAZORPAY_KEY_ID
        return key.isNotBlank() &&
            key != "RAZORPAY_KEY_ID_DEFAULT_VALUE" &&
            (key.startsWith("rzp_live_") || key.startsWith("rzp_test_"))
    }

    /**
     * Builds the Stripe PaymentSheet.Configuration with dark automotive styling.
     */
    fun buildStripeSheetConfig(): PaymentSheet.Configuration {
        return PaymentSheet.Configuration.Builder(merchantDisplayName = "AK Auto Diagnostics - ECU LAB")
            .allowsDelayedPaymentMethods(true)
            .build()
    }

    /**
     * Generates a clean, deterministic hardware Device ID (e.g., "AK-7F3A-92C1")
     * so the technician can activate via offline key if needed.
     */
    fun getDeviceHardwareCode(): String {
        val androidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "ECULAB001"
        } catch (_: Exception) {
            "ECULAB001"
        }
        val hash = sha256(androidId.uppercase(Locale.US))
        return "AK-${hash.substring(0, 4)}-${hash.substring(4, 8)}"
    }

    /**
     * Computes the expected Activation Key for a given Device Hardware Code.
     * Also accepts the universal master admin code "AK9791PRO".
     */
    fun generateActivationKeyForDevice(deviceCode: String): String {
        val normalized = deviceCode.trim().uppercase(Locale.US)
        val digest = sha256("$normalized:$LICENSE_SECRET_SALT")
        return "PRO-${digest.substring(0, 4)}-${digest.substring(4, 8)}"
    }

    fun verifyActivationKey(inputKey: String): Boolean {
        val clean = inputKey.trim().uppercase(Locale.US)
        if (clean == "AK9791PRO" || clean == "AK-9791-PRO") return true
        val expected = generateActivationKeyForDevice(getDeviceHardwareCode())
        return clean == expected
    }

    /**
     * Builds the secure Razorpay Checkout HTML payload for embedded WebView checkout
     * when `RAZORPAY_KEY_ID` is configured in the AI Studio Secrets panel.
     */
    fun buildRazorpayCheckoutHtml(
        plan: SubscriptionPlan,
        customerEmail: String = "akautodiagnostics4@gmail.com"
    ): String {
        val keyId = BuildConfig.RAZORPAY_KEY_ID
        val amountPaise = plan.priceInr * 100
        val deviceCode = getDeviceHardwareCode()
        val optionsJson = JSONObject().apply {
            put("key", keyId)
            put("amount", amountPaise)
            put("currency", "INR")
            put("name", "AK Auto Diagnostics")
            put("description", "ECU LAB - ${plan.title}")
            put("prefill", JSONObject().apply {
                put("email", customerEmail)
            })
            put("notes", JSONObject().apply {
                put("device_id", deviceCode)
                put("plan_id", plan.id)
            })
            put("theme", JSONObject().apply {
                put("color", "#1E88E5")
            })
        }.toString()

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
                <style>
                    body { background-color: #0A0D12; color: #FFFFFF; font-family: sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; text-align: center; }
                </style>
            </head>
            <body>
                <div>
                    <h3>Initializing Secure Razorpay Checkout...</h3>
                    <p>Plan: ${plan.title} (₹${plan.priceInr})</p>
                </div>
                <script>
                    var options = $optionsJson;
                    options.handler = function (response) {
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onPaymentSuccess(response.razorpay_payment_id);
                        }
                    };
                    options.modal = {
                        ondismiss: function() {
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onPaymentCancelled();
                            }
                        }
                    };
                    var rzp = new Razorpay(options);
                    rzp.on('payment.failed', function (response){
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onPaymentError(response.error.description || 'Payment failed');
                        }
                    });
                    rzp.open();
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Launches installed UPI payment apps (Google Pay, PhonePe, Paytm, BHIM) via standard NPCI UPI deep-link.
     */
    fun buildUpiPaymentIntent(plan: SubscriptionPlan, upiId: String = "akautodiagnostics@upi"): Intent {
        val trRef = "ECULAB${System.currentTimeMillis() % 1000000}"
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", "AK Auto Diagnostics")
            .appendQueryParameter("tn", "ECU LAB ${plan.title} (${getDeviceHardwareCode()})")
            .appendQueryParameter("am", "${plan.priceInr}.00")
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tr", trRef)
            .build()

        return Intent(Intent.ACTION_VIEW, uri)
    }

    fun isProUnlocked(): Boolean {
        val unlocked = prefs.getBoolean(KEY_IS_PRO_UNLOCKED, false)
        if (!unlocked) return false
        val expiry = prefs.getLong(KEY_EXPIRY_EPOCH_MS, Long.MAX_VALUE)
        return System.currentTimeMillis() < expiry
    }

    fun getActivePlanTitle(): String {
        return prefs.getString(KEY_ACTIVE_PLAN_TITLE, "Standard Access") ?: "Standard Access"
    }

    fun getLastPaymentId(): String? {
        return prefs.getString(KEY_LAST_PAYMENT_ID, null)
    }

    fun getLastPaymentDate(): String? {
        return prefs.getString(KEY_LAST_PAYMENT_DATE, null)
    }

    fun recordSuccessfulPayment(
        paymentId: String,
        plan: SubscriptionPlan,
        methodLabel: String
    ): PaymentReceipt {
        val now = System.currentTimeMillis()
        val expiryMs = now + (plan.durationDays.toLong() * 24L * 60L * 60L * 1000L)
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(now))

        prefs.edit()
            .putBoolean(KEY_IS_PRO_UNLOCKED, true)
            .putString(KEY_ACTIVE_PLAN_ID, plan.id)
            .putString(KEY_ACTIVE_PLAN_TITLE, plan.title)
            .putString(KEY_LAST_PAYMENT_ID, paymentId)
            .putString(KEY_LAST_PAYMENT_DATE, dateStr)
            .putLong(KEY_EXPIRY_EPOCH_MS, expiryMs)
            .apply()

        return PaymentReceipt(
            paymentId = paymentId,
            planId = plan.id,
            planTitle = plan.title,
            amountInr = plan.priceInr,
            method = methodLabel,
            timestampFormatted = dateStr
        )
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02X".format(it) }
    }
}
