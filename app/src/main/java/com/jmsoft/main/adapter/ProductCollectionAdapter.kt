package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.weightUnit
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogDeleteUserBinding
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
    private val productList: ArrayList<ProductDataModel>,

) :
    RecyclerView.Adapter<ProductCollectionAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCollectionBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productList[position],position)
    }



    inner class MyViewHolder(private val binding: ItemCollectionBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Product Data
        private lateinit var productData: ProductDataModel

        // flag variable for Cart Status
        private var isProductExistInCart:Boolean? = false

        // Cart Product UUID
        private var cartProductUUID:String? = null

        private var position:Int = -1

        fun bind(productData: ProductDataModel,position: Int) {

            this.productData = productData
            this.position = position

            //Set the Product image
            setProductName()

//            setProductWeight()

//            setProductCategory()

//            setProductType()

//            setProductCarat()

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

        private fun setProductCarat(){
            binding.tvProductCarat.text = productData.productCarat.toString()
        }

        private fun setProductCategory() {
//            binding.tvProductCategory.text = productCategory
        }

        private fun setProductType(){
            binding.tvProductType.text = productData.metalTypeUUID?.let {
                Utils.getMetalTypeNameThroughMetalTypeUUID(
                    it
                )
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setProductWeight() {
            binding.tvProductWeight.text = "${productData.productWeight} $weightUnit"
        }

        // Getting Cart Product UUID for Deleting the product from the cart
        private fun getCartProductUUID(){

            if (isProductExistInCart == true) {

                cartProductUUID = Utils.GetSession().userUUID?.let { productData.productUUID?.let { it1 ->
                    Utils.getCartUUID(it,
                        it1
                    )
                } }
            }
        }


        // Set the Product Cart Status
        private fun setCartStatus(){

            isProductExistInCart = Utils.GetSession().userUUID?.let {
                productData.productUUID?.let { it1 ->
                    Utils.isProductExistInCartTable(
                        it,
                        it1
                    )
                }
            }

            Utils.E(isProductExistInCart.toString())

            if (isProductExistInCart == true){
                binding.ivCartStatus.setImageResource(R.drawable.icon_cross)
            }
            else if(isProductExistInCart == false){
                binding.ivCartStatus.setImageResource(R.drawable.icon_cart_white)

            }
        }

        //Set the Product image
        private fun setProductImage() {

            val arrayOfImages = productData.productImageUri?.split(",")?.toTypedArray()

            val bitmap = Utils.getImageFromInternalStorage(
                context,
                arrayOfImages?.get(0).toString()
            )
            binding.ivProductImage.setImageBitmap(bitmap)
        }

        //Set the Product price
        private fun setProductPrice() {

            binding.tvProductPrice.text = productData.productCost?.let {
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
                bundle.putString(Constants.productUUID, productData.productUUID)

                //Navigate to Product
                (context as DashboardActivity).navController?.navigate(R.id.productDetail,bundle)
            }

            // Click on Cart Status
            else if(v == binding.mcvCartStatus){

                if (isProductExistInCart == true){

                    cartProductUUID?.let { Utils.deleteProductFromCart(it)}

                    Utils.T(context, context.getString(R.string.removed_from_the_cart))
                    isProductExistInCart = false
                    binding.ivCartStatus.setImageResource(R.drawable.icon_cart_white)

                }
                else if (isProductExistInCart == false) {

                    val cardDataModel = CartDataModel()
                    cardDataModel.cartUUID = Utils.generateUUId()
                    cardDataModel.productUUID = productData.productUUID
                    cardDataModel.userUUID = Utils.GetSession().userUUID
                    cardDataModel.productQuantity = 1

                    Utils.insertProductInCartTable(cardDataModel)

                    isProductExistInCart = true
                    binding.ivCartStatus.setImageResource(R.drawable.icon_cross)

                    // Getting Cart Product UUID for Deleting the product from the cart
                    getCartProductUUID()

                    Utils.T(context, context.getString(R.string.added_in_the_cart))


                }
            }

        }

    }
}