package com.jmsoft.Utility.UtilityTools

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.jmsoft.basic.UtilityTools.Utils
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import com.rscja.deviceapi.interfaces.ConnectionStatusCallback
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback

class RFIDSetUp(private val context: Context, private val callback: RFIDCallback) {

    private var mUHF: RFIDWithUHFBLE? = RFIDWithUHFBLE.getInstance()
    private var mIsScanning = false
    private var mHandler: Handler? = null

    interface RFIDCallback {
        fun onTagRead(tagInfo: UHFTAGInfo)
        fun onError(message: String)

    }

    init {

        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {

                when (msg.what) {
                    1 -> {
                        val tagInfo = msg.obj as UHFTAGInfo
                        handleTagData(tagInfo)
                    }
                    2 -> mIsScanning = false
                    3 ->{ mIsScanning = true }
                }
            }
        }
    }

    fun onResume(macAddress:String) {
        initRFID(macAddress)
    }

    fun onPause() {
        stopRFIDScan()
    }

    private fun initRFID(macAddress:String) {

        if (mUHF?.init(context) != true) {
            val errorMessage = "Failed to initialize RFID reader"
            Utils.E(errorMessage)
            Utils.T(context, errorMessage)
            callback.onError(errorMessage)
            return
        }

        Utils.E("MAC Address is : + $macAddress")

        mUHF?.connect(macAddress)

        mUHF?.setInventoryCallback(IUHFInventoryCallback { uhftagInfo ->
            mHandler?.obtainMessage(1, uhftagInfo)?.sendToTarget()

        })

        startRFIDScan()
    }

    fun startRFIDScan() {
        if (!mIsScanning && (mUHF?.startInventoryTag() == true)) {
            mHandler?.sendEmptyMessage(3) // Notify scanning started
        } else {
            val errorMessage = "Failed to start RFID scanning"
            Utils.E(errorMessage)
            Utils.T(context, errorMessage)
            callback.onError(errorMessage)
        }
    }

    fun stopRFIDScan() {
        if (mIsScanning && mUHF?.stopInventory() == true) {
            mHandler?.sendEmptyMessage(2) // Notify scanning stopped
        } else {
            val errorMessage = "Failed to stop RFID scanning"
            Utils.E(errorMessage)
            Utils.T(context, errorMessage)
            callback.onError(errorMessage)
        }
    }

    private fun handleTagData(tagInfo: UHFTAGInfo) {
        callback.onTagRead(tagInfo)

        val epc = tagInfo.epc
        val tid = tagInfo.tid
        val user = tagInfo.user
        val rssi = tagInfo.rssi
        Utils.E("EPC: $epc")
        Utils.E("TID: $tid")
        Utils.E("User: $user")
        Utils.E("RSSI: $rssi")
    }
}
