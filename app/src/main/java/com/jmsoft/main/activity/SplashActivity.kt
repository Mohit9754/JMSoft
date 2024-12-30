package com.jmsoft.main.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Utils

/**
 * Splash Activity
 *
 * Checks if session present or not
 *
 */

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val activity: Activity = this@SplashActivity

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

                    // If Session present then intent to Dashboard Activity
                    if (Utils.IS_LOGIN()) {
                        Utils.I_clear(activity, DashboardActivity::class.java, null)
                    } else {

                        //Intent to login Activity with clear back Stack
                        Utils.I_clear(activity, LoginActivity::class.java, null)
                    }
                }
            }
        }
        thread.start()
    }

}