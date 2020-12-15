package com.devmcry.imageprocessor.ui.opengl.filter;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.devmcry.imageprocessor.ui.opengl.util.EFramebufferObject;

import org.jetbrains.annotations.NotNull;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES30.GL_ARRAY_BUFFER;
import static android.opengl.GLES30.GL_FLOAT;
import static android.opengl.GLES30.GL_TEXTURE0;
import static android.opengl.GLES30.GL_TEXTURE_2D;
import static android.opengl.GLES30.GL_TRIANGLE_STRIP;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class GlPreviewFilter extends GlFilter implements FilterInterface {

    private float[] MVPMatrix = new float[16];
    private float[] ProjMatrix = new float[16];
    private float[] MMatrix = new float[16];
    private float[] VMatrix = new float[16];

    private float mSourceWHRatio = -1;

    public float[] getSTMatrix() {
        return STMatrix;
    }

    private float[] STMatrix = new float[16];


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

    public GlPreviewFilter(final int texTarget, float sourceWHRatio) {
        super(VERTEX_SHADER, createFragmentShaderSourceOESIfNeed(texTarget));
        this.texTarget = texTarget;
        this.mSourceWHRatio = sourceWHRatio;

        Matrix.setIdentityM(STMatrix, 0);
        Matrix.setLookAtM(VMatrix, 0,
                0.0f, 0.0f, 5.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        );
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



    @Override
    public void draw(final int texName, final EFramebufferObject fbo) {
        useProgram();

        Matrix.multiplyMM(MVPMatrix, 0, VMatrix, 0, MMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, ProjMatrix, 0, MVPMatrix, 0);

        GLES30.glUniformMatrix4fv(getHandle("uMVPMatrix"), 1, false, MVPMatrix, 0);
        GLES30.glUniformMatrix4fv(getHandle("uSTMatrix"), 1, false, STMatrix, 0);
        GLES30.glUniform1f(getHandle("uCRatio"), mSourceWHRatio);

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



    @Override
    public void setupAfterSizeChange(int width, int height) {
        super.setup();
        // width height 为最终展示高宽
        // mSourceWHRatio 为素材的宽高比例

        // 关键：视频居中
        // 由于视频和图片是顶点对齐，为了将视频居中需要对视频渲染的正交矩阵进行重新运算
        // bottom = ratioVideo / ratioImage
        // top = 2 + bottom
        // left right bottom top 取值 -1 ～ 1
        float bottom = -mSourceWHRatio * height / width;
        float top = 2 + bottom;

        Matrix.orthoM(ProjMatrix, 0, -mSourceWHRatio, mSourceWHRatio, bottom, top, 5, 7);
        Matrix.setIdentityM(MMatrix, 0);

        // buffer setup when size is changed
        mBufferObject = new EFramebufferObject();
        mBufferObject.setup(width, (int) (width / mSourceWHRatio));
    }
}
