package com.devmcry.imageprocessor.ui.opengl;

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

            GLES30.glGenFramebuffers(args.length, args, 0);
            framebufferName = args[0];
            GLES30.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName);

            GLES30.glGenRenderbuffers(args.length, args, 0);
            renderbufferName = args[0];
            GLES30.glBindRenderbuffer(GL_RENDERBUFFER, renderbufferName);
            GLES30.glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height);
            GLES30.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderbufferName);

            GLES30.glGenTextures(args.length, args, 0);
            texName = args[0];
            GLES30.glBindTexture(GL_TEXTURE_2D, texName);

            GLES30.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            GLES30.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            GLES30.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);



            GLES30.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
            GLES30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texName, 0);

            final int status = GLES30.glCheckFramebufferStatus(GL_FRAMEBUFFER);
            if (status != GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Failed to initialize framebuffer object " + status);
            }
        } catch (final RuntimeException e) {
            release();
            throw e;
        }

        GLES30.glBindFramebuffer(GL_FRAMEBUFFER, saveFramebuffer);
        GLES30.glBindRenderbuffer(GL_RENDERBUFFER, saveRenderbuffer);
        GLES30.glBindTexture(GL_TEXTURE_2D, saveTexName);
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
