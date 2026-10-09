package com.example.deliverlk.data.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.deliverlk.domain.repository.ProofStorage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.cancellation.CancellationException

class FirebaseProofStorage(
    private val context: Context,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : ProofStorage {

    override suspend fun uploadProof(deliveryId: String, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val compressedBytes = compressImage(imageUri, 1280, 80)
                    ?: throw Exception("Could not process photo")

                val path = "proofs/$deliveryId/${System.currentTimeMillis()}.jpg"
                val ref = storage.reference.child(path)

                ref.putBytes(compressedBytes).await()
                val downloadUrl = ref.downloadUrl.await().toString()

                Result.success(downloadUrl)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Result.failure(Exception("Proof photo upload failed: ${e.localizedMessage}"))
            }
        }
    }

    private fun compressImage(uri: Uri, maxDimension: Int, quality: Int): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            var width = options.outWidth
            var height = options.outHeight
            if (width <= 0 || height <= 0) return null

            var sampleSize = 1
            while (width / sampleSize > maxDimension || height / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val stream2 = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(stream2, null, decodeOptions)
            stream2.close()

            if (bitmap == null) return null

            val outStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
            bitmap.recycle()
            outStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}
