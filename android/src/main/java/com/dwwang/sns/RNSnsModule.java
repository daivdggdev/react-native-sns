package com.dwwang.sns;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.util.Base64;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
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
import java.util.List;
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

        context.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "RNSns";
    }

    @ReactMethod
    public void setUmSocialAppkey(String appKey) {
        //UMShareAPI.init(mContext, appKey);

        // 三方获取用户资料时每次都要进行授权
        UMShareConfig config = new UMShareConfig();
        config.isNeedAuthOnGetUserInfo(true);
        UMShareAPI.get(mContext).setShareConfig(config);
    }

    @ReactMethod
    public void openLog(Boolean isOpen) {
        // umeng暂没有找到对应的接口
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
    public boolean isInstall(String type) {
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

        UMShareAPI umShareAPI = UMShareAPI.get(mContext);
        Activity activity = mContext.getCurrentActivity();
        return umShareAPI.isInstall(activity, platform);
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
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    WritableMap params = Arguments.createMap();
                    params.putString("uid", jsonObject.getString("uid"));
                    params.putString("username", jsonObject.getString("name"));
                    params.putString("iconUrl", jsonObject.getString("iconurl"));
                    params.putString("gender", jsonObject.getString("gender"));

                    // 微博返回的openid为空
                    if (jsonObject.has("openid")) {
                        params.putString("openId", jsonObject.getString("openid"));
                    } else if (jsonObject.has("uid")) {
                        params.putString("openId", jsonObject.getString("uid"));
                    }

                    promise.resolve(params);
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
    public void showShareMenuView(String url, String title, String imageUrl, String description, final Promise promise) {
        Activity activity = mContext.getCurrentActivity();
        UMShareListener umShareListener = new UMShareListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
                Log.i(TAG, "showShareMenuView onStart");
            }

            @Override
            public void onResult(SHARE_MEDIA share_media) {
                Log.i(TAG, "showShareMenuView onResult");
                promise.resolve(true);
            }

            @Override
            public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                Log.i(TAG, "showShareMenuView onError = " + throwable.getLocalizedMessage());
                promise.reject("E_FAILED_TO_SHARE", throwable.getLocalizedMessage());
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media) {
                Log.i(TAG, "showShareMenuView onCancel");
                promise.reject("E_USER_CANCEL", "取消分享");
            }
        };

        UMImage thumb = new UMImage(mContext, imageUrl);
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

    @ReactMethod
    public void showShareMenuViewWithImage(String imageBase64, final Promise promise) {
        Activity activity = mContext.getCurrentActivity();
        UMShareListener umShareListener = new UMShareListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
                Log.i(TAG, "showShareMenuView onStart");
            }

            @Override
            public void onResult(SHARE_MEDIA share_media) {
                Log.i(TAG, "showShareMenuView onResult");
                promise.resolve(true);
            }

            @Override
            public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                Log.i(TAG, "showShareMenuView onError = " + throwable.getLocalizedMessage());
                promise.reject("E_FAILED_TO_SHARE", throwable.getLocalizedMessage());
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media) {
                Log.i(TAG, "showShareMenuView onCancel");
                promise.reject("E_USER_CANCEL", "取消分享");
            }
        };

        byte[] b = Base64.decode(imageBase64, Base64.DEFAULT);
        UMImage image = new UMImage(mContext, b);

        new ShareAction(activity)
                .withMedia(image)
                .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ, SHARE_MEDIA.SINA)
                .setCallback(umShareListener)
                .open();
    }

    @ReactMethod
    public void hasAnyMarketInstalled(Promise promise) {
        boolean isInstalled = false;

        try {
            Intent intent = new Intent();
            intent.setData(Uri.parse("market://details?id=android.browser"));
            List<ResolveInfo> list = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            isInstalled = (list.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        promise.resolve(isInstalled);
    }

    @ReactMethod
    public void appraiseInMarket() {
        try {
            Activity activity = mContext.getCurrentActivity();
            String packageName = activity.getPackageName();

            Uri uri = Uri.parse("market://details?id=" + packageName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
