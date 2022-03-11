package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnTouchListener {
    private static final String TAG = "MainActivity";

    public static final int OVERLAY_PERMISSION_REQUEST_CODE_THREAD = 1025;
    private int mTouchSlop;
    int startX = 0, startY = 0;  //ACTION_DOWN时的rawX,rawY
    int originX = 0, originY = 0; //ACTION_DOWN时的悬浮窗x/y坐标
    boolean isPerformClick = false;  //是否点击
    Button mFloatingButton;
    WindowManager.LayoutParams mLayoutParams;
    WindowManager mWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Activity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Activity onResume");
        super.onResume();

        if (hasGrantedAlertWindow()) { //已经获取悬浮层权限
            showFloatWindow();
        } else {
            permissionAlertWindow(); // 申请悬浮层权限
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE_THREAD) {
            if (!hasGrantedAlertWindow()) {
                Toast.makeText(this, "请前往设置页面打开应用悬浮窗权限", Toast.LENGTH_LONG).show();
                finish();
            } else {
                showFloatWindow();
            }
        }
    }

    /**
     * 显示悬浮窗
     *
     */
    private void showFloatWindow() {
        mWindowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        mFloatingButton = new Button(this);
        mFloatingButton.setText("button");
        mFloatingButton.setOnTouchListener(this);
        mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //8.0及以上
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParams.x = 0;
        mLayoutParams.y = 0;
        mWindowManager.addView(mFloatingButton, mLayoutParams);

        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        System.out.println("mTouchSlop-->" + mTouchSlop);
    }

    /**
     * 悬浮窗权限检查
     */
    private void permissionAlertWindow() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //8.0以上
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE_THREAD);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //6.0以上
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE_THREAD);
            }
        } catch (Throwable e) {
            Toast.makeText(this, "请前往设置页面打开应用悬浮窗权限", Toast.LENGTH_LONG).show();
        }
    }

    //判断android 6.0以上alertWindow权限 悬浮窗权限
    public boolean hasGrantedAlertWindow() {
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this);//检查指定的上下文能否绘制悬浮窗
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) event.getRawX();
                startY = (int) event.getRawY();
                originX = mLayoutParams.x;
                originY = mLayoutParams.y;
                System.out.println("startX-->" + startX);
                System.out.println("startY-->" + startY);
                isPerformClick = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                //判断是CLICK还是MOVE,只要移动过，就认为不是点击
                if (Math.abs(startX - event.getRawX()) >= mTouchSlop || Math.abs(startY - event.getRawY()) >= mTouchSlop) {
                    isPerformClick = false;
                }
                mLayoutParams.x = (int) (event.getRawX() - startX + originX);
                mLayoutParams.y = (int) (event.getRawY() - startY + originY);
                System.out.println("getRawX-->" + event.getRawX());
                System.out.println("getRawY-->" + event.getRawY());
                mWindowManager.updateViewLayout(mFloatingButton, mLayoutParams);
                return true;
            case MotionEvent.ACTION_UP:
                if (isPerformClick) {
                    //点击事件的触发
                }
                return !isPerformClick;
            default:
                break;
        }
        return false;
    }
}
