package com.jmsoft.Utility.UtilityTools

import android.app.Dialog
import android.content.Context
import com.jmsoft.basic.UtilityTools.Utils

object GetProgressBar {

    private var progressBarDialog: Dialog? = null
    fun getInstance(context: Context): Dialog {

        if (progressBarDialog == null){
            progressBarDialog = Utils.initProgressDialog(context)
        }

        return progressBarDialog as Dialog
    }
}