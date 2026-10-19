package com.example.deliverlk.data.firebase

import android.content.Context
import android.net.Uri
import com.example.deliverlk.core.util.ImageCompressor
import com.example.deliverlk.domain.repository.ProofStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class FirestoreProofStorage(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ProofStorage {

    override suspend fun saveProofImage(
        deliveryId: String,
        proofId: String,
        imageBytes: ByteArray,
        byUid: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val proofDocRef = firestore.collection(FirestoreConstants.DELIVERIES)
                    .document(deliveryId)
                    .collection("proofs")
                    .document(proofId)

                val proofData = mapOf(
                    "image" to Blob.fromBytes(imageBytes),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "byUid" to byUid,
                    "sizeBytes" to imageBytes.size.toLong(),
                    "mimeType" to "image/jpeg"
                )

                proofDocRef.set(proofData).await()
                Result.success(proofId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Result.failure(Exception("Firestore proof save failed: ${e.localizedMessage}"))
            }
        }
    }

    override suspend fun getProofImage(deliveryId: String, proofId: String): Result<ByteArray> {
        return withContext(Dispatchers.IO) {
            try {
                val docSnap = firestore.collection(FirestoreConstants.DELIVERIES)
                    .document(deliveryId)
                    .collection("proofs")
                    .document(proofId)
                    .get()
                    .await()

                if (!docSnap.exists()) {
                    return@withContext Result.failure(Exception("Proof image not found"))
                }

                val blob = docSnap.getBlob("image")
                    ?: return@withContext Result.failure(Exception("No image blob in document"))

                Result.success(blob.toBytes())
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Result.failure(e)
            }
        }
    }

    override suspend fun uploadProof(deliveryId: String, imageUri: Uri): Result<String> {
        return try {
            val bytes = ImageCompressor.compressImage(context, imageUri)
                ?: return Result.failure(Exception("Could not compress photo"))
            val uid = auth.currentUser?.uid ?: "unknown"
            saveProofImage(deliveryId, "final", bytes, uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}