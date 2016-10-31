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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.Toast;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ShareUtils {

    private static final String META_KEY_TENCENT_APP_ID = "TencentAppId";
    private static final String META_KEY_WX_APP_ID = "WXAppId";
    private static final String META_KEY_WX_APP_SECRET = "WXAppSecret";
    private static final String META_KEY_SINA_APP_KEY = "SinaAppKey";

    private static final String GET_ACCESS_TOKEN_CGI =
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";


    public interface OnGetAccessTokenListener {
        void onSuccess(String openId, String accessToken);

        void onFailed();
    }


    public static String getAppName(Context context) {
        String pkgName = context.getPackageName();
        PackageManager mgr = context.getPackageManager();

        try {
            ApplicationInfo info = mgr.getApplicationInfo(pkgName, 0);
            return String.valueOf(mgr.getApplicationLabel(info));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return pkgName;
    }

    public static Drawable getAppIcon(Context context) {
        String pkgName = context.getPackageName();
        PackageManager mgr = context.getPackageManager();

        try {
            ApplicationInfo info = mgr.getApplicationInfo(pkgName, 0);
            return mgr.getApplicationIcon(info);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getMetaData(Context context, String metaKey) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (info.metaData != null && info.metaData.containsKey(metaKey)) {
                Object value = info.metaData.get(metaKey);
                return value != null ? value.toString() : null;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String getTencentApiId(Context context) {
        return ShareUtils.getMetaData(context, META_KEY_TENCENT_APP_ID);
    }

    public static String getWXApiId(Context context) {
        return ShareUtils.getMetaData(context, META_KEY_WX_APP_ID);
    }

    public static String getWXApiSecret(Context context) {
        return ShareUtils.getMetaData(context, META_KEY_WX_APP_SECRET);
    }

    public static String getSinaAppKey(Context context) {
        return ShareUtils.getMetaData(context, META_KEY_SINA_APP_KEY);
    }

    public static IWXAPI createNewWXApi(Context context, boolean checkSignature) {
        String wxAppId = ShareUtils.getWXApiId(context);
        IWXAPI wxApi = WXAPIFactory.createWXAPI(context, wxAppId, checkSignature);
        wxApi.registerApp(wxAppId);
        return wxApi;
    }

    public static boolean ensureWXApi(final Activity activity, IWXAPI wxApi) {
        if (wxApi.isWXAppInstalled()) {
            if (!wxApi.isWXAppSupportAPI()) {
                Toast.makeText(activity, R.string.wx_not_support_share, Toast.LENGTH_SHORT).show();
                return false;
            }

            return true;
        }

        return false;
    }

    public static void getWXAccessToken(
            Context context, String code, final OnGetAccessTokenListener listener) {

        boolean success = false;

        try {
            URL httpUrl = new URL(String.format(GET_ACCESS_TOKEN_CGI,
                    ShareUtils.getWXApiId(context), ShareUtils.getWXApiSecret(context), code));
            HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();
            if (stream != null) {
                String content = "";
                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(stream));
                String inputLine = "";
                while ((inputLine = bufferReader.readLine()) != null) {
                    content += inputLine + "\n";
                }

                try {
                    JSONObject json = new JSONObject(content);
                    String openId = ShareUtils.safeGetString(json, "openid");
                    String accessToken = ShareUtils.safeGetString(json, "access_token");

                    if (!TextUtils.isEmpty(openId) && !TextUtils.isEmpty(accessToken)) {
                        success = true;
                        listener.onSuccess(openId, accessToken);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!success) {
            listener.onFailed();
        }
    }

    public static String safeGetString(JSONObject json, String node) {
        if (json.has(node)) {
            try {
                return json.getString(node);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
