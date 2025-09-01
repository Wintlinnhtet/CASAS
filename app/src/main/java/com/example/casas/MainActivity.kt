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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

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
    @Suppress("DEPRECATION")
    private fun currentSsidBssid(): Pair<String, String> {
        try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.activeNetwork?.let { net ->
                cm.getNetworkCapabilities(net)?.let { caps ->
                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        (caps.transportInfo as? WifiInfo)?.let { w ->
                            val ssid = (w.ssid ?: "Unknown").trim('"')
                            val bssid = w.bssid ?: "Unknown"
                            val clean = if (bssid == "02:00:00:00:00:00") "Unknown" else bssid
                            if (ssid != "<unknown ssid>" || clean != "Unknown") return ssid to clean
                        }
                    }
                }
            }
            // Fallback for older devices / OEMs
            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wm.connectionInfo?.let { info ->
                val ssid = (info.ssid ?: "Unknown").trim('"')
                val bssid = info.bssid ?: "Unknown"
                val clean = if (bssid == "02:00:00:00:00:00") "Unknown" else bssid
                return ssid to clean
            }
        } catch (_: Exception) { }
        return "Unknown" to "Unknown"
    }

    private fun debugWifiAccess() {
        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasNearby = if (Build.VERSION.SDK_INT >= 33)
            ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
        else true

        val lm = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        val locationOn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) lm.isLocationEnabled else {
            try { Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE) != 0 } catch (_: Exception) { false }
        }

        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val active = cm.activeNetwork
        val caps = active?.let { cm.getNetworkCapabilities(it) }
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val wifiInfo = caps?.transportInfo as? WifiInfo

        android.util.Log.i("WifiDiag",
            "fine=$hasFine coarse=$hasCoarse nearby=$hasNearby locOn=$locationOn isWifi=$isWifi " +
                    "wifiInfo=${wifiInfo != null} ssid=${wifiInfo?.ssid} bssid=${wifiInfo?.bssid}")

        if (!hasFine) {
            Toast.makeText(this, "Grant PRECISE Location permission (not Approximate).", Toast.LENGTH_LONG).show()
        }
        if (!locationOn) {
            Toast.makeText(this, "Turn ON device Location in Quick Settings.", Toast.LENGTH_LONG).show()
        }
        if (!isWifi) {
            Toast.makeText(this, "Not on Wi-Fi (or MIUI is masking). Connect to a normal Wi-Fi.", Toast.LENGTH_LONG).show()
        }
    }

    private fun publishAttendanceEvent() {
        val (ssid, bssid) = currentSsidBssid()
        android.util.Log.d("WifiCheck", "SSID=$ssid  BSSID=$bssid")

        val ip = localIpv4()
        val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date())
        val payload = JSONObject().apply {
            put("userId", "app_user_001")
            put("wifiSsid", ssid)
            put("wifiBssid", bssid)
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
        val needsFine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val needsNearby = ContextCompat.checkSelfPermission(
                this, Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED

            // 🔹 On API 33/34 you need BOTH: NEARBY + FINE
            if (needsNearby) {
                requestNearbyWifiPermLauncher.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
            if (needsFine) {
                requestLocationPermsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } else {
            val needsCoarse = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            if (needsFine || needsCoarse) {
                requestLocationPermsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }

        // 🔹 Also ensure device Location is ON (otherwise SSID/BSSID are masked)
        val lm = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        val locationOn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lm.isLocationEnabled
        } else {
            // simple fallback for very old devices
            try { android.provider.Settings.Secure.getInt(contentResolver,
                android.provider.Settings.Secure.LOCATION_MODE) != 0 } catch (_: Exception) { false }
        }
        if (!locationOn) {
            Toast.makeText(this, "Turn on Location to read Wi-Fi SSID/BSSID", Toast.LENGTH_LONG).show()
            startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
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
