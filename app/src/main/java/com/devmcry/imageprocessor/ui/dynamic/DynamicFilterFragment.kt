package com.devmcry.imageprocessor.ui.dynamic

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.devmcry.imageprocessor.R

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/3 11:30
 *  @description :
 */
class DynamicFilterFragment : Fragment() {
    private val dynamicFilterViewModel: DynamicFilterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dynamic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dynamicFilterViewModel.displayDecoders()
        val final = context?.externalCacheDir?.absolutePath + "/output.mp4"
        val cmd =
            "-y -i /storage/emulated/0/Download/base2.mp4 -c:v libvpx-vp9 -i /storage/emulated/0/Download/cover_24fps.webm -filter_complex overlay -vcodec libx264 -preset veryfast -c:a copy -s 1920x1080 $final"

        val start = System.currentTimeMillis()
        FFmpeg.executeAsync(cmd) { _, returnCode ->
            when (returnCode) {
                RETURN_CODE_SUCCESS -> {
                    Log.d("=====", "cost ${System.currentTimeMillis() - start}")
                    Log.d("=====", "Async command execution completed successfully.")
                }
                RETURN_CODE_CANCEL -> {
                    Log.d("=====", "Async command execution cancelled by user.")
                }
                else -> {
                    Log.d("=====", "Async command execution failed with returnCode=$returnCode")
                }
            }
        }
    }
}