package com.devmcry.imageprocessor.ui.opengl

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devmcry.imageprocessor.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/3 15:55
 *  @description :
 */
class OpenGLFragment : Fragment() {
    private lateinit var root: View
    private lateinit var fab: FloatingActionButton

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glRender: OpenGLRender

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_opengl, container, false)
        glSurfaceView = root.findViewById(R.id.surfaceView)
        fab = root.findViewById(R.id.fab)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initOpenGL()
    }

    private fun initView() {
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun initOpenGL() {
        glSurfaceView.runCatching {
            glRender = OpenGLRender(requireContext())
            setEGLContextClientVersion(3)
            setRenderer(glRender)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            post {
                val bitmap = BitmapFactory.decodeFile("/storage/emulated/0/Download/wallhaven-lmkk2y.jpg").also {
                    glSurfaceView.layoutParams.width = it.width
                    glSurfaceView.layoutParams.height = it.height
                }
                queueEvent {
                    glRender.mBitmap = bitmap
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }
}