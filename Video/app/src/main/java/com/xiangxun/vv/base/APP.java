package com.xiangxun.vv.base;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.view.WindowManager;

import com.xiangxun.video.R;
import com.xiangxun.video.camera.VCamera;
import com.xiangxun.video.camera.util.DeviceUtils;
import com.xiangxun.video.camera.util.FileUtils;
import com.xiangxun.video.common.CommonCons;
import com.xiangxun.video.common.ToastApp;
import com.xiangxun.video.wedget.CircleProgress;

import java.io.File;

/**
 * Created by Zhangyuhui/Darly on 2017/6/13.
 * Copyright by [Zhangyuhui/Darly]
 * ©2017 XunXiang.Company. All rights reserved.
 *
 * @TODO:
 */
public class APP extends Application {

    private static APP instance;

    public static final String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VCVideo/";

    public static APP getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 设置拍摄视频缓存路径


        File boot = new File(ROOT);
        if (!boot.exists()) {
            boot.mkdir();
        }
        VCamera.setVideoCachePath(boot + "/recoder/");
        //  VCamera.setVideoCachePath(FileUtils.getRecorderPath());
        // 开启log输出,ffmpeg输出到logcat
        VCamera.setDebugMode(false);
        // 初始化拍摄SDK，必须
        VCamera.initialize(this);
    }

    public final static int AVAILABLE_SPACE = 500;//M

    /**
     * 检测用户手机是否剩余可用空间200M以上
     *
     * @return
     */
    public static boolean isAvailableSpace() {
        if (instance == null) {
            return false;
        }
        //检测磁盘空间
        if (FileUtils.showFileAvailable() < AVAILABLE_SPACE) {
            ToastApp.showToast(instance, instance.getString(R.string.record_check_available_faild, AVAILABLE_SPACE));
            return false;
        }
        return true;
    }
}
