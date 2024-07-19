package com.jmsoft.basic.Database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.graphics.drawable.toBitmap
import com.jmsoft.R
import com.jmsoft.Utility.Database.AddressDataModel
import com.jmsoft.Utility.Database.CartDataModel
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.Utility.Database.CollectionDataModel
import com.jmsoft.Utility.Database.ContactDataModel
//import com.jmsoft.Utility.Database.CollectionDataModel
import com.jmsoft.Utility.Database.DeviceDataModel
import com.jmsoft.Utility.Database.MetalTypeDataModel
import com.jmsoft.Utility.Database.OrderDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.user
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.main.model.DeviceModel
import java.util.UUID

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
        db?.execSQL("PRAGMA foreign_keys=ON")
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
        db?.execSQL("PRAGMA foreign_keys=ON")

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


    // Deleting the product from the cart through cart
    fun deleteProductFromCart(userUUID: String,productUUID: String) {

        open()
        db!!.delete(
            CartDataModel.TABLE_NAME_CART, "${CartDataModel.Key_userUUID} = ? AND ${CartDataModel.Key_productUUID} = ? ", arrayOf(userUUID,productUUID)
        )
        close()
    }

    //Deleting the address from the address table
    fun deleteAddress(addressUUID: String) {

        open()

        db!!.delete(
            AddressDataModel.TABLE_NAME_ADDRESS, AddressDataModel.Key_addressUUID + " = '"
                    + addressUUID + "'", null
        )
        close()

    }

    // Deleting the contact from the contact table
    fun deleteContact(contactUUID: String) {

        open()

        db!!.delete(
            ContactDataModel.TABLE_NAME_CONTACT, ContactDataModel.Key_contactUUID + " = '"
                    + contactUUID + "'", null
        )
        close()

    }

    // Update Quantity of Product in Cart Table
    fun updateProductQuantity(quantity: Int, cartUUID: String) {

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

    // Update Quantity of Product in Cart Table
    fun updateProductQuantityInCart(userUUID: String,productUUID: String,quantity: Int) {

        open()

        val values = ContentValues()
        values.put(CartDataModel.Key_productQuantity, quantity)

        db!!.update(
            CartDataModel.TABLE_NAME_CART,
            values,
            "${CartDataModel.Key_userUUID} = ? AND ${CartDataModel.Key_productUUID} = ?",
            arrayOf(userUUID,productUUID)
        )
        close()
    }

    // Check if Metal type already Exit in the metal Type Table
    fun isMetalTypeExist(metalTypeName: String): Boolean? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${MetalTypeDataModel.TABLE_NAME_METAL_TYPE} WHERE ${MetalTypeDataModel.Key_metalTypeName} == ?",
            arrayOf(metalTypeName)
        )

        val result = cursor?.moveToFirst()

        cursor?.close()

        return result
    }

    // Check if Metal type exist in the metal type table accept metalTypeUUId
    fun isMetalTypeExistAccept(metalTypeDataModel: MetalTypeDataModel): Boolean? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${MetalTypeDataModel.TABLE_NAME_METAL_TYPE} WHERE ${MetalTypeDataModel.Key_metalTypeUUID} != ? AND ${MetalTypeDataModel.Key_metalTypeName} == ?",
            arrayOf(metalTypeDataModel.metalTypeUUID,metalTypeDataModel.metalTypeName)
        )

        val result = cursor?.moveToFirst()

        cursor?.close()

        return result
    }


    // Deleting Metal Type from the Product table
    private fun deleteMetalTypeUUIDFromProductTable(metalTypeUUID: String){
        open()

        val contentValues = ContentValues()
        contentValues.put(ProductDataModel.Key_metalTypeUUID,"")

        db?.update(ProductDataModel.TABLE_NAME_PRODUCT,contentValues,"${ProductDataModel.Key_metalTypeUUID} = ?",
            arrayOf(metalTypeUUID)
        )

    }

    // Delete Metal Type from the metal type table
    fun deleteMetalType(metalTypeUUID: String) {

        open()

//        deleteMetalTypeUUIDFromProductTable(metalTypeUUID)

        db?.delete(
            MetalTypeDataModel.TABLE_NAME_METAL_TYPE,
            "${MetalTypeDataModel.Key_metalTypeUUID} == ?",
            arrayOf(metalTypeUUID)
        )
    }

    // Delete Product from the product table
    fun deleteProduct(productUUID: String) {

        open()

        db?.delete(
            ProductDataModel.TABLE_NAME_PRODUCT,
            "${ProductDataModel.Key_productUUID} == ?",
            arrayOf(productUUID)
        )

    }

    // Get All the metal type from the metal type table
    @SuppressLint("Range")
    fun getAllMetalType(): ArrayList<MetalTypeDataModel> {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${MetalTypeDataModel.TABLE_NAME_METAL_TYPE}",
            null
        )

        cursor?.moveToFirst()

        val metalTypeList = ArrayList<MetalTypeDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {
                val metalTypeData = MetalTypeDataModel()

                metalTypeData.metalTypeUUID =
                    cursor.getString(cursor.getColumnIndex(MetalTypeDataModel.Key_metalTypeUUID))
                metalTypeData.metalTypeName =
                    cursor.getString(cursor.getColumnIndex(MetalTypeDataModel.Key_metalTypeName))

                metalTypeList.add(metalTypeData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return metalTypeList

    }

    // Updating metal type in the metal type table
    fun updateMetalType(metalTypeUUID: String, metalTypeName: String) {

        open()

        val metalTypeValue = ContentValues().apply {

            put(MetalTypeDataModel.Key_metalTypeName, metalTypeName)
        }

        db?.update(
            MetalTypeDataModel.TABLE_NAME_METAL_TYPE,
            metalTypeValue,
            "${MetalTypeDataModel.Key_metalTypeUUID} == ?",
            arrayOf(metalTypeUUID)
        )

    }

    // Add Metal type in Metal_Type table
    fun addMetalTypeInTheMetalTypeTable(metalTypeDataModel: MetalTypeDataModel) {

        open()

        val metalTypeValue = ContentValues().apply {

            put(MetalTypeDataModel.Key_metalTypeUUID, metalTypeDataModel.metalTypeUUID)
            put(MetalTypeDataModel.Key_metalTypeName, metalTypeDataModel.metalTypeName)
        }

        db?.insert(MetalTypeDataModel.TABLE_NAME_METAL_TYPE, null, metalTypeValue)
    }


    // Add Collection in the Collection table
    fun addCollection(collectionDataModel: CollectionDataModel) {

        open()

        val collectionValue = ContentValues().apply {

            put(CollectionDataModel.Key_collectionUUID, collectionDataModel.collectionUUID)
            put(CollectionDataModel.Key_collectionName, collectionDataModel.collectionName)
            put(CollectionDataModel.Key_collectionImageUri, collectionDataModel.collectionImageUri)
        }

        db?.insert(CollectionDataModel.TABLE_NAME_COLLECTION, null, collectionValue)
    }


    // Get All collection from the collection table
    @SuppressLint("Range")
    fun getAllCollection(): ArrayList<CollectionDataModel> {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${CollectionDataModel.TABLE_NAME_COLLECTION}",
            null
        )

        cursor?.moveToFirst()

        val collectionList = ArrayList<CollectionDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {
                val collectionData = CollectionDataModel()

                collectionData.collectionUUID =
                    cursor.getString(cursor.getColumnIndex(CollectionDataModel.Key_collectionUUID))
                collectionData.collectionName =
                    cursor.getString(cursor.getColumnIndex(CollectionDataModel.Key_collectionName))

                collectionData.collectionImageUri =
                    cursor.getString(cursor.getColumnIndex(CollectionDataModel.Key_collectionImageUri))

                collectionList.add(collectionData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return collectionList

    }

    // Checks if Product is Exist in Cart table
    @SuppressLint("Recycle")
    fun isProductExistInCartTable(userUUID: String, productUUID: String): Boolean? {
        open()
        val cursor = db?.rawQuery(
            "SELECT * FROM ${CartDataModel.TABLE_NAME_CART} WHERE ${CartDataModel.Key_userUUID} == '$userUUID' AND ${CartDataModel.Key_productUUID} == '$productUUID' ",
            null
        )

        val result = cursor?.moveToFirst()

        cursor?.close()

        return result
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

        val cartUUID = cursor?.getString(cursor.getColumnIndex(CartDataModel.Key_cartUUID))
        cursor?.close()

        return cartUUID
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
        val result = !((cursor?.moveToFirst()) ?: false)
        cursor?.close()

        return result
    }

    // Inserting Address in Address table
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

    // is this address uuid exist
    @SuppressLint("Recycle")
    fun isAddressUUIDExist(addressUUID: String): Boolean? {

        read()

        val cursor = db?.rawQuery("SELECT ${AddressDataModel.Key_address} FROM ${AddressDataModel.TABLE_NAME_ADDRESS} WHERE ${AddressDataModel.Key_addressUUID}  == ? ", arrayOf(addressUUID))
        return cursor?.moveToFirst()

    }

    // get address through address uuid
    @SuppressLint("Range")
    fun getAddressThroughAddressUUID(addressUUID: String): AddressDataModel {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${AddressDataModel.TABLE_NAME_ADDRESS} WHERE ${AddressDataModel.Key_addressUUID} == ?",
            arrayOf(addressUUID)
        )

        cursor?.moveToFirst()

        val addressData = AddressDataModel()

        if (cursor != null && cursor.count > 0) {

            addressData.addressId =
                cursor.getInt(cursor.getColumnIndex(AddressDataModel.Key_addressId))

            addressData.addressUUID =
                cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_addressUUID))

            addressData.userUUID =
                cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_userUUID))

            addressData.firstName =
                cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_firstName))

            addressData.lastName =
                cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_lastName))

            addressData.address =
                cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_address))

            addressData.phoneNumber =
                cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_phoneNumber))

            addressData.zipCode =
                cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_zipCode))

            cursor.close()
        }
        close()

        return addressData
    }


    // Inserting Contact in Contact table
    fun insertContact(contactDataModel: ContactDataModel) {

        open()

        val contactValue = ContentValues().apply {

            put(ContactDataModel.Key_contactUUID, contactDataModel.contactUUID)
            put(ContactDataModel.Key_profileUri, contactDataModel.profileUri)
            put(ContactDataModel.Key_firstName, contactDataModel.firstName)
            put(ContactDataModel.Key_lastName, contactDataModel.lastName)
            put(ContactDataModel.Key_phoneNumber, contactDataModel.phoneNumber)
            put(ContactDataModel.Key_emailAddress, contactDataModel.emailAddress)
            put(ContactDataModel.Key_type, contactDataModel.type)
            put(ContactDataModel.Key_userUUID, contactDataModel.userUUID)
        }

        db?.insert(ContactDataModel.TABLE_NAME_CONTACT, null, contactValue)
    }

    // get total  Order
    @SuppressLint("Recycle")
    fun getTotalOrder(): Int {

        read()

        val cursor = db?.rawQuery(
            "SELECT COUNT(*) FROM ${OrderDataModel.TABLE_NAME_ORDER}",
            null
        )

        var count = 0
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor?.close()

        return count
    }



    // Inserting Order in Order table
    fun insertOrder(orderDataModel: OrderDataModel) {

        open()

        val orderValue = ContentValues().apply {

            put(OrderDataModel.Key_orderUUID, orderDataModel.orderUUID)
            put(OrderDataModel.Key_orderNo, orderDataModel.orderNo)
            put(OrderDataModel.Key_productUUIDUri, orderDataModel.productUUIDUri)
            put(OrderDataModel.Key_productQuantityUri, orderDataModel.productQuantityUri)
            put(OrderDataModel.Key_userUUID, orderDataModel.userUUID)
            put(OrderDataModel.Key_addressUUID, orderDataModel.addressUUID)
            put(OrderDataModel.Key_status, orderDataModel.status)
            put(OrderDataModel.Key_totalAmount, orderDataModel.totalAmount)
            put(OrderDataModel.Key_pdfName, orderDataModel.pdfName)
            put(OrderDataModel.Key_date, orderDataModel.date)

        }

        db?.insert(OrderDataModel.TABLE_NAME_ORDER, null, orderValue)

    }

    // is Order exist
    @SuppressLint("Recycle")
    fun isOrderExist(orderUUID: String): Boolean? {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT ${OrderDataModel.Key_orderUUID} FROM ${OrderDataModel.TABLE_NAME_ORDER} WHERE ${OrderDataModel.Key_orderUUID} == ? ",
            arrayOf(orderUUID)
        )

        return cursor?.moveToFirst()

    }

    // delete order
     fun deleteOrder(orderUUID: String) {

        open()

        db?.delete(
            OrderDataModel.TABLE_NAME_ORDER, "${OrderDataModel.Key_orderUUID} == ?",
            arrayOf(orderUUID)
        )
    }

    // get  order of particular user
    @SuppressLint("Recycle", "Range")
    fun getOrderByUUID(orderUUID: String): OrderDataModel {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${OrderDataModel.TABLE_NAME_ORDER} WHERE ${OrderDataModel.Key_orderUUID} == ?" ,
            arrayOf(orderUUID)
        )

        val orderDataModel = OrderDataModel()

        if (cursor != null && cursor.moveToFirst()) {

            orderDataModel.orderNo = cursor.getString(cursor.getColumnIndex(OrderDataModel.Key_orderNo))

            orderDataModel.orderUUID = cursor.getString(cursor.getColumnIndex(OrderDataModel.Key_orderUUID))

            orderDataModel.productUUIDUri = cursor.getString(cursor.getColumnIndex(OrderDataModel.Key_productUUIDUri))

            orderDataModel.productQuantityUri = cursor.getString(cursor.getColumnIndex(OrderDataModel.Key_productQuantityUri))

            orderDataModel.totalAmount = cursor.getDouble(cursor.getColumnIndex(OrderDataModel.Key_totalAmount))

        }

        return orderDataModel
    }

    // Get Order UUID
    @SuppressLint("Range")
    fun getOrderUUID(userUUID: String, productUUID: String): String? {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT ${OrderDataModel.Key_orderUUID} FROM ${OrderDataModel.TABLE_NAME_ORDER} WHERE ${OrderDataModel.Key_userUUID} == ? AND ${OrderDataModel.Key_productUUIDUri} == ? AND ${OrderDataModel.Key_status} == ?",
            arrayOf(userUUID,productUUID,Constants.New)
        )

        cursor?.moveToFirst()

        val orderUUID = cursor?.getString(cursor.getColumnIndex(OrderDataModel.Key_orderUUID))

        cursor?.close()

        return orderUUID
    }

    // get orders of particular user
    @SuppressLint("Recycle", "Range")
    fun getOrders(userUUID: String,status:String): ArrayList<OrderDataModel> {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${OrderDataModel.TABLE_NAME_ORDER} WHERE ${OrderDataModel.Key_userUUID} == ? AND ${OrderDataModel.Key_status} == ?",
            arrayOf(userUUID,status)
        )

        cursor?.moveToFirst()

        val orderList = ArrayList<OrderDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val orderData = OrderDataModel()

                orderData.orderUUID =
                    cursor.getString(cursor.getColumnIndex(OrderDataModel.Key_orderUUID))

                orderData.orderNo =
                    cursor.getString(cursor.getColumnIndex(OrderDataModel.Key_orderNo))

                orderData.productUUIDUri =
                    cursor.getString(cursor.getColumnIndex(OrderDataModel.Key_productUUIDUri))

                orderData.productQuantityUri =
                    cursor.getString(cursor.getColumnIndex(OrderDataModel.Key_productQuantityUri))

                orderData.addressUUID =
                    cursor.getString(cursor.getColumnIndex(OrderDataModel.Key_addressUUID))

                orderData.totalAmount =
                    cursor.getDouble(cursor.getColumnIndex(OrderDataModel.Key_totalAmount))

                orderData.pdfName =
                    cursor.getString(cursor.getColumnIndex(OrderDataModel.Key_pdfName))

                orderData.date =
                    cursor.getString(cursor.getColumnIndex(OrderDataModel.Key_date))

                orderList.add(orderData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return orderList

    }

    // update Order in Order table
    fun updateOrder(orderDataModel: OrderDataModel) {

        open()

        val orderValue = ContentValues().apply {

            put(OrderDataModel.Key_productUUIDUri, orderDataModel.productUUIDUri)
            put(OrderDataModel.Key_totalAmount, orderDataModel.totalAmount)
            put(OrderDataModel.Key_productQuantityUri, orderDataModel.productQuantityUri)

        }

        db?.update(OrderDataModel.TABLE_NAME_ORDER,orderValue,"${OrderDataModel.Key_orderUUID} == ?", arrayOf(orderDataModel.orderUUID))

    }

    // update order status to confirm
    fun updateOrderStatus(orderDataModel: OrderDataModel) {

        open()

        val orderValue = ContentValues().apply {

            put(OrderDataModel.Key_addressUUID, orderDataModel.addressUUID)
            put(OrderDataModel.Key_pdfName, orderDataModel.pdfName)
            put(OrderDataModel.Key_status, orderDataModel.status)
            put(OrderDataModel.Key_date, orderDataModel.date)

        }

        db?.update(OrderDataModel.TABLE_NAME_ORDER,orderValue,"${OrderDataModel.Key_orderUUID} == ?", arrayOf(orderDataModel.orderUUID))


    }

    // update quantity in order table
    fun updateQuantityInOrder(orderDataModel: OrderDataModel) {

        open()

        val orderValue = ContentValues().apply {
            put(OrderDataModel.Key_productQuantityUri, orderDataModel.productQuantityUri)
            put(OrderDataModel.Key_totalAmount,orderDataModel.totalAmount)
        }

        db?.update(OrderDataModel.TABLE_NAME_ORDER,orderValue,"${OrderDataModel.Key_orderUUID} == ?", arrayOf(orderDataModel.orderUUID))


    }

    // Update Address in the Address Table
    fun updateAddressInTheAddressTable(addressDataModel: AddressDataModel) {

        open()

        val addressValue = ContentValues().apply {

            put(AddressDataModel.Key_firstName, addressDataModel.firstName)
            put(AddressDataModel.Key_lastName, addressDataModel.lastName)
            put(AddressDataModel.Key_address, addressDataModel.address)
            put(AddressDataModel.Key_phoneNumber, addressDataModel.phoneNumber)
            put(AddressDataModel.Key_zipCode, addressDataModel.zipCode)
            put(AddressDataModel.Key_userUUID, addressDataModel.userUUID)
        }

        db?.update(
            AddressDataModel.TABLE_NAME_ADDRESS,
            addressValue,
            "${AddressDataModel.Key_addressUUID} = ?",
            arrayOf(addressDataModel.addressUUID)
        )
    }

    // Update Contact in the Contact Table
    fun updateContactInTheContactTable(contactDataModel: ContactDataModel) {

        open()

        val contactValue = ContentValues().apply {

            put(ContactDataModel.Key_profileUri, contactDataModel.profileUri)
            put(ContactDataModel.Key_firstName, contactDataModel.firstName)
            put(ContactDataModel.Key_lastName, contactDataModel.lastName)
            put(ContactDataModel.Key_emailAddress, contactDataModel.emailAddress)
            put(ContactDataModel.Key_phoneNumber, contactDataModel.phoneNumber)
            put(ContactDataModel.Key_type, contactDataModel.type)
        }

        db?.update(
            ContactDataModel.TABLE_NAME_CONTACT,
            contactValue,
            "${ContactDataModel.Key_contactUUID} = ?",
            arrayOf(contactDataModel.contactUUID)
        )

    }

    // Get All Contact of particular user from the Contact table
    @SuppressLint("Range")
    fun getAllContactThroughUserUUID(userUUID: String): ArrayList<ContactDataModel> {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${ContactDataModel.TABLE_NAME_CONTACT} WHERE ${ContactDataModel.Key_userUUID} == ? ",
            arrayOf(userUUID)
        )

        cursor?.moveToFirst()

        val contactList = ArrayList<ContactDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val contactData = ContactDataModel()

                contactData.contactId =
                    cursor.getInt(cursor.getColumnIndex(ContactDataModel.Key_contactId))
                contactData.contactUUID =
                    cursor.getString(cursor.getColumnIndex(ContactDataModel.Key_contactUUID))
                contactData.userUUID =
                    cursor.getString(cursor.getColumnIndex(ContactDataModel.Key_userUUID))

                contactData.profileUri =
                    cursor.getString(cursor.getColumnIndex(ContactDataModel.Key_profileUri))

                contactData.firstName =
                    cursor.getString(cursor.getColumnIndex(ContactDataModel.Key_firstName))

                contactData.lastName =
                    cursor.getString(cursor.getColumnIndex(ContactDataModel.Key_lastName))

                contactData.phoneNumber =
                    cursor.getString(cursor.getColumnIndex(ContactDataModel.Key_phoneNumber))

                contactData.emailAddress =
                    cursor.getString(cursor.getColumnIndex(ContactDataModel.Key_emailAddress))

                contactData.type =
                    cursor.getString(cursor.getColumnIndex(ContactDataModel.Key_type))

                contactList.add(contactData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return contactList
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

                addressData.firstName =
                    cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_firstName))

                addressData.lastName =
                    cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_lastName))

                addressData.address =
                    cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_address))

                addressData.phoneNumber =
                    cursor.getString(cursor.getColumnIndex(AddressDataModel.Key_phoneNumber))

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
    fun getCategoryNameThroughCategoryUUID(categoryUUID: String): String? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CategoryDataModel.TABLE_NAME_CATEGORY} WHERE ${CategoryDataModel.Key_categoryUUID} == '$categoryUUID'",
            null
        )

        cursor?.moveToFirst()

        val categoryName =
            cursor?.getString(cursor.getColumnIndex(CategoryDataModel.Key_categoryName))

        cursor?.close()

        return categoryName
    }


    //Getting the MetalType Name through MetalTyp UUID
    @SuppressLint("Recycle", "Range")
    fun getMetalTypeNameThroughMetalTypeUUID(metalTypeUUID: String): String? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${MetalTypeDataModel.TABLE_NAME_METAL_TYPE} WHERE ${MetalTypeDataModel.Key_metalTypeUUID} == ?",
            arrayOf(metalTypeUUID)
        )

        cursor?.moveToFirst()

        val metalTypeName =
            cursor?.getString(cursor.getColumnIndex(MetalTypeDataModel.Key_metalTypeName))


        cursor?.close()

        return metalTypeName
    }

    // Getting the MetalTypeUUID through MetalType name
    @SuppressLint("Recycle", "Range")
    fun getMetalTypeUUIDThroughMetalTypeName(metalTypeName: String): String? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${MetalTypeDataModel.TABLE_NAME_METAL_TYPE} WHERE ${MetalTypeDataModel.Key_metalTypeName} == ?",
            arrayOf(metalTypeName)
        )

        cursor?.moveToFirst()

        val metalTypeUUID =
            cursor?.getString(cursor.getColumnIndex(MetalTypeDataModel.Key_metalTypeUUID))

        cursor?.close()

        return metalTypeUUID
    }

    // Getting the Collection UUID through Collection name
    @SuppressLint("Recycle", "Range")
    fun getCollectionUUIDThroughCollectionName(collectionName: String): String? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CollectionDataModel.TABLE_NAME_COLLECTION} WHERE ${CollectionDataModel.Key_collectionName} == ?",
            arrayOf(collectionName)
        )

        cursor?.moveToFirst()

        val collectionUUID =
            cursor?.getString(cursor.getColumnIndex(CollectionDataModel.Key_collectionUUID))

        cursor?.close()

        return collectionUUID
    }

    //Get All Products of particular Collection  from the Product table
    @SuppressLint("Range")
    suspend fun getProductsThroughCollection(
        collectionUUIDList: List<String>,
        productUUID: String,
        numberOfItems:Int
    ): ArrayList<ProductDataModel> {

        read()

        // Construct the " LIKE" conditions dynamically based on the list size
        val likeConditions = (1..collectionUUIDList.size).joinToString(separator = " OR ") {
            "${ProductDataModel.Key_collectionUUID} LIKE ?"
        }

        val query = "SELECT DISTINCT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_productUUID} != ? AND ${ProductDataModel.Key_productRFIDCode} != ? AND $likeConditions LIMIT $numberOfItems"

        val queryArgs = mutableListOf<String>()

        queryArgs.add(productUUID)  // Add the productUUID as the first argument
        queryArgs.add("")

        // Add the LIKE pattern arguments for collectionUUIDList
        queryArgs.addAll(collectionUUIDList.map { "%$it%" })

        // Execute the query
        val cursor: Cursor? = db?.rawQuery(query, queryArgs.toTypedArray())

        val productList = ArrayList<ProductDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val productData = ProductDataModel()

                productData.productUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))

                productData.productName =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
                productData.metalTypeUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_metalTypeUUID))
                productData.collectionUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_collectionUUID))
                productData.productOrigin =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productOrigin))
                productData.productWeight =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
                productData.productCarat =
                    cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
                productData.productPrice =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
                productData.categoryUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productRFIDCode =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFIDCode))
                productData.productBarcodeUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeUri))
                productData.productBarcodeData =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeData))
                productData.productImageUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImageUri))

                if (productUUID != productData.productUUID){
                    productList.add(productData)
                }

            } while (cursor.moveToPrevious())

            cursor.close()
        }

        close()

        return productList
    }


    //Get All Products of particular Collection  from the Product table
//    @SuppressLint("Range")
//    fun getProductsThroughCollection(
//        collectionUUIDList: List<String>,
//        productUUID: String
//    ): ArrayList<ProductDataModel> {
//
//        read()
//
//        val productList = ArrayList<ProductDataModel>()
//
//        for (collectionUUID in collectionUUIDList) {
//
//            val cursor: Cursor? = db?.rawQuery(
//                "SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_productUUID} != ? AND ${ProductDataModel.Key_collectionUUID} LIKE ?",
//                arrayOf(productUUID,"%$collectionUUID%")
//            )
//
//            if (cursor != null && cursor.count > 0) {
//
//                cursor.moveToLast()
//
//                 product@
//                 do {
//
//                    val productData = ProductDataModel()
//
//                    productData.productUUID =
//                        cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
//
//                    productData.productName =
//                        cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
//                    productData.metalTypeUUID =
//                        cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_metalTypeUUID))
//                    productData.collectionUUID =
//                        cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_collectionUUID))
//                    productData.productOrigin =
//                        cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productOrigin))
//                    productData.productWeight =
//                        cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
//                    productData.productCarat =
//                        cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
//                    productData.productCost =
//                        cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productCost))
//                    productData.categoryUUID =
//                        cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
//                    productData.productDescription =
//                        cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
//                    productData.productRFIDCode =
//                        cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFIDCode))
//                    productData.productBarcodeUri =
//                        cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeUri))
//                    productData.productBarcodeData =
//                        cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeData))
//                    productData.productImageUri =
//                        cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImageUri))
//
//                    for (data in productList) {
//                        if (data.productUUID == productData.productUUID) {
//                            continue@product
//                        }
//                    }
//
//                    productList.add(productData)
//
//                } while (cursor.moveToPrevious())
//
//                cursor.close()
//            }
//            close()
//        }
//
//        return productList
//    }


    //Get All Products of particular category and collection  from the Product table
    @SuppressLint("Range")
    fun getAllProductsThroughCategoryAndCollection(
        categoryUUID: String,
        collectionUUID: String
    ): ArrayList<ProductDataModel> {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_categoryUUID} == ? AND ${ProductDataModel.Key_collectionUUID} LIKE ?",
            arrayOf(categoryUUID,"%$collectionUUID%")
        )

        cursor?.moveToFirst()

        val productList = ArrayList<ProductDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val productData = ProductDataModel()

                productData.productUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
                productData.productName =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
                productData.metalTypeUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_metalTypeUUID))
                productData.collectionUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_collectionUUID))
                productData.productOrigin =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productOrigin))
                productData.productWeight =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
                productData.productCarat =
                    cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
                productData.productPrice =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
                productData.categoryUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productRFIDCode =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFIDCode))
                productData.productBarcodeUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeUri))
                productData.productBarcodeData =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeData))
                productData.productImageUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImageUri))

                productList.add(productData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return productList
    }

    //Get All Products from the Product table Accept Collection
    @SuppressLint("Range", "Recycle")
    fun getAllProductsAcceptCollection(collectionUUIDList: List<String>): ArrayList<ProductDataModel> {

        read()

//        Utils.E()

        // Construct the "NOT LIKE" conditions dynamically based on the list size
        val notLikeConditions = (1..collectionUUIDList.size).joinToString(separator = " AND ") {
            "${ProductDataModel.Key_collectionUUID} NOT LIKE ?"
        }

        // Build the SQL query with the dynamically constructed "NOT LIKE" conditions
        val query = "SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE $notLikeConditions AND ${ProductDataModel.Key_productRFIDCode} != ?"

        val queryArgs = mutableListOf<String>()

        queryArgs.addAll(collectionUUIDList.map { "%$it%" }.toMutableList())
        queryArgs.add("")
//        queryArgs[queryArgs.size] = ""

        // Execute the query
        val cursor: Cursor? = db?.rawQuery(query, queryArgs.toTypedArray())

        val productList = ArrayList<ProductDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {
                val productData = ProductDataModel()

                productData.productUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
                productData.productName =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
                productData.metalTypeUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_metalTypeUUID))
                productData.collectionUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_collectionUUID))
                productData.productOrigin =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productOrigin))
                productData.productWeight =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
                productData.productCarat =
                    cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
                productData.productPrice =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
                productData.categoryUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productRFIDCode =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFIDCode))
                productData.productBarcodeUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeUri))
                productData.productBarcodeData =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeData))
                productData.productImageUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImageUri))

                productList.add(productData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
            close()


        return productList
    }

    // Remove rfid data of product
    fun removeRfidCode(productUUID: String) {

        open()

        val rfidValue = ContentValues().apply {

            put(ProductDataModel.Key_productRFIDCode, "")
        }

        db?.update(
            ProductDataModel.TABLE_NAME_PRODUCT,
            rfidValue,
            "${ProductDataModel.Key_productUUID} == ?",
            arrayOf(productUUID)
        )
    }

    // Get All Products that has RFID from the Product table
    @SuppressLint("Range")
    suspend fun getAllProductsThatHasRFID(): ArrayList<ProductDataModel> {

        read()

        val cursor: Cursor? =
                db?.rawQuery("SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_productRFIDCode} != ?  ", arrayOf(""))

        cursor?.moveToFirst()

        val productList = ArrayList<ProductDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val productData = ProductDataModel()

                productData.productUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
                productData.productName =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
                productData.metalTypeUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_metalTypeUUID))
                productData.collectionUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_collectionUUID))
                productData.productOrigin =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productOrigin))
                productData.productWeight =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
                productData.productCarat =
                    cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
                productData.productPrice =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
                productData.productCost =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productCost))
                productData.categoryUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productRFIDCode =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFIDCode))
                productData.productBarcodeUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeUri))
                productData.productBarcodeData =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeData))
                productData.productImageUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImageUri))

                productList.add(productData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return productList
    }


    // Get All Products from the Product table
    @SuppressLint("Range")
    suspend fun getAllProducts(): ArrayList<ProductDataModel> {

        read()

        val cursor: Cursor? =
            db?.rawQuery("SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} ", null)

        cursor?.moveToFirst()

        val productList = ArrayList<ProductDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val productData = ProductDataModel()

                productData.productUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
                productData.productName =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
                productData.metalTypeUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_metalTypeUUID))
                productData.collectionUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_collectionUUID))
                productData.productOrigin =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productOrigin))
                productData.productWeight =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
                productData.productCarat =
                    cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
                productData.productPrice =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
                productData.productCost =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productCost))
                productData.categoryUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productRFIDCode =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFIDCode))
                productData.productBarcodeUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeUri))
                productData.productBarcodeData =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeData))
                productData.productImageUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImageUri))

                productList.add(productData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return productList
    }

    //Get All Products Accept one Product from product table
    @SuppressLint("Range")
    fun getAllProductsAcceptProduct(productUUID: String): ArrayList<ProductDataModel> {

        read()

        val cursor: Cursor? =
            db?.rawQuery("SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_productUUID} != ? AND ${ProductDataModel.Key_productRFIDCode} != ? ", arrayOf(productUUID,""))

        cursor?.moveToFirst()

        val productList = ArrayList<ProductDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val productData = ProductDataModel()

                productData.productUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
                productData.productName =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
                productData.metalTypeUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_metalTypeUUID))
                productData.collectionUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_collectionUUID))
                productData.productOrigin =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productOrigin))
                productData.productWeight =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
                productData.productCarat =
                    cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
                productData.productPrice =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
                productData.productCost =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productCost))
                productData.categoryUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productRFIDCode =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFIDCode))
                productData.productBarcodeUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeUri))
                productData.productBarcodeData =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeData))
                productData.productImageUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImageUri))

                productList.add(productData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return productList
    }


    /* Get All Products from the Product table Accept the collection */
    @SuppressLint("Range")
    suspend fun getAllProductsAcceptCollection(collectionUUID: String): ArrayList<ProductDataModel> {

        read()

        val cursor: Cursor? =
            db?.rawQuery("SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_collectionUUID} NOT LIKE ?", arrayOf("%$collectionUUID%"))

        cursor?.moveToFirst()

        val productList = ArrayList<ProductDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val productData = ProductDataModel()

                productData.productUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
                productData.productName =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
                productData.metalTypeUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_metalTypeUUID))
                productData.collectionUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_collectionUUID))
                productData.productOrigin =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productOrigin))
                productData.productWeight =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
                productData.productCarat =
                    cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
                productData.productPrice =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
                productData.productCost =
                    cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productCost))
                productData.categoryUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
                productData.productDescription =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
                productData.productRFIDCode =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFIDCode))
                productData.productBarcodeUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeUri))
                productData.productBarcodeData =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeData))
                productData.productImageUri =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImageUri))

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

            productData.productUUID =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
            productData.productName =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
            productData.metalTypeUUID =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_metalTypeUUID))
            productData.collectionUUID =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_collectionUUID))
            productData.productOrigin =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productOrigin))
            productData.productWeight =
                cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
            productData.productCarat =
                cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
            productData.productPrice =
                cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
            productData.productCost =
                cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productCost))
            productData.categoryUUID =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
            productData.productDescription =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
            productData.productRFIDCode =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFIDCode))
            productData.productBarcodeUri =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeUri))

            productData.productBarcodeData =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeData))

            productData.productImageUri =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImageUri))

        }
        cursor?.close()
        close()

        return productData
    }


    // Checks if rFIDCode Exist in the product table
    @SuppressLint("Range", "Recycle")
     fun isRFIDExist(rFIDCode: String): Boolean? {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_productRFIDCode} == ?",
            arrayOf(rFIDCode)
        )

        val result = cursor?.moveToFirst()
        cursor?.close()

        close()

        return result
    }

    // Get all products uuid
    @SuppressLint("Range")
    fun getAllProductUUID():ArrayList<String> {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT ${ProductDataModel.Key_productUUID} FROM ${ProductDataModel.TABLE_NAME_PRODUCT}",
            null
        )

        val productUUIDList = ArrayList<String>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {

                val productUUID =
                    cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))

                productUUIDList.add(productUUID)

            } while (cursor.moveToPrevious())

            cursor.close()
        }

        close()

        return productUUIDList
    }

    // Getting the Product through RFIDCode
    @SuppressLint("Range", "Recycle")
    fun getProductThroughRFIDCode(rFIDCode: String): ProductDataModel {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_productRFIDCode} == ?",
            arrayOf(rFIDCode)
        )

        val productData = ProductDataModel()

        if (cursor != null && cursor.moveToFirst()) {

            productData.productUUID =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productUUID))
            productData.productName =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productName))
            productData.metalTypeUUID =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_metalTypeUUID))
            productData.collectionUUID =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_collectionUUID))
            productData.productOrigin =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productOrigin))
            productData.productWeight =
                cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productWeight))
            productData.productCarat =
                cursor.getInt(cursor.getColumnIndex(ProductDataModel.Key_productCarat))
            productData.productPrice =
                cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productPrice))
            productData.productCost =
                cursor.getDouble(cursor.getColumnIndex(ProductDataModel.Key_productCost))
            productData.categoryUUID =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
            productData.productDescription =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productDescription))
            productData.productRFIDCode =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productRFIDCode))
            productData.productBarcodeUri =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeUri))

            productData.productBarcodeData =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productBarcodeData))

            productData.productImageUri =
                cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_productImageUri))

        }
        cursor?.close()
        close()

        return productData
    }


    // Add Category in the Category table
    fun addCategory(categoryDataModel: CategoryDataModel) {

        open()

        val categoryValue = ContentValues().apply {
            put(CategoryDataModel.Key_categoryUUID, categoryDataModel.categoryUUID)
            put(CategoryDataModel.Key_categoryName, categoryDataModel.categoryName)
        }

        db?.insert(CategoryDataModel.TABLE_NAME_CATEGORY, null, categoryValue)

    }

    // Updating Category in the Category table
    fun updateCategory(categoryDataModel: CategoryDataModel) {

        open()

        val categoryValue = ContentValues().apply {

            put(CategoryDataModel.Key_categoryName, categoryDataModel.categoryName)
        }

        db?.update(
            CategoryDataModel.TABLE_NAME_CATEGORY,
            categoryValue,
            "${CategoryDataModel.Key_categoryUUID} == ?",
            arrayOf(categoryDataModel.categoryUUID)
        )

    }

    // Updating Collection in the Collection table
    fun updateCollection(collectionDataModel: CollectionDataModel) {

        open()

        val collectionValue = ContentValues().apply {

            put(CollectionDataModel.Key_collectionName, collectionDataModel.collectionName)
            put(CollectionDataModel.Key_collectionImageUri, collectionDataModel.collectionImageUri)

        }

        db?.update(
            CollectionDataModel.TABLE_NAME_COLLECTION,
            collectionValue,
            "${CollectionDataModel.Key_collectionUUID} == ?",
            arrayOf(collectionDataModel.collectionUUID)
        )
    }

    // Get All Category of the Particular Collection
    @SuppressLint("Recycle", "Range")
    fun getAllCategoryOfParticularCollection(collectionUUID: String): ArrayList<CategoryDataModel> {
        read()

        val categoryDataList = ArrayList<CategoryDataModel>()

        // Query to fetch category UUIDs for the given collection UUID
        val cursor = db?.rawQuery(
            "SELECT DISTINCT ${ProductDataModel.Key_categoryUUID} FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_collectionUUID} LIKE ?",
            arrayOf("%$collectionUUID%")
        )

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val categoryUUID = cursor.getString(cursor.getColumnIndex(ProductDataModel.Key_categoryUUID))
                // Fetch category details using the getCategoryThroughCategoryUUID function
                val categoryDataModel = getCategoryThroughCategoryUUID(categoryUUID)
                categoryDataList.add(categoryDataModel)
            }
            cursor.close()
        }

        return categoryDataList
    }

    // Check if Collection exist in the product section
    @SuppressLint("Recycle")
    fun isCollectionExistInTheProduct(productUUID: String, collectionUUID: String): Boolean? {

        read()

        val cursor = db?.rawQuery(
            "SELECT  ${ProductDataModel.Key_collectionUUID} FROM ${ProductDataModel.TABLE_NAME_PRODUCT} WHERE ${ProductDataModel.Key_productUUID} == ? AND ${ProductDataModel.Key_collectionUUID} LIKE ?",
            arrayOf(productUUID,"%$collectionUUID%")
        )

        val result = cursor?.moveToFirst()

        cursor?.close()

        return result
    }

    // Delete collection uuid from the product table
    fun deleteCollectionUUIDFromProductTable(collectionUUID: String){
        open()

        val contentValues = ContentValues()
        contentValues.put(ProductDataModel.Key_collectionUUID,"")

        db?.update(ProductDataModel.TABLE_NAME_PRODUCT,contentValues,"${ProductDataModel.Key_collectionUUID} LIKE ?",
            arrayOf("%$collectionUUID%")
        )

    }

    // Delete Category from the Category table
    fun deleteCategory(categoryUUID: String) {

        open()

//        deleteCategoryUUIDFromProductTable(categoryUUID)

        db?.delete(
            CategoryDataModel.TABLE_NAME_CATEGORY, "${CategoryDataModel.Key_categoryUUID} == ?",
            arrayOf(categoryUUID)
        )
    }

    // get row count of order table
    fun getRowCount(): Int {

        read()

        val cursor = db?.rawQuery("SELECT COUNT(*) FROM ${OrderDataModel.TABLE_NAME_ORDER}", null)
        var rowCount = 0

        // Use the cursor to fetch the count
        cursor?.use {
            if (it.moveToFirst()) {
                rowCount = it.getInt(0)
            }
        }

        return rowCount

    }

    // Delete cart from the cart table
    fun deleteCart(userUUID: String) {

        open()

        db?.delete(
            CartDataModel.TABLE_NAME_CART, "${CartDataModel.Key_userUUID} == ?",
            arrayOf(userUUID)
        )
    }

    // Delete Collection from the Collection table
    fun deleteCollection(collectionUUID: String) {

        open()

        db?.delete(
            CollectionDataModel.TABLE_NAME_COLLECTION, "${CollectionDataModel.Key_collectionUUID} == ?",
            arrayOf(collectionUUID)
        )

    }

    // Get all the category form the category table
    @SuppressLint("Range")
    fun getAllCategory(): ArrayList<CategoryDataModel> {

        read()

        val cursor: Cursor? = db?.rawQuery(
            "SELECT * FROM ${CategoryDataModel.TABLE_NAME_CATEGORY}",
            null
        )

        cursor?.moveToFirst()

        val categoryList = ArrayList<CategoryDataModel>()

        if (cursor != null && cursor.count > 0) {

            cursor.moveToLast()

            do {
                val categoryData = CategoryDataModel()

                categoryData.categoryUUID =
                    cursor.getString(cursor.getColumnIndex(CategoryDataModel.Key_categoryUUID))
                categoryData.categoryName =
                    cursor.getString(cursor.getColumnIndex(CategoryDataModel.Key_categoryName))

                categoryList.add(categoryData)

            } while (cursor.moveToPrevious())

            cursor.close()
        }
        close()

        return categoryList

    }


    //add Product in the Product table
    suspend fun addProduct(productDataModel: ProductDataModel) {

        open()

        val productValue = ContentValues().apply {

            put(ProductDataModel.Key_productUUID, productDataModel.productUUID)
            put(ProductDataModel.Key_productName, productDataModel.productName)
            put(ProductDataModel.Key_metalTypeUUID, productDataModel.metalTypeUUID)
            put(ProductDataModel.Key_collectionUUID, productDataModel.collectionUUID)
            put(ProductDataModel.Key_categoryUUID, productDataModel.categoryUUID)
            put(ProductDataModel.Key_productOrigin, productDataModel.productOrigin)
            put(ProductDataModel.Key_productWeight, productDataModel.productWeight)
            put(ProductDataModel.Key_productCarat, productDataModel.productCarat)
            put(ProductDataModel.Key_productPrice, productDataModel.productPrice)
            put(ProductDataModel.Key_productCost, productDataModel.productCost)
            put(ProductDataModel.Key_categoryUUID, productDataModel.categoryUUID)
            put(ProductDataModel.Key_productDescription, productDataModel.productDescription)
            put(ProductDataModel.Key_productRFIDCode, productDataModel.productRFIDCode)
            put(ProductDataModel.Key_productBarcodeUri, productDataModel.productBarcodeUri)
            put(ProductDataModel.Key_productBarcodeData, productDataModel.productBarcodeData)
            put(ProductDataModel.Key_productImageUri, productDataModel.productImageUri)
        }

        db?.insert(ProductDataModel.TABLE_NAME_PRODUCT, null, productValue)

    }

    // update Product in the Product table
    suspend fun updateProduct(productDataModel: ProductDataModel) {

        open()

        val productValue = ContentValues().apply {

            put(ProductDataModel.Key_productName, productDataModel.productName)
            put(ProductDataModel.Key_metalTypeUUID, productDataModel.metalTypeUUID)
            put(ProductDataModel.Key_collectionUUID, productDataModel.collectionUUID)
            put(ProductDataModel.Key_categoryUUID, productDataModel.categoryUUID)
            put(ProductDataModel.Key_productOrigin, productDataModel.productOrigin)
            put(ProductDataModel.Key_productWeight, productDataModel.productWeight)
            put(ProductDataModel.Key_productCarat, productDataModel.productCarat)
            put(ProductDataModel.Key_productPrice, productDataModel.productPrice)
            put(ProductDataModel.Key_productCost, productDataModel.productCost)
            put(ProductDataModel.Key_categoryUUID, productDataModel.categoryUUID)
            put(ProductDataModel.Key_productDescription, productDataModel.productDescription)
            put(ProductDataModel.Key_productRFIDCode, productDataModel.productRFIDCode)
            put(ProductDataModel.Key_productBarcodeUri, productDataModel.productBarcodeUri)
            put(ProductDataModel.Key_productBarcodeData, productDataModel.productBarcodeData)
            put(ProductDataModel.Key_productImageUri, productDataModel.productImageUri)
        }

        db?.update(ProductDataModel.TABLE_NAME_PRODUCT, productValue, "${ProductDataModel.Key_productUUID} == ?",
            arrayOf(productDataModel.productUUID)
        )

    }

    // Check if Category exist in the category table
    @SuppressLint("Recycle")
    fun isCategoryExist(categoryName: String): Boolean? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CategoryDataModel.TABLE_NAME_CATEGORY} WHERE ${CategoryDataModel.Key_categoryName} == '$categoryName'",
            null
        )

        val result = cursor?.moveToFirst()
        cursor?.close()
        return result
    }

    /* Check if Category exist in the category table accept category uuid */
    @SuppressLint("Recycle")
    fun isCategoryExistAccept(categoryDataModel: CategoryDataModel): Boolean? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CategoryDataModel.TABLE_NAME_CATEGORY} WHERE ${CategoryDataModel.Key_categoryUUID} != ? AND ${CategoryDataModel.Key_categoryName} == ?",
            arrayOf(categoryDataModel.categoryUUID,categoryDataModel.categoryName)
        )

        val result = cursor?.moveToFirst()
        cursor?.close()
        return result
    }

    // Check if Collection exist in the category table
    @SuppressLint("Recycle")
    fun isCollectionExist(collectionName:String): Boolean? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CollectionDataModel.TABLE_NAME_COLLECTION} WHERE ${CollectionDataModel.Key_collectionName} == ?",
            arrayOf(collectionName)
        )

        val result = cursor?.moveToFirst()
        cursor?.close()
        return result
    }

    /* Check if Collection exist in the category table accept collectionUUID */
    @SuppressLint("Recycle")
    fun isCollectionExistAccept(collectionDataModel: CollectionDataModel): Boolean? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CollectionDataModel.TABLE_NAME_COLLECTION} WHERE ${CollectionDataModel.Key_collectionUUID} != ? AND ${CollectionDataModel.Key_collectionName} == ?",
            arrayOf(collectionDataModel.collectionUUID,collectionDataModel.collectionName)
        )

        val result = cursor?.moveToFirst()
        cursor?.close()
        return result
    }

    // get collection through Collection UUID
    @SuppressLint("Recycle", "Range")
    fun getCollectionThroughUUID(collectionUUID: String): CollectionDataModel {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CollectionDataModel.TABLE_NAME_COLLECTION} WHERE ${CollectionDataModel.Key_collectionUUID} == ?",
            arrayOf(collectionUUID)
        )

        val collectionDataModel = CollectionDataModel()

        if (cursor != null && cursor.moveToFirst()) {

            collectionDataModel.collectionUUID = cursor.getString(cursor.getColumnIndex(CollectionDataModel.Key_collectionUUID))
            collectionDataModel.collectionName = cursor.getString(cursor.getColumnIndex(CollectionDataModel.Key_collectionName))
            collectionDataModel.collectionImageUri = cursor.getString(cursor.getColumnIndex(CollectionDataModel.Key_collectionImageUri))
        }

        cursor?.close()

        return collectionDataModel
    }

    //Getting the Category UUId through Category Name
    @SuppressLint("Recycle", "Range")
    fun getCategoryUUIDThroughCategoryName(categoryName: String): String? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CategoryDataModel.TABLE_NAME_CATEGORY} WHERE ${CategoryDataModel.Key_categoryName} == '$categoryName'",
            null
        )

        cursor?.moveToFirst()

        val categoryUUID =
            cursor?.getString(cursor.getColumnIndex(CategoryDataModel.Key_categoryUUID))

        cursor?.close()

        return categoryUUID
    }

    // update collection in the product
    fun updateCollectionInProduct(productDataModel: ProductDataModel) {

        val collectionValues = ContentValues().apply {
            put(ProductDataModel.Key_collectionUUID, productDataModel.collectionUUID)
        }

        db?.update(ProductDataModel.TABLE_NAME_PRODUCT, collectionValues,
            "${ProductDataModel.Key_productUUID} = ?",
            arrayOf(productDataModel.productUUID) )

    }

    //Getting the Category through Category UUID
    @SuppressLint("Recycle", "Range")
    fun getCategoryThroughCategoryUUID(categoryUUID: String): CategoryDataModel {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CategoryDataModel.TABLE_NAME_CATEGORY} WHERE ${CategoryDataModel.Key_categoryUUID} == ?",
            arrayOf(categoryUUID)
        )

        val categoryDataModel = CategoryDataModel()

        if (cursor != null && cursor.moveToFirst()) {
            categoryDataModel.categoryUUID = cursor.getString(cursor.getColumnIndex(CategoryDataModel.Key_categoryUUID))
            categoryDataModel.categoryName = cursor.getString(cursor.getColumnIndex(CategoryDataModel.Key_categoryName))
        }

        return categoryDataModel
    }

    //Getting the Category UUId through Category Name
    @SuppressLint("Recycle", "Range")
    fun getMetalTypeThrough(categoryName: String): String? {

        read()

        val cursor = db?.rawQuery(
            "SELECT * FROM ${CategoryDataModel.TABLE_NAME_CATEGORY} WHERE ${CategoryDataModel.Key_categoryName} == '$categoryName'",
            null
        )

        cursor?.moveToFirst()

        val categoryUUID =
            cursor?.getString(cursor.getColumnIndex(CategoryDataModel.Key_categoryUUID))

        cursor?.close()

        return categoryUUID
    }


    //Inserting the Admin in the user table at the time of table creation
    @SuppressLint("UseCompatLoadingForDrawables")
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


        val imageBitmap = cx.getDrawable(R.drawable.default_image)?.toBitmap()

        // generate new order UUID
        Utils.storeOrderUUID(cx, Utils.generateUUId())

        imageBitmap?.let { Utils.saveToInternalStorage(cx, it,Constants.Default_Image) }
        imageBitmap?.let { Utils.saveToInternalStorage(cx, it,Constants.Default_Image) }

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

    //this method checks if any user has this phone number in the User table
    @SuppressLint("Recycle")
    fun isAnyUserHasThisPhoneNumber(phoneNumber: String, userUUID: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_phoneNumber} = '$phoneNumber' And ${UserDataModel.Key_userUUID} != '$userUUID' ",
            null
        )
        val result = cursor.moveToFirst()
        cursor?.close()
        return result
    }

    //this method checks if any user has this email
    @SuppressLint("Recycle")
    fun isAnyUserHasThisEmail(email: String, userUUID: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_email} = '$email' And ${UserDataModel.Key_userUUID} != '$userUUID'",
            null
        )

        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }


    // Checks is Phone Number Already Exist in the user table
    @SuppressLint("Recycle")
    fun isPhoneNumberExist(phoneNumber: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_phoneNumber} = '$phoneNumber' ",
            null
        )

        val result = cursor.moveToFirst()
        cursor?.close()
        return result
    }

    // Checks is Phone Number Already Exist in the Address table accept my phone number
    @SuppressLint("Recycle")
    fun isPhoneNumberExistInAddressTableAcceptMine(
        phoneNumber: String,
        addressUUID: String
    ): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${AddressDataModel.TABLE_NAME_ADDRESS} where ${AddressDataModel.Key_phoneNumber} = '$phoneNumber' And ${AddressDataModel.Key_addressUUID} != '$addressUUID' ",
            null
        )
        val result = cursor.moveToFirst()
        cursor?.close()
        return result
    }

    // Checks is Phone Number Already Exist in the Contact table accept my phone number
    @SuppressLint("Recycle")
    fun isPhoneNumberExistInContactTableAcceptMine(
        phoneNumber: String,
        contactUUID: String
    ): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${ContactDataModel.TABLE_NAME_CONTACT} where ${ContactDataModel.Key_phoneNumber} = ? And ${ContactDataModel.Key_contactUUID} != ?",
            arrayOf(phoneNumber,contactUUID)
        )
        val result = cursor.moveToFirst()
        cursor?.close()
        return result
    }

    // Checks is email Already Exist in the Contact table accept my email
    @SuppressLint("Recycle")
    fun isEmailExistInContactTableAcceptMine(
        email: String,
        contactUUID: String
    ): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${ContactDataModel.TABLE_NAME_CONTACT} where ${ContactDataModel.Key_emailAddress} = ? And ${ContactDataModel.Key_contactUUID} != ?",
            arrayOf(email,contactUUID)
        )
        val result = cursor.moveToFirst()
        cursor?.close()
        return result
    }


    // Checks is Phone Number Already Exist in the Address table
    @SuppressLint("Recycle")
    fun isPhoneNumberExistInAddressTable(phoneNumber: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${AddressDataModel.TABLE_NAME_ADDRESS} where ${AddressDataModel.Key_phoneNumber} = '$phoneNumber' ",
            null
        )

        val result = cursor.moveToFirst()
        cursor?.close()
        return result
    }

    // Checks is Phone Number Already Exist in the Contact table
    @SuppressLint("Recycle")
    fun isPhoneNumberExistInContactTable(phoneNumber: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${ContactDataModel.TABLE_NAME_CONTACT} where ${ContactDataModel.Key_phoneNumber} = ? ",
            arrayOf(phoneNumber)
        )

        val result = cursor.moveToFirst()
        cursor?.close()
        return result
    }

    // Checks is Email Already Exist in the Contact table
    @SuppressLint("Recycle")
    fun isEmailExistInContactTable(email: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${ContactDataModel.TABLE_NAME_CONTACT} where ${ContactDataModel.Key_emailAddress} = ? ",
            arrayOf(email)
        )

        val result = cursor.moveToFirst()
        cursor?.close()
        return result
    }

    // Checks is Email Already Exist in the user table
    @SuppressLint("Recycle")
    fun isEmailExist(email: String): Boolean {

        read()
        val cursor = db!!.rawQuery(
            "SELECT * From ${UserDataModel.TABLE_NAME_USER} where ${UserDataModel.Key_email} = '$email' ",
            null
        )

        val result = cursor.moveToFirst()
        cursor?.close()
        return result
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
        @SuppressLint("Recycle") val cursor = db!!.rawQuery(
            "select * from " + UserDataModel.TABLE_NAME_USER, null
        )
        val result = !cursor.moveToFirst()
        cursor.close()

        return result
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
        cursor.close()
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