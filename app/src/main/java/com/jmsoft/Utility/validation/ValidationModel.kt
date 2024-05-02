package com.jmsoft.basic.validation

import android.widget.EditText
import android.widget.TextView
import com.rscja.team.qcom.deviceapi.T

class ValidationModel {
    var type: Validation.Type? = null
    var field: String? = null
    var editText: EditText? = null
    var editText1: EditText? = null
    var errorMessage: String? = null
    var errorTextView: TextView? = null
    var Parameter: String? = null
    var arrayListSize:Int? =null
    var textView:TextView? = null
    var isImageSelected:Boolean? = null

    constructor() {}

    constructor(type: Validation.Type?,isImageSelected: Boolean?, textView: TextView?) {
        this.type = type
        this.isImageSelected = isImageSelected
        this.errorTextView = textView
    }

    constructor(type: Validation.Type?, textView: TextView?,errorTextView: TextView?){
        this.type = type
        this.textView = textView
        this.errorTextView = errorTextView
    }

    constructor(type: Validation.Type?, editText: EditText?, editText1: EditText?) {
        this.type = type
        this.editText = editText
        this.editText1 = editText1
    }

    constructor(type: Validation.Type?, arrayListSize:Int?, textView: TextView?,editText: EditText?) {
        this.type = type
        this.arrayListSize = arrayListSize
        this.errorTextView = textView
        this.editText = editText
    }

    constructor(type: Validation.Type?, field: String?, errorMessage: String?) {
        this.type = type
        this.field = field
        this.errorMessage = errorMessage
    }

    constructor(type: Validation.Type?, editText: EditText?) {
        this.type = type
        this.editText = editText
    }

    constructor(
        type: Validation.Type?,
        field: String?,
        editText: EditText?,
        editText1: EditText?,
        errorMessage: String?,
        errorTextView: TextView?,
        Parameter: String?
    ) {
        this.type = type
        this.field = field
        this.editText = editText
        this.editText1 = editText1
        this.errorMessage = errorMessage
        this.errorTextView = errorTextView
        this.Parameter = Parameter
    }

    constructor(
        type: Validation.Type?,
        editText: EditText?,
        editText1: EditText?,
        errorTextView: TextView?
    ) {
        this.type = type
        this.editText = editText
        this.editText1 = editText1
        this.errorTextView = errorTextView
    }

    constructor(
        type: Validation.Type?,
        field: String?,
        errorMessage: String?,
        errorTextView: TextView?
    ) {
        this.type = type
        this.field = field
        this.errorMessage = errorMessage
        this.errorTextView = errorTextView
    }

    constructor(type: Validation.Type?, editText: EditText?, errorTextView: TextView?) {
        this.type = type
        this.editText = editText
        this.errorTextView = errorTextView
    }

    constructor(
        type: Validation.Type?,
        editText: EditText?,
        editText1: EditText?,
        errorTextView: TextView?,
        Parameter: String?
    ) {
        this.type = type
        this.editText = editText
        this.editText1 = editText1
        this.errorTextView = errorTextView
        this.Parameter = Parameter
    }

    constructor(
        type: Validation.Type?,
        field: String?,
        errorMessage: String?,
        errorTextView: TextView?,
        Parameter: String?
    ) {
        this.type = type
        this.field = field
        this.errorMessage = errorMessage
        this.errorTextView = errorTextView
        this.Parameter = Parameter
    }

    constructor(
        type: Validation.Type?,
        editText: EditText?,
        errorTextView: TextView?,
        Parameter: String?
    ) {
        this.type = type
        this.editText = editText
        this.errorTextView = errorTextView
        this.Parameter = Parameter
    }
}