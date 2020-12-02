package com.devmcry.imageprocessor.ui.merge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import androidx.core.graphics.scale
import java.io.File
import kotlin.math.floor

/**
 *  @author : DevMcryYu
 *  @date : 2020/11/26 15:49
 *  @description :
 */
object MergePicUtils {
    private const val MERGE_RESULT_WIDTH = 1080f

    // 先尝试竖图拼接，后续会有模板
    fun merge(context: Context, paths: List<String>): Bitmap? {
        val start = System.currentTimeMillis()
        if (paths.isNullOrEmpty()) {
            return null
        }
        val preModels = mutableListOf<PreMergeModel>()
        paths.forEach { path ->
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            if (path.contains("content://")) {
                context.contentResolver.openInputStream(Uri.parse(path))?.run {
                    BitmapFactory.decodeStream(this, null, options)
                    preModels.add(PreMergeModel(path, options.outWidth, options.outHeight))
                    close()
                }
            } else {
                BitmapFactory.decodeFile(path, options)
                preModels.add(PreMergeModel(path, options.outWidth, options.outHeight))
            }
        }
        val totalWidth = MERGE_RESULT_WIDTH.toInt()
        var totalHeight = 0
        preModels.forEach { model ->
            model.scale = model.width / MERGE_RESULT_WIDTH
            model.width = MERGE_RESULT_WIDTH.toInt()
            model.height = (model.height / model.scale).toInt()
            totalHeight += model.height
        }

        val bitmapList = mutableListOf<Bitmap>()
        preModels.forEach {
            if (it.imagePath.contains("content://")) {
                context.contentResolver.openInputStream(Uri.parse(it.imagePath))?.run {
                    var bitmap = BitmapFactory.decodeStream(this)
                    bitmap = bitmap.scale(it.width, it.height)
                    close()
                    bitmapList.add(bitmap)
                }
            } else {
                var bitmap = BitmapFactory.decodeFile(it.imagePath)
                bitmap = bitmap.scale(it.width, it.height)
                bitmapList.add(bitmap)
            }
        }
        val finalBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)
        var currentHeight = 0
        bitmapList.forEach {
            canvas.drawBitmap(it, 0f, currentHeight.toFloat(), null)
            currentHeight += it.height
            it.recycle()
        }
        Log.d("=====", "total cost ${System.currentTimeMillis() - start} millis")
        return finalBitmap
    }

    private fun getPowerOfTwoForSampleRatio(ratio: Float): Int {
        val k = Integer.highestOneBit(floor(ratio).toInt())
        return if (k == 0) 1 else k
    }
}

data class PreMergeModel(
    var imagePath: String,
    var width: Int = 0,
    var height: Int = 0,
    var scale: Float = 0f
)