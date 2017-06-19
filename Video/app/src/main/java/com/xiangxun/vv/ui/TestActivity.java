package com.xiangxun.vv.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.xiangxun.video.common.ToastApp;
import com.xiangxun.video.ui.RecordFragment;
import com.xiangxun.video.ui.RecordFragment.OnResultBackListener;
import com.xiangxun.vv.R;

/**
 * Created by Zhangyuhui/Darly on 2017/6/19.
 * Copyright by [Zhangyuhui/Darly]
 * ©2017 XunXiang.Company. All rights reserved.
 *
 * @TODO: 建立一个Test类，调用Fragment
 */
public class TestActivity extends Activity implements OnResultBackListener {

    private FragmentManager fragmentManager;

    private RecordFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }

    private void initView() {
        initFragments(RecordFragment.class,
                R.id.test_fragment);
    }

    private void initFragments(Class<?> cls, int resId) {
        fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragment == null) {
            try {
                fragment = (RecordFragment) cls.newInstance();
                fragment.setOnResultBackListener(this);
                transaction.add(resId, fragment);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            if (fragment.isVisible())
                return;
            transaction.show(fragment);
        }
        transaction.commit();
    }

    @Override
    public void resultBack(String path) {
        ToastApp.showToast(path);
    }
}
