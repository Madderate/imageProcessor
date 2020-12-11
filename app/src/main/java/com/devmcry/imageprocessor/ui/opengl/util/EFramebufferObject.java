package com.devmcry.imageprocessor.ui.opengl.util;

import android.opengl.GLES30;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES30.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES30.GL_DEPTH_ATTACHMENT;
import static android.opengl.GLES30.GL_DEPTH_COMPONENT16;
import static android.opengl.GLES30.GL_FRAMEBUFFER;
import static android.opengl.GLES30.GL_FRAMEBUFFER_BINDING;
import static android.opengl.GLES30.GL_FRAMEBUFFER_COMPLETE;
import static android.opengl.GLES30.GL_LINEAR;
import static android.opengl.GLES30.GL_MAX_RENDERBUFFER_SIZE;
import static android.opengl.GLES30.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES30.GL_NEAREST;
import static android.opengl.GLES30.GL_RENDERBUFFER;
import static android.opengl.GLES30.GL_RENDERBUFFER_BINDING;
import static android.opengl.GLES30.GL_RGBA;
import static android.opengl.GLES30.GL_TEXTURE_2D;
import static android.opengl.GLES30.GL_TEXTURE_BINDING_2D;
import static android.opengl.GLES30.GL_UNSIGNED_BYTE;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class EFramebufferObject {

    private int width;
    private int height;
    private int framebufferName;
    private int renderbufferName;
    private int texName;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTexName() {
        return texName;
    }

    public void setup(final int width, final int height) {
        final int[] args = new int[1];

        GLES30.glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);
        if (width > args[0] || height > args[0]) {
            throw new IllegalArgumentException("GL_MAX_TEXTURE_SIZE " + args[0]);
        }

        GLES30.glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE, args, 0);
        if (width > args[0] || height > args[0]) {
            throw new IllegalArgumentException("GL_MAX_RENDERBUFFER_SIZE " + args[0]);
        }

        GLES30.glGetIntegerv(GL_FRAMEBUFFER_BINDING, args, 0);
        final int saveFramebuffer = args[0];
        GLES30.glGetIntegerv(GL_RENDERBUFFER_BINDING, args, 0);
        final int saveRenderbuffer = args[0];
        GLES30.glGetIntegerv(GL_TEXTURE_BINDING_2D, args, 0);
        final int saveTexName = args[0];

        release();

        try {
            this.width = width;
            this.height = height;


            texName = EglUtil.INSTANCE.createFBOTexture(args, width, height);
            framebufferName = EglUtil.INSTANCE.createFrameBuffer(args);
            renderbufferName = EglUtil.INSTANCE.createRenderBuffer(args);

            EglUtil.INSTANCE.bindFBO(framebufferName, texName, renderbufferName, width, height);

            final int status = GLES30.glCheckFramebufferStatus(GL_FRAMEBUFFER);
            if (status != GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Failed to initialize framebuffer object " + status);
            }
        } catch (final RuntimeException e) {
            release();
            throw e;
        }

        GLES30.glBindTexture(GL_TEXTURE_2D, saveTexName);
        GLES30.glBindFramebuffer(GL_FRAMEBUFFER, saveFramebuffer);
        GLES30.glBindRenderbuffer(GL_RENDERBUFFER, saveRenderbuffer);
    }

    public void release() {
        final int[] args = new int[1];
        args[0] = texName;
        GLES30.glDeleteTextures(args.length, args, 0);
        texName = 0;
        args[0] = renderbufferName;
        GLES30.glDeleteRenderbuffers(args.length, args, 0);
        renderbufferName = 0;
        args[0] = framebufferName;
        GLES30.glDeleteFramebuffers(args.length, args, 0);
        framebufferName = 0;
    }

    public void enable() {
        GLES30.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName);
    }


}
