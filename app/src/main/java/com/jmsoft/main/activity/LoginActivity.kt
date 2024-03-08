package com.jmsoft.main.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ActivityLoginBinding
import java.util.Locale


class LoginActivity : AppCompatActivity() {
    val activity: Activity = this@LoginActivity

    lateinit var binding: ActivityLoginBinding
    @RequiresApi(Build.VERSION_CODES.S)
    private val permissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADMIN
    )
    private val permissionsRequestCode = 100 // You can use any value for the request code


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        setLocale("ar")
        setContentView(binding.root)

        binding.btnBluetoothOn?.setOnClickListener {
            if (hasBluetoothPermissions()) {
                enableBluetooth(activity, 1000)
            } else {
                requestPermissions()
            }
        }


    }

    var bluetoothIntent: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {

            }
        }


    fun enableBluetooth(activity: Activity, requestCode: Int) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            // Handle this case accordingly
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothIntent.launch(enableBtIntent)
        } else {

            // Bluetooth is already enabled
            // You can perform further actions here if needed
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, permissionsRequestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionsRequestCode) {
            // Check if all permissions are granted
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                // All permissions are granted, proceed with your logic
                // For example, start Bluetooth functionality
                enableBluetooth(activity, 1000)
            } else {
                Utils.T(activity, "Please allow the Permission To connect with your Device")
                // Permissions are not granted, handle the scenario
                // For example, show a message to the user or disable Bluetooth functionality
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    fun Context.hasBluetoothPermissions(): Boolean {
        val bluetoothPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
        val bluetoothAdminPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
        val bluetoothScanPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
        val bluetoothConnectPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)

        return bluetoothPermission == PackageManager.PERMISSION_GRANTED &&
                bluetoothAdminPermission == PackageManager.PERMISSION_GRANTED &&
                bluetoothScanPermission == PackageManager.PERMISSION_GRANTED &&
                bluetoothConnectPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}