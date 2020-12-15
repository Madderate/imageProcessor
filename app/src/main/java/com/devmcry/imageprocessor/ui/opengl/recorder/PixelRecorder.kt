package com.devmcry.imageprocessor.ui.opengl.recorder

import android.opengl.GLES30
import android.os.Build
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer
import java.nio.IntBuffer

/**
 * Created by balibell on 2020/12/14.
 */
class PixelRecorder(recordListener: OnRecordListener? =null) {
    companion object {

    }

    var mRecordWidth: Int = 0
    var mRecordHeight: Int = 0

    // RGBA 4字节
    private val mPixelStride = 4
    // 对齐 4字节
    private var mRowStride = 0

    // 像素缓冲
    private var mPboIds: IntBuffer? = null
    private var mPboIndex = 0
    private var mPboNewIndex = 0

    // 像素缓存内存大小
    private var mPboSize = 0


    // 是否开启PBO
    var isRecording = false
    // record准备好没
    private var isRecordInit = false


    // 图像时间戳，用于录制帧数判断
    private val mLastTimestamp: Long = 0

    // record helper
    private var mRecordHelper: RecordHelper = RecordHelper()


    init {
        mRecordHelper.setOnRecordListener(recordListener)
    }

    // 开始录制
    fun startRecord() {
        if (isRecording) {
            return
        }
        isRecording = true
        isRecordInit = true
        mPboIndex = 0
        mPboNewIndex = 1
        mRecordHelper.start()
    }

    // 停止录制
    fun stopRecord() {
        if (!isRecording) {
            return
        }
        isRecording = false
        mRecordHelper.stop()
    }

    private fun destroyPixelBuffers() {
        if (mPboIds != null) {
            GLES30.glDeleteBuffers(2, mPboIds)
            mPboIds = null
        }
    }

    // 初始化2个pbo，交替使用
    fun initPixelBuffer(width: Int, height: Int) {
        if (mPboIds != null && (mRecordWidth != width || mRecordHeight != height)) {
            destroyPixelBuffers()
        }
        if (mPboIds != null) {
            return
        }

        mRecordWidth = width
        mRecordHeight = height

        // OpenGLES默认应该是4字节对齐应，但是不知道为什么在索尼Z2上效率反而降低
        // 并且跟ImageReader最终计算出来的rowStride也和我这样计算出来的不一样，这里怀疑跟硬件和分辨率有关
        // 这里默认取得128的倍数，这样效率反而高，为什么？
        val align = 128 //128字节对齐
        mRowStride = width * mPixelStride + (align - 1) and (align - 1).inv()
        mPboSize = mRowStride * height
        mPboIds = IntBuffer.allocate(2)

        // 生成2个PBO
        GLES30.glGenBuffers(2, mPboIds)
        // 绑定到第一个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds!!.get(0))
        // 设置内存大小
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, mPboSize, null, GLES30.GL_STATIC_READ)
        // 绑定到第二个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds!!.get(1))
        // 设置内存大小
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, mPboSize, null, GLES30.GL_STATIC_READ)

        // 解除绑定PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun bindPixelBuffer() {
        // 绑定到第一个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds!![mPboIndex])
        GLES30.glReadPixels(0, 0, mRecordWidth, mRecordHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, 0)

        // 第一帧没有数据跳出
        if (isRecordInit) {
            changeIndexAndUnbindPBO()
            isRecordInit = false
            return
        }

        // 绑定到第二个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds!![mPboNewIndex])

        // glMapBufferRange会等待DMA传输完成，所以需要交替使用pbo
        // 映射内存
        val byteBuffer = GLES30.glMapBufferRange(
            GLES30.GL_PIXEL_PACK_BUFFER,
            0,
            mPboSize,
            GLES30.GL_MAP_READ_BIT
        ) as ByteBuffer

        // 解除映射
        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER)
        changeIndexAndUnbindPBO()

        // 交给mRecordHelper录制
        mRecordHelper.onRecord(byteBuffer, mRecordWidth, mRecordHeight, mPixelStride, mRowStride, mLastTimestamp);
    }

    // 解绑pbo
    private fun changeIndexAndUnbindPBO() {
        // 解除绑定PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)

        // 交换索引
        mPboIndex = (mPboIndex + 1) % 2
        mPboNewIndex = (mPboNewIndex + 1) % 2
    }
}