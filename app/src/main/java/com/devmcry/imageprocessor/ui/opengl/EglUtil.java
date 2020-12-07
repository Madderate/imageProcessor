package com.devmcry.imageprocessor.ui.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLException;
import android.opengl.GLUtils;
import android.util.Log;

import com.devmcry.imageprocessor.BuildConfig;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES30.GL_ARRAY_BUFFER;
import static android.opengl.GLES30.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES30.GL_LINK_STATUS;
import static android.opengl.GLES30.GL_STATIC_DRAW;
import static android.opengl.GLES30.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES30.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES30.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES30.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES30.GL_TRUE;
import static android.opengl.GLES30.glCreateProgram;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class EglUtil {

    public static final int NO_TEXTURE = -1;

    private static final int FLOAT_SIZE_BYTES = 4;

    public static int loadShader(final String strSource, final int iType) {
        int[] compiled = new int[1];
        int iShader = GLES30.glCreateShader(iType);
        GLES30.glShaderSource(iShader, strSource);
        GLES30.glCompileShader(iShader);
        GLES30.glGetShaderiv(iShader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.d("Load Shader Failed", "Compilation\n" + GLES30.glGetShaderInfoLog(iShader));
            return 0;
        }
        return iShader;
    }

    public static int createProgram(final int vertexShader, final int pixelShader) throws GLException {
        final int program = glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("Could not create program");
        }

        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, pixelShader);

        GLES30.glLinkProgram(program);
        final int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GL_TRUE) {
            GLES30.glDeleteProgram(program);
            throw new RuntimeException("Could not link program");
        }
        return program;
    }

    public static void checkEglError(String operation) {
        if (!BuildConfig.DEBUG) return;
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            throw new RuntimeException(operation + ": glError " + error);
        }
    }

    public static void setupSampler(final int target, final int mag, final int min) {
        GLES30.glTexParameterf(target, GL_TEXTURE_MAG_FILTER, mag);
        GLES30.glTexParameterf(target, GL_TEXTURE_MIN_FILTER, min);
        GLES30.glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }


    public static int createBuffer(final float[] data) {
        return createBuffer(toFloatBuffer(data));
    }

    public static int createBuffer(final FloatBuffer data) {
        final int[] buffers = new int[1];
        GLES30.glGenBuffers(buffers.length, buffers, 0);
        updateBufferData(buffers[0], data);
        return buffers[0];
    }

    public static FloatBuffer toFloatBuffer(final float[] data) {
        final FloatBuffer buffer = ByteBuffer
                .allocateDirect(data.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(data).position(0);
        return buffer;
    }


    public static void updateBufferData(final int bufferName, final FloatBuffer data) {
        GLES30.glBindBuffer(GL_ARRAY_BUFFER, bufferName);
        GLES30.glBufferData(GL_ARRAY_BUFFER, data.capacity() * FLOAT_SIZE_BYTES, data, GL_STATIC_DRAW);
        GLES30.glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public static int loadTexture(final Bitmap img, final int usedTexId, final boolean recycle) {
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES30.glGenTextures(1, textures, 0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, img, 0);
        } else {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId);
            GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, img);
            textures[0] = usedTexId;
        }
        if (recycle) {
            img.recycle();
        }
        return textures[0];
    }
}
