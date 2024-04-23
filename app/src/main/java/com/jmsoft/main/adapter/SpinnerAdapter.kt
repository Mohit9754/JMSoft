package com.jmsoft.main.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.jmsoft.R
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.CategoryDataModel

class SpinnerAdapter(
    private val context: Context,
    private val categoryDataList: ArrayList<CategoryDataModel>
) : ArrayAdapter<CategoryDataModel>(context, R.layout.item_spinner, categoryDataList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: View.inflate(context, R.layout.item_spinner, null)
        val textView = view.findViewById<TextView>(R.id.tvCategoryName)
        textView.text = categoryDataList[position].categoryName
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view.findViewById<TextView>(R.id.tvCategoryName)
        textView.text = categoryDataList[position].categoryName
        return view
    }
}
