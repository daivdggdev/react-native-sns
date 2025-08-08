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
import com.umeng.socialize.media.UMMin;
import com.tencent.tauth.Tencent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RNSnsModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNSnsModule";
    ReactApplicationContext mContext;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        // @Override
        // public void onActivityResult(Activity activity, int requestCode, int
        // resultCode, Intent data) {
        // UMShareAPI.get(mContext).onActivityResult(requestCode, resultCode, data);
        // }

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            super.onActivityResult(activity, requestCode, resultCode, data);
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
        // UMShareAPI.init(mContext, appKey);

        // 三方获取用户资料时每次都要进行授权
        UMShareConfig config = new UMShareConfig();
        config.isNeedAuthOnGetUserInfo(true);
        UMShareAPI.get(mContext).setShareConfig(config);

        // QQ官方sdk授权
        Tencent.setIsPermissionGranted(true);
    }

    @ReactMethod
    public void openLog(Boolean isOpen) {
        // umeng暂没有找到对应的接口
    }

    @ReactMethod
    public void setPlaform(String type, String appKey, String appSecret, String redirectUrl, String fileProvider) {
        switch (type) {
            case "weixin":
                PlatformConfig.setWeixin(appKey, appSecret);
                PlatformConfig.setWXFileProvider(fileProvider);
                break;

            case "weibo":
                PlatformConfig.setSinaWeibo(appKey, appSecret, redirectUrl);
                PlatformConfig.setSinaFileProvider(fileProvider);
                break;

            case "qq":
                PlatformConfig.setQQZone(appKey, appSecret);
                PlatformConfig.setQQFileProvider(fileProvider);
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
                Log.i(TAG, "getPlatformInfo onStart");
            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> data) {
                try {
                    Log.i(TAG, "getPlatformInfo onComplete: " + data.toString());
                    JSONObject jsonObject = new JSONObject(data);
                    WritableMap params = Arguments.createMap();

                    String openId = jsonObject.optString("openid", "");
                    String uid = jsonObject.optString("uid", "");

                    params.putString("uid", uid);
                    params.putString("openId", openId);
                    params.putString("username", jsonObject.optString("name", ""));
                    params.putString("iconUrl", jsonObject.optString("iconurl", ""));
                    params.putString("gender", jsonObject.optString("gender", ""));

                    // 微博返回的openid为空，使用uid
                    if (openId == null || openId.isEmpty()) {
                        params.putString("openId", uid);
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
    public void showShareMenuView(String url, String title, String imageUrl, String description,
            final Promise promise) {
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
                .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ)
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
                .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ)
                .setCallback(umShareListener)
                .open();
    }

    @ReactMethod
    public void showShareMiniProgram(String url, String title, String imageUrl, String description, String path,
            String appId,
            final Promise promise) {
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

        // 兼容低版本的网页链接
        UMMin umMin = new UMMin(url);
        // 小程序消息封面图片
        umMin.setThumb(new UMImage(mContext, imageUrl));
        // 小程序消息title
        umMin.setTitle(title);
        // 小程序消息描述
        umMin.setDescription(description);
        // 小程序页面路径
        umMin.setPath(path);
        // 小程序原始id,在微信平台查询
        umMin.setUserName(appId);

        new ShareAction(activity)
                .withMedia(umMin)
                .setPlatform(SHARE_MEDIA.WEIXIN)
                .setCallback(umShareListener)
                .share();
    }

    @ReactMethod
    public void hasAnyMarketInstalled(Promise promise) {
        boolean isInstalled = false;

        try {
            Intent intent = new Intent();
            intent.setData(Uri.parse("market://details?id=android.browser"));
            List<ResolveInfo> list = mContext.getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
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
