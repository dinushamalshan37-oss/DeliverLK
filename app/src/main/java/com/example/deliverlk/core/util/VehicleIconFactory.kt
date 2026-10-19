package com.example.deliverlk.core.util

import android.content.Context
import android.graphics.*
import com.example.deliverlk.domain.model.VehicleStatus
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object VehicleIconFactory {

    private val cache = mutableMapOf<VehicleStatus, BitmapDescriptor>()

    fun getVehicleIcon(context: Context, status: VehicleStatus): BitmapDescriptor {
        return cache.getOrPut(status) {
            createVehicleBitmapDescriptor(status)
        }
    }

    private fun createVehicleBitmapDescriptor(status: VehicleStatus): BitmapDescriptor {
        val sizePx = 96
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = when (status) {
                VehicleStatus.MOVING -> Color.parseColor("#00897B") // Teal Green
                VehicleStatus.IDLE -> Color.parseColor("#FFB300")   // Amber
                VehicleStatus.OFFLINE -> Color.parseColor("#757575") // Grey
            }
        }

        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.WHITE
        }

        val cx = sizePx / 2f
        val cy = sizePx / 2f

        val bodyRect = RectF(cx - 18f, cy - 32f, cx + 18f, cy + 32f)
        canvas.drawRoundRect(bodyRect, 10f, 10f, fillPaint)
        canvas.drawRoundRect(bodyRect, 10f, 10f, outlinePaint)

        val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#E0F7FA")
        }
        val windshieldRect = RectF(cx - 14f, cy - 28f, cx + 14f, cy - 14f)
        canvas.drawRoundRect(windshieldRect, 4f, 4f, glassPaint)

        val wheelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#212121")
        }
        canvas.drawRect(RectF(cx - 22f, cy - 24f, cx - 18f, cy - 14f), wheelPaint)
        canvas.drawRect(RectF(cx + 18f, cy - 24f, cx + 22f, cy - 14f), wheelPaint)
        canvas.drawRect(RectF(cx - 22f, cy + 14f, cx - 18f, cy + 24f), wheelPaint)
        canvas.drawRect(RectF(cx + 18f, cy + 14f, cx + 22f, cy + 24f), wheelPaint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
