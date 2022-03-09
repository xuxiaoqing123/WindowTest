package com.example.myapplication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.Iterator;
import java.util.Set;

import androidx.core.app.ActivityCompat;
import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;

public class App extends Application {

    private static final String TAG = "AppHook";

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.d(TAG, "Application onCreate");
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
//        Log.d(TAG, "Application attachBaseContext");
        hookRequestPermissions();
    }

    private void hookRequestPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
//        DexposedBridge.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                Log.d(TAG, "before hook:" + param.toString() + ", onCreate..");
//            }
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Log.d(TAG, "after hook:" + param.toString() + ", onCreate..");
//            }
//        });
//
//        DexposedBridge.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                Log.d(TAG, "before hook:" + param.toString() + ", onResume..");
//            }
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Log.d(TAG, "after hook:" + param.toString() + ", onResume..");
//            }
//        });

        DexposedBridge.findAndHookMethod(ActivityCompat.class, "requestPermissions", Activity.class, String[].class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
//                Log.d(TAG, "before hook:" + param.toString() + ", requestPermissions..");
                if (param.args != null && param.args.length > 2 && param.args[1] != null) {
                    if (param.args[1] instanceof String[]) {
                        String[] permissions = (String[])param.args[1];
                        int size = permissions.length;
                        if (size > 0) {
                            for (int i = 0; i < size; i++) {
//                                Log.d(TAG, "hook requestPermissions.. " + permissions[i]);
                            }
                        }
                    }
                }
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
//                Log.d(TAG, "after hook:" + param.toString() + ", requestPermissions..");
            }
        });

        DexposedBridge.findAndHookMethod(Activity.class, "startActivityForResult", String.class, Intent.class, int.class, Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d(TAG, "before hook startActivityForResult..");

                if (param.args != null && param.args.length > 2 && param.args[0] != null && param.args[1] != null) {
                    if (param.args[0] instanceof String) {
                        String requestCode = (String) param.args[0];
                        if (TextUtils.equals(requestCode, "@android:requestPermissions:")) { //权限弹窗
                            Log.d(TAG, "hook startActivityForResult for permission");
                            if (param.args[1] instanceof Intent) {
                                Intent permissionIntent = (Intent)param.args[1];
                                String[] permissions = permissionIntent.getStringArrayExtra("android.content.pm.extra.REQUEST_PERMISSIONS_NAMES");
                                int size = permissions.length;
                                if (size > 0) {
                                    for (int i = 0; i < size; i++) {
                                        Log.d(TAG, "hook requestPermissions.. " + permissions[i]);
                                    }
                                }
                            }
                        }
                    }

                }
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "after hook startActivityForResult..");
            }
        });





//        try {
//            Pine.hook(Activity.class.getDeclaredMethod("onCreate", Bundle.class), new MethodHook() {
//                @Override
//                public void beforeCall(Pine.CallFrame callFrame) {
//                    Log.d(TAG, "hook Before " + callFrame.thisObject + " onCreate()");
//                    System.out.println("Activity verifyStoragePermissions" + callFrame.thisObject + " onCreate()");
//                }
//
//                @Override
//                public void afterCall(Pine.CallFrame callFrame) {
//                    Log.d(TAG, "hook After " + callFrame.thisObject + " onCreate()");
//                }
//            });
//            Pine.hook(Activity.class.getDeclaredMethod("onResume"), new MethodHook() {
//                @Override
//                public void beforeCall(Pine.CallFrame callFrame) {
//                    Log.d(TAG, "hook Before " + callFrame.thisObject + " onResume()");
//                }
//
//                @Override
//                public void afterCall(Pine.CallFrame callFrame) {
//                    Log.d(TAG, "hook After " + callFrame.thisObject + " onResume()");
//                }
//            });
//            Pine.hook(ActivityCompat.class.getDeclaredMethod("requestPermissions", Activity.class, String[].class, int.class), new MethodHook() {
//                @Override
//                public void beforeCall(Pine.CallFrame callFrame) {
//                    Log.d(TAG, "hook Before " + callFrame.thisObject + " requestPermissions()");
//                }
//
//                @Override
//                public void afterCall(Pine.CallFrame callFrame) {
//                    Log.d(TAG, "hook After " + callFrame.thisObject + " requestPermissions()");
//                }
//            });
//
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//            Log.d(TAG, e.getStackTrace().toString());
//        }
    }


}
