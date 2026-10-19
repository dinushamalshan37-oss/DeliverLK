package com.example.deliverlk.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageCompressor {

    const val MAX_LONGEST_EDGE = 900
    const val TARGET_MAX_BYTES = 200 * 1024 // 200 KB
    const val HARD_MAX_BYTES = 700 * 1024 // 700 KB
    const val INITIAL_QUALITY = 70
    const val MIN_QUALITY = 40

    fun calculateScaledDimensions(width: Int, height: Int, maxLongestEdge: Int = MAX_LONGEST_EDGE): Pair<Int, Int> {
        if (width <= 0 || height <= 0) return Pair(1, 1)
        val longestEdge = maxOf(width, height)
        if (longestEdge <= maxLongestEdge) {
            return Pair(width, height)
        }
        val scale = maxLongestEdge.toFloat() / longestEdge.toFloat()
        val newWidth = (width * scale).toInt().coerceAtLeast(1)
        val newHeight = (height * scale).toInt().coerceAtLeast(1)
        return Pair(newWidth, newHeight)
    }

    fun nextQualityStep(currentQuality: Int, minQuality: Int = MIN_QUALITY): Int {
        val next = currentQuality - 10
        return if (next < minQuality) minQuality else next
    }

    suspend fun compressImage(context: Context, imageUri: Uri): ByteArray? = withContext(Dispatchers.Default) {
        try {
            val contentResolver = context.contentResolver

            val rotationDegrees = getRotationFromUri(context, imageUri)

            var inputStream: InputStream = contentResolver.openInputStream(imageUri) ?: return@withContext null
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            val origWidth = options.outWidth
            val origHeight = options.outHeight
            if (origWidth <= 0 || origHeight <= 0) return@withContext null

            val (targetWidth, targetHeight) = calculateScaledDimensions(origWidth, origHeight, MAX_LONGEST_EDGE)
            var inSampleSize = 1
            while (origWidth / (inSampleSize * 2) >= targetWidth && origHeight / (inSampleSize * 2) >= targetHeight) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            inputStream = contentResolver.openInputStream(imageUri) ?: return@withContext null
            var bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions) ?: return@withContext null
            inputStream.close()

            if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) {
                    bitmap.recycle()
                    bitmap = rotated
                }
            }

            val currentLongest = maxOf(bitmap.width, bitmap.height)
            if (currentLongest > MAX_LONGEST_EDGE) {
                val scale = MAX_LONGEST_EDGE.toFloat() / currentLongest.toFloat()
                val scaledWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
                val scaledHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
                if (scaledBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = scaledBitmap
                }
            }

            var quality = INITIAL_QUALITY
            var currentBitmap = bitmap
            var resultBytes: ByteArray

            while (true) {
                val baos = ByteArrayOutputStream()
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                resultBytes = baos.toByteArray()

                if (resultBytes.size <= TARGET_MAX_BYTES || quality <= MIN_QUALITY) {
                    if (resultBytes.size > HARD_MAX_BYTES && (currentBitmap.width > 300 || currentBitmap.height > 300)) {
                        val newWidth = (currentBitmap.width * 0.7f).toInt().coerceAtLeast(1)
                        val newHeight = (currentBitmap.height * 0.7f).toInt().coerceAtLeast(1)
                        val downscaled = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true)
                        if (downscaled != currentBitmap && currentBitmap != bitmap) {
                            currentBitmap.recycle()
                        }
                        currentBitmap = downscaled
                        quality = MIN_QUALITY
                        continue
                    }
                    break
                }
                quality = nextQualityStep(quality, MIN_QUALITY)
            }

            if (currentBitmap != bitmap) {
                currentBitmap.recycle()
            }
            bitmap.recycle()

            resultBytes
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getRotationFromUri(context: Context, uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            inputStream.close()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }
}