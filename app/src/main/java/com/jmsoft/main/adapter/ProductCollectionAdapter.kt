package com.jmsoft.main.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.database.CartDataModel
import com.jmsoft.Utility.database.ProductDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemCollectionBinding
import com.jmsoft.main.activity.DashboardActivity

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

        // Product position
        private var position:Int = -1

        fun bind(productData: ProductDataModel,position: Int) {

            this.productData = productData
            this.position = position

            // Set the Product image
            setProductName()

            // Set the Product price
            setProductPrice()

            // Set the Product image
            setProductImage()

            // Set the Product Cart Status
            setCartStatus()

            // Getting Cart Product UUID for Deleting the product from the cart
            getCartProductUUID()

            // Set Click on Collection Item
            binding.mcvCollectionItem.setOnClickListener(this)

            // Set Click on CartStatus
            binding.mcvCartStatus.setOnClickListener(this)

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

        // Set the Product image
        private fun setProductImage() {

            val arrayOfImages = productData.productImageUri?.split(",")?.toTypedArray()

            val bitmap = Utils.getImageFromInternalStorage(
                context,
                arrayOfImages?.get(0).toString()
            )
            binding.ivProductImage.setImageBitmap(bitmap)
        }

        // Set the Product price
        private fun setProductPrice() {

            binding.tvProductPrice.text = productData.productPrice?.let {
                Utils.roundToTwoDecimalPlaces(
                    it
                )
            }?.let { Utils.getThousandSeparate(it) }
        }


        // Set the Product image
        private fun setProductName() {

            binding.tvProductName.text = productData.productName
        }

        //Handle All the Clicks
        override fun onClick(v: View?) {

            // Click on Collection item
            if (v == binding.mcvCollectionItem) {

                GetProgressBar.getInstance(context)?.show()

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

                    // Delete from order table
                    productData.productUUID?.let { productData.productPrice?.let { it1 ->
                        Utils.removeOrder(context,it,
                            it1
                        )
                    } }

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

                    // insert order
                    productData.productUUID?.let { productData.productPrice?.let { it1 ->
                        Utils.insertOrder(context,it,
                            it1
                        )
                    } }

                    Utils.T(context, context.getString(R.string.added_in_the_cart))


                }
            }

        }

    }
}