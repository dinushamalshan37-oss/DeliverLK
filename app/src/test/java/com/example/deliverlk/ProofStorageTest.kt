package com.example.deliverlk

import com.example.deliverlk.data.fake.FakeProofStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProofStorageTest {

    @Test
    fun testFakeProofStorage_saveAndRetrieveBytes() = runBlocking {
        val storage = FakeProofStorage()
        val testBytes = "test_image_bytes_12345".toByteArray()

        val saveResult = storage.saveProofImage("del_101", "final", testBytes, "driver_uid_01")
        assertTrue(saveResult.isSuccess)
        assertEquals("final", saveResult.getOrNull())

        val getResult = storage.getProofImage("del_101", "final")
        assertTrue(getResult.isSuccess)
        assertArrayEquals(testBytes, getResult.getOrNull())
    }

    @Test
    fun testProofDataPayloadMap_format() {
        val deliveryId = "del_101"
        val proofId = "final"
        val recipientName = "Nimali"
        val note = "Left at door"

        val proofMetadataMap = mapOf(
            "proofId" to proofId,
            "recipientName" to recipientName,
            "note" to note
        )

        assertEquals("final", proofMetadataMap["proofId"])
        assertEquals("Nimali", proofMetadataMap["recipientName"])
        assertEquals("Left at door", proofMetadataMap["note"])
    }
}