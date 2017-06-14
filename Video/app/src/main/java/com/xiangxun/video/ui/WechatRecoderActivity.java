package com.xiangxun.video.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangxun.video.R;
import com.xiangxun.video.base.APP;
import com.xiangxun.video.base.BaseActivity;
import com.xiangxun.video.base.OnDialogListener;
import com.xiangxun.video.camera.MediaRecorderBase;
import com.xiangxun.video.camera.MediaRecorderNative;
import com.xiangxun.video.camera.VCamera;
import com.xiangxun.video.camera.model.MediaObject;
import com.xiangxun.video.camera.util.DeviceUtils;
import com.xiangxun.video.camera.util.FileUtils;
import com.xiangxun.video.common.CommonCons;
import com.xiangxun.video.common.ConvertToUtils;
import com.xiangxun.video.common.RecoderAttrs;
import com.xiangxun.video.wedget.ProgressImage;
import com.yixia.videoeditor.adapter.UtilityAdapter;

import java.io.File;
import java.util.ArrayList;

/**
 * @Author maimingliang@gmail.com
 * <p/>
 * Created by maimingliang on 2016/9/25.
 */
public class WechatRecoderActivity extends BaseActivity implements MediaRecorderBase.OnErrorListener, MediaRecorderBase.OnEncodeListener {


    /**
     * 宽高比
     */
    private static int WIDTH_RATIO = 3;
    private static int HEIGHT_RATIO = 4;

    /**
     * 录制最长时间
     */
    public static int RECORD_TIME_MAX = 10 * 1000;
    /**
     * 录制最小时间
     */
    public static int RECORD_TIME_MIN = 3 * 1000;

    /**
     * 按住拍偏移距离
     */
    private static float OFFSET_DRUTION = 25.0f;

    /**
     * titel_bar 取消颜色
     */
    private static int TITEL_BAR_CANCEL_TEXT_COLOR = 0xFF00FF00;


    /**
     * 按住拍 字体颜色
     */
    private static int PRESS_BTN_COLOR = 0xFF00FF00;

    /**
     * progress 小于录制最少时间的颜色
     */
    private static int LOW_MIN_TIME_PROGRESS_COLOR = 0xFFFC2828;
    /**
     * progress 颜色
     */
    private static int PROGRESS_COLOR = 0xFF00FF00;

    private static int PRESS_BTN_BG = 0xFF00FF00;

    /**
     * 对焦图片宽度
     */
    private int mFocusWidth;
    /**
     * 底部背景色
     */
    private int mBackgroundColorNormal, mBackgroundColorPress;
    /**
     * 屏幕宽度
     */
    private int mWindowWidth;

    /**
     * SDK视频录制对象
     */
    private MediaRecorderBase mMediaRecorder;
    /**
     * 视频信息
     */
    private MediaObject mMediaObject;

    /**
     * 对焦动画
     */
    private Animation mFocusAnimation;
    private boolean mCreated;


    private boolean isCancelRecoder;
    private boolean isRecoder;

    private Handler mHandler = new Handler();


    private static OnDialogListener mOnDialogListener;

    TextView mTvRecorderCancel;
    TextView mTvSelectVideo;
    RelativeLayout mLayoutHeader;
    SurfaceView mSurfaceView;
    ImageView mImgRecordFocusing;
    RelativeLayout mRlRecoderSurfaceview;
    ProgressImage mBtnPress;
    LinearLayout mRlRecorderBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mCreated = false;
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏
        setContentView(R.layout.activity_wechat_recoder);
        initCustomerAttrs();
        initView();
        initData();
        mCreated = true;
    }


    private void initView() {

        mTvRecorderCancel = (TextView) findViewById(R.id.tv_recorder_cancel);
        mTvSelectVideo = (TextView) findViewById(R.id.tv_select_video);
        mLayoutHeader = (RelativeLayout) findViewById(R.id.layout_header);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mImgRecordFocusing = (ImageView) findViewById(R.id.img_record_focusing);
        mRlRecoderSurfaceview = (RelativeLayout) findViewById(R.id.rl_recoder_surfaceview);
        mBtnPress = (ProgressImage) findViewById(R.id.btn_press);
        mRlRecorderBottom = (LinearLayout) findViewById(R.id.rl_recorder_bottom);
        mBtnPress.setMax(RECORD_TIME_MAX);
        mBtnPress.setCricleProgressColor(R.color.blue);
        mBtnPress.setRoundWidth(20);

    }


    @Override
    protected void onResume() {
        super.onResume();
        UtilityAdapter.freeFilterParser();
        UtilityAdapter.initFilterParser();

        if (mMediaRecorder == null) {
            initMediaRecorder();
        } else {
            mMediaRecorder.prepare();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRecord();
        UtilityAdapter.freeFilterParser();
        if (mMediaRecorder != null)
            mMediaRecorder.release();

    }

    /**
     * 初始化拍摄SDK
     */
    private void initMediaRecorder() {
        mMediaRecorder = new MediaRecorderNative();

        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setOnEncodeListener(this);
        File f = new File(VCamera.getVideoCachePath());
        if (!FileUtils.checkFile(f)) {
            f.mkdirs();
        }
        String key = String.valueOf(System.currentTimeMillis());
        mMediaObject = mMediaRecorder.setOutputDirectory(key,
                VCamera.getVideoCachePath() + key);
        mMediaRecorder.setSurfaceHolder(mSurfaceView.getHolder());
        mMediaRecorder.prepare();
    }

    /**
     * 手动对焦
     */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private boolean checkCameraFocus(MotionEvent event) {
        mImgRecordFocusing.setVisibility(View.GONE);
        float x = event.getX();
        float y = event.getY();
        float touchMajor = event.getTouchMajor();
        float touchMinor = event.getTouchMinor();

        Rect touchRect = new Rect((int) (x - touchMajor / 2),
                (int) (y - touchMinor / 2), (int) (x + touchMajor / 2),
                (int) (y + touchMinor / 2));
        // The direction is relative to the sensor orientation, that is, what
        // the sensor sees. The direction is not affected by the rotation or
        // mirroring of setDisplayOrientation(int). Coordinates of the rectangle
        // range from -1000 to 1000. (-1000, -1000) is the upper left point.
        // (1000, 1000) is the lower right point. The width and height of focus
        // areas cannot be 0 or negative.
        // No matter what the zoom level is, (-1000,-1000) represents the top of
        // the currently visible camera frame
        if (touchRect.right > 1000)
            touchRect.right = 1000;
        if (touchRect.bottom > 1000)
            touchRect.bottom = 1000;
        if (touchRect.left < 0)
            touchRect.left = 0;
        if (touchRect.right < 0)
            touchRect.right = 0;

        if (touchRect.left >= touchRect.right
                || touchRect.top >= touchRect.bottom)
            return false;

        ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
        focusAreas.add(new Camera.Area(touchRect, 1000));
        if (!mMediaRecorder.manualFocus(new Camera.AutoFocusCallback() {

            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                // if (success) {
                mImgRecordFocusing.setVisibility(View.GONE);
                // }
            }
        }, focusAreas)) {
            mImgRecordFocusing.setVisibility(View.GONE);
        }

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mImgRecordFocusing
                .getLayoutParams();
        int left = touchRect.left - (mFocusWidth / 2);// (int) x -
        // (focusingImage.getWidth()
        // / 2);
        int top = touchRect.top - (mFocusWidth / 2);// (int) y -
        // (focusingImage.getHeight()
        // / 2);
        if (left < 0)
            left = 0;
        else if (left + mFocusWidth > mWindowWidth)
            left = mWindowWidth - mFocusWidth;
        if (top + mFocusWidth > mWindowWidth)
            top = mWindowWidth - mFocusWidth;

        lp.leftMargin = left;
        lp.topMargin = top;
        mImgRecordFocusing.setLayoutParams(lp);
        mImgRecordFocusing.setVisibility(View.VISIBLE);

        if (mFocusAnimation == null)
            mFocusAnimation = AnimationUtils.loadAnimation(this,
                    R.anim.record_focus);

        mImgRecordFocusing.startAnimation(mFocusAnimation);

//		mHandler.sendEmptyMessageDelayed(HANDLE_HIDE_RECORD_FOCUS, 3500);// 最多3.5秒也要消失
        return true;
    }


    private void initCustomerAttrs() {


        int maxTime = getIntent().getIntExtra(CommonCons.RECORD_TIME_MAX, 0);

        if (maxTime != 0) {
            RECORD_TIME_MAX = maxTime;
        }

        int minTime = getIntent().getIntExtra(CommonCons.RECORD_TIME_MIN, 0);

        if (minTime != 0) {
            RECORD_TIME_MIN = minTime;
        }

        int offset = getIntent().getIntExtra(CommonCons.OFFSET_DRUTION, 0);

        if (offset != 0) {
            OFFSET_DRUTION = offset;
        }

        int cancelColor = getIntent().getIntExtra(CommonCons.TITEL_BAR_CANCEL_TEXT_COLOR, 0);

        if (cancelColor != 0) {
            TITEL_BAR_CANCEL_TEXT_COLOR = cancelColor;
        }

        int btnColor = getIntent().getIntExtra(CommonCons.PRESS_BTN_COLOR, 0);

        if (btnColor != 0) {
            PRESS_BTN_COLOR = btnColor;
        }

        int minTimeProgressColor = getIntent().getIntExtra(CommonCons.LOW_MIN_TIME_PROGRESS_COLOR, 0);

        if (minTimeProgressColor != 0) {
            LOW_MIN_TIME_PROGRESS_COLOR = minTimeProgressColor;
        }
        int color = getIntent().getIntExtra(CommonCons.PROGRESS_COLOR, 0);


        if (color != 0) {
            PROGRESS_COLOR = color;
        }

        int pressbg = getIntent().getIntExtra(CommonCons.PRESS_BTN_BG, 0);

        if (pressbg != 0) {
            PRESS_BTN_BG = pressbg;
        }

    }


    private void initData() {
        mWindowWidth = DeviceUtils.getScreenWidth(this);

        mFocusWidth = ConvertToUtils.dipToPX(this, 64);
        try {
            mImgRecordFocusing.setImageResource(R.drawable.ms_video_focus_icon);
        } catch (OutOfMemoryError e) {
            Log.e("maiml", e.getMessage());
        }

        mTvRecorderCancel.setTextColor(TITEL_BAR_CANCEL_TEXT_COLOR);

        setListener();
    }


    private void startRecoder() {
        if (!APP.isAvailableSpace()) {
            return;
        }
        isCancelRecoder = false;
        if (mMediaRecorder == null) {
            return;
        }
        MediaObject.MediaPart part = mMediaRecorder.startRecord();
        if (part == null) {
            return;
        }
        isRecoder = true;

    }


    private void startEncoding() {
        mMediaRecorder.startEncoding();
    }


    private void stopAll() {
        stopRecord();
        isRecoder = false;


    }

    private void releaseCancelRecoder() {
        isCancelRecoder = true;
    }

    private void slideCancelRecoder() {
        isCancelRecoder = false;
    }

    private void recoderShortTime() {
        removeRecoderPart();
        mHandler.postDelayed(mRunable, 1000l);
    }

    private void hideRecoderTxt() {
    }

    private void removeRecoderPart() {
        // 回删
        if (mMediaObject != null) {
            mMediaObject.removeAllPart();

        }
    }


    /**
     * 停止录制
     */
    private void stopRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stopRecord();
        }
    }

    private Runnable mRunable = new Runnable() {
        @Override
        public void run() {
            hideRecoderTxt();
        }
    };


    private void setListener() {
        if (DeviceUtils.hasICS()) {
            mSurfaceView.setOnTouchListener(onSurfaveViewTouchListener);
        }

        mBtnPress.setOnTouchListener(onVideoRecoderTouchListener);

        mTvRecorderCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    /**
     * 点击屏幕录制
     */
    private View.OnTouchListener onVideoRecoderTouchListener = new View.OnTouchListener() {
        private float startY;
        private float moveY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mMediaRecorder == null) {
                return false;
            }

            switch (event.getAction()) {

                default:
                    return true;
                case MotionEvent.ACTION_DOWN:

                    startY = event.getY();
                    //startRecoderAnim(mBtnPress);
                    mBtnPress.start();
                    startRecoder();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int durationMove = mMediaObject.getDuration();
                    if (durationMove >= RECORD_TIME_MAX) {
                        stopAll();
                        // stopRecoderAnim(mBtnPress);
                        mBtnPress.stop();
                        return true;
                    }
                    moveY = event.getY();
                    float drution = moveY - startY;

                    if ((drution > 0.0f) && Math.abs(drution) > OFFSET_DRUTION) {
                        slideCancelRecoder();

                    }
                    if ((drution < 0.0f) && (Math.abs(drution) > OFFSET_DRUTION)) {
                        releaseCancelRecoder();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    stopAll();
                    //stopRecoderAnim(mBtnPress);
                    mBtnPress.stop();
                    if (isCancelRecoder) {
                        hideRecoderTxt();
                        removeRecoderPart();
                        return true;
                    }
                    int duration = mMediaObject.getDuration();
                    if (duration < RECORD_TIME_MIN) {
                        recoderShortTime();
                        return true;
                    }
                    startEncoding();
                    break;

            }

            return true;


        }

    };


    /**
     * 点击屏幕录制
     */
    private View.OnTouchListener onSurfaveViewTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mMediaRecorder == null || !mCreated) {
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 检测是否手动对焦
                    if (checkCameraFocus(event))
                        return true;
                    break;
            }
            return true;
        }

    };


    @Override
    public void onVideoError(int what, int extra) {

    }

    @Override
    public void onAudioError(int what, String message) {

    }

    @Override
    public void onEncodeStart() {

        if (mOnDialogListener != null) {
            mOnDialogListener.onShowDialog(this);
        } else {
            showProgress("", "正在处理中...");

        }
    }

    @Override
    public void onEncodeProgress(int progress) {

    }

    @Override
    public void onEncodeComplete() {
        if (mOnDialogListener != null) {
            mOnDialogListener.onHideDialog(this);
        } else {
            hideProgress();
        }
        String outputVideoPath = mMediaObject.getOutputVideoPath();
        //编码完成后，直接调用播放器，不用跳转回原始页面。
        Intent data = new Intent(this, PlayActivity.class);
        data.putExtra("path", outputVideoPath);
        startActivity(data);
    }

    @Override
    public void onEncodeError() {
        if (mOnDialogListener != null) {
            mOnDialogListener.onHideDialog(this);
        } else {
            hideProgress();
        }
        Toast.makeText(this, "视频转码失败",
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        if (mMediaObject != null)
            mMediaObject.delete();
        finish();
        overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
    }


    public static void launchActivity(Context context, RecoderAttrs attrs, int requestCode) {

        if (context instanceof OnDialogListener) {
            mOnDialogListener = (OnDialogListener) context;
        }

        Bundle bundle = new Bundle();
        Intent intent = new Intent(context, WechatRecoderActivity.class);


        if (attrs != null) {
            bundle.putInt(CommonCons.RECORD_TIME_MAX, attrs.getRecoderTimeMax());
            bundle.putInt(CommonCons.RECORD_TIME_MIN, attrs.getRecoderTimeMin());
            bundle.putInt(CommonCons.TITEL_BAR_CANCEL_TEXT_COLOR, attrs.getTitelBarCancelTextColor());
            bundle.putInt(CommonCons.PRESS_BTN_COLOR, attrs.getPressBtnColor());
            bundle.putInt(CommonCons.OFFSET_DRUTION, attrs.getOffsetDrution());
            bundle.putInt(CommonCons.LOW_MIN_TIME_PROGRESS_COLOR, attrs.getLowMinTimeProgressColor());
            bundle.putInt(CommonCons.PROGRESS_COLOR, attrs.getProgressColor());
            bundle.putInt(CommonCons.PRESS_BTN_BG, attrs.getPressBtnBg());
            intent.putExtras(bundle);
        }

        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    public static void launchActivity(Context context, int requestCode) {
        launchActivity(context, null, requestCode);
    }

}
