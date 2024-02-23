package com.jmsoft.main.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jmsoft.R
import java.util.Locale


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale("ar")
        setContentView(R.layout.activity_login)

    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}