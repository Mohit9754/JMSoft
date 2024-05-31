package com.jmsoft.Utility.UtilityTools

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.jmsoft.R
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.Utility.Database.CollectionDataModel
import com.jmsoft.Utility.Database.MetalTypeDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.main.enum.ProductColumnName
import com.jmsoft.main.`interface`.ExcelReadSuccess
import org.apache.poi.EncryptedDocumentException
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.OfficeXmlFileException
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.collections.ArrayList

class ExcelReader(private val excelReadSuccess: ExcelReadSuccess) {

    private val productList = ArrayList<ProductDataModel>()
    lateinit var workbook: Workbook
    lateinit var file: File
    lateinit var fileDir: File

    private fun isInEnum(name: String): Boolean {

        return try {
            enumValueOf<ProductColumnName>(name)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    @SuppressLint("DefaultLocale")
    fun readExcelFileFromAssets(
        context: Context,
        filePath: String
    ) {

        productList.clear()
        file = File(filePath)
        try {
            if (!file.exists() || file.length() == 0L) {

                Utils.T(context, "Invalid File")
            }
            if (file.length() > Int.MAX_VALUE) {

                Utils.T(context, "File too big")
                excelReadSuccess.onReadFail()

//                excelExceptionListData.postValue("File too big")
            }

            val myInput = FileInputStream(file)
            val firstRow: MutableList<String> = arrayListOf()

            if (file.name.endsWith("xlsx")) {
                workbook = XSSFWorkbook(myInput)
            } else {
                workbook = HSSFWorkbook(myInput)
            }

            workbook = addColumnIfNotAdded(workbook)
            val mySheet = workbook.getSheetAt(0)
            val rowIter: Iterator<Row> = mySheet.iterator()

            while (rowIter.hasNext()) {
                val row: Row = rowIter.next()
                val cellIter1: Iterator<Cell> = row.cellIterator()

                if (row.rowNum == 0) {

                    while (cellIter1.hasNext()) {

                        val firstCell: Cell = cellIter1.next()

                        val columnName = firstCell.toString().trim().lowercase().replace(" ","")

                        firstRow.add(columnName)

                        if (!isInEnum(columnName) && columnName != "status") {

                            Utils.T(context, "Column name $columnName does not match")

                            excelReadSuccess.onReadFail()

                            return
                        }
                    }

                    if (firstRow.contains(ProductColumnName.productname.toString())) {

                        if (firstRow.contains(ProductColumnName.categorynam.toString())){

                            if (firstRow.contains(ProductColumnName.metaltype.toString())) {

                                if (firstRow.contains(ProductColumnName.description.toString())) {

                                    if (firstRow.contains(ProductColumnName.origin.toString())) {

                                        if (firstRow.contains(ProductColumnName.weight.toString())) {

                                            if (firstRow.contains(ProductColumnName.carat.toString())) {

                                                if (firstRow.contains(ProductColumnName.price.toString())) {

                                                    if (firstRow.contains(ProductColumnName.cost.toString())) {

                                                        if (firstRow.contains(ProductColumnName.barcode.toString())) {

                                                        }
                                                        else {

                                                            Utils.T(context,
                                                                context.getString(
                                                                    R.string.column_does_not_exist,
                                                                    ProductColumnName.barcode
                                                                ))

                                                            excelReadSuccess.onReadFail()
                                                            return

                                                        }

                                                    }
                                                    else {

                                                        Utils.T(context,
                                                            context.getString(
                                                                R.string.column_does_not_exist,
                                                                ProductColumnName.cost
                                                            ))

                                                        excelReadSuccess.onReadFail()
                                                        return

                                                    }

                                                }
                                                else {

                                                    Utils.T(context,
                                                        context.getString(
                                                            R.string.column_does_not_exist,
                                                            ProductColumnName.price
                                                        ))

                                                    excelReadSuccess.onReadFail()
                                                    return

                                                }

                                            }
                                            else {

                                                Utils.T(context,
                                                    context.getString(
                                                        R.string.column_does_not_exist,
                                                        ProductColumnName.carat
                                                    ))

                                                excelReadSuccess.onReadFail()
                                                return

                                            }
                                        }
                                        else {

                                            Utils.T(context,
                                                context.getString(
                                                    R.string.column_does_not_exist,
                                                    ProductColumnName.weight
                                                ))

                                            excelReadSuccess.onReadFail()
                                            return

                                        }
                                    }
                                    else {

                                        Utils.T(context,
                                            context.getString(
                                                R.string.column_does_not_exist,
                                                ProductColumnName.origin
                                            ))

                                        excelReadSuccess.onReadFail()
                                        return

                                    }
                                }
                                else {

                                    Utils.T(context,
                                        context.getString(
                                            R.string.column_does_not_exist,
                                            ProductColumnName.description
                                        ))

                                    excelReadSuccess.onReadFail()
                                    return

                                }

                            }
                            else {

                                Utils.T(context,
                                    context.getString(
                                        R.string.column_does_not_exist,
                                        ProductColumnName.metaltype
                                    ))

                                excelReadSuccess.onReadFail()
                                return
                            }
                        }
                        else {

                            Utils.T(context,
                                context.getString(
                                    R.string.column_does_not_exist,
                                    ProductColumnName.categorynam
                                ))

                            excelReadSuccess.onReadFail()
                            return

                         }
                    }

                    else {

                        Utils.T(context,
                            context.getString(
                                R.string.column_does_not_exist,
                                ProductColumnName.productname
                            ))
                        excelReadSuccess.onReadFail()
                        return

                    }

                }

                val cellIter: Iterator<Cell> = row.cellIterator()

                if (row.rowNum > 0) {

                    val productDataModel = ProductDataModel()

                    while (cellIter.hasNext()) {

                        for (i in firstRow) {

                            if (cellIter.hasNext()) {

                                val cell: Cell = cellIter.next()


                                when (i) {

                                    ProductColumnName.productname.toString() -> {

                                        productDataModel.productName = cell.toString().trim()

                                    }

                                    ProductColumnName.metaltype.toString() -> {

                                        val isMetalTypeExist =
                                            Utils.isMetalTypeExist(Utils.capitalizeData(cell.toString()))

                                        if (isMetalTypeExist == true) {

                                            val metalTypeUUID =
                                                Utils.getMetalTypeUUIDThroughMetalTypeName(
                                                    Utils.capitalizeData(cell.toString())
                                                )

                                            productDataModel.metalTypeUUID = metalTypeUUID

                                        } else {

                                            val metalTypeDataModel = MetalTypeDataModel()
                                            val metalTypeUUID = Utils.generateUUId()
                                            metalTypeDataModel.metalTypeUUID = metalTypeUUID
                                            metalTypeDataModel.metalTypeName =
                                                Utils.capitalizeData(cell.toString())

                                            Utils.addMetalTypeInTheMetalTypeTable(metalTypeDataModel)

                                            productDataModel.metalTypeUUID = metalTypeUUID

                                        }
                                    }

                                    ProductColumnName.collectionname.toString() -> {

                                        val isCollectionExist =
                                            Utils.isCollectionExist(Utils.capitalizeData(cell.toString()))

                                        if (isCollectionExist == true) {

                                            val collectionUUID =
                                                Utils.getCollectionUUIDThroughCollectionName(
                                                    Utils.capitalizeData(cell.toString())
                                                )
                                            productDataModel.collectionUUID = collectionUUID

                                        } else {

                                            val collectionDataModel = CollectionDataModel()
                                            val collectionUUID = Utils.generateUUId()
                                            collectionDataModel.collectionUUID = collectionUUID
                                            collectionDataModel.collectionName =
                                                Utils.capitalizeData(cell.toString())
                                            collectionDataModel.collectionImageUri =
                                                Constants.Default_Image

                                            Utils.addCollection(collectionDataModel)

                                            productDataModel.collectionUUID = collectionUUID

                                        }
                                    }

                                    ProductColumnName.origin.toString() -> {

                                        productDataModel.productOrigin = cell.toString().trim()

                                    }

                                    ProductColumnName.weight.toString() -> {

                                        productDataModel.productWeight =
                                            Utils.roundToTwoDecimalPlaces(
                                                cell.toString().toDouble()
                                            )

                                    }

                                    ProductColumnName.carat.toString() -> {

                                        try {
                                            productDataModel.productCarat = cell.toString().toDouble().toInt()

                                        } catch (e: Exception) {

                                            Utils.T(context,
                                                context.getString(R.string.invalid_carat_type))
                                            excelReadSuccess.onReadFail()
                                            return
                                        }

                                    }

                                    ProductColumnName.price.toString() -> {

                                        try {
                                            productDataModel.productPrice =
                                                Utils.roundToTwoDecimalPlaces(
                                                    cell.toString().toDouble()
                                                )

                                        } catch (e: Exception) {

                                            Utils.T(context,
                                                context.getString(R.string.invalid_product_price))
                                            excelReadSuccess.onReadFail()
                                            return
                                        }
                                    }

                                    ProductColumnName.cost.toString() -> {

                                        try {

                                            productDataModel.productCost =
                                                Utils.roundToTwoDecimalPlaces(
                                                    cell.toString().toDouble()
                                                )

                                        } catch (e: Exception) {

                                            Utils.T(context,
                                                context.getString(R.string.invalid_product_cost))
                                            excelReadSuccess.onReadFail()
                                            return
                                        }
                                    }

                                    ProductColumnName.categorynam.toString() -> {

                                        val isCategoryExist =
                                            Utils.isCategoryExist(Utils.capitalizeData(cell.toString()))


                                        if (isCategoryExist == true) {

                                            val categoryUUID =
                                                Utils.getCategoryUUIDThroughCategoryName(
                                                    Utils.capitalizeData(cell.toString())
                                                )

                                            productDataModel.categoryUUID = categoryUUID

                                        } else {

                                            val categoryDataModel = CategoryDataModel()
                                            val categoryUUID = Utils.generateUUId()
                                            categoryDataModel.categoryUUID = categoryUUID
                                            categoryDataModel.categoryName =
                                                Utils.capitalizeData(cell.toString())

                                            Utils.addCategory(categoryDataModel)

                                            productDataModel.categoryUUID = categoryUUID

                                        }
                                    }

                                    ProductColumnName.description.toString() -> {

                                        productDataModel.productDescription = cell.toString().trim()

                                    }

                                    ProductColumnName.barcode.toString() -> {

                                        val barcodeBitmap =
                                            Utils.genBarcodeBitmap(context, cell.toString().trim())

                                        if (barcodeBitmap != null) {

                                            val barcodeImageUri = Utils.generateUUId()

                                            Utils.saveToInternalStorage(
                                                context,
                                                barcodeBitmap,
                                                barcodeImageUri
                                            )

                                            productDataModel.productBarcodeData =
                                                cell.toString().trim()
                                            productDataModel.productBarcodeUri = barcodeImageUri

                                        }
                                    }

                                    else -> {

                                        if (i != "status") {
                                            Utils.T(context, "No Column match $i")
                                            excelReadSuccess.onReadFail()
                                            return

                                        }
                                    }
                                }

                                productDataModel.productImageUri = "${Constants.Default_Image},${Constants.Default_Image}"
                                productDataModel.productRFIDCode = ""
                                productDataModel.productUUID = Utils.generateUUId()

                            }
                        }

                        productList.add(productDataModel)

                    }
                }
            }

            excelReadSuccess.onReadSuccess(productList)

        } catch (e: Exception) {
            e.printStackTrace()

        Utils.T(context, context.getString(R.string.exception_in_reading))

//            excelExceptionListData.postValue(e.message.orEmpty())
        }
    }

    private fun addColumnIfNotAdded(workBook: Workbook): Workbook {

        val sheet = workBook.getSheetAt(0)
        val rowIterator: Iterator<Row> = sheet.iterator()
        while (rowIterator.hasNext()) {
            val row: Row = rowIterator.next()
            Log.e("ApachPOI row count : ", row.count().toString())
            val cellIterator: Iterator<Cell> = row.cellIterator()
            while (cellIterator.hasNext()) {
                val column: Cell = cellIterator.next()
                if (!cellIterator.hasNext()) {
                    if (column.cellType == Cell.CELL_TYPE_STRING) {
                        if (column.stringCellValue.equals(Constants.Status) ||
                            column.stringCellValue.equals(Constants.Completed)
                        ) {
                            if (column.stringCellValue.equals(Constants.Completed)) {
                                val style = workBook.createCellStyle()
                                style.fillBackgroundColor = IndexedColors.YELLOW.index
                                style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
                                row.rowStyle = style
                                Log.e("Already completed", "so setting green")
                            }
                        } else {
                            val cell = row.createCell(row.lastCellNum + 1)
                            cell.setCellValue(Constants.Status)
                            Log.e("column.stringCellValue ", "else case")
                        }
                    } else {
                        val cell = row.createCell(row.lastCellNum + 1)
                        cell.setCellValue(Constants.Status)
                        Log.e("column.cellType ", "else case")
                    }
                }
            }
        }
        return workBook
    }

    fun isEncrypt(filepath: String): Boolean {

        val file = File(filepath)
        val myInput = FileInputStream(file)
        val workbook: Workbook

        try {
            if (file.name.contains("xlsx")) {
                return try {
                    try {
                        POIFSFileSystem(myInput)
                    } catch (_: IOException) {
                    }
                    true
                } catch (e: OfficeXmlFileException) {
                    false
                }
            } else {
                workbook = HSSFWorkbook(myInput)
            }
        } catch (e: EncryptedDocumentException) {
            return true
        }
        return false
    }

    fun checkPassword(password: String, path: String): Boolean {
        try {
            val file = File(path)
            val wb = WorkbookFactory.create(
                file,
                password
            )
        } catch (e: EncryptedDocumentException) {
            return false
        }
        return true
    }

}
