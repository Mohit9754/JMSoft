package com.jmsoft.basic.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer


class ActionReceiver : BroadcastReceiver() {
    var Status = 0
    var trip_pooling_id: String? = null
    var countDownTimer: CountDownTimer? = null
    var status = "Accepted"
    override fun onReceive(context: Context, intent: Intent) {}
}