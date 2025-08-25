package com.example.casas

import android.Manifest
import android.app.KeyguardManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.casas.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // --- MQTT service binding ---
    private var mqttService: MqttForegroundService? = null
    private var bound = false
    private val svcConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mqttService = (service as MqttForegroundService.LocalBinder).getService()
            bound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false; mqttService = null
        }
    }

    private lateinit var biometricExecutor: java.util.concurrent.Executor
    private var afterAuthNavigate: Boolean = false

    // --- permission launchers (must be fields, not locals) ---
    private lateinit var requestNearbyWifiPermLauncher: ActivityResultLauncher<String>
    private lateinit var requestLocationPermsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestNotifPerm: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init permission launchers
        requestNearbyWifiPermLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* granted? you can toast/log if you want */ }

        requestLocationPermsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { /* results: map<String,Boolean> */ }

        requestNotifPerm = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* ignore result; FGS still runs, but notification may be hidden if denied */ }

        // start + bind MQTT foreground service
        val intent = Intent(this, MqttForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
        bindService(intent, svcConn, Context.BIND_AUTO_CREATE)

        biometricExecutor = ContextCompat.getMainExecutor(this)

        binding.loginButton.setOnClickListener(View.OnClickListener {
            val u = binding.username.text.toString()
            val p = binding.password.text.toString()
            if (u == "user" && p == "1234") {
                afterAuthNavigate = true
                authenticateAndPublish()
            } else {
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        if (bound) unbindService(svcConn)
        super.onDestroy()
    }

    // ===== AUTH FLOW =====
    private fun authenticateAndPublish() {
        requestWifiPermissionsIfNeeded()
        showBiometricOrCredentialPrompt()
    }

    private fun showBiometricOrCredentialPrompt() {
        val authenticators = Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL
        val bm = BiometricManager.from(this)

        when (bm.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> { /* ok */ }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val enroll = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                    .putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, authenticators)
                startActivity(enroll); return
            }
            else -> {
                val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (!km.isDeviceSecure) { startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)); return }
            }
        }

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                publishAttendanceEvent()
                if (afterAuthNavigate) {
                    startActivity(Intent(this@MainActivity, activity_dashboard::class.java))
                    finish()
                }
            }
            override fun onAuthenticationError(code: Int, msg: CharSequence) {
                super.onAuthenticationError(code, msg)
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }

        val prompt = BiometricPrompt(this, biometricExecutor, callback)
        val info =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authenticate to verify attendance")
                    .setSubtitle("Use biometric or device credentials")
                    .setAllowedAuthenticators(authenticators)
                    .build()
            else
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authenticate to verify attendance")
                    .setSubtitle("Use biometric or device credentials")
                    .setDeviceCredentialAllowed(true)
                    .build()

        prompt.authenticate(info)
    }

    // ===== MQTT PUBLISH =====
    private fun publishAttendanceEvent() {
        val ssid = currentSsid()
        val ip = localIpv4()
        val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date())

        val payload = JSONObject().apply {
            put("userId", "app_user_001")
            put("wifiSsid", ssid)
            put("wifiIpAddress", ip)
            put("timestamp", ts)
        }.toString()

        if (bound && mqttService != null) {
            mqttService?.publish("your_app/auth_events", payload, qos = 1, retained = false)
            Toast.makeText(this, "Attendance event sent", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "MQTT service not connected", Toast.LENGTH_SHORT).show()
        }
    }

    // ===== PERMISSIONS (for SSID on Android 10–14) =====
    private fun requestWifiPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNearby =
                ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) ==
                        PackageManager.PERMISSION_GRANTED
            if (!hasNearby) {
                requestNearbyWifiPermLauncher.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        } else {
            val needFine =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED
            val needCoarse =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED
            if (needFine || needCoarse) {
                requestLocationPermsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }

    // ===== NET HELPERS =====
    private fun currentSsid(): String {
        return try {
            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            wm.connectionInfo?.ssid?.trim('"') ?: "Unknown"
        } catch (_: Exception) { "Unknown" }
    }

    private fun localIpv4(): String {
        return try {
            val ifaces = NetworkInterface.getNetworkInterfaces().toList()
            for (intf in ifaces) {
                if (!intf.isUp || intf.isLoopback) continue
                for (addr in intf.inetAddresses) {
                    if (addr is Inet4Address && addr.isSiteLocalAddress) return addr.hostAddress
                }
            }
            "N/A"
        } catch (_: Exception) { "N/A" }
    }
}
