package com.example.deliverlk.core.designsystem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.deliverlk.R
import com.example.deliverlk.domain.repository.ProofStorage

object ProofPhotoCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }
}

@Composable
fun ProofPhoto(
    deliveryId: String,
    proofId: String,
    proofStorage: ProofStorage?,
    modifier: Modifier = Modifier,
    fallbackPhotoUri: String? = null
) {
    val cacheKey = "$deliveryId/$proofId"
    var bitmapState by remember(cacheKey) { mutableStateOf<Bitmap?>(ProofPhotoCache.memoryCache.get(cacheKey)) }
    var isLoading by remember(cacheKey) { mutableStateOf(bitmapState == null) }
    var isError by remember(cacheKey) { mutableStateOf(false) }
    var showFullScreenDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cacheKey, proofStorage) {
        if (bitmapState == null && proofStorage != null) {
            isLoading = true
            isError = false
            val result = proofStorage.getProofImage(deliveryId, proofId)
            result.onSuccess { bytes ->
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (decoded != null) {
                    ProofPhotoCache.memoryCache.put(cacheKey, decoded)
                    bitmapState = decoded
                } else {
                    isError = true
                }
            }.onFailure {
                isError = true
            }
            isLoading = false
        } else if (bitmapState == null && proofStorage == null) {
            isLoading = false
            isError = fallbackPhotoUri.isNullOrBlank()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = bitmapState != null) { showFullScreenDialog = true },
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
            bitmapState != null -> {
                Image(
                    bitmap = bitmapState!!.asImageBitmap(),
                    contentDescription = stringResource(R.string.proof_of_delivery),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            isError -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.BrokenImage, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.photo_unavailable),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showFullScreenDialog && bitmapState != null) {
        Dialog(
            onDismissRequest = { showFullScreenDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmapState!!.asImageBitmap(),
                    contentDescription = stringResource(R.string.proof_of_delivery),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = { showFullScreenDialog = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}