package com.devmcry.imageprocessor.ui.opengl.filter;

import android.opengl.GLES30;

import com.devmcry.imageprocessor.ui.opengl.filter.GlFilter;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES30.GL_ARRAY_BUFFER;
import static android.opengl.GLES30.GL_FLOAT;
import static android.opengl.GLES30.GL_TEXTURE0;
import static android.opengl.GLES30.GL_TEXTURE_2D;
import static android.opengl.GLES30.GL_TRIANGLE_STRIP;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class GlPreviewFilter extends GlFilter {

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "uniform float uCRatio;\n" +

                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying highp vec2 vTextureCoord;\n" +

                    "void main() {\n" +
                    "vec4 scaledPos = aPosition;\n" +
                    "scaledPos.x = scaledPos.x * uCRatio;\n" +
                    "gl_Position = uMVPMatrix * scaledPos;\n" +
                    "vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private final int texTarget;

    public GlPreviewFilter(final int texTarget) {
        super(VERTEX_SHADER, createFragmentShaderSourceOESIfNeed(texTarget));
        this.texTarget = texTarget;
    }

    private static String createFragmentShaderSourceOESIfNeed(final int texTarget) {
        if (texTarget == GL_TEXTURE_EXTERNAL_OES) {
            return new StringBuilder()
                    .append("#extension GL_OES_EGL_image_external : require\n")
                    .append(DEFAULT_FRAGMENT_SHADER.replace("sampler2D", "samplerExternalOES"))
                    .toString();
        }
        return DEFAULT_FRAGMENT_SHADER;
    }

    public void draw(final int texName, final float[] mvpMatrix, final float[] stMatrix, final float aspectRatio) {
        useProgram();

        GLES30.glUniformMatrix4fv(getHandle("uMVPMatrix"), 1, false, mvpMatrix, 0);
        GLES30.glUniformMatrix4fv(getHandle("uSTMatrix"), 1, false, stMatrix, 0);
        GLES30.glUniform1f(getHandle("uCRatio"), aspectRatio);

        GLES30.glBindBuffer(GL_ARRAY_BUFFER, getVertexBufferName());
        GLES30.glEnableVertexAttribArray(getHandle("aPosition"));
        GLES30.glVertexAttribPointer(getHandle("aPosition"), VERTICES_DATA_POS_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_POS_OFFSET);
        GLES30.glEnableVertexAttribArray(getHandle("aTextureCoord"));
        GLES30.glVertexAttribPointer(getHandle("aTextureCoord"), VERTICES_DATA_UV_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_UV_OFFSET);

        // NO.3.1 激活纹理单元
        GLES30.glActiveTexture(GL_TEXTURE0);
        // NO.3.2 绑定纹理ID texName 到纹理单元
        GLES30.glBindTexture(texTarget, texName);
        // NO.3.3 将激活的纹理单元传递到着色器
        GLES30.glUniform1i(getHandle(DEFAULT_UNIFORM_SAMPLER), 0);

        GLES30.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        GLES30.glDisableVertexAttribArray(getHandle("aPosition"));
        GLES30.glDisableVertexAttribArray(getHandle("aTextureCoord"));
        GLES30.glBindBuffer(GL_ARRAY_BUFFER, 0);
        GLES30.glBindTexture(GL_TEXTURE_2D, 0);
    }
}
