package com.devmcry.imageprocessor.ui.dynamic

import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import androidx.lifecycle.ViewModel
import java.io.IOException


/**
 *  @author : DevMcryYu
 *  @date : 2020/12/3 11:30
 *  @description :
 */
class DynamicFilterViewModel : ViewModel() {
    fun displayDecoders() {
        val list = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val codecs = list.codecInfos
        codecs.forEach {
            if (!it.isEncoder) {
                Log.d("====", "displayDecoders ${it.name}")
            }
        }
    }

    fun chooseVideoTrack(extractor: MediaExtractor): MediaFormat? {
        val count = extractor.trackCount
        for (i in 0 until count) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true) {
                extractor.selectTrack(i)
                return format
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun createCodec(format: MediaFormat, surface: Surface): MediaCodec? {
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val codec = MediaCodec.createByCodecName(codecList.findDecoderForFormat(format))
        codec.configure(format, surface, null, 0)
        return codec
    }
}