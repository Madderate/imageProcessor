package com.devmcry.imageprocessor

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class TriangleActivity : AppCompatActivity() {

    private val CONTEXT_CLIENT_VERSION = 3

    private lateinit var root: View
    private lateinit var surfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_triangle)

        root = findViewById(R.id.rootConstraintView)
        surfaceView = root.findViewById(R.id.surfaceView)

        if (detectOpenGLES30()) {
            surfaceView.setEGLContextClientVersion(CONTEXT_CLIENT_VERSION)
            surfaceView.setRenderer(TriangleRenderer(this))
        } else {
            Log.e(
                "TriangleActivity",
                "OpenGL ES 3.0 not supported on device.  Exiting..."
            )
            finish()
        }
    }

    override fun onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume()
        surfaceView.onResume()
    }

    override fun onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause()
        surfaceView.onPause()
    }


    private fun detectOpenGLES30(): Boolean {
        val am =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = am.deviceConfigurationInfo
        return info.reqGlEsVersion >= 0x30000
    }
}