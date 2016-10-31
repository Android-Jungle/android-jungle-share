/**
 * Android Jungle-Share framework project.
 *
 * Copyright 2016 Arno Zhang <zyfgood12@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jungle.share;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShareHelper {

    private static final int THUMB_IMAGE_SIZE = 120;


    public interface ShareImageLoader {
        public interface Callback {
            void onSuccess(Bitmap bitmap);

            void onFailed(int retCode);
        }

        void loadImage(String url, Callback callback);
    }


    public static enum ShareReturnType {
        SUCCESS,
        FAILED,
        CANCELED
    }


    public interface OnShareListener {
        void onSuccess();

        void onCancel();

        void onFailed(String message);
    }


    private Tencent mTencent;
    private IWXAPI mWXApi;
    private IWeiboShareAPI mWeiboShareAPI = null;
    private ShareImageLoader mShareImageLoader;
    private Map<String, WeakReference<OnShareListener>> mWXShareListenerList = new HashMap<>();
    private Map<String, WeakReference<OnShareListener>> mWBlogShareListenerList = new HashMap<>();


    public void setShareImageLoader(ShareImageLoader shareImageLoader) {
        mShareImageLoader = shareImageLoader;
    }

    public void destroy() {
        if (mWXApi != null) {
            mWXApi.unregisterApp();
        }
    }

    private void ensureTencent(Context context) {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(ShareUtils.getTencentApiId(context), context);
        }
    }

    private boolean ensureWXApi(final Activity activity) {
        if (mWXApi == null) {
            mWXApi = ShareUtils.createNewWXApi(activity, true);
        }

        return ShareUtils.ensureWXApi(activity, mWXApi);
    }

    private boolean ensureWblogShare(Activity activity) {
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(activity, ShareUtils.getSinaAppKey(activity));
        mWeiboShareAPI.registerApp();

        if (!mWeiboShareAPI.isWeiboAppInstalled()) {
            Toast.makeText(activity, R.string.wblog_not_installed, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public IWeiboShareAPI getWeiboShareAPI() {
        return mWeiboShareAPI;
    }

    private String getErrorMsg(UiError uiError) {
        if (!TextUtils.isEmpty(uiError.errorDetail)) {
            return uiError.errorDetail;
        }

        return uiError.errorMessage;
    }

    public void shareToQQ(Activity activity, ShareInfo shareInfo, final OnShareListener listener) {
        ensureTencent(activity);

        Bundle bundle = new Bundle();
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, shareInfo.mTitle);
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareInfo.mSummary);
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareInfo.mShareUrl);
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, shareInfo.mImageUrl);
        bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, ShareUtils.getAppName(activity));

        mTencent.shareToQQ(activity, bundle, new IUiListener() {
            @Override
            public void onComplete(Object o) {
                listener.onSuccess();
            }

            @Override
            public void onError(UiError uiError) {
                listener.onFailed(getErrorMsg(uiError));
            }

            @Override
            public void onCancel() {
                listener.onCancel();
            }
        });
    }

    public void shareToQZone(Activity activity, ShareInfo shareInfo, final OnShareListener listener) {
        ensureTencent(activity);

        Bundle bundle = new Bundle();
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, shareInfo.mTitle);
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareInfo.mSummary);
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareInfo.mShareUrl);
        bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, ShareUtils.getAppName(activity));

        if (!TextUtils.isEmpty(shareInfo.mImageUrl)) {
            ArrayList<String> images = new ArrayList<>();
            images.add(shareInfo.mImageUrl);
            bundle.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, images);
        }

        mTencent.shareToQzone(activity, bundle, new IUiListener() {
            @Override
            public void onComplete(Object o) {
                listener.onSuccess();
            }

            @Override
            public void onError(UiError uiError) {
                listener.onFailed(getErrorMsg(uiError));
            }

            @Override
            public void onCancel() {
                listener.onCancel();
            }
        });
    }

    public void notifyWXShareResult(String transaction, ShareReturnType returnType) {
        WeakReference<OnShareListener> ref = mWXShareListenerList.remove(transaction);
        if (ref == null) {
            return;
        }

        OnShareListener listener = ref.get();
        if (listener == null) {
            return;
        }

        if (returnType == ShareReturnType.SUCCESS) {
            listener.onSuccess();
        } else if (returnType == ShareReturnType.FAILED) {
            listener.onFailed(null);
        } else if (returnType == ShareReturnType.CANCELED) {
            listener.onCancel();
        }
    }

    public void shareToWXFriend(
            final Activity activity,
            final ShareInfo shareInfo,
            final OnShareListener listener) {

        if (!ensureWXApi(activity)) {
            if (listener != null) {
                listener.onFailed(null);
            }

            return;
        }

        if (!TextUtils.isEmpty(shareInfo.mImageUrl) && mShareImageLoader != null) {
            mShareImageLoader.loadImage(shareInfo.mImageUrl, new ShareImageLoader.Callback() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    shareToWXFriend(activity, shareInfo, bitmap, listener);
                }

                @Override
                public void onFailed(int retCode) {
                    if (listener != null) {
                        listener.onFailed(null);
                    }

                    Toast.makeText(activity, R.string.share_failed, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            shareToWXFriend(activity, shareInfo, null, listener);
        }
    }

    public void shareToWXFriendsGroup(
            final Activity activity,
            final ShareInfo shareInfo,
            final OnShareListener listener) {

        if (!ensureWXApi(activity)) {
            if (listener != null) {
                listener.onFailed(null);
            }

            return;
        }

        if (!TextUtils.isEmpty(shareInfo.mImageUrl) && mShareImageLoader != null) {
            mShareImageLoader.loadImage(shareInfo.mImageUrl, new ShareImageLoader.Callback() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    shareToWXFriendsGroup(activity, shareInfo, bitmap, listener);
                }

                @Override
                public void onFailed(int retCode) {
                    if (listener != null) {
                        listener.onFailed(null);
                    }

                    Toast.makeText(activity, R.string.share_failed, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            shareToWXFriendsGroup(activity, shareInfo, null, listener);
        }
    }

    public void shareToWXFriend(
            Activity activity, ShareInfo shareInfo, Bitmap bmp, OnShareListener listener) {

        if (!ensureWXApi(activity)) {
            if (listener != null) {
                listener.onFailed(null);
            }

            return;
        }

        SendMessageToWX.Req req = getWXShareReq(
                activity, shareInfo, bmp, SendMessageToWX.Req.WXSceneSession);
        if (listener != null) {
            mWXShareListenerList.put(req.transaction,
                    new WeakReference<OnShareListener>(listener));
        }

        mWXApi.sendReq(req);
    }

    public void shareToWXFriendsGroup(
            Activity activity, ShareInfo shareInfo, Bitmap bmp, OnShareListener listener) {

        if (!ensureWXApi(activity)) {
            if (listener != null) {
                listener.onFailed(null);
            }

            return;
        }

        SendMessageToWX.Req req = getWXShareReq(
                activity, shareInfo, bmp, SendMessageToWX.Req.WXSceneTimeline);
        if (listener != null) {
            mWXShareListenerList.put(req.transaction,
                    new WeakReference<OnShareListener>(listener));
        }

        mWXApi.sendReq(req);
    }

    private static Bitmap createThumbImage(Bitmap bmp) {
        if (bmp == null) {
            return null;
        }

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        if (width <= THUMB_IMAGE_SIZE && height <= THUMB_IMAGE_SIZE) {
            return bmp;
        }

        int thumbHeightSize = height * THUMB_IMAGE_SIZE / width;
        return Bitmap.createScaledBitmap(bmp, THUMB_IMAGE_SIZE, thumbHeightSize, true);
    }

    private SendMessageToWX.Req getWXShareReq(
            Context context, ShareInfo shareInfo, Bitmap bmp, int scene) {

        WXWebpageObject obj = new WXWebpageObject();
        obj.webpageUrl = shareInfo.mShareUrl;

        WXMediaMessage message = new WXMediaMessage(obj);
        message.title = shareInfo.mTitle;
        message.description = shareInfo.mSummary;

        Bitmap thumbBmp = null;
        if (bmp != null) {
            thumbBmp = createThumbImage(bmp);
        } else {
            Drawable drawable = ShareUtils.getAppIcon(context);
            if (drawable instanceof BitmapDrawable) {
                thumbBmp = ((BitmapDrawable) drawable).getBitmap();
            }
        }

        if (thumbBmp != null) {
            message.setThumbImage(thumbBmp);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = message;
        req.scene = scene;

        return req;
    }

    private String buildTransaction(String type) {
        return TextUtils.isEmpty(type) ? String.valueOf(System.currentTimeMillis())
                : type + System.currentTimeMillis();
    }

    public void shareToSinaWblog(
            final Activity activity,
            final ShareInfo shareInfo,
            final OnShareListener listener) {

        if (!TextUtils.isEmpty(shareInfo.mImageUrl) && mShareImageLoader != null) {
            mShareImageLoader.loadImage(shareInfo.mImageUrl, new ShareImageLoader.Callback() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    shareToSinaWblog(activity, shareInfo, bitmap, listener);
                }

                @Override
                public void onFailed(int retCode) {
                    if (listener != null) {
                        listener.onFailed(null);
                    }

                    Toast.makeText(activity, R.string.share_failed, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            shareToSinaWblog(activity, shareInfo, null, listener);
        }
    }

    public void shareToSinaWblog(
            final Activity activity,
            final ShareInfo shareInfo,
            final Bitmap bitmap,
            final OnShareListener listener) {

        if (!ensureWblogShare(activity)) {
            if (listener != null) {
                listener.onFailed(null);
            }

            return;
        }

        SendMultiMessageToWeiboRequest request = getWblogShareReq(activity, shareInfo, bitmap);
        if (listener != null) {
            mWBlogShareListenerList.put(request.transaction,
                    new WeakReference<OnShareListener>(listener));
        }

        mWeiboShareAPI.sendRequest(activity, request);
    }

    private SendMultiMessageToWeiboRequest getWblogShareReq(
            Activity activity, ShareInfo shareInfo, Bitmap bitmap) {

        WeiboMultiMessage message = new WeiboMultiMessage();

        WebpageObject webPage = new WebpageObject();
        webPage.identify = Utility.generateGUID();
        webPage.title = shareInfo.mTitle;
        webPage.description = shareInfo.mSummary;
        webPage.actionUrl = shareInfo.mShareUrl;
        message.mediaObject = webPage;

        if (bitmap == null) {
            Drawable drawable = ShareUtils.getAppIcon(activity);
            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }
        }

        webPage.setThumbImage(createThumbImage(bitmap));

        ImageObject image = new ImageObject();
        image.setImageObject(bitmap);
        message.imageObject = image;

        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = buildTransaction("wblog");
        request.multiMessage = message;

        return request;
    }

    public void notifyWblogShareResult(String transaction, ShareReturnType returnType) {
        WeakReference<OnShareListener> ref = mWBlogShareListenerList.remove(transaction);
        if (ref == null) {
            return;
        }

        OnShareListener listener = ref.get();
        if (listener == null) {
            return;
        }

        if (returnType == ShareReturnType.SUCCESS) {
            listener.onSuccess();
        } else if (returnType == ShareReturnType.FAILED) {
            listener.onFailed(null);
        } else if (returnType == ShareReturnType.CANCELED) {
            listener.onCancel();
        }
    }
}
