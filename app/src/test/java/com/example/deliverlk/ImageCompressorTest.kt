package com.example.deliverlk

import com.example.deliverlk.core.util.ImageCompressor
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageCompressorTest {

    @Test
    fun testCalculateScaledDimensions_noScaleWhenSmallerThanMax() {
        val (w, h) = ImageCompressor.calculateScaledDimensions(800, 600, 900)
        assertEquals(800, w)
        assertEquals(600, h)
    }

    @Test
    fun testCalculateScaledDimensions_scalesWhenWidthIsLongest() {
        val (w, h) = ImageCompressor.calculateScaledDimensions(1800, 1200, 900)
        assertEquals(900, w)
        assertEquals(600, h)
    }

    @Test
    fun testCalculateScaledDimensions_scalesWhenHeightIsLongest() {
        val (w, h) = ImageCompressor.calculateScaledDimensions(1000, 2000, 900)
        assertEquals(450, w)
        assertEquals(900, h)
    }

    @Test
    fun testNextQualityStep_reducesBy10UntilMinQuality() {
        var quality = ImageCompressor.INITIAL_QUALITY
        quality = ImageCompressor.nextQualityStep(quality, 40)
        assertEquals(60, quality)

        quality = ImageCompressor.nextQualityStep(quality, 40)
        assertEquals(50, quality)

        quality = ImageCompressor.nextQualityStep(quality, 40)
        assertEquals(40, quality)

        quality = ImageCompressor.nextQualityStep(quality, 40)
        assertEquals(40, quality)
    }
}