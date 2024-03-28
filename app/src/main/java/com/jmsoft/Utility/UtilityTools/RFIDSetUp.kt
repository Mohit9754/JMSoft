package com.jmsoft.Utility.UtilityTools

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.jmsoft.basic.UtilityTools.Utils
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback


class RFIDSetUp {

    private var mUHF: RFIDWithUHFBLE? = null
    private var context:Context

    private var mIsScanning = false

    private var mHandler: Handler? = null


   constructor(context: Context){
       this.context = context
       mUHF = RFIDWithUHFBLE.getInstance()
       mHandler = object : Handler(Looper.getMainLooper()) {
           override fun handleMessage(msg: Message) {
               when (msg.what) {
                   1 -> {
                       val tagInfo = msg.obj as UHFTAGInfo
                       handleTagData(tagInfo)
                   }

                   2 -> mIsScanning = false
                   3 -> mIsScanning = true
               }
           }
       }
   }


    public fun onResume() {
        // Initialize RFID reader
        initRFID()
    }

    public fun onPause() {
        // Stop RFID scanning
        stopRFIDScan()
    }

    private fun initRFID() {
        if (!mUHF!!.init(context)) {
            Utils.E("Failed to initialize RFID reader")
            Utils.T(context, "Failed to initialize RFID reader")
            return
        }

        //--
        mUHF?.setInventoryCallback(IUHFInventoryCallback { uhftagInfo ->
            mHandler?.obtainMessage(1, uhftagInfo)?.sendToTarget()
        })


        // Start RFID scanning
        startRFIDScan()
    }

    private fun startRFIDScan() {
        if (!mIsScanning) {
            if (mUHF!!.startInventoryTag()) {
                mHandler?.sendEmptyMessage(3) // Notify scanning started
            } else {
                Utils.E( "Failed to start RFID scanning")
                Utils.T(context, "Failed to start RFID scanning")
            }
        }
    }

    private fun stopRFIDScan() {
        if (mIsScanning) {
            if (mUHF!!.stopInventory()) {
                mHandler?.sendEmptyMessage(2) // Notify scanning stopped
            } else {
                Utils.E( "Failed to stop RFID scanning")
                Utils.T(context, "Failed to stop RFID scanning")
            }
        }
    }

    private fun handleTagData(tagInfo: UHFTAGInfo) {
        // Handle RFID tag data
        val epc = tagInfo.epc
        val tid = tagInfo.tid
        val user = tagInfo.user
        val rssi = tagInfo.rssi
        Utils.E( "EPC: $epc")
        Utils.E( "TID: $tid")
        Utils.E( "User: $user")
        Utils.E( "RSSI: $rssi")
    }
}