package com.dwwang.sns;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class RNSnsModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNSnsModule";
    ReactApplicationContext mContext;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            UMShareAPI.get(mContext).onActivityResult(requestCode, resultCode, data);
        }
    };

    public RNSnsModule(ReactApplicationContext context) {
        super(context);
        this.mContext = context;

        // 三方获取用户资料时每次都要进行授权
        UMShareConfig config = new UMShareConfig();
        config.isNeedAuthOnGetUserInfo(true);
        UMShareAPI.get(context).setShareConfig(config);

        context.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "RNSns";
    }

    @ReactMethod
    public void setPlaform(String type, String appKey, String appSecret, String redirectUrl) {
        switch (type) {
            case "weixin":
                PlatformConfig.setWeixin(appKey, appSecret);
                break;

            case "weibo":
                PlatformConfig.setSinaWeibo(appKey, appSecret, redirectUrl);
                break;

            case "qq":
                PlatformConfig.setQQZone(appKey, appSecret);
                break;
        }
    }

    @ReactMethod
    public void getPlatformInfo(String type, final Promise promise) {
        SHARE_MEDIA platform = SHARE_MEDIA.QQ;
        switch (type) {
            case "weixin":
                platform = SHARE_MEDIA.WEIXIN;
                break;

            case "weibo":
                platform = SHARE_MEDIA.SINA;
                break;

            case "qq":
                platform = SHARE_MEDIA.QQ;
                break;
        }

        Activity activity = mContext.getCurrentActivity();
        UMShareAPI.get(mContext).getPlatformInfo(activity, platform, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> data) {
//                String temp = "";
//                for (String key : data.keySet()) {
//                    temp = temp + key + " : " + data.get(key) + "\n";
//                }
//
//                Log.i(TAG, "onComplete = " + temp);
                try {
                    String json = new JSONObject(data).toString();
                    promise.resolve(json);
                } catch (Exception e) {
                    promise.reject("E_DATA_PARSE_FAILED", e);
                }
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                promise.reject("E_FAILED_TO_GET_INFO", "error: " + throwable.getLocalizedMessage());
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                promise.reject("E_USER_CANCEL", "error: " + i);
            }
        });
    }

    @ReactMethod
    public void showShareMenuView(String url, String title, String description) {
        Activity activity = mContext.getCurrentActivity();
        UMShareListener umShareListener = new UMShareListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
                Log.i(TAG, "showShareMenuView onStart");
            }

            @Override
            public void onResult(SHARE_MEDIA share_media) {
                Log.i(TAG, "showShareMenuView onResult");
            }

            @Override
            public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                Log.i(TAG, "showShareMenuView onError = " + throwable.getLocalizedMessage());
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media) {
                Log.i(TAG, "showShareMenuView onCancel");
            }
        };

        UMImage thumb =  new UMImage(mContext, R.drawable.ic_launcher);
        UMWeb web = new UMWeb(url);
        web.setTitle(title);
        web.setThumb(thumb);
        web.setDescription(description);

        new ShareAction(activity)
                .withMedia(web)
                .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ, SHARE_MEDIA.SINA)
                .setCallback(umShareListener)
                .open();
    }

    @Override
    public Map<String, Object> getConstants() {
        UMShareAPI umShareAPI = UMShareAPI.get(mContext);
        Activity activity = mContext.getCurrentActivity();

        final Map<String, Object> constants = new HashMap<>();
        constants.put("isAndroid", true);
        constants.put("isWXSupport", umShareAPI.isSupport(activity, SHARE_MEDIA.WEIXIN));
        constants.put("isQQSupport", umShareAPI.isSupport(activity, SHARE_MEDIA.QQ));
        constants.put("isSinaSupport", umShareAPI.isSupport(activity, SHARE_MEDIA.SINA));
        return constants;
    }
}
