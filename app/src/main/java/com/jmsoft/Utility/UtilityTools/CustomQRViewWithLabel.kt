package com.jmsoft.Utility.UtilityTools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

class CustomQRViewWithLabel @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{

    private var qrBitmap: Bitmap? = null

    // Paint for drawing the label's background
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    // Paint for drawing the border
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK // Color of the border
        style = Paint.Style.STROKE
        strokeWidth = 4f // Width of the border line
    }

    // Paint for drawing text
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.LEFT
    }

    // Paint for drawing text
    private val textPaintJM = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 32f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.LEFT
    }

    // Properties for the text content
    var barcodeData: String = "JM431"
    var weightText: String = "W 102.55g"
    var dimensionText: String = "D 3.45g"
    var priceText: String = "P 50,00DH"

    /*  // Bitmap for the image
      private val imageBitmap: Bitmap = BitmapFactory.decodeResource(
          context.resources,
          R.drawable.qr_code_sample
      ) // Replace with your image
  */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dimensions and radius for the main rounded rectangle
        val rectWidth = 620f  // Width of the main rectangle
        val rectHeight = 265f // Height of the main rectangle
        val cornerRadius = 0f // Radius for the rounded corners

        // Create a Path to draw the custom shape
        val path = Path()

        // Start from the top-left corner of the main rectangle
        path.moveTo(50f + cornerRadius, 50f)

        // Top side with top-right rounded corner
        path.lineTo(50f + rectWidth - cornerRadius, 50f)
        path.quadTo(50f + rectWidth, 50f, 50f + rectWidth, 50f + cornerRadius)

        // Right side of the main rectangle
        path.lineTo(50f + rectWidth, 50f + rectHeight - cornerRadius)
        path.quadTo(50f + rectWidth, 50f + rectHeight, 50f + rectWidth - cornerRadius, 50f + rectHeight)


        // Continue the bottom side with bottom-left rounded corner
        path.lineTo(50f + cornerRadius, 50f + rectHeight)
        path.quadTo(50f, 50f + rectHeight, 50f, 50f + rectHeight - cornerRadius)

        // Left side of the main rectangle
        path.lineTo(50f, 50f + cornerRadius)
        path.quadTo(50f, 50f, 50f + cornerRadius, 50f)

        // Draw the shape background
        canvas.drawPath(path, paint)

        // Draw the border on top of the shape
        canvas.drawPath(path, borderPaint)

        // Draw the rotated text inside the main rectangle
        canvas.save()
        canvas.rotate(
            90f,
            118f + rectWidth / 9,
            118f + rectHeight / 2
        ) // Rotate around the center of the main rectangle
        canvas.drawText(
            barcodeData,
            380f / 3,
            (-180f) ,
            textPaintJM
        ) // Draw text at the center
        canvas.restore()

        // Draw the QR code if available
        qrBitmap?.let {
            // Scale the QR code to desired width and height
            val scaleWidth = 110f // Set the desired width for the QR code
            val scaleHeight = 120f // Set the desired height for the QR code
            val matrix = Matrix()
            matrix.postScale(scaleWidth / it.width, scaleHeight / it.height)

            // Create the scaled bitmap
            val scaledQRCode = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, false)

            // Position of the QR code inside the rectangle
            val qrX = 430f + rectWidth / 5 - scaledQRCode.width / 2
            val qrY = 103f + rectHeight / 2 - scaledQRCode.height / 2

            // Draw the scaled QR code on canvas inside the rectangle
            canvas.drawBitmap(scaledQRCode, qrX, qrY, null)
        }

        // Draw text at the *top end* of the rectangle
        val textX = 500f // Adjusted to align horizontally near the left edge
        val textY = 100f // Slightly inside the rectangle but close to the top
        canvas.drawText(weightText, textX, textY, textPaint)
        canvas.drawText(priceText, textX, textY + 35f, textPaint)
        canvas.drawText(dimensionText, textX, textY + 67f, textPaint)
    }


    // Methods to update the text content
    fun updateWeightText(text: String) {
        weightText = text
        invalidate() // Request a redraw
    }

    fun updateDimensionText(text: String) {
        dimensionText = text
        invalidate()
    }

    fun updatePriceText(text: String) {
        priceText = text
        invalidate()
    }

    fun barcodeDataText(text: String) {
        barcodeData = text
        invalidate()
    }

    // Set the QR code bitmap
    fun setQRCodeBitmap(bitmap: Bitmap) {
        this.qrBitmap = bitmap
        invalidate()
    }
}
