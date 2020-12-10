package com.devmcry.imageprocessor.ui.opengl.filter;

class WaterMarkFilter extends GlFilter {
    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = aPosition;\n" +
                    "  vTextureCoord = aTextureCoord;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    public WaterMarkFilter() {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
    }
}
