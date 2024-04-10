package com.jmsoft.basic.Database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.jmsoft.Utility.Database.AddressDataModel
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.Utility.Database.DeviceDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.user
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.main.model.DeviceModel

class DatabaseHelper(cx: Context) {

    private val dm: DataManager
    var cx: Context
    private var db: SQLiteDatabase? = null

    /**
     * UserDataHelper constructor
     *
     * @param cx //
     */
    init {
        instance = this
        this.cx = cx
        dm = DataManager(cx, DataManager.DATABASE_NAME, null, DataManager.DATABASE_VERSION)
    }


    /**
     * open db
     */
    fun open() {
        db = dm.writableDatabase
    }

    /**
     * close db
     */
    fun close() {
        //  db.close();
    }

    /**
     * read db
     */
    fun read() {
        db = dm.readableDatabase
    }

    //Deleting the product from the cart through cart UUID
    fun deleteProductFromCart(cartUUID: String) {

        open()
        db!!.delete(
            CartDataModel.TABLE_NAME_CART, CartDataModel.Key_cartUUID + " = '"
                    + cartUUID + "'", null
        )
        close()
    }

    //Deleting the address from the address table
    fun deleteAddress(addressUUID: String){

        open()

        db!!.delete(
            AddressDataModel.TABLE_NAME_ADDRESS, AddressDataModel.Key_addressUUID + " = '"
                    + addressUUID + "'", null
        )
        close()

    }

    // Update Quantity of Product in Cart Table
    fun updateProductQuantity(quantity:Int,cartUUID: String){

        open()

        val values = ContentValues()
        values.put(CartDataModel.Key_productQuantity, quantity)


        db!!.update(
            CartDataModel.TABLE_NAME_CART,
            values,
            "${CartDataModel.Key_cartUUID} = '${cartUUID}'",
            null
        )

        close()
    }

    // Checks if Product is Exist in Cart table
    @SuppressLint("Recycle")
    fun isProductExistInCartTable(userUUID: String, productUUID: String): Boolean? {
        open()
        val cursor = db?.rawQuery(
            "SELECT * FROM ${CartDataModel.TABLE_NAME_CART} WHERE ${CartDataModel.Key_userUUID} == '$userUUID' AND ${CartDataModel.Key_productUUID} == '$productUUID' ",
            null
        )

        return cursor?.moveToFirst()
    }

    // get cartUUId Through userUUID and ProductUUID
    @SuppressLint("Recycle", "Range")
    fun getCartUUID(userUUID: String, productUUID: String): String? {
        open()
        val cursor = db?.rawQuery(
            "SELECT * FROM ${CartDataModel.TABLE_NAME_CART} WHERE ${CartDataModel.Key_userUUID} == '$userUUID' AND ${CartDataModel.Key_productUUID} == '$productUUID' ",
            null
        )
        cursor?.moveToFirst()

        return  cursor?.getString(cursor.getColumnIndex(CartDataModel.Key_cartUUID))
    }

    //Inserting Product in Cart table
    fun insertProductInCartTable(cardDataModel: CartDataModel) {

        open()

        val cardValue = ContentValues().apply {

            put(CartDataModel.Key_cartUUID, cardDataModel.cartUUID)
            put(CartDataModel.Key_productUUID, cardDataModel.productUUID)
            put(CartDataModel.Key_userUUID, cardDataModel.userUUID)
            put(CartDataModel.Key_productQuantity, cardDataModel.productQuantity)

        }

        db?.insert(CartDataModel.TABLE_NAME_CART, null, cardValue)
    }

    //get Cart through user UUId
    @SuppressLint("Range")
    fun getCartThroughUserUUID(userUUID: String): ArrayList<CartDataModel> {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${CartDataModel.TABLE_NAME_CART} WHERE ${CartDataModel.Key_userUUID} == '$userUUID' ",
            null
        )

        cursor?.moveToFirst()

        val cardList = ArrayList<CartDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val cardData = CartDataModel()

                cardData.cartId =
                    cursor.getInt(cursor.getColumnIndex(CartDataModel.Key_cartId))
                cardData.cartUUID =
                    cursor.getString(cursor.getColumnIndex(CartDataModel.Key_cartUUID))
                cardData.userUUID =
                    cursor.getString(cursor.getColumnIndex(CartDataModel.Key_userUUID))
                cardData.productUUID =
                    cursor.getString(cursor.getColumnIndex(CartDataModel.Key_productUUID))
                cardData.productQuantity =
                    cursor.getInt(cursor.getColumnIndex(CartDataModel.Key_productQuantity))

                cardList.add(cardData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return cardList

    }

    //Checks if cart is empty
    @SuppressLint("Range", "Recycle")
    fun isCartEmpty(userUUID: String): Boolean {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${CartDataModel.TABLE_NAME_CART} WHERE ${CartDataModel.Key_userUUID} == '$userUUID' ",
            null
        )
        return !((cursor?.moveToFirst())?: false)
    }

    //Inserting Address in Address table
    fun insertAddressInAddressTable(addressDataModel: AddressDataModel) {

        open()

        val addressValue = ContentValues().apply {

            put(AddressDataModel.Key_addressUUID, addressDataModel.addressUUID)
            put(AddressDataModel.Key_firstName, addressDataModel.firstName)
            put(AddressDataModel.Key_lastName, addressDataModel.lastName)
            put(AddressDataModel.Key_address, addressDataModel.address)
            put(AddressDataModel.Key_phoneNumber, addressDataModel.phoneNumber)
            put(AddressDataModel.Key_zipCode, addressDataModel.zipCode)
            put(AddressDataModel.Key_userUUID, addressDataModel.userUUID)
        }

        db?.insert(AddressDataModel.TABLE_NAME_ADDRESS, null, addressValue)
    }

    //Get All Address of particular user from the Address table
    @SuppressLint("Range")
    fun getAllAddressThroughUserUUID(userUUID: String): ArrayList<AddressDataModel> {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${AddressDataModel.TABLE_NAME_ADDRESS} WHERE ${AddressDataModel.Key_userUUID} == '$userUUID' ",
            null
        )

        cursor?.moveToFirst()

        val addressList = ArrayList<AddressDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val addressData = AddressDataModel()

                addressData.addressId =
                    cursor.getInt(cursor.getColumnIndex(AddressDataModel.Key_addressId))
                addressData.addressUUID =
                    cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_addressUUID))
                addressData.userUUID =
                    cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_userUUID))
                addressData.address =
                    cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_address))
                addressData.zipCode =
                    cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_zipCode))

                addressList.add(addressData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return addressList
    }


    //Getting the Category Name through Category UUID
    @SuppressLint("Recycle", "Range")
    fun getCategoryNameThroughCategoryUUID(categoryUUID: String): String {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CategoryDataModel.TABLE_NAME_CATEGORY} WHERE ${CategoryDataModel.Key_categoryUUID} == '$categoryUUID'",
            null
        )

        cursor?.moveToFirst()

        return cursor?.getString(cursor.getColumnIndex(CategoryDataModel.Key_categoryName))!!
    }

    //Get All Products of particular category  from the Product table
    @SuppressLint("Range")
    fun getProductsThroughCategory(
        productCategory: String,
        productUUID: String
    ): ArrayList<ProductDataModel> {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_productCategory} == '$productCategory' AND ${ProductDataModel.Key_productUUID} != '$productUUID' ",
            null
        )

        cursor?.moveToFirst()

        val productList = ArrayList<ProductDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val productData = ProductDataModel()

                productData.productUUId =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
                productData.categoryUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
                productData.productName =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
                productData.productImage =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImage))
                productData.productPrice =
                    cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
                productData.productCategory =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productCategory))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productWeight =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
                productData.productMetalType =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productMetalType))
                productData.productUnitOfMeasurement =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUnitOfMeasurement))
                productData.productCarat =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
                productData.productRFID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFID))

                productList.add(productData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return productList
    }

    //Get All Products from the Product table Accept Category one Category
    @SuppressLint("Range")
    fun getAllProductsAcceptCategory(categoryName: String): ArrayList<ProductDataModel> {

        read()

        val cursor: Cursor? =
            db?.rawQuery("SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_productCategory} != '$categoryName' ", null)

        cursor?.moveToFirst()

        val productList = ArrayList<ProductDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val productData = ProductDataModel()

                productData.productUUId =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
                productData.categoryUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
                productData.productName =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
                productData.productImage =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImage))
                productData.productPrice =
                    cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
                productData.productCategory =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productCategory))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productWeight =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
                productData.productMetalType =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productMetalType))
                productData.productUnitOfMeasurement =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUnitOfMeasurement))
                productData.productCarat =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
                productData.productRFID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFID))

                productList.add(productData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return productList
    }


    //Get All Products from the Product table
    @SuppressLint("Range")
    fun getAllProducts(): ArrayList<ProductDataModel> {

        read()

        val cursor: Cursor? =
            db?.rawQuery("SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} ", null)

        cursor?.moveToFirst()

        val productList = ArrayList<ProductDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val productData = ProductDataModel()

                productData.productUUId =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
                productData.categoryUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
                productData.productName =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
                productData.productImage =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImage))
                productData.productPrice =
                    cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
                productData.productCategory =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productCategory))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productWeight =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
                productData.productMetalType =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productMetalType))
                productData.productUnitOfMeasurement =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUnitOfMeasurement))
                productData.productCarat =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
                productData.productRFID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFID))

                productList.add(productData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return productList
    }

    //Getting the Product through Product UUID
    @SuppressLint("Range", "Recycle")
    fun getProductThroughProductUUID(productUUID: String): ProductDataModel {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_productUUID} == '$productUUID' ",
            null
        )

        val productData = ProductDataModel()

        if (cursor != null && cursor.moveToFirst()) {

            productData.productUUId =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
            productData.categoryUUID =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
            productData.productName =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
            productData.productImage =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImage))
            productData.productCategory =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productCategory))
            productData.productPrice =
                cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
            productData.productDescription =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
            productData.productDescription =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
            productData.productWeight =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
            productData.productUnitOfMeasurement =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUnitOfMeasurement))
            productData.productMetalType =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productMetalType))
            productData.productCarat =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
            productData.productRFID =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFID))

        }
        cursor?.close()
        close()

        return productData
    }


    //Inserting Category in Category table
    fun insertCategoryInCategoryTable(categoryDataModel: CategoryDataModel) {

        open()

        val categoryValue = ContentValues().apply {
            put(CategoryDataModel.Key_categoryUUID, categoryDataModel.categoryUUID)
            put(CategoryDataModel.Key_categoryName, categoryDataModel.categoryName)
        }

        db?.insert(CategoryDataModel.TABLE_NAME_CATEGORY, null, categoryValue)

    }

    //Inserting Product in Product table
    fun insertProductInProductTable(productDataModel: ProductDataModel) {

        open()

        val productValue = ContentValues().apply {

            put(ProductDataModel.Key_productUUID, productDataModel.productUUId)
            put(ProductDataModel.Key_categoryUUID, productDataModel.categoryUUID)
            put(ProductDataModel.Key_productName, productDataModel.productName)
            put(ProductDataModel.Key_productImage, productDataModel.productImage)
            put(ProductDataModel.Key_productPrice, productDataModel.productPrice)
            put(ProductDataModel.Key_productCategory, productDataModel.productCategory)
            put(ProductDataModel.Key_productDescription, productDataModel.productDescription)
            put(ProductDataModel.Key_productWeight, productDataModel.productWeight)
            put(ProductDataModel.Key_productMetalType, productDataModel.productMetalType)
            put(ProductDataModel.Key_productUnitOfMeasurement, productDataModel.productUnitOfMeasurement)
            put(ProductDataModel.Key_productCarat, productDataModel.productCarat)
            put(ProductDataModel.Key_productRFID, productDataModel.productRFID)
        }

        db?.insert(ProductDataModel.TABLE_NAME_PRODUCT, null, productValue)

    }

    // Check if Category exist in the category table
    @SuppressLint("Recycle")
    fun isCategoryExist(categoryName: String): Boolean? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CategoryDataModel.TABLE_NAME_CATEGORY} WHERE ${CategoryDataModel.Key_categoryName} == '$categoryName'",
            null
        )

        return cursor?.moveToFirst()
    }

    //Getting the Category UUId through Category Name
    @SuppressLint("Recycle", "Range")
    fun getCategoryUUIDThroughCategoryName(categoryName: String): String {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CategoryDataModel.TABLE_NAME_CATEGORY} WHERE ${CategoryDataModel.Key_categoryName} == '$categoryName'",
            null
        )

        var categoryName = ""

        if (cursor != null && cursor.moveToFirst()) {
            categoryName =
                cursor.getString(cursor.getColumnIndex(CategoryDataModel.Key_categoryUUID))!!
        }

        return categoryName
    }

    //Inserting the Admin in the user table at the time of table creation
    fun insertAdmin(db: SQLiteDatabase) {

        val adminUserValues = ContentValues().apply {

            put(UserDataModel.Key_userType, Constants.admin)
            put(UserDataModel.Key_userUUID, Utils.generateUUId())
            put(UserDataModel.Key_firstName, Utils.getName(cx))
            put(UserDataModel.Key_lastName, "")
            put(UserDataModel.Key_email, Utils.getEmail(cx)?.lowercase())
            put(UserDataModel.Key_phoneNumber, "")
            put(UserDataModel.Key_profileUri, "")
            put(UserDataModel.Key_password, Utils.encodeText(Utils.getPassword(cx).toString()))
        }
        db.insert(UserDataModel.TABLE_NAME_USER, null, adminUserValues)
    }

    /**
     * delete by user id from the table
     *
     * @param userData //
     */
    fun delete(userData: UserDataModel) {
        open()
        db!!.delete(
            UserDataModel.TABLE_NAME_USER_SESSION, UserDataModel.Key_userUUID + " = '"
                    + userData.userUUID + "'", null
        )
        close()
    }

    /**
     * delete All Data From the Session Table
     */
    fun deleteSession() {
        open()
        db!!.delete(UserDataModel.TABLE_NAME_USER_SESSION, null, null)
        close()
    }

    //Deleting the User through the UserUUId from the user table
    fun deleteUserThroughUserUUID(userUUID: String) {
        open()
        val selection = "${UserDataModel.Key_userUUID} = ?"
        val selectionArgs = arrayOf(userUUID)
        db!!.delete(UserDataModel.TABLE_NAME_USER, selection, selectionArgs)
        close()
    }

    /**
     * is Exist in table
     *
     * Checks if userUUID is same or not in the Session table
     * @param userData //
     * @return //
     */
    private fun isExist(userData: UserDataModel): Boolean {
        read()
        @SuppressLint("Recycle") val cur = db!!.rawQuery(
            "SELECT * FROM ${UserDataModel.TABLE_NAME_USER_SESSION} WHERE ${UserDataModel.Key_userUUID} = ?",
            arrayOf(userData.userUUID)
        )
        return cur.moveToFirst()
    }

    // Get User through Email And Password from the user table
    @SuppressLint("Range")
    fun getUserThroughEmailAndPassword(email: String, password: String): UserDataModel {

        read()
        val selection = "${UserDataModel.Key_email} = ? AND ${UserDataModel.Key_password} = ?"
        val selectionArgs =
            arrayOf(email, Utils.encodeText(password)) // match the password after encode it

        val cursor: Cursor? = db?.query(
            UserDataModel.TABLE_NAME_USER,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        cursor?.moveToFirst()

        val userData = UserDataModel()

        if (cursor != null) {

            userData.userUUID = cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userUUID))

            userData.userType =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userType))

            userData.firstName =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_firstName))
            userData.lastName =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_lastName))
            userData.email =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_email))
            userData.phoneNumber =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_phoneNumber))
            userData.profileUri =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_profileUri))
            userData.password =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_password))

            cursor.close()
        }
        close()

        return userData
    }

    // Check if User is valid or not through user table
    fun isValidUser(email: String, password: String): Boolean {
        read()
        val selection = "${UserDataModel.Key_email} = ? AND ${UserDataModel.Key_password} = ?"

        val selectionArgs =
            arrayOf(email, Utils.encodeText(password)) // match the password after encode it

        val cursor: Cursor = db?.query(
            UserDataModel.TABLE_NAME_USER,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        ) ?: return false

        val isValid = cursor.moveToFirst()
        cursor.close() // Close the cursor to release resources
        return isValid
    }

    //this method checks if any user has this phone number
    @SuppressLint("Recycle")
    fun isAnyUserHasThisPhoneNumber(phoneNumber: String, userUUID: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_phoneNumber} = '$phoneNumber' And ${UserDataModel.Key_userUUID} != '$userUUID' ",
            null
        )

        return cursor.moveToFirst()

    }

    //this method checks if any user has this email
    @SuppressLint("Recycle")
    fun isAnyUserHasThisEmail(email: String, userUUID: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_email} = '$email' And ${UserDataModel.Key_userUUID} != '$userUUID'",
            null
        )

        return cursor.moveToFirst()
    }


    // Checks is Phone Number Already Exist in the user table
    @SuppressLint("Recycle")
    fun isPhoneNumberExist(phoneNumber: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_phoneNumber} = '$phoneNumber' ",
            null
        )

        return cursor.moveToFirst()
    }

    // Checks is Phone Number Already Exist in the Address table
    @SuppressLint("Recycle")
    fun isPhoneNumberExistInAddressTable(phoneNumber: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${AddressDataModel.TABLE_NAME_ADDRESS} where ${AddressDataModel.Key_phoneNumber} = '$phoneNumber' ",
            null
        )

        return cursor.moveToFirst()
    }

    // Checks is Email Already Exist in the user table
    @SuppressLint("Recycle")
    fun isEmailExist(email: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_email} = '$email' ",
            null
        )

        return cursor.moveToFirst()
    }


    // getting All User Details Accept Admin form the user table
    @SuppressLint("Range")
    fun getAllUserDetails(): ArrayList<UserDataModel> {

        read()
        @SuppressLint("Recycle") val cursor = db!!.rawQuery(
            "select * from " + UserDataModel.TABLE_NAME_USER + " where " + UserDataModel.Key_userType + "='"
                    + user + "'", null
        )

        val userList = ArrayList<UserDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val userData = UserDataModel()

                userData.userUUID =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userUUID))

                userData.userType =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userType))

                userData.firstName =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_firstName))
                userData.lastName =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_lastName))
                userData.email =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_email))
                userData.phoneNumber =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_phoneNumber))
                userData.profileUri =
                    cursor.getString(cursor.getColumnIndex(UserDataModel.Key_profileUri))

                userList.add(userData)

            } while (cursor.moveToPrevious())
            cursor.close()
        }
        close()

        return userList
    }

    // get User Details through userUUID from the User table
    @SuppressLint("Range")
    fun getUserDetailsThroughUserUUID(userUUID: String): UserDataModel {
        read()
        @SuppressLint("Recycle") val cursor = db?.rawQuery(
            "SELECT * FROM ${UserDataModel.TABLE_NAME_USER} WHERE ${UserDataModel.Key_userUUID} = ?",
            arrayOf(userUUID)
        )
        cursor?.moveToFirst()
        val userData = UserDataModel()

        if (cursor != null && cursor.count > 0) {

            userData.userUUID = cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userUUID))

            userData.userType =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userType))

            userData.firstName =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_firstName))
            userData.lastName =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_lastName))
            userData.email =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_email))
            userData.phoneNumber =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_phoneNumber))
            userData.profileUri =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_profileUri))

            userData.password =
                cursor.getString(cursor.getColumnIndex(UserDataModel.Key_password))

            cursor.close()
        }
        close()

        return userData
    }

    // Checks is User Table Empty
    fun isUserTableEmpty(): Boolean {

        read()
        @SuppressLint("Recycle") val cur = db!!.rawQuery(
            "select * from " + UserDataModel.TABLE_NAME_USER, null
        )
        return !cur.moveToFirst()
    }

    //Updating the User Profile in the User Table
    fun updateProfileInUserTable(profileName: String, userUUID: String): Boolean {

        open()

        val contentValues = ContentValues().apply {
            put(UserDataModel.Key_profileUri, profileName)
        }

        val whereClause = "${UserDataModel.Key_userUUID} = ?"
        val whereArgs = arrayOf(userUUID)

        val rowsAffected =
            db?.update(UserDataModel.TABLE_NAME_USER, contentValues, whereClause, whereArgs)

        if (rowsAffected!! > 0) {
            return true
        } else {
            return false
        }
    }

    //Insert new user in the User table
    fun insetDataInUserTable(userData: UserDataModel) {

        open()
        val values = ContentValues()

        // values.put(UserData.KEY_ID, userData.userId);
        values.put(UserDataModel.Key_userType, userData.userType)
        values.put(UserDataModel.Key_userUUID, userData.userUUID)
        values.put(UserDataModel.Key_firstName, userData.firstName)
        values.put(UserDataModel.Key_lastName, userData.lastName)
        values.put(UserDataModel.Key_email, userData.email)
        values.put(UserDataModel.Key_phoneNumber, userData.phoneNumber)
        values.put(UserDataModel.Key_profileUri, userData.profileUri)
        values.put(UserDataModel.Key_password, userData.password)

        db!!.insert(UserDataModel.TABLE_NAME_USER, null, values)

        close()

    }

    //Insert Device Data in the Device Table
    fun insertNewDeviceData(deviceDataModel: DeviceDataModel) {

        open()
        val values = ContentValues()

        values.put(DeviceDataModel.Key_deviceUUID, deviceDataModel.deviceUUID)
        values.put(DeviceDataModel.Key_deviceName, deviceDataModel.deviceName)
        values.put(DeviceDataModel.Key_deviceType, deviceDataModel.deviceType)
        values.put(DeviceDataModel.Key_deviceAddress, deviceDataModel.deviceAddress)
        values.put(DeviceDataModel.Key_userUUID, deviceDataModel.userUUID)

        db!!.insert(DeviceDataModel.TABLE_NAME_DEVICE, null, values)

        close()
    }

    //Delete Device from Device table through DeviceUUID
    fun deleteDeviceThoughDeviceUUID(deviceUUID: String) {
        open()
        db?.delete(
            DeviceDataModel.TABLE_NAME_DEVICE,
            "${DeviceDataModel.Key_deviceUUID} = ?",
            arrayOf(deviceUUID)
        )
        close()
    }

    //Update User Details in the User Table
    fun updateUserDetails(userDataModel: UserDataModel) {
        open()

        val values = ContentValues()

        values.put(UserDataModel.Key_firstName, userDataModel.firstName)
        values.put(UserDataModel.Key_lastName, userDataModel.lastName)
        values.put(UserDataModel.Key_email, userDataModel.email)
        values.put(UserDataModel.Key_phoneNumber, userDataModel.phoneNumber)
        values.put(UserDataModel.Key_password, userDataModel.password)

        db!!.update(
            UserDataModel.TABLE_NAME_USER,
            values,
            "${UserDataModel.Key_userUUID} = '${userDataModel.userUUID}'",
            null
        )

        close()
    }

    //getting All the Devices of particular from the device table through userUUID
    @SuppressLint("Recycle", "Range")
    fun getDevicesThroughUserUUID(userUUID: String): ArrayList<DeviceModel> {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${DeviceDataModel.TABLE_NAME_DEVICE} WHERE ${UserDataModel.Key_userUUID} = ?",
            arrayOf(userUUID)
        )

        val deviceItem = ArrayList<DeviceModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val deviceData = DeviceModel()

                deviceData.deviceUUID =
                    cursor.getString(cursor.getColumnIndex(DeviceDataModel.Key_deviceUUID))
                deviceData.deviceName =
                    cursor.getString(cursor.getColumnIndex(DeviceDataModel.Key_deviceName))
                deviceData.deviceType =
                    cursor.getString(cursor.getColumnIndex(DeviceDataModel.Key_deviceType))
                deviceData.deviceAddress =
                    cursor.getString(cursor.getColumnIndex(DeviceDataModel.Key_deviceAddress))

                deviceItem.add(deviceData)

            } while (cursor.moveToPrevious())
            cursor.close()
        }
        close()
        return deviceItem
    }

    // Check if no device for particular user in the Device Table
    @SuppressLint("Recycle")
    fun isNoDeviceForUser(userUUID: String): Boolean {
        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${DeviceDataModel.TABLE_NAME_DEVICE} WHERE ${UserDataModel.Key_userUUID} = ?",
            arrayOf(userUUID)
        )
        val result = cursor!!.moveToFirst()
        close()
        return !result
    }

    /**
     * insert Data in Session table
     * if user id is same it will update the user details
     *
     * @param userData //
     */
    fun insertDataInSessionTable(userData: UserDataModel) {
        open()
        val values = ContentValues()

        values.put(UserDataModel.Key_userUUID, userData.userUUID)
        values.put(UserDataModel.Key_userType, userData.userType)
        values.put(UserDataModel.Key_firstName, userData.firstName)
        values.put(UserDataModel.Key_lastName, userData.lastName)
        values.put(UserDataModel.Key_email, userData.email)
        values.put(UserDataModel.Key_phoneNumber, userData.phoneNumber)
        values.put(UserDataModel.Key_profileUri, userData.profileUri)

        if (!isExist(userData)) {

            db!!.insert(UserDataModel.TABLE_NAME_USER_SESSION, null, values)
        } else {

            db!!.update(
                UserDataModel.TABLE_NAME_USER_SESSION,
                values,
                "${UserDataModel.Key_userUUID} = '${userData.userUUID}'",
                null
            )
        }
        close()
    }// userData.userId = cursor.getString(cursor.getColumnIndex(UserData.KEY_ID));

    /**
     * Return User Array List From Session Table
     *
     * @return //
     */
    @get:SuppressLint("Range")
    val list: ArrayList<UserDataModel>
        get() {
            val userItem = ArrayList<UserDataModel>()
            read()
            val cursor =
                db!!.rawQuery("select * from " + UserDataModel.TABLE_NAME_USER_SESSION, null)
            if (cursor != null && cursor.count > 0) {
                cursor.moveToLast()
                do {

                    val userData = UserDataModel()

                    userData.userUUID =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userUUID))

                    userData.userType =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_userType))

                    userData.firstName =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_firstName))
                    userData.lastName =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_lastName))
                    userData.email =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_email))
                    userData.phoneNumber =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_phoneNumber))
                    userData.profileUri =
                        cursor.getString(cursor.getColumnIndex(UserDataModel.Key_profileUri))

                    userItem.add(userData)

                } while (cursor.moveToPrevious())
                cursor.close()
            }
            close()
            return userItem
        }

    companion object {
        /**
         * DatabaseHelper instance
         *
         * @return //
         */
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: DatabaseHelper
            private set
    }
}