package com.example.deliverlk.data.fake

import android.net.Uri
import com.example.deliverlk.domain.repository.ProofStorage

class FakeProofStorage : ProofStorage {
    override suspend fun uploadProof(deliveryId: String, imageUri: Uri): Result<String> {
        return Result.success("https://example.com/fake_proof_${deliveryId}.jpg")
    }

    override suspend fun saveProofImage(
        deliveryId: String,
        proofId: String,
        imageBytes: ByteArray,
        byUid: String,
    ): Result<String> {
        return Result.success("https://example.com/fake_proof_${deliveryId}_${proofId}.jpg")
    }

    override suspend fun getProofImage(deliveryId: String, proofId: String): Result<ByteArray> {
        return Result.success(ByteArray(0))
    }
}
