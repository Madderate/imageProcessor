package com.devmcry.imageprocessor.ui.opengl.recorder;

import android.graphics.Bitmap;

/**
 * 录制数据
 *
 * @author Created by jz on 2017/5/2 11:10
 */
public interface OnRecordListener {
    void onRecord(Bitmap bitmap);
}