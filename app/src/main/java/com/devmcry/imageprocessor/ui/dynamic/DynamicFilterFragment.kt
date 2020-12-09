package com.devmcry.imageprocessor.ui.dynamic

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.GLSurfaceView
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
import com.devmcry.imageprocessor.ui.opengl.OpenGLFragment
import com.devmcry.imageprocessor.ui.opengl.OpenGLRender

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/3 11:30
 *  @description :
 */
class DynamicFilterFragment : Fragment() {
    private val dynamicFilterViewModel: DynamicFilterViewModel by viewModels()
    private lateinit var dynamicFilterContainer: DynamicFilterContainer

    companion object {
        const val REQ_CHOOSE_PICS = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dynamic, container, false)
        dynamicFilterContainer = root.findViewById(R.id.surfaceView)
        root.findViewById<View>(R.id.mockEditPanel).setOnClickListener {
            Intent().run {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(this, getString(R.string.choose_pics)),
                    REQ_CHOOSE_PICS
                )
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        dynamicFilterViewModel.displayDecoders()
//        val final = context?.externalCacheDir?.absolutePath + "/output.mp4"
//        val cmd =
//            "-y -i /storage/emulated/0/Download/base2.mp4 -c:v libvpx-vp9 -i /storage/emulated/0/Download/cover_24fps.webm -filter_complex overlay -vcodec libx264 -preset veryfast -c:a copy -s 1920x1080 $final"
//
//        val start = System.currentTimeMillis()
//        FFmpeg.executeAsync(cmd) { _, returnCode ->
//            when (returnCode) {
//                RETURN_CODE_SUCCESS -> {
//                    Log.d("=====", "cost ${System.currentTimeMillis() - start}")
//                    Log.d("=====", "Async command execution completed successfully.")
//                }
//                RETURN_CODE_CANCEL -> {
//                    Log.d("=====", "Async command execution cancelled by user.")
//                }
//                else -> {
//                    Log.d("=====", "Async command execution failed with returnCode=$returnCode")
//                }
//            }
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CHOOSE_PICS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.data?.run {
                            context?.contentResolver?.openInputStream(this)?.run {
                                BitmapFactory.decodeStream(this).also {
                                    dynamicFilterContainer.updateGLSurfaceView(it)
                                    close()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}