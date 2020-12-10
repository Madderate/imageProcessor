package com.devmcry.imageprocessor.ui.opengl.renderer;

import android.graphics.SurfaceTexture;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;


/**
 * Created by sudamasayuki on 2017/05/16.
 */

class ESurfaceTexture implements SurfaceTexture.OnFrameAvailableListener {

    private SurfaceTexture surfaceTexture;
    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener;

    ESurfaceTexture(final int texName) {
        surfaceTexture = new SurfaceTexture(texName);
        surfaceTexture.setOnFrameAvailableListener(this);
    }

    void setOnFrameAvailableListener(final SurfaceTexture.OnFrameAvailableListener l) {
        onFrameAvailableListener = l;
    }


    int getTextureTarget() {
        return GL_TEXTURE_EXTERNAL_OES;
    }

    void updateTexImage() {
        surfaceTexture.updateTexImage();
    }

    void getTransformMatrix(final float[] mtx) {
        surfaceTexture.getTransformMatrix(mtx);
    }

    SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
        if (onFrameAvailableListener != null) {
            onFrameAvailableListener.onFrameAvailable(this.surfaceTexture);
        }
    }

    public void release() {
        surfaceTexture.release();
    }
}