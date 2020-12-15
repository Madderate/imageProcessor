package com.devmcry.imageprocessor.ui.opengl.util;

import android.graphics.SurfaceTexture;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;


/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class ESurfaceTexture implements SurfaceTexture.OnFrameAvailableListener {

    private SurfaceTexture surfaceTexture;
    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener;

    public int getTextureType() {
        return textureType;
    }
    private int textureType;

    public int getTextureId() {
        return textureId;
    }
    private int textureId;

    public ESurfaceTexture(final int texId, int texType) {
        surfaceTexture = new SurfaceTexture(texId);
        surfaceTexture.setOnFrameAvailableListener(this);
        textureId = texId;
        textureType = texType;
    }

    public void setOnFrameAvailableListener(final SurfaceTexture.OnFrameAvailableListener l) {
        onFrameAvailableListener = l;
    }


    public void updateTexImage() {
        surfaceTexture.updateTexImage();
    }

    public void getTransformMatrix(final float[] mtx) {
        surfaceTexture.getTransformMatrix(mtx);
    }

    public SurfaceTexture getSurfaceTexture() {
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
