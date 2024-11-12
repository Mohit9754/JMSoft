package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.utility.database.CartDataModel
import com.jmsoft.utility.database.OrderDataModel
import com.jmsoft.utility.database.ProductDataModel
import com.jmsoft.utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentCartBinding
import com.jmsoft.databinding.ItemCardListBinding
import com.jmsoft.main.activity.DashboardActivity

/**
 * Cart list Adapter
 *
 * Showing the catalog details
 *
 */
class CartListAdapter(
    private val context: Context, private val cardList: ArrayList<CartDataModel>,
    private val fragmentCartBinding: FragmentCartBinding,
    private val orderStatus:Boolean
) :
    RecyclerView.Adapter<CartListAdapter.MyViewHolder>() {

    // Price of each product in the cart
    private var cartPrice = ArrayList<Double>()

    private var totalAmount:Double = 0.0

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

        // Cart Data
        private lateinit var cartData: CartDataModel

        // Product Data
        private lateinit var productData: ProductDataModel

        // Position
        private var position: Int = 0

        // bind method
         fun bind(cartData: CartDataModel, position: Int) {

            this.cartData = cartData
            this.position = position

            // Getting the product data from cart's present cartUUID
            val productData = cartData.productUUID?.let {  Utils.getProductThroughProductUUID(it) }

            if (cartData.productUUID != null) {
                this.productData = Utils.getProductThroughProductUUID(cartData.productUUID!!)
            }

            // Calculating price of each product for storing in cartPrice array
            val price = cartData.productQuantity?.let { productData?.productPrice?.times(it) }
            price?.let { cartPrice.add(it) }

            //Set the Product image
            setProductImage()

            //Set the Product name
            setProductName()

            //Set the Product quantity
            setProductQuantity()

            //Set the Product price
            setProductPrice()

            // At the last Set Total Price
            if (cardList.size - 1 == position) {

                setTotalPrice()

                val productQuantity = cardList.map { it.productQuantity }.toMutableList()

                productQuantity.reverse()

                val productQuantityUri = productQuantity.joinToString().replace(" ", "")

                updateQuantityInOrder(context,productQuantityUri,totalAmount)

                GetProgressBar.getInstance(context)?.dismiss()

            }

            //Set Click on plus button
            binding.tvPlus.setOnClickListener(this)

            //Set Click on minus button
            binding.tvMinus.setOnClickListener(this)

            //Set Click on delete button
            binding.mcvDelete.setOnClickListener(this)

            //Set Click on Cart Product
            binding.mcvCartProduct.setOnClickListener(this)

        }

        // Set Total Price of the cart
        private fun setTotalPrice() {

            var totalPrice = 0.0

            for (price in cartPrice) {
                totalPrice += price
            }

            totalAmount = totalPrice

            fragmentCartBinding.tvTotalPriceVerification?.text = Utils.getThousandSeparate(Utils.roundToTwoDecimalPlaces(totalPrice))
        }

        // Set the Product price
        private fun setProductPrice() {

            val price = cartData.productQuantity?.let { productData.productPrice?.times(it) }

            if (price != null) {

                cartPrice[position] = price
            }

            binding.tvPrice.text = price?.let { Utils.getThousandSeparate(it) }

        }

        // Set the Product quantity
        private fun setProductQuantity() {

            Utils.E("Product quantity is ###${cartData.productQuantity}###")


            binding.tvQuantity.text = cartData.productQuantity.toString()
        }

        //Set the Product name
        private fun setProductName() {

            binding.tvName.text = productData.productName
        }

        //Set the product image
        private fun setProductImage() {

            val arrayOfImages = productData.productImageUri?.split(",")?.toTypedArray()

            val bitmap = Utils.getImageFromInternalStorage(
                context,
                arrayOfImages?.get(0).toString()
            )

            binding.ivProduct.setImageBitmap(bitmap)
        }

        // Update the Product Quantity in Cart table
        private fun updateQuantityOfProduct() {

            cartData.productQuantity?.let {
                cartData.cartUUID?.let { it1 ->
                    Utils.updateProductQuantity(
                        it,
                        it1
                    )
                }
            }
        }

        private fun updateQuantityInOrder(context: Context,productQuantityUri:String,totalAmount:Double) {

            val newOrder: OrderDataModel? = if (!orderStatus){ Utils.getOrderUUID(context)
                ?.let { Utils.getOrderByUUID(it) } } else null

            val orderDataModel = OrderDataModel()

            orderDataModel.orderUUID = if (orderStatus) cartData.cartUUID else newOrder?.orderUUID

            orderDataModel.productQuantityUri = productQuantityUri

            orderDataModel.totalAmount = totalAmount

            Utils.updateQuantityInOrder(orderDataModel)

        }

        //Handles All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // Clicked on plus button
            if (v == binding.tvPlus) {

                //Increment the product quantity by 1
                cartData.productQuantity = cartData.productQuantity?.plus(1)


                // Update the Product Quantity in Cart table
                if (!orderStatus) updateQuantityOfProduct() else Utils.GetSession().userUUID?.let {
                    cartData.productUUID?.let { it1 ->
                        cartData.productQuantity?.let { it2 ->
                            Utils.updateProductQuantityInCart(
                                it, it1, it2
                            )
                        }
                    }
                }

                setProductQuantity()
                setProductPrice()
                setTotalPrice()

                val productQuantity = cardList.map { it.productQuantity }.toMutableList()
                productQuantity.reverse()

                val productQuantityUri = productQuantity.joinToString().replace(" ", "")

                // update quantity
                updateQuantityInOrder(context,productQuantityUri,totalAmount)


            }

            // Clicked on minus button
            else if (v == binding.tvMinus) {

                if (cartData.productQuantity != 1) {

                    //Decrement the product quantity by 1
                    cartData.productQuantity = cartData.productQuantity?.minus(1)

                    // Update the Product Quantity in Cart table
                    if (!orderStatus) updateQuantityOfProduct() else Utils.GetSession().userUUID?.let {
                        cartData.productUUID?.let { it1 ->
                            cartData.productQuantity?.let { it2 ->
                                Utils.updateProductQuantityInCart(
                                    it, it1, it2
                                )
                            }
                        }
                    }

                    setProductQuantity()
                    setProductPrice()
                    setTotalPrice()

                    val productQuantity = cardList.map { it.productQuantity }.toMutableList()
                    productQuantity.reverse()

                    val productQuantityUri = productQuantity.joinToString().replace(" ", "")

                    // update quantity
                    updateQuantityInOrder(context,productQuantityUri,totalAmount)

                }
            }

            // Clicked on delete button
            else if (v == binding.mcvDelete) {

                cardList.removeAt(position)

                // Delete the Product from the cart
                if (!orderStatus) cartData.cartUUID?.let { Utils.deleteProductFromCart(it) } else Utils.GetSession().userUUID?.let {
                    cartData.productUUID?.let { it1 ->
                        Utils.deleteProductFromCart(
                            it, it1
                        )
                    }
                }

                notifyDataSetChanged()

                // Clear the cart price array
                cartPrice.clear()

                // if cart is empty then show the cart empty image
                if (cardList.isEmpty()) {

                    fragmentCartBinding.rlCartManagement?.visibility = View.GONE
                    fragmentCartBinding.llCartEmpty?.visibility = View.VISIBLE

                }

                // remove from order table
                productData.productUUID?.let { productData.productPrice?.let { it1 ->
                    Utils.removeOrder(context,it,
                        it1
                    )
                } }

            }

            // Clicked on cart product
            else if(v == binding.mcvCartProduct) {

                GetProgressBar.getInstance(context)?.show()

                val bundle = Bundle()
                //Giving the product UUID
                bundle.putString(Constants.productUUID, productData.productUUID)
                (context as DashboardActivity).navController?.navigate(R.id.productDetail,bundle)

            }
        }
    }
}