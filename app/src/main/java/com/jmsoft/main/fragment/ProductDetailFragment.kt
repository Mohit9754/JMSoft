package com.jmsoft.main.fragment

import android.annotation.SuppressLint
import android.app.Dialog
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
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.OrderDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.productSectionHeight
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentProductDetailBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CatalogAdapter
import com.jmsoft.main.adapter.ProductCollectionAdapter
import com.jmsoft.main.adapter.ProductImageAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class ProductDetailFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentProductDetailBinding

    //Product Data
    private lateinit var productData: ProductDataModel

    // Flag variable for checking if Product Exist In Cart
    private var isProductExistInCart = false

    private var heightOfllProductSection:Int? = null

    private var collectionUUIDData:String? = null

    private var mayLikeProductList = ArrayList<ProductDataModel>()

    private var catalogAdapter:CatalogAdapter? = null

    private var offset = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentProductDetailBinding.inflate(layoutInflater)

        // Get height
        val height = savedInstanceState?.getInt(productSectionHeight)

        if (height != null && binding.llProductSection != null) {

            //Setting the Product Section Height through Screen height
            val layoutParams = binding.llProductSection!!.layoutParams as LinearLayout.LayoutParams
            layoutParams.height = height
            binding.llProductSection?.setLayoutParams(layoutParams)
            heightOfllProductSection = height

        } else {

            //Setting the Product Section Height through Screen height
            setProductSectionHeight()
        }

        // Hide the Search option
        (requireActivity() as DashboardActivity).binding?.mcvSearch?.visibility = View.GONE

        // set the Clicks , initialization And Setup
        lifecycleScope.launch(Dispatchers.Main) {
            init()
        }

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
    private suspend fun setUpCollectionItemRecyclerView(collectionUUID: String, productUUID: String) {

        if (collectionUUID.isNotEmpty()) {

            val collectionUUIDList = productData.collectionUUID?.split(",")

            val collectionData = collectionUUIDList?.get(0)?.let { Utils.getCollectionThroughUUID(it) }

            collectionUUIDData = collectionUUIDList?.get(0).toString()

            val totalScreenWidth = Utils.getScreenWidth(requireActivity())

            val noOfItems = if (totalScreenWidth > 1600) 3 else 2

            val result =
                collectionUUIDList?.let { lifecycleScope.async(Dispatchers.IO) {
                    return@async Utils.getProductsThroughCollection(it, productUUID,noOfItems)
                } }

            val productList = result?.await()

            withContext(Dispatchers.Main) {

                binding.ivCollectionImage?.setImageBitmap(collectionData?.collectionImageUri?.let {
                    Utils.getImageFromInternalStorage(requireActivity(),
                        it
                    )
                })

                if (productList?.isNotEmpty() == true) {

                    binding.mcvCollection?.visibility  = View.VISIBLE

                    val adapter = ProductCollectionAdapter(requireActivity(), productList)

                    binding.rvCollection?.layoutManager =
                        LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
                    binding.rvCollection?.adapter = adapter

                }
                else {
                    binding.mcvCollection?.visibility  = View.GONE
                }
            }
        }
        else {

            withContext(Dispatchers.Main) {
                binding.mcvCollection?.visibility  = View.GONE
            }
        }
    }

    private fun getMayLikeRvData() {

        val collectionUUIDList = productData.collectionUUID?.split(",")?.toMutableList()

        val productList = if (productData.collectionUUID?.isNotEmpty() == true) {

            collectionUUIDList.let {
                collectionUUIDList.let { it1 ->
                    it1?.let { it2 ->
                        Utils.getAllProductsAcceptCollection(
                            it2,offset
                        )
                    }
                }
            } }  else {

            productData.productUUID?.let { Utils.getAllProductsAcceptProduct(it,offset) }
        }

        if (productList?.isEmpty() == true) binding.progressBar?.visibility = View.GONE

        productList?.let { mayLikeProductList.addAll(it) }
        offset+=Constants.Limit

        catalogAdapter?.notifyItemRangeInserted(offset,mayLikeProductList.size)

    }

    // Setting the May also like RecyclerView
    private fun setUpMayLikeRecyclerView() {

        if (mayLikeProductList.isNotEmpty()) {

            catalogAdapter = CatalogAdapter(requireActivity(), mayLikeProductList,binding.progressBar)

            binding.rvCatalog?.layoutManager =
                GridLayoutManager(requireActivity(), 3) // Span Count is set to 3
            binding.rvCatalog?.adapter = catalogAdapter

        }
        else {
            binding.tvMayLike?.visibility  = View.GONE
        }
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
    private suspend fun setUpProductDetails(productUUID: String) {

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async Utils.getProductThroughProductUUID(productUUID)
        }

        productData = result.await()

        // Setup Product Image Recycler View
        productData.productImageUri?.let { setUpProductImageRecyclerView(it) }

        binding.tvProductName?.text = productData.productName
        binding.tvProductWeight?.text =
            "${productData.productWeight} ${Constants.weightUnit}"

        binding.tvProductCarat?.text = productData.productCarat.toString()
        binding.tvProductType?.text = productData.metalTypeUUID?.let {
            Utils.getMetalTypeNameThroughMetalTypeUUID(
                it
            )
        }

        binding.tvProductCategory?.text =
            productData.categoryUUID?.let { Utils.getCategoryNameThroughCategoryUUID(it) }

        binding.tvProductDescription?.text = productData.productDescription
        binding.tvProductPrice?.text = productData.productPrice?.let {
            Utils.roundToTwoDecimalPlaces(
                it
            )
        }?.let { Utils.getThousandSeparate(it) }

    }

    // Setting the Product Section Height through Screen height
    private fun setProductSectionHeight() {

        val screenHeight = Utils.getScreenHeight(requireActivity())

        val dashboardActivity = (requireActivity() as DashboardActivity)

        //Height of toolbar and bottom
        val heightOfToolbarAndBottom =
            (dashboardActivity.binding?.rlBottom?.height?.let { dashboardActivity.binding?.toolbar?.height?.plus(it) })

        val statusBarHeight = Utils.getStatusbarHeight(requireActivity())

        // Set the height of the layout
        val layoutParams = binding.llProductSection?.layoutParams as LinearLayout.LayoutParams

        if (heightOfToolbarAndBottom != null) {
            layoutParams.height = (screenHeight - heightOfToolbarAndBottom) - statusBarHeight - dashboardActivity.binding?.rlBottom?.height!!
            heightOfllProductSection = (screenHeight - heightOfToolbarAndBottom) - statusBarHeight - dashboardActivity.binding?.rlBottom?.height!!
        }

        binding.llProductSection?.setLayoutParams(layoutParams)

    }

    // Set the Clicks , initialization And Setup
    private suspend fun init() {

        binding.nsvProductDetail.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->

            if (scrollY >= v.getChildAt(v.childCount - 1)
                    .measuredHeight - v.measuredHeight && scrollY > oldScrollY
            ) {

                binding.progressBar?.visibility = View.VISIBLE

                getMayLikeRvData()
            }
        }

        // getting the product UUID
        val productUUID = arguments?.getString(Constants.productUUID)

        // Set the Product Details
        val job = productUUID?.let { lifecycleScope.launch(Dispatchers.Main){ setUpProductDetails(it) } }

        job?.join()

        // Checks if Product Already Added in card
        isProductAlreadyAddedInCard()

        // Set up collection Recycler view
        val jobCollection = lifecycleScope.launch(Dispatchers.IO) {
            productData.collectionUUID?.let {
                productData.productUUID?.let { it1 ->
                    setUpCollectionItemRecyclerView(
                        it,
                        it1
                    )
                }
            }
        }

        getMayLikeRvData()

        // Setting the May also like RecyclerView

        val jobMayLike = lifecycleScope.launch(Dispatchers.Main) {
            setUpMayLikeRecyclerView()
        }

        // Set Click on Cart Status button
        binding.llCartStatus?.setOnClickListener(this)

        binding.mcvExploreCollection?.setOnClickListener(this)

        jobCollection.join()
        jobMayLike.join()

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    // Save the height of the LinearLayout (product section)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the height of the LinearLayout
        heightOfllProductSection?.let { outState.putInt(productSectionHeight, it) }
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

                // Delete from order table
                productData.productUUID?.let { productData.productPrice?.let { it1 ->
                    Utils.removeOrder(requireContext(),it,
                        it1
                    )
                } }

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

                // insert order
                productData.productUUID?.let { productData.productPrice?.let { it1 ->
                    Utils.insertOrder(requireActivity(),it,
                        it1,
                    )
                } }

                Utils.T(requireActivity(), getString(R.string.added_successfully))
            }
        }

        else if (v == binding.mcvExploreCollection){

            GetProgressBar.getInstance(requireActivity())?.show()

            val bundle = Bundle()

            //Giving the collection UUID
            bundle.putString(Constants.collectionUUID,collectionUUIDData)
            (context as DashboardActivity).navController?.navigate(R.id.collectionDetail, bundle)

        }
    }
}