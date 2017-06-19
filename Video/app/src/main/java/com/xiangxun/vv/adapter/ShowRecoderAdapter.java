package com.xiangxun.vv.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangxun.video.adapter.ParentAdapter;
import com.xiangxun.vv.R;

import java.io.File;
import java.util.List;

/**
 * Created by Zhangyuhui/Darly on 2017/6/14.
 * Copyright by [Zhangyuhui/Darly]
 * ©2017 XunXiang.Company. All rights reserved.
 *
 * @TODO:
 */
public class ShowRecoderAdapter extends ParentAdapter<File> {
    public ShowRecoderAdapter(List<File> data, int resID, Context context) {
        super(data, resID, context);
    }

    @Override
    public View HockView(int position, View view, ViewGroup parent, int resID, Context context, File file) {

        ViewHocker hocker = null;
        if (view == null) {
            hocker = new ViewHocker();
            view = LayoutInflater.from(context).inflate(resID, null);
            hocker.iv = (ImageView) view.findViewById(R.id.id_item_show_iv);
            hocker.tv = (TextView) view.findViewById(R.id.id_item_show_tv);
            view.setTag(hocker);
        } else {
            hocker = (ViewHocker) view.getTag();
        }
        hocker.iv.setImageBitmap(getVideoThumb(file.getPath()));
        hocker.tv.setText(file.getName() + "--" + (file.length() / 1024) + "KB");
        return view;
    }

    class ViewHocker {
        ImageView iv;
        TextView tv;
    }


    /**
     * 获取视频文件缩略图 API>=8(2.2)
     *
     * @param path 视频文件的路径
     * @param kind 缩略图的分辨率：MINI_KIND、MICRO_KIND、FULL_SCREEN_KIND
     * @return Bitmap 返回获取的Bitmap
     */
    public static Bitmap getVideoThumb(String path, int kind) {
        return ThumbnailUtils.createVideoThumbnail(path, kind);
    }

    public static Bitmap getVideoThumb(String path) {
        return getVideoThumb(path, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
    }
}
