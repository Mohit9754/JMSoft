@file:Suppress("DEPRECATION")

package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemDeviceListBinding
import com.jmsoft.databinding.ItemWifiPasswordBinding

class WIFIScanAdapter(
    private val context: Context,
    private val wifiScanList: ArrayList<ScanResult>,
    private var connectedSSID: String? = null,
    private var connectedBSSID: String? = null

) : RecyclerView.Adapter<WIFIScanAdapter.MyViewHolder>() {

    private var selectedBinding: ItemDeviceListBinding? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemDeviceListBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = wifiScanList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(wifiScanList[position])
    }

    @SuppressLint("MissingPermission")
    inner class MyViewHolder(private val binding: ItemDeviceListBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var scanResult: ScanResult

        fun bind(scanResult: ScanResult) {

            this.scanResult = scanResult

            // hide views
            hide()

            // set device name
            setDeviceName()

            // set device mac address
            setDeviceMacAddress()

            // check connection
            checkConnection()

            // check protected status
            checkProtectedStatus()

            binding.mcvDevice.setOnClickListener(this)

        }

        // check protected status
        private fun checkProtectedStatus() {

            if (isWifiPasswordProtected(scanResult)) {
                binding.ivDelete.setImageResource(R.drawable.ic_wifi_password)
            } else {
                binding.ivDelete.setImageResource(R.drawable.ic_wifi)
            }
        }

        // check connection
        private fun checkConnection() {

            if (scanResult.SSID == connectedSSID?.replace(
                    "\"",
                    ""
                ) || scanResult.BSSID == connectedBSSID
            ) {

                // Call setConnectedStatus() on the main thread
                setConnectedStatus()

            }
        }

        // hide views
        private fun hide() {

            binding.tvDeviceType.visibility = View.GONE
            binding.llStatus.visibility = View.GONE
            binding.mcvReconnect.visibility = View.GONE
        }

        // set device name
        private fun setDeviceName() {
            binding.tvDeviceName.text = scanResult.SSID
        }

        // set device mac address
        private fun setDeviceMacAddress() {
            binding.tvMacAddress.text = scanResult.BSSID
        }

        // get wifi security types
        private fun getWifiSecurityTypes(): String {

            val capabilities = scanResult.capabilities
            val securityTypes = mutableListOf<String>()

            // Check for common security protocols and add them to the list
            if (capabilities.contains(Constants.WEP)) {
                securityTypes.add(Constants.WEP)
            }
            if (capabilities.contains(Constants.WPA)) {
                securityTypes.add(Constants.WPA)
            }
            if (capabilities.contains(Constants.WPA2)) {
                securityTypes.add(Constants.WPA2)
            }
            if (capabilities.contains(Constants.WPA3)) {
                securityTypes.add(Constants.WPA3)
            }
            if (capabilities.contains(Constants.PSK)) {
                securityTypes.add(Constants.PSK)
            }
            if (capabilities.contains(Constants.EAP)) {
                securityTypes.add(Constants.EAP)
            }

            // Return a formatted string or indicate that the network is open
            return if (securityTypes.isNotEmpty()) {
                securityTypes.joinToString(" / ") // Join with commas
            } else {
                "Open Network"
            }
        }

        // check if wifi password protected
        private fun isWifiPasswordProtected(scanResult: ScanResult): Boolean {
            val capabilities = scanResult.capabilities

            // Check for common security protocols
            return capabilities.contains(Constants.WEP) ||
                    capabilities.contains(Constants.WPA) ||
                    capabilities.contains(Constants.WPA2) ||
                    capabilities.contains(Constants.WPA3) ||
                    capabilities.contains(Constants.PSK) ||
                    capabilities.contains(Constants.EAP)
        }

        // check connection status
        private fun setConnectedStatus() {

            selectedBinding?.llStatus?.visibility = View.GONE
            selectedBinding = binding

            binding.llStatus.visibility = View.VISIBLE

            binding.mcvIndicator.setCardBackgroundColor(context.getColor(R.color.green))
            binding.tvStatus.text = Constants.connected
            binding.tvStatus.setTextColor(context.getColor(R.color.green))
        }

        // connect to wifi
        private fun connectToWifi(
            ssid: String,
            password: String? = null,
            onConnectionStatus: (Boolean) -> Unit
        ) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above
                val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .apply {
                        if (password != null) {
                            setWpa2Passphrase(password)
                        }
                    }
                    .build()

                val networkRequest = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(wifiNetworkSpecifier)
                    .build()

                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                connectivityManager.requestNetwork(
                    networkRequest,
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: android.net.Network) {
                            // Successfully connected to the network
                            onConnectionStatus(true)

                        }

                        override fun onUnavailable() {
                            // Connection failed
                            onConnectionStatus(false)
                        }
                    })
            }
            else {
                // For Android 9 and below
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

                @Suppress("DEPRECATION") // Suppress deprecation warning for older versions
                val wifiConfig = WifiConfiguration().apply {
                    SSID = "\"$ssid\""
                    if (password != null) {
                        preSharedKey = "\"$password\""
                        allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                    } else {
                        allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                    }
                }

                @Suppress("DEPRECATION") // Suppress deprecation warning for older versions
                val netId = wifiManager.addNetwork(wifiConfig)
                @Suppress("DEPRECATION") // Suppress deprecation warning for older versions
                if (netId != -1) {
                    wifiManager.disconnect()
                    wifiManager.enableNetwork(netId, true)
                    wifiManager.reconnect()

                    // Successfully connected to the network
                    onConnectionStatus(true)

                } else {
                    // Connection failed
                    onConnectionStatus(false)

                }
            }
        }

        // password dialog
        private fun showPasswordDialog() {

            var isPasswordVisible = false

            val dialog = Dialog(context)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(true)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val dialogBinding = ItemWifiPasswordBinding.inflate(LayoutInflater.from(context))
            dialog.setContentView(dialogBinding.root)

            dialogBinding.tvWifiName.text = scanResult.SSID
            dialogBinding.tvSecurity.text = getWifiSecurityTypes()

            Utils.setFocusChangeListener(
                context,
                dialogBinding.etPassword,
                dialogBinding.mcvPassword
            )
            Utils.disableButton(dialogBinding.mcvConnect)

            dialogBinding.ivPasswordVisibility.setOnClickListener {
                Utils.passwordVisibility(
                    isPasswordVisible,
                    dialogBinding.etPassword,
                    dialogBinding.ivPasswordVisibility
                )
                isPasswordVisible = !isPasswordVisible
            }

            dialogBinding.etPassword.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (s.toString().isNotEmpty()) {
                        Utils.enableButton(dialogBinding.mcvConnect)
                    } else {
                        Utils.disableButton(dialogBinding.mcvConnect)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            dialogBinding.mcvCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.mcvConnect.setOnClickListener {
                dialog.dismiss()

                connectToWifi(scanResult.SSID, dialogBinding.etPassword.text.toString()) {

                    if (it) {
                        connectedSSID = scanResult.SSID
                        connectedBSSID = scanResult.BSSID

                        setConnectedStatus()

                        Utils.T(context, context.getString(R.string.connected_successfully))

                    } else {

                        Utils.T(
                            context,
                            context.getString(R.string.authentication_failed_please_check_your_password_and_try_again)
                        )
                    }
                }
            }

            dialog.setCancelable(true)
            dialog.show()
        }

        // Handles All the Clicks
        @SuppressLint("MissingPermission", "NotifyDataSetChanged")
        override fun onClick(view: View?) {

            when (view) {

                binding.mcvDevice -> {

                    if (binding.llStatus.visibility != View.VISIBLE) {

                        if (isWifiPasswordProtected(scanResult)) {

                            showPasswordDialog()

                        } else {

                            connectToWifi(scanResult.SSID) {

                                if (it) {

                                    connectedSSID = scanResult.SSID
                                    connectedBSSID = scanResult.BSSID

                                    setConnectedStatus()

                                    Utils.T(
                                        context,
                                        context.getString(R.string.connected_successfully)
                                    )

                                } else {

                                    Utils.T(
                                        context,
                                        context.getString(R.string.failed_to_connect_to_the_network)
                                    )

                                }
                            }
                        }

                    } else {

                        Utils.T(context, context.getString(R.string.device_is_already_connected))

                    }
                }
            }
        }
    }
}
