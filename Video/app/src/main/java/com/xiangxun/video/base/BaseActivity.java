package com.xiangxun.video.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Window;

import com.google.gson.Gson;
import com.xiangxun.video.camera.model.MediaObject;
import com.xiangxun.video.camera.model.MediaObject.MediaPart;
import com.xiangxun.video.camera.util.FileUtils;
import com.xiangxun.video.camera.util.Log;
import com.xiangxun.video.camera.util.StringUtils;
import com.xiangxun.video.wedget.ShowLoading;

import java.io.File;
import java.io.FileOutputStream;

public class BaseActivity extends Activity {

    protected ShowLoading loading;

    public ShowLoading showProgress(String title, String message) {
        return showProgress(title, message, -1);
    }

    public ShowLoading showProgress(String title, String message, int theme) {
        if (loading == null) {
            if (theme > 0)
                loading = new ShowLoading(this, theme);
            else
                loading = new ShowLoading(this);
        }

        if (!StringUtils.isEmpty(title))
            loading.setTitle(title);
        loading.setMessage(message);
        loading.show();
        return loading;
    }

    public void hideProgress() {
        if (loading != null) {
            loading.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        hideProgress();
        loading = null;
    }

    /**
     * 反序列化对象
     */
    protected static MediaObject restoneMediaObject(String obj) {
        try {
            String str = FileUtils.readFile(new File(obj));
            Gson gson = new Gson();
            MediaObject result = gson.fromJson(str.toString(),
                    MediaObject.class);
            result.getCurrentPart();
            preparedMediaObject(result);
            return result;
        } catch (Exception e) {
            if (e != null)
                Log.e("VCamera", "readFile", e);
        }
        return null;
    }

    /**
     * 预处理数据对象
     */
    public static void preparedMediaObject(MediaObject mMediaObject) {
        if (mMediaObject != null && mMediaObject.getMedaParts() != null) {
            int duration = 0;
            for (MediaPart part : mMediaObject.getMedaParts()) {
                part.startTime = duration;
                part.endTime = part.startTime + part.duration;
                duration += part.duration;
            }
        }
    }

    /**
     * 序列号保存视频数据
     */
    public static boolean saveMediaObject(MediaObject mMediaObject) {
        if (mMediaObject != null) {
            try {
                if (StringUtils.isNotEmpty(mMediaObject.getObjectFilePath())) {
                    FileOutputStream out = new FileOutputStream(
                            mMediaObject.getObjectFilePath());
                    Gson gson = new Gson();
                    out.write(gson.toJson(mMediaObject).getBytes());
                    out.flush();
                    out.close();
                    return true;
                }
            } catch (Exception e) {
                Log.e("maiml", e);
            }
        }
        return false;
    }
}
