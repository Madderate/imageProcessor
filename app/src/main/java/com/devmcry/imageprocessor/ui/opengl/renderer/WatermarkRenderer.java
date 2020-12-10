package com.devmcry.imageprocessor.ui.opengl.renderer;

public class WatermarkRenderer  {

//    private final float[] TEX_VERTICES = {
//            0.0f, 1.0f,
//            1.0f, 1.0f,
//            0.0f, 0.0f,
//            1.0f, 0.0f
//    };
//    private final float[] POS_VERTICES = {
//            0.2f, -1.0f,
//            1.0f, -1.0f,
//            0.2f, -0.9f,
//            1.0f, -0.9f
//    };
//
//    private static final String VERTEX_SHADER =
//            "attribute vec4 a_position;\n" +
//                    "attribute vec2 a_texcoord;\n" +
//                    "varying vec2 v_texcoord;\n" +
//                    "void main() {\n" +
//                    "  gl_Position = a_position;\n" +
//                    "  v_texcoord = a_texcoord;\n" +
//                    "}\n";
//    private static final String FRAGMENT_SHADER =
//            "precision mediump float;\n" +
//                    "uniform sampler2D tex_sampler;\n" +
//                    "varying vec2 v_texcoord;\n" +
//                    "void main() {\n" +
//                    "  gl_FragColor = texture2D(tex_sampler, v_texcoord);\n" +
//                    "}\n";
//
//    private final int FLOAT_SIZE_BYTES = 4;
//
//    private Bitmap  watermark;
//
//    private int shaderProgram;
//    private int texSamplerHandle;
//    private int texCoordHandle;
//    private int posCoordHandle;
//    private FloatBuffer texVertices;
//    private FloatBuffer posVertices;
//
//    private int[] textureHandles = new int[1];
//
//    @Override
//    public void configureOpenGL() {
//
//        shaderProgram = GLES30.glCreateProgram();
//        GLES30.glAttachShader(shaderProgram, EglUtil.loadShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER));
//        GLES30.glAttachShader(shaderProgram, EglUtil.loadShader(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER));
//        GLES30.glLinkProgram(shaderProgram);
//
//        int[] linkStatus = new int[1];
//        GLES30.glGetProgramiv(shaderProgram, GLES30.GL_LINK_STATUS, linkStatus, 0);
//        if (linkStatus[0] != GLES30.GL_TRUE) {
//            String info = GLES30.glGetProgramInfoLog(shaderProgram);
//            GLES30.glDeleteProgram(shaderProgram);
//            shaderProgram = 0;
//            throw new RuntimeException("Could not link program: " + info);
//        }
//
//        texSamplerHandle = GLES30.glGetUniformLocation(shaderProgram, "tex_sampler");
//        texCoordHandle = GLES30.glGetAttribLocation(shaderProgram, "a_texcoord");
//        posCoordHandle = GLES30.glGetAttribLocation(shaderProgram, "a_position");
//    }
//
//    @Override
//    public void createBuffers() {
//        ByteBuffer byteBuffer;
//
//        byteBuffer = ByteBuffer.allocateDirect(TEX_VERTICES.length * FLOAT_SIZE_BYTES);
//        byteBuffer.order(ByteOrder.nativeOrder());
//        texVertices = byteBuffer.asFloatBuffer();
//        texVertices.put(TEX_VERTICES);
//        texVertices.position(0);
//
//        byteBuffer = ByteBuffer.allocateDirect(POS_VERTICES.length * FLOAT_SIZE_BYTES);
//        byteBuffer.order(ByteOrder.nativeOrder());
//        posVertices = byteBuffer.asFloatBuffer();
//        posVertices.put(POS_VERTICES);
//        posVertices.position(0);
//    }
//
//    @Override
//    public void draw() {
//
//        // Select the program.
//        GLES30.glUseProgram(shaderProgram);
//
//        GLES30.glEnable(GLES30.GL_BLEND);
//        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA );
//
//        // Set the vertex attributes
//        GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 0, texVertices);
//        GLES30.glEnableVertexAttribArray(texCoordHandle);
//        GLES30.glVertexAttribPointer(posCoordHandle, 2, GLES30.GL_FLOAT, false, 0, posVertices);
//        GLES30.glEnableVertexAttribArray(posCoordHandle);
//
//        // Set the input texture
//        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
//        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandles[0]);
//        GLES30.glUniform1i(texSamplerHandle, 0);
//
//        GLES30.glGenTextures(textureHandles.length, textureHandles, 0);
//        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandles[0]);
//
//        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, watermark, 0);
//
//        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
//        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
//        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
//        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
//
//        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
//
//        GLES30.glDisableVertexAttribArray(texCoordHandle);
//        GLES30.glDisableVertexAttribArray(posCoordHandle);
//
//        GLES30.glDisable(GLES30.GL_BLEND);
//
//        GLES30.glUseProgram(0);
//    }
//
//
//
//    public void setWatermarkTexture(Bitmap bitmap) {
//        watermark = bitmap;
//    }
//
//    @Override
//    public void onSurfaceCreated(EGLConfig config) {
//
//    }
//
//    @Override
//    public void onSurfaceChanged(int width, int height) {
//
//    }
//
//    @Override
//    public void onDrawFrame(EFramebufferObject fbo) {
//
//    }
}
