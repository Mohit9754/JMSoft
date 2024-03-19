package com.jmsoft.basic.UtilityTools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

object AppHelper {
    fun getFileDataFromDrawable(drawable: Drawable): ByteArray {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun getFileDataFromDrawablePNG(drawable: Drawable): ByteArray {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun getFileDataFromBitmap(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun getBitmapFromVectorDrawable(context: Context?, drawableId: Int): ByteArray {
        val drawable = ContextCompat.getDrawable(context!!, drawableId)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun getFileDataFromDrawable(context: Context, uri: Uri?): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            val iStream = context.contentResolver.openInputStream(uri!!)
            val bufferSize = 2048
            val buffer = ByteArray(bufferSize)
            var len = 0
            if (iStream != null) {
                while (iStream.read(buffer).also { len = it } != -1) {
                    byteArrayOutputStream.write(buffer, 0, len)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return byteArrayOutputStream.toByteArray()
    }

    fun getBytes(context: Context, uri: Uri?): ByteArray {
        val iStream: InputStream?
        var byteBuffer: ByteArrayOutputStream? = null
        try {
            iStream = context.contentResolver.openInputStream(uri!!)
            byteBuffer = ByteArrayOutputStream()
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var len = 0
            while (iStream!!.read(buffer).also { len = it } != -1) {
                byteBuffer.write(buffer, 0, len)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        assert(byteBuffer != null)
        return byteBuffer!!.toByteArray()
    }

    fun prepareFilePart(context: Context, partName: String?, fileUri: Uri?): MultipartBody.Part {
        val requestBody =
            getFileDataFromDrawable(context, fileUri).toRequestBody(Constants.CONTENT_IMAGE.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName!!, "Image" + Constants.IMAGE_JPEG, requestBody)
    }

    fun prepareAudiFilePart(context: Context, partName: String?, fileUri: Uri?): MultipartBody.Part {
        val audioFile = File(fileUri.toString())
        val requestFile = RequestBody.create("audio/*".toMediaTypeOrNull(), audioFile)
        return MultipartBody.Part.createFormData(partName!!, Constants.audioFile + Constants.AUDIO_MP3, requestFile)
    }

    fun prepareVideoFilePart(context: Context, partName: String?, fileUri: Uri?): MultipartBody.Part {
        val requestBody =
            getFileDataFromDrawable(context, fileUri).toRequestBody(Constants.CONTENT_VIDEO.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName!!, Constants.videoFile + Constants.VIDEO_MP4, requestBody)
    }

    fun prepareFilePart(partName: String?, fileUri: Drawable): MultipartBody.Part {
        val requestBody =
            getFileDataFromDrawable(fileUri).toRequestBody(Constants.CONTENT_IMAGE.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName!!, "Image" + Constants.IMAGE_JPEG, requestBody)
    }

    fun prepareFilePartPng(partName: String?, fileUri: Drawable): MultipartBody.Part {
        val requestBody =
            getFileDataFromDrawablePNG(fileUri).toRequestBody(Constants.CONTENT_IMAGE.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName!!, "Image" + Constants.IMAGE_PNG, requestBody)
    }

    fun prepareFilePart(context: Context?, partName: String?, fileUri: Int): MultipartBody.Part {
        val requestBody =
            getBitmapFromVectorDrawable(context,fileUri).toRequestBody(Constants.CONTENT_IMAGE.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName!!, "Image" + Constants.IMAGE_JPEG, requestBody)
    }

    fun prepareFilePart(partName: String?): MultipartBody.Part {
        val requestBody = "".toRequestBody(Constants.CONTENT_IMAGE.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName!!, "Image" + Constants.IMAGE_JPEG, requestBody)
    }

    fun prepareFilePartPDF(context: Context, partName: String, fileUri: Uri?): MultipartBody.Part {
        val requestBody: RequestBody = getBytes(context, fileUri).toRequestBody(Constants.CONTENT_ALL_DOC.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, partName + Constants.PDF, requestBody)

    }
}