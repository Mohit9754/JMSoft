package com.jmsoft.main.activity

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ActivityPdfViewBinding
import java.io.File

class PdfViewActivity : AppCompatActivity() {

    private val binding by lazy { ActivityPdfViewBinding.inflate(layoutInflater) }
    private val activity: Activity = this@PdfViewActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()

    }

    // get pdfName and open it
    private fun init() {

        val pdfName = intent.getStringExtra(Constants.pdfName)

        if (pdfName != null) {

            val pdfFile = File(activity.getExternalFilesDir(null), "${Constants.path}${pdfName}.pdf")

            if (pdfFile.exists()) {

                binding.pdfView.fromFile(pdfFile)
                    .defaultPage(1)
                    .showMinimap(false)
                    .enableSwipe(true)
                    .swipeVertical(true)
                    .load()

            }
            else {

                Utils.T(this, getString(R.string.file_not_found))
            }
        }

        else {

            Utils.T(activity, getString(R.string.pdf_name_is_null))
        }

    }
}