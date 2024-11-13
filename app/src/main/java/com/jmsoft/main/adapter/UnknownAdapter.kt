package com.jmsoft.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.databinding.ItemExpectedBinding
import com.jmsoft.main.activity.DashboardActivity

class UnknownAdapter(
    private val context: Context,
    private val unKnownList: ArrayList<String>,
) :
    RecyclerView.Adapter<UnknownAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemExpectedBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = unKnownList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(unKnownList[position], position)
    }

    inner class MyViewHolder(private val binding: ItemExpectedBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var rfidCode:String
        private var position:Int = -1

        fun bind(rfidCode: String,position: Int) {

            this.rfidCode = rfidCode
            this.position = position

            setRFIDCode()

            binding.mcvProduct.setOnClickListener(this)

        }

        private fun setRFIDCode() {
            binding.tvRFIDCode.text = rfidCode
        }

        override fun onClick(v: View?) {

            if (v == binding.mcvProduct) {

                // Show progress bar
                GetProgressBar.getInstance(context)?.show()

                (context as DashboardActivity).navController?.navigate(R.id.catalog, null)

            }
        }
    }
}