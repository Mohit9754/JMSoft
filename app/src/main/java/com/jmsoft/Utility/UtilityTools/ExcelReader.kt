package com.jmsoft.Utility.UtilityTools

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.jmsoft.R
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.Utility.Database.CollectionDataModel
import com.jmsoft.Utility.Database.MetalTypeDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.Database.StockLocationDataModel
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

//    private fun isInEnum(name: String): Boolean {
//
//        return try {
//            enumValueOf<ProductColumnName>(name)
//            true
//        } catch (e: IllegalArgumentException) {
//            false
//        }
//    }

    private fun isInEnum(displayName: String): Boolean {
        return ProductColumnName.values().any { it.displayName == displayName }
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
                excelReadSuccess.onReadFail()
                return
            }

            if (file.length() > Int.MAX_VALUE) {
                Utils.T(context, "File is too big")
                excelReadSuccess.onReadFail()
                return
            }

            val myInput = FileInputStream(file)
            val firstRow: MutableList<String> = arrayListOf()
            val workbook = if (file.name.endsWith("xlsx")) {
                XSSFWorkbook(myInput)
            } else {
                HSSFWorkbook(myInput)
            }

            val mySheet = workbook.getSheetAt(0)
            val numberOfRows = mySheet.physicalNumberOfRows

            for (rowIndex in 0 until numberOfRows) {
                val row = mySheet.getRow(rowIndex) ?: continue

                if (rowIndex == 0) {

                    // Process the header row
                    for (cell in row) {
                        val columnName = cell.toString().trim().lowercase().replace(" ", "")
                        firstRow.add(columnName)

                        // Validate column names
                        if (!isInEnum(columnName)) {

                            Utils.T(context, "Column name $columnName does not match")
                            excelReadSuccess.onReadFail()
                            return
                        }
                    }

                    if (firstRow.contains(ProductColumnName.PRODUCT_NAME.displayName)) {

                        if (firstRow.contains(ProductColumnName.CATEGORY_NAME.displayName)) {

                            if (firstRow.contains(ProductColumnName.METAL_TYPE.displayName)) {

                                if (firstRow.contains(ProductColumnName.DESCRIPTION.displayName)) {

                                    if (firstRow.contains(ProductColumnName.ORIGIN.displayName)) {

                                        if (firstRow.contains(ProductColumnName.WEIGHT.displayName)) {

                                            if (firstRow.contains(ProductColumnName.CARAT.displayName)) {

                                                if (firstRow.contains(ProductColumnName.PRICE.displayName)) {

                                                    if (firstRow.contains(ProductColumnName.DESCRIPTION.displayName)) {

                                                        if (firstRow.contains(ProductColumnName.BARCODE.displayName)) {

                                                            if (firstRow.contains(ProductColumnName.NAME.displayName)) {

                                                                if (firstRow.contains(
                                                                        ProductColumnName.PARENT.displayName
                                                                    )
                                                                ) {


                                                                } else {

                                                                    Utils.T(
                                                                        context,
                                                                        context.getString(
                                                                            R.string.column_does_not_exist,
                                                                            ProductColumnName.PARENT.displayName
                                                                        )
                                                                    )

                                                                    excelReadSuccess.onReadFail()
                                                                    return

                                                                }

                                                            } else {

                                                                Utils.T(
                                                                    context,
                                                                    context.getString(
                                                                        R.string.column_does_not_exist,
                                                                        ProductColumnName.NAME.displayName
                                                                    )
                                                                )

                                                                excelReadSuccess.onReadFail()
                                                                return

                                                            }

                                                        } else {

                                                            Utils.T(
                                                                context,
                                                                context.getString(
                                                                    R.string.column_does_not_exist,
                                                                    ProductColumnName.BARCODE.displayName
                                                                )
                                                            )

                                                            excelReadSuccess.onReadFail()
                                                            return

                                                        }

                                                    } else {

                                                        Utils.T(
                                                            context,
                                                            context.getString(
                                                                R.string.column_does_not_exist,
                                                                ProductColumnName.COST.displayName
                                                            )
                                                        )

                                                        excelReadSuccess.onReadFail()
                                                        return

                                                    }

                                                } else {

                                                    Utils.T(
                                                        context,
                                                        context.getString(
                                                            R.string.column_does_not_exist,
                                                            ProductColumnName.PRICE.displayName
                                                        )
                                                    )

                                                    excelReadSuccess.onReadFail()
                                                    return

                                                }

                                            } else {

                                                Utils.T(
                                                    context,
                                                    context.getString(
                                                        R.string.column_does_not_exist,
                                                        ProductColumnName.CARAT.displayName
                                                    )
                                                )

                                                excelReadSuccess.onReadFail()
                                                return

                                            }
                                        } else {

                                            Utils.T(
                                                context,
                                                context.getString(
                                                    R.string.column_does_not_exist,
                                                    ProductColumnName.WEIGHT.displayName
                                                )
                                            )

                                            excelReadSuccess.onReadFail()
                                            return

                                        }
                                    } else {

                                        Utils.T(
                                            context,
                                            context.getString(
                                                R.string.column_does_not_exist,
                                                ProductColumnName.ORIGIN.displayName
                                            )
                                        )

                                        excelReadSuccess.onReadFail()
                                        return

                                    }
                                } else {

                                    Utils.T(
                                        context,
                                        context.getString(
                                            R.string.column_does_not_exist,
                                            ProductColumnName.DESCRIPTION.displayName
                                        )
                                    )

                                    excelReadSuccess.onReadFail()
                                    return

                                }

                            } else {

                                Utils.T(
                                    context,
                                    context.getString(
                                        R.string.column_does_not_exist,
                                        ProductColumnName.METAL_TYPE.displayName
                                    )
                                )

                                excelReadSuccess.onReadFail()
                                return
                            }
                        } else {

                            Utils.T(
                                context,
                                context.getString(
                                    R.string.column_does_not_exist,
                                    ProductColumnName.CATEGORY_NAME.displayName
                                )
                            )

                            excelReadSuccess.onReadFail()
                            return

                        }
                    } else {

                        Utils.T(
                            context,
                            context.getString(
                                R.string.column_does_not_exist,
                                ProductColumnName.PRODUCT_NAME.displayName
                            )
                        )
                        excelReadSuccess.onReadFail()
                        return

                    }


                } else {

                    // Process the data rows
                    val productDataModel = ProductDataModel()
                    var rowHasData = false
                    var parentUUID = Utils.generateUUId()
                    var stockLocationName:String = ""

                    for ((index, cell) in row.withIndex()) {

                        val columnName = firstRow.getOrNull(index) ?: continue
                        val cellValue = cell.toString().trim()

                        if (cellValue.isNotEmpty()) {
                            rowHasData = true
                        } else {
                            continue
                        }

                        when (columnName) {

                            ProductColumnName.PRODUCT_NAME.displayName -> {

                                productDataModel.productName = cellValue

                            }

                            ProductColumnName.NAME.displayName -> {

                                stockLocationName = cellValue

                            }

                            ProductColumnName.METAL_TYPE.displayName -> {

                                val isMetalTypeExist =
                                    Utils.isMetalTypeExist(Utils.capitalizeData(cellValue))

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
                                        Utils.capitalizeData(cellValue)

                                    Utils.addMetalTypeInTheMetalTypeTable(metalTypeDataModel)

                                    productDataModel.metalTypeUUID = metalTypeUUID

                                }
                            }

                            ProductColumnName.PARENT.displayName -> {

                                val isParentExist = Utils.isParentExist(Utils.capitalizeData(cellValue))

                                if (isParentExist == true) {

                                    parentUUID =
                                        Utils.getParentUUIDThroughParentName(
                                            Utils.capitalizeData(cellValue)
                                        ).toString()

                                } else {

                                    val stockLocationDataModel = StockLocationDataModel()

                                    stockLocationDataModel.stockLocationUUID = parentUUID

                                    stockLocationDataModel.stockLocationName =
                                        Utils.capitalizeData(cell.toString())

                                    stockLocationDataModel.stockLocationParentUUID = ""

                                    Utils.addStockLocation(stockLocationDataModel)

                                }

                            }

                            ProductColumnName.COLLECTION_NAME.displayName -> {

                                val isCollectionExist =
                                    Utils.isCollectionExist(Utils.capitalizeData(cellValue))

                                if (isCollectionExist == true) {

                                    val collectionUUID =
                                        Utils.getCollectionUUIDThroughCollectionName(
                                            Utils.capitalizeData(cellValue)
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

                            ProductColumnName.ORIGIN.displayName -> {

                                productDataModel.productOrigin = cellValue

                            }

                            ProductColumnName.WEIGHT.displayName -> {

                                try {

                                    productDataModel.productWeight =
                                        Utils.roundToTwoDecimalPlaces(
                                            cellValue.toDouble()
                                        )

                                } catch (e: Exception) {

                                    Utils.T(
                                        context,
                                        context.getString(R.string.invalid_weight_type)
                                    )
                                    excelReadSuccess.onReadFail()
                                    return

                                }

                            }

                            ProductColumnName.CARAT.displayName -> {

                                try {

                                    productDataModel.productCarat = cellValue.toDouble().toInt()

                                } catch (e: Exception) {

                                    Utils.T(
                                        context,
                                        context.getString(R.string.invalid_carat_type)
                                    )
                                    excelReadSuccess.onReadFail()
                                    return
                                }

                            }

                            ProductColumnName.PRICE.displayName -> {

                                try {
                                    productDataModel.productPrice =
                                        Utils.roundToTwoDecimalPlaces(
                                            Utils.removeThousandSeparators(cellValue)
                                                .toDouble()
                                        )

                                } catch (e: Exception) {

                                    Utils.T(
                                        context,
                                        context.getString(R.string.invalid_product_price)
                                    )
                                    excelReadSuccess.onReadFail()
                                    return
                                }
                            }

                            ProductColumnName.COST.displayName -> {

                                try {

                                    productDataModel.productCost =
                                        Utils.roundToTwoDecimalPlaces(
                                            Utils.removeThousandSeparators(cellValue)
                                                .toDouble()
                                        )

                                } catch (e: Exception) {

                                    Utils.T(
                                        context,
                                        context.getString(R.string.invalid_product_cost)
                                    )
                                    excelReadSuccess.onReadFail()
                                    return
                                }
                            }

                            ProductColumnName.CATEGORY_NAME.displayName -> {

                                val isCategoryExist =
                                    Utils.isCategoryExist(Utils.capitalizeData(cellValue))


                                if (isCategoryExist == true) {

                                    val categoryUUID =
                                        Utils.getCategoryUUIDThroughCategoryName(
                                            Utils.capitalizeData(cellValue)
                                        )

                                    productDataModel.categoryUUID = categoryUUID

                                } else {

                                    val categoryDataModel = CategoryDataModel()
                                    val categoryUUID = Utils.generateUUId()
                                    categoryDataModel.categoryUUID = categoryUUID
                                    categoryDataModel.categoryName =
                                        Utils.capitalizeData(cellValue)

                                    Utils.addCategory(categoryDataModel)

                                    productDataModel.categoryUUID = categoryUUID

                                }
                            }

                            ProductColumnName.DESCRIPTION.displayName -> {

                                productDataModel.productDescription = cellValue

                            }

                            ProductColumnName.BARCODE.displayName -> {

                                val barcodeBitmap =
                                    Utils.genBarcodeBitmap(context, cellValue)

                                if (barcodeBitmap != null) {

                                    val barcodeImageUri = Utils.generateUUId()

                                    Utils.saveToInternalStorage(
                                        context,
                                        barcodeBitmap,
                                        barcodeImageUri
                                    )

                                    productDataModel.productBarcodeData =
                                        cellValue
                                    productDataModel.productBarcodeUri = barcodeImageUri

                                }

                            }

                            else -> {

                                Utils.T(context, "No Column match $columnName")
                                excelReadSuccess.onReadFail()
                                return

                            }
                        }
                    }


                    if (rowHasData) {

                        val isStockLocationExist = Utils.isStockLocationExist(Utils.capitalizeData(stockLocationName),parentUUID)

                        if (isStockLocationExist) {

                            val stockLocationUUID = Utils.getStockLocationUUID(Utils.capitalizeData(stockLocationName),parentUUID)

                            productDataModel.stockLocationUUID = stockLocationUUID
                        }
                        else {

                            val stockLocationUUID = Utils.generateUUId()

                            val stockLocationDataModel = StockLocationDataModel()
                            stockLocationDataModel.stockLocationUUID = stockLocationUUID
                            stockLocationDataModel.stockLocationName = Utils.capitalizeData(stockLocationName)
                            stockLocationDataModel.stockLocationParentUUID = parentUUID

                            Utils.addStockLocation(stockLocationDataModel)

                            productDataModel.stockLocationUUID = stockLocationUUID

                        }

                        productDataModel.productImageUri =
                            "${Constants.Default_Image},${Constants.Default_Image}"
                        productDataModel.productRFIDCode = ""
                        productDataModel.productUUID = Utils.generateUUId()
                        productList.add(productDataModel)
                    }

                }
            }

            excelReadSuccess.onReadSuccess(productList)

        } catch (e: Exception) {

            // Get the stack trace elements
            val stackTrace = e.stackTrace

            if (stackTrace.isNotEmpty()) {
                // Get the first stack trace element
                val element = stackTrace[0]
                // Print the line number

                Utils.E("Exception occurred at line number: ${element.lineNumber}")
                Utils.E("Exception occurred at line number: ${e.message}")
                Utils.E("Exception occurred at line number: ${e::class.simpleName}")

            } else {
                Utils.E("No stack trace available.")
            }

            Utils.T(context, context.getString(R.string.exception_in_reading))
            excelReadSuccess.onReadFail()

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
