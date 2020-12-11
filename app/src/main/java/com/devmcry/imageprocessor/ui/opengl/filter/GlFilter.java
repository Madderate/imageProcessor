package com.devmcry.imageprocessor.ui.opengl.filter;

import android.content.res.Resources;
import android.opengl.GLES30;

import com.devmcry.imageprocessor.ui.opengl.util.EFramebufferObject;
import com.devmcry.imageprocessor.ui.opengl.util.EglUtil;

import java.util.HashMap;

import static android.opengl.GLES30.GL_FLOAT;
import static android.opengl.GLES30.GL_FRAGMENT_SHADER;
import static android.opengl.GLES30.GL_VERTEX_SHADER;
import static android.opengl.GLES30.glGetAttribLocation;
import static android.opengl.GLES30.glGetUniformLocation;
import static android.opengl.GLES30.glUseProgram;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class GlFilter {

    public static final String DEFAULT_UNIFORM_SAMPLER = "sTexture";


    protected static final String DEFAULT_VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "gl_Position = aPosition;\n" +
                    "vTextureCoord = aTextureCoord.xy;\n" +
                    "}\n";

    protected static final String DEFAULT_FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";


    private static final float[] VERTICES_DATA = new float[]{
            // X, Y, Z, U, V
            -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.0f
    };




    private static final int FLOAT_SIZE_BYTES = 4;
    protected static final int VERTICES_DATA_POS_SIZE = 3;
    protected static final int VERTICES_DATA_UV_SIZE = 2;
    protected static final int VERTICES_DATA_STRIDE_BYTES = (VERTICES_DATA_POS_SIZE + VERTICES_DATA_UV_SIZE) * FLOAT_SIZE_BYTES;
    protected static final int VERTICES_DATA_POS_OFFSET = 0 * FLOAT_SIZE_BYTES;
    protected static final int VERTICES_DATA_UV_OFFSET = VERTICES_DATA_POS_OFFSET + VERTICES_DATA_POS_SIZE * FLOAT_SIZE_BYTES;

    private final String vertexShaderSource;
    private final String fragmentShaderSource;
    protected float[] verticeFragmentData = VERTICES_DATA;

    private int program;

    private int vertexShader;
    private int fragmentShader;

    private int vertexBufferName;

    private final HashMap<String, Integer> handleMap = new HashMap<String, Integer>();

    public GlFilter() {
        this(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
    }

    public GlFilter(final Resources res, final int vertexShaderSourceResId, final int fragmentShaderSourceResId) {
        this(res.getString(vertexShaderSourceResId), res.getString(fragmentShaderSourceResId));
    }

    public GlFilter(final String vertexShaderSource, final String fragmentShaderSource) {
        this.vertexShaderSource = vertexShaderSource;
        this.fragmentShaderSource = fragmentShaderSource;
    }

    public void setup() {
        release();
        vertexShader = EglUtil.INSTANCE.loadShader(vertexShaderSource, GL_VERTEX_SHADER);
        fragmentShader = EglUtil.INSTANCE.loadShader(fragmentShaderSource, GL_FRAGMENT_SHADER);
        program = EglUtil.INSTANCE.createProgram(vertexShader, fragmentShader);
        vertexBufferName = EglUtil.INSTANCE.createBuffer(verticeFragmentData);
    }


    public void release() {
        GLES30.glDeleteProgram(program);
        program = 0;
        GLES30.glDeleteShader(vertexShader);
        vertexShader = 0;
        GLES30.glDeleteShader(fragmentShader);
        fragmentShader = 0;
        GLES30.glDeleteBuffers(1, new int[]{vertexBufferName}, 0);
        vertexBufferName = 0;

        handleMap.clear();
    }

    public void draw(final int texName, final EFramebufferObject fbo) {
        useProgram();

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferName);
        GLES30.glEnableVertexAttribArray(getHandle("aPosition"));
        GLES30.glVertexAttribPointer(getHandle("aPosition"), VERTICES_DATA_POS_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_POS_OFFSET);
        GLES30.glEnableVertexAttribArray(getHandle("aTextureCoord"));
        GLES30.glVertexAttribPointer(getHandle("aTextureCoord"), VERTICES_DATA_UV_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_UV_OFFSET);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texName);
        GLES30.glUniform1i(getHandle("sTexture"), 0);

        onDraw();

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        GLES30.glDisableVertexAttribArray(getHandle("aPosition"));
        GLES30.glDisableVertexAttribArray(getHandle("aTextureCoord"));
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    protected void onDraw() {
    }

    protected final void useProgram() {
        glUseProgram(program);
    }

    protected final int getVertexBufferName() {
        return vertexBufferName;
    }

    protected final int getHandle(final String name) {
        final Integer value = handleMap.get(name);
        if (value != null) {
            return value.intValue();
        }

        int location = glGetAttribLocation(program, name);
        if (location == -1) {
            location = glGetUniformLocation(program, name);
        }
        if (location == -1) {
            throw new IllegalStateException("Could not get attrib or uniform location for " + name);
        }
        handleMap.put(name, Integer.valueOf(location));
        return location;
    }


    // 针对图片进行坐标转换
    static public float[] adjustImageScaling(float[] data, int imageWidth, int imageHeight, int outputWidth, int outputHeight) {
        // 必须符合 VERTICES_DATA 结构
        if (data.length != VERTICES_DATA.length) {
            return data;
        }
        float ratioMax = Math.min(outputWidth * 1f / imageWidth, outputHeight * 1f / imageHeight);
        int imageWidthNew = Math.round(imageWidth * ratioMax);
        int imageHeightNew = Math.round(imageHeight * ratioMax);
        float ratioWidth = outputWidth / imageWidthNew;
        float ratioHeight = outputHeight / imageHeightNew;

        return new float[] {
                data[0]/ratioWidth, data[1]/ratioHeight, data[2], data[3], data[4],
                data[5]/ratioWidth, data[6]/ratioHeight, data[7], data[8], data[9],
                data[10]/ratioWidth, data[11]/ratioHeight, data[12], data[13], data[14],
                data[15]/ratioWidth, data[16]/ratioHeight, data[17], data[18], data[19]
        };
    }
}
