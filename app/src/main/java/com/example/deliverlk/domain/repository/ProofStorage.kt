package com.example.deliverlk.domain.repository

import android.net.Uri

interface ProofStorage {
    suspend fun uploadProof(deliveryId: String, imageUri: Uri): Result<String>
    suspend fun saveProofImage(deliveryId: String, proofId: String, imageBytes: ByteArray, byUid: String): Result<String>
    suspend fun getProofImage(deliveryId: String, proofId: String): Result<ByteArray>
}