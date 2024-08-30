package com.jmsoft.Utility.UtilityTools

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.main.`interface`.PairStatusCallback
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.entity.UHFTAGInfo
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

                    3 -> {
                        mIsScanning = true
                    }
                }
            }
        }
    }

    fun onResume(macAddress: String,frequency:Int,pairStatusCallback: PairStatusCallback) {
        initRFID(macAddress,frequency,pairStatusCallback)
    }

    fun onPause(pairStatusCallback:PairStatusCallback) {
        stopRFIDScan(pairStatusCallback)
    }

    private fun initRFID(macAddress: String,frequency: Int,pairStatusCallback:PairStatusCallback) {

        if (mUHF?.init(context) != true) {
            val errorMessage = "Failed to initialize RFID reader"
            Utils.E(errorMessage)
            Utils.T(context, errorMessage)
            callback.onError(errorMessage)
            pairStatusCallback.pairFail()
            return
        }

        Utils.E("MAC Address is : + $macAddress")

        mUHF?.connect(macAddress)

        mUHF?.setFrequencyMode(frequency)

        mUHF?.setInventoryCallback(IUHFInventoryCallback { uhftagInfo ->
            mHandler?.obtainMessage(1, uhftagInfo)?.sendToTarget()
        })

        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(3000)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    startRFIDScan(pairStatusCallback)
                }
            }
        }

        thread.start()
    }

    fun startRFIDScan(pairStatusCallback: PairStatusCallback) {

        if (!mIsScanning && (mUHF?.startInventoryTag() == true)) {
            mHandler?.sendEmptyMessage(3) // Notify scanning started
            pairStatusCallback.pairSuccess()
        } else {
            val errorMessage = "Failed to start RFID scanning"
            pairStatusCallback.pairFail()
            Utils.E(errorMessage)
            //  Utils.T(context, errorMessage)
            callback.onError(errorMessage)
        }
    }

    fun stopRFIDScan(pairStatusCallback:PairStatusCallback) {
        if (mIsScanning) {

//            mUHF?.free()
//            mUHF?.uhfStopUpdate()
//            mUHF?.disconnect()

            if (mUHF?.stopInventory() == true) {

                mHandler?.sendEmptyMessage(2) // Notify scanning stopped
                pairStatusCallback.pairSuccess()

                Utils.E("Scanner has been stopped")

            }

        } else {
            val errorMessage = "Failed to stop RFID scanning"
            pairStatusCallback.pairFail()
            Utils.E(errorMessage)
            //   Utils.T(context, errorMessage)
            callback.onError(errorMessage)
        }
    }

    fun getScanningStatus(): Boolean {
        return mIsScanning
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