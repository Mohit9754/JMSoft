package com.jmsoft.main.adapter

import android.annotation.SuppressLint
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
import com.jmsoft.databinding.FragmentCatalogBinding
import com.jmsoft.databinding.ItemCatalogBinding
import com.jmsoft.main.activity.DashboardActivity

/**
 * Catalog Adapter
 *
 * Showing the catalog details
 *
 */

class CatalogAdapter(
    private val context: Context,
    private var productList: ArrayList<ProductDataModel>,
) :
    RecyclerView.Adapter<CatalogAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemCatalogBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    inner class MyViewHolder(private val binding: ItemCatalogBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Product Data
        private lateinit var productData: ProductDataModel

        // flag variable for Cart Status
        private var isProductExistInCart: Boolean? = false

        // Cart Product UUID
        private var cartProductUUID: String? = null

        fun bind(productData: ProductDataModel) {

            this.productData = productData

            //Set the Product name
            setProductName()

            //Set the Product weight
            setProductWeight()

            //Set the Product type
            setProductType()

            //Set the Product category
            setProductCategory()

            //Set the Product carat
            setProductCarat()

            //Set the Product price
            setProductPrice()

            //Set the Product image
            setProductImage()

            // Set the Product Cart Status
            setCartStatus()

            // Getting Cart Product UUID for Deleting the product from the cart
            getCartProductUUID()

            //Setting Click on Catalog Item
            binding.mcvCatalogItem.setOnClickListener(this)

            //Setting Click on CartStatus
            binding.mcvCartStatus.setOnClickListener(this)
        }

        // Getting Cart Product UUID for Deleting the product from the cart
        private fun getCartProductUUID() {

            if (isProductExistInCart == true) {

                cartProductUUID = Utils.GetSession().userUUID?.let {
                    productData.productUUID?.let { it1 ->
                        Utils.getCartUUID(
                            it,
                            it1
                        )
                    }
                }
            }
        }

        // Set the Product Cart Status
        private fun setCartStatus() {

            isProductExistInCart = Utils.GetSession().userUUID?.let {
                productData.productUUID?.let { it1 ->
                    Utils.isProductExistInCartTable(
                        it,
                        it1
                    )
                }
            }

            if (isProductExistInCart == true) {
                binding.ivCartStatus.setImageResource(R.drawable.icon_cart_white)
            } else if (isProductExistInCart == false) {
                binding.ivCartStatus.setImageResource(R.drawable.icon_add)

            }
        }

        //Set the Product image
        private fun setProductImage() {

            val arrayOfImages = productData.productImageUri?.split(",")?.toTypedArray()

            val bitmap = Utils.getImageFromInternalStorage(
                context,
                arrayOfImages?.get(0).toString()
            )

            binding.tvProductImage?.setImageBitmap(bitmap)
        }

        //Set the Product price
        private fun setProductPrice() {

            binding.tvProductPrice.text = productData.productCost?.let {
                Utils.roundToTwoDecimalPlaces(
                    it
                )
            }?.let { Utils.getThousandSeparate(it.toDouble()) }
        }

        //Set the Product carat
        private fun setProductCarat() {
            binding.tvProductCarat.text = productData.productCarat.toString()
        }

        //Set the Product category
        private fun setProductCategory() {

            binding.tvProductCategory.text =
                productData.categoryUUID?.let { Utils.getCategoryNameThroughCategoryUUID(it) }
        }

        //Set the Product type
        private fun setProductType() {
            binding.tvProductType.text = productData.metalTypeUUID?.let {
                Utils.getMetalTypeNameThroughMetalTypeUUID(
                    it
                )
            }
        }

        //Set the Product weight
        @SuppressLint("SetTextI18n")
        private fun setProductWeight() {
            binding.tvProductWeight.text =
                "${productData.productWeight} ${productData.productWeight} "
        }

        //Set the Product name
        private fun setProductName() {

            binding.tvProductName.text = productData.productName
        }

        //Handles All the Clicks
        override fun onClick(v: View?) {

            // Click on Catalog item
            if (v == binding.mcvCatalogItem) {

                val bundle = Bundle()
                //Giving the product UUID
                bundle.putString(Constants.productUUID, productData.productUUID)
                (context as DashboardActivity).navController?.navigate(R.id.productDetail, bundle)

            }
            // Click on Cart Status
            else if (v == binding.mcvCartStatus) {

                if (isProductExistInCart == true) {

                    cartProductUUID?.let { Utils.deleteProductFromCart(it) }

                    Utils.T(context, context.getString(R.string.removed_from_the_cart))

                    isProductExistInCart = false
                    binding.ivCartStatus.setImageResource(R.drawable.icon_add)

                } else if (isProductExistInCart == false) {

                    val cardDataModel = CartDataModel()
                    cardDataModel.cartUUID = Utils.generateUUId()
                    cardDataModel.productUUID = productData.productUUID
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

    @SuppressLint("NotifyDataSetChanged")
    fun addFilterList(filteredProductList: ArrayList<ProductDataModel>) {
        productList = filteredProductList
        notifyDataSetChanged()
    }

}