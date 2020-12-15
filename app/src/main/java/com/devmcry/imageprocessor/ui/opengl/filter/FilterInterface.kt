package com.devmcry.imageprocessor.ui.opengl.filter

import com.devmcry.imageprocessor.ui.opengl.util.EFramebufferObject

interface FilterInterface {
    fun setupAfterSizeChange(width: Int, height: Int): Unit
}