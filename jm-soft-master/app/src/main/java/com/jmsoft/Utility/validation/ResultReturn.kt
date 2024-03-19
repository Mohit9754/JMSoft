package com.jmsoft.basic.validation

import android.widget.TextView

class ResultReturn(
    var type: Validation.Type?,
    var aBoolean: Boolean,
    var errorMessage: String?,
    var parameter: String?,
    var errorTextView: TextView?
)