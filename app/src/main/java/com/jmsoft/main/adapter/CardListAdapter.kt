package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentCardBinding
import com.jmsoft.databinding.ItemCardListBinding
import com.jmsoft.databinding.ItemCatalogBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.model.CardModel
import okio.Utf8
import java.text.NumberFormat
import java.util.Locale

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class CardListAdapter(
    private val context: Context, private val cardList: ArrayList<CardModel>,
    private val fragmentCardBinding: FragmentCardBinding
) :
    RecyclerView.Adapter<CardListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCardListBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = cardList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        // bind method
        holder.bind(cardList[position], position)

    }

    inner class MyViewHolder(private val binding: ItemCardListBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var cardData: CardModel

        private var position: Int = 0

        // bind method
        fun bind(cardData: CardModel, position: Int) {

            this.cardData = cardData
            this.position = position

            //Set the jewellery image
            setJewellerImage()

            //Set the jewellery name
            setJewelleryName()

            //Set the jewellery quantity
            setJewelleryQuantity()

            //Set the jewellery price
            setJewelleryPrice()

            //Set Click on plus button
            binding.tvPlus.setOnClickListener(this)

            //Set Click on minus button
            binding.tvMinus.setOnClickListener(this)

            //Set Click on delete button
            binding.mcvDelete.setOnClickListener(this)

        }

        //Set the jewellery price
        private fun setJewelleryPrice() {

            val price = cardData.jewelleryQuantity?.let { cardData.jewelleryPrice?.times(it) }
            val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
            binding.tvPrice.text = numberFormat.format(price).toString()

        }

        //Set the jewellery quantity
        private fun setJewelleryQuantity() {
            binding.tvQuantity.text = cardData.jewelleryQuantity.toString()
        }

        //Set the jewellery name
        private fun setJewelleryName() {

            binding.tvName.text = cardData.jewelleryName
        }

        //Set the jewellery image
        private fun setJewellerImage() {

            cardData.jewelleryImage?.let { binding.ivJewellery.setImageResource(it) }
        }

        //Handles All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            //Set Click on plus button
            if (v == binding.tvPlus) {


                cardData.jewelleryQuantity = cardData.jewelleryQuantity?.plus(1)
                setJewelleryQuantity()
                setJewelleryPrice()

            }

            //Set Click on minus button
            else if (v == binding.tvMinus) {

                if (cardData.jewelleryQuantity != 1) {

                    cardData.jewelleryQuantity = cardData.jewelleryQuantity?.minus(1)
                    setJewelleryQuantity()
                    setJewelleryPrice()

                }
            }

            //Set Click on delete button
            else if (v == binding.mcvDelete) {

                cardList.removeAt(position)
                Utils.E(position.toString())

                notifyDataSetChanged()

                if (cardList.isEmpty()) {

                    fragmentCardBinding.ivEmptyCard?.visibility = View.VISIBLE
                    fragmentCardBinding.rlVerification?.visibility = View.GONE
                    fragmentCardBinding.llProgressStatusName?.visibility = View.GONE
                    fragmentCardBinding.progressBar?.visibility = View.GONE

                }
            }

        }
    }
}