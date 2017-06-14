package com.xiangxun.video.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.xiangxun.video.R;
import com.xiangxun.video.adapter.ShowRecoderAdapter;
import com.xiangxun.video.common.CommonCons;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhangyuhui/Darly on 2017/6/14.
 * Copyright by [Zhangyuhui/Darly]
 * ©2017 XunXiang.Company. All rights reserved.
 *
 * @TODO: 以GridView展示列表效果。
 */
public class ShowRecoderActivity extends Activity implements OnItemClickListener {

    private GridView gv;

    private ShowRecoderAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        File file = new File(CommonCons.ROOT);
        File[] list = file.listFiles();

        List<File> fils = new ArrayList<File>();
        for (int i = 0; i < list.length; i++) {
            if (list[i].isFile()) {
                fils.add(list[i]);
            }
        }
        gv = (GridView) findViewById(R.id.id_show_gridview);
        adapter = new ShowRecoderAdapter(fils, R.layout.item_show, this);
        gv.setAdapter(adapter);

        gv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File f = (File) parent.getItemAtPosition(position);
        Intent data = new Intent(this, PlayActivity.class);
        data.putExtra("path", f.getPath());
        startActivity(data);
    }
}
