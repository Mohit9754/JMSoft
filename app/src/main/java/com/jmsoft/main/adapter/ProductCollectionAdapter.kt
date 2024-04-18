package com.jmsoft.main.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemCollectionBinding
import com.jmsoft.main.activity.DashboardActivity

/**
 * Collection Adapter
 *
 * Showing the catalog details
 *
 */
class ProductCollectionAdapter(
    private val context: Context,
    private val productList: ArrayList<ProductDataModel>
) :
    RecyclerView.Adapter<ProductCollectionAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCollectionBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    inner class MyViewHolder(private val binding: ItemCollectionBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Product Data
        private lateinit var productData: ProductDataModel

        // flag variable for Cart Status
        private var isProductExistInCart:Boolean? = false

        // Cart Product UUID
        private var cartProductUUID:String? = null

        fun bind(productData: ProductDataModel) {

            this.productData = productData

            //Set the Product image
            setProductName()

            //Set the Product price
            setProductPrice()

            //Set the Product image
            setProductImage()

            // Set the Product Cart Status
            setCartStatus()

            // Getting Cart Product UUID for Deleting the product from the cart
            getCartProductUUID()

            //Setting Click on Collection Item
            binding.mcvCollectionItem.setOnClickListener(this)

            //Setting Click on CartStatus
            binding.mcvCartStatus.setOnClickListener(this)
        }

        // Getting Cart Product UUID for Deleting the product from the cart
        private fun getCartProductUUID(){

            if (isProductExistInCart == true) {

                cartProductUUID = Utils.GetSession().userUUID?.let { productData.productUUId?.let { it1 ->
                    Utils.getCartUUID(it,
                        it1
                    )
                } }
            }
        }


        // Set the Product Cart Status
        private fun setCartStatus(){

            isProductExistInCart = Utils.GetSession().userUUID?.let {
                productData.productUUId?.let { it1 ->
                    Utils.isProductExistInCartTable(
                        it,
                        it1
                    )
                }
            }

            Utils.E(isProductExistInCart.toString())

            if (isProductExistInCart == true){
                binding.ivCartStatus.setImageResource(R.drawable.icon_cart_white)
            }
            else if(isProductExistInCart == false){
                binding.ivCartStatus.setImageResource(R.drawable.icon_add)

            }
        }

        //Set the Product image
        private fun setProductImage() {

            val arrayOfImages = productData.productImage?.split(",")?.toTypedArray()

            val bitmap = Utils.getImageFromInternalStorage(
                context,
                arrayOfImages?.get(0).toString()
            )
            binding.ivProductImage.setImageBitmap(bitmap)
        }

        //Set the Product price
        private fun setProductPrice() {

            binding.tvProductPrice.text = productData.productPrice?.let {
                Utils.roundToTwoDecimalPlaces(
                    it
                )
            }?.let { Utils.getThousandSeparate(it) }
        }


        //Set the Product image
        private fun setProductName() {

            binding.tvProductName.text = productData.productName
        }

        //Handles All the Clicks
        override fun onClick(v: View?) {

            //Click on Collection item
            if (v == binding.mcvCollectionItem) {

                val bundle = Bundle()
                //Giving the product UUID
                bundle.putString(Constants.productUUID, productData.productUUId)

                //Navigate to Product
                (context as DashboardActivity).navController?.navigate(R.id.product,bundle)
            }

            // Click on Cart Status
            else if(v == binding.mcvCartStatus){

                if (isProductExistInCart == true){

                    cartProductUUID?.let { Utils.deleteProductFromCart(it)}

                    Utils.T(context, context.getString(R.string.removed_from_the_cart))
                    isProductExistInCart = false
                    binding.ivCartStatus.setImageResource(R.drawable.icon_add)

                }
                else if (isProductExistInCart == false) {

                    val cardDataModel = CartDataModel()
                    cardDataModel.cartUUID = Utils.generateUUId()
                    cardDataModel.productUUID = productData.productUUId
                    cardDataModel.userUUID = Utils.GetSession().userUUID
                    cardDataModel.productQuantity = 1

                    Utils.insertProductInCartTable(cardDataModel)

                    isProductExistInCart = true
                    binding.ivCartStatus.setImageResource(R.drawable.icon_cart_white)

                    // Getting Cart Product UUID for Deleting the product from the cart
                    getCartProductUUID()

                    Utils.T(context, context.getString(R.string.added_in_the_cart))


                }
            }

        }

    }
}