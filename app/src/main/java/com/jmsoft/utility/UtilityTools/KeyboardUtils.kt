package com.jmsoft.basic.UtilityTools

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Created by Hritik on 2/10/2023.
 */
object KeyboardUtils {
    /**
     *
     * @param activity
     */
    fun showKeyboard(activity: Activity) {
        val inputManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager?.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     *
     * @param activity
     */
    fun hideKeyboard(activity: Activity) {
        val inputManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (activity.currentFocus != null) {
            inputManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    /**
     *
     * @param view
     */
    fun hideKeyboard(view: View) {
        val inputManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}