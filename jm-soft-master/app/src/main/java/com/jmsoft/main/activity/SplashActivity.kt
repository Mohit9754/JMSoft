package com.jmsoft.main.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Utils

class SplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    Utils.I_clear(this@SplashActivity, LoginActivity::class.java, null)
                }
            }
        }
        thread.start()

    }

}