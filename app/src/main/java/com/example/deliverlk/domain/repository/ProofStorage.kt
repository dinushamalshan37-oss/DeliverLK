package com.example.deliverlk.domain.repository

import android.net.Uri

interface ProofStorage {
    suspend fun uploadProof(deliveryId: String, imageUri: Uri): Result<String>
}
