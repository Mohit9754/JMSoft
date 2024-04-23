package com.jmsoft.main.fragment

import android.annotation.SuppressLint
import android.content.Context.WINDOW_SERVICE
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentProductDetailBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CatalogAdapter
import com.jmsoft.main.adapter.ProductCollectionAdapter
import com.jmsoft.main.adapter.ProductImageAdapter

@Suppress("DEPRECATION")
class ProductDetailFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentProductDetailBinding

    //Product Data
    private lateinit var productData: ProductDataModel

    // Flag variable for checking if Product Exist In Cart
    private var isProductExistInCart = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentProductDetailBinding.inflate(layoutInflater)

        // Hide the Search option
        (requireActivity() as DashboardActivity).binding?.mcvSearch?.visibility = View.GONE

        // set the Clicks , initialization And Setup
        init()

        return binding.root
    }

    // Setup Product Image Recycler View
    private fun setUpProductImageRecyclerView(productImages: String) {

        val arrayOfImages = productImages.split(",").toTypedArray()

        val bitmapImages = ArrayList<Bitmap>()


        for (image in arrayOfImages) {

            Utils.getImageFromInternalStorage(requireActivity(), image)
                ?.let { bitmapImages.add(it) }
        }

        val adapter = binding.ivProduct?.let {
            binding.llLeftBtn?.let { it1 ->
                binding.llRightBtn?.let { it2 ->
                    ProductImageAdapter(
                        requireActivity(), bitmapImages, it,
                        it1, it2
                    )
                }
            }
        }

        binding.rvProductImage?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvProductImage?.adapter = adapter

    }

    // Set up collection Recycler view
    private fun setUpCollectionItemRecyclerView(productCategory: String, productUUID: String) {

        val productList = Utils.getProductsThroughCategory(productCategory, productUUID)

        val adapter = ProductCollectionAdapter(requireActivity(), productList)

        binding.rvCollection?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
        binding.rvCollection?.adapter = adapter

    }

    // Setting the May also like RecyclerView
    private fun setUpMayLikeRecyclerView() {

        val productList =
            productData.categoryUUID?.let { Utils.getAllProductsAcceptCategory(it) }

        val catalogAdapter = productList?.let { CatalogAdapter(requireActivity(), it) }

        binding.rvCatalog?.layoutManager =
            GridLayoutManager(requireActivity(), 3) // Span Count is set to 3
        binding.rvCatalog?.adapter = catalogAdapter
    }


    // Checks if Product Already Added in card
    private fun isProductAlreadyAddedInCard() {

        val isExistInCart = Utils.GetSession().userUUID?.let {
            productData.productUUID?.let { it1 ->
                Utils.isProductExistInCartTable(
                    it,
                    it1
                )
            }
        }

        if (isExistInCart == true) {
            isProductExistInCart = true
            setCartStatus()

        } else if (isExistInCart == false) {

            isProductExistInCart = false
            setCartStatus()
        }
    }

    // Set Cart Status Button
    private fun setCartStatus() {

        if (isProductExistInCart) {
            binding.tvCartStatus?.text = requireActivity().getString(R.string.remove_from_cart)
            binding.llCartStatus?.setBackgroundResource(R.drawable.bg_button)
        } else {

            binding.tvCartStatus?.text = requireActivity().getString(R.string.add_to_cart)
            binding.llCartStatus?.setBackgroundResource(R.drawable.bg_add_to_card)
        }
    }

    // Set the Product Details
    @SuppressLint("SetTextI18n")
    private fun setUpProductDetails(productUUID: String) {

        productData = Utils.getProductThroughProductUUID(productUUID)

        // Setup Product Image Recycler View
        productData.productImageUri?.let { setUpProductImageRecyclerView(it) }

        binding.tvProductName?.text = productData.productName
        binding.tvProductWeight?.text =
            "${productData.productWeight} ${productData.productWeight} "

        binding.tvProductCarat?.text = productData.productCarat.toString()
        binding.tvProductType?.text = productData.metalTypeUUID?.let {
            Utils.getMetalTypeNameThroughMetalTypeUUID(
                it
            )
        }

        binding.tvProductCategory?.text =
            productData.categoryUUID?.let { Utils.getCategoryNameThroughCategoryUUID(it) }

        binding.tvProductDescription?.text = productData.productDescription
        binding.tvProductPrice?.text = productData.productCost?.let {
            Utils.roundToTwoDecimalPlaces(
                it
            )
        }?.let { Utils.getThousandSeparate(it) }

        // Set up collection Recycler view
        productData.categoryUUID?.let {
            productData.productUUID?.let { it1 ->
                setUpCollectionItemRecyclerView(
                    it,
                    it1
                )
            }
        }
    }

    //Setting the Product Section Height through Screen height
    private fun setProductSectionHeight() {

        val windowManager = requireActivity().getSystemService(WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay
        val displayMetrics = resources.displayMetrics

        val screenHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            windowMetrics.bounds.height()
        } else {
            @Suppress("DEPRECATION")
            display.getRealMetrics(displayMetrics)
            displayMetrics.heightPixels
        }

        val dashboardActivity = (requireActivity() as DashboardActivity)

        //Height of toolbar and bottom
        val heightOfToolbarAndBottom =
            (dashboardActivity.binding?.rlBottom?.height?.let { dashboardActivity.binding?.toolbar?.height?.plus(it) })

        val statusBarHeight = Utils.getStatusbarHeight(requireActivity())

        // Set the height of the layout
        val layoutParams = binding.llProductSection?.layoutParams as LinearLayout.LayoutParams
        if (heightOfToolbarAndBottom != null) {
            layoutParams.height = (screenHeight - heightOfToolbarAndBottom) - statusBarHeight
        }
        binding.llProductSection?.setLayoutParams(layoutParams)

    }

    // Set the Clicks , initialization And Setup
    private fun init() {

        //Setting the Product Section Height through Screen height
        setProductSectionHeight()

        // getting the product UUID
        val productUUID = arguments?.getString(Constants.productUUID)

        // Set the Product Details
        productUUID?.let { setUpProductDetails(it) }

        // Checks if Product Already Added in card
        isProductAlreadyAddedInCard()

        // Setting the May also like RecyclerView
        setUpMayLikeRecyclerView()

        // Set Click on Cart Status button
        binding.llCartStatus?.setOnClickListener(this)

    }

    // Handle all the clicks
    override fun onClick(v: View?) {

        //Set Click on Add to Card button
        if (v == binding.llCartStatus) {

            if (isProductExistInCart) {

                val cardUUID = Utils.GetSession().userUUID?.let {
                    productData.productUUID?.let { it1 ->
                        Utils.getCartUUID(
                            it,
                            it1
                        )
                    }
                }
                cardUUID?.let { Utils.deleteProductFromCart(it) }

                isProductExistInCart = false
                setCartStatus()

                Utils.T(requireActivity(), getString(R.string.removed_successfully))


            } else {

                val cardDataModel = CartDataModel()
                cardDataModel.cartUUID = Utils.generateUUId()
                cardDataModel.productUUID = productData.productUUID
                cardDataModel.userUUID = Utils.GetSession().userUUID
                cardDataModel.productQuantity = 1

                Utils.insertProductInCartTable(cardDataModel)

                isProductExistInCart = true
                setCartStatus()

                Utils.T(requireActivity(), getString(R.string.added_successfully))
            }
        }
    }
}