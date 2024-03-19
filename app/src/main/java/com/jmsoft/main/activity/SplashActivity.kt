package com.jmsoft.main.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jmsoft.R
import com.jmsoft.basic.Database.UserDataHelper
import com.jmsoft.basic.UtilityTools.Utils

/**
 * Splash Activity
 *
 * Checks if session present or not
 *
 */

class SplashActivity : AppCompatActivity() {
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
                        Utils.I_clear(this@SplashActivity, DashboardActivity::class.java, null)
                    } else {

                        //Intent to login Activity with clear back Stack
                        Utils.I_clear(this@SplashActivity, LoginActivity::class.java, null)
                    }
                }
            }
        }
        thread.start()

    }

}