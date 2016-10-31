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

package com.jungle.share.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.jungle.share.R;
import com.jungle.share.ShareUtils;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONException;
import org.json.JSONObject;

public class QQLoginAgentActivity extends Activity {

    protected static final String QQ_PERMISSION_SCOPE = "get_user_info,get_simple_userinfo";
    protected static final String KEY_QQ_OPEN_ID = "qq_open_id";
    protected static final String KEY_QQ_ACCESS_TOKEN = "qq_access_token";
    protected static final String KEY_QQ_TOKEN_EXPIRE_INFO = "qq_token_expire_info";


    public static void loginByQQ(Context context, Class<? extends QQLoginAgentActivity> clazz) {
        Intent intent = new Intent(context, clazz);
        context.startActivity(intent);
    }


    private Tencent mTencent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String expireInfo = getSavedExpireInfo();
        try {
            Long.parseLong(expireInfo);
        } catch (NumberFormatException e) {
            expireInfo = null;
        }

        mTencent = Tencent.createInstance(ShareUtils.getTencentApiId(this), this);

        if (expireInfo != null) {
            mTencent.setOpenId(getSavedOpenId());
            mTencent.setAccessToken(getSavedAccessToken(), expireInfo);
        }

        mTencent.login(this, QQ_PERMISSION_SCOPE, mLoginListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, mLoginListener);
    }

    protected void saveOpenInfo(String openId, String accessToken, String expireInfo) {
    }

    protected String getSavedOpenId() {
        return null;
    }

    protected String getSavedAccessToken() {
        return null;
    }

    protected String getSavedExpireInfo() {
        return null;
    }

    protected void loginByQQ(String openId, String accessToken, String nickName, String portraitUrl) {
    }

    protected void handleLoginFailed(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }

    private IUiListener mLoginListener = new IUiListener() {
        @Override
        public void onComplete(final Object o) {
            if (o instanceof JSONObject) {
                JSONObject result = (JSONObject) o;

                try {
                    final String openId = result.getString("openid");
                    final String accessToken = result.getString("access_token");
                    final String expireInfo = result.getString("expires_in");

                    long expireValue = 0;
                    try {
                        expireValue = Long.parseLong(expireInfo);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    expireValue = System.currentTimeMillis() + expireValue * 1000;
                    saveOpenInfo(openId, accessToken, String.valueOf(expireValue));

                    UserInfo userInfo = new UserInfo(QQLoginAgentActivity.this, mTencent.getQQToken());
                    userInfo.getUserInfo(new IUiListener() {
                        @Override
                        public void onComplete(Object o) {
                            String nickName = null;
                            String portraitUrl = null;

                            if (o instanceof JSONObject) {
                                JSONObject data = (JSONObject) o;
                                nickName = ShareUtils.safeGetString(data, "nickname");
                                portraitUrl = ShareUtils.safeGetString(data, "figureurl_qq_2");

                                if (TextUtils.isEmpty(portraitUrl)) {
                                    portraitUrl = ShareUtils.safeGetString(data, "figureurl_qq_1");
                                }
                            }

                            loginByQQ(openId, accessToken, nickName, portraitUrl);
                        }

                        @Override
                        public void onError(UiError uiError) {
                            loginByQQ(openId, accessToken, null, null);
                        }

                        @Override
                        public void onCancel() {
                            loginByQQ(openId, accessToken, null, null);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            finish();
        }

        @Override
        public void onError(UiError uiError) {
            String msg = uiError.errorMessage;
            if (TextUtils.isEmpty(msg)) {
                msg = uiError.errorDetail;

                if (TextUtils.isEmpty(msg)) {
                    msg = String.format(getString(R.string.login_failed_error), uiError.errorCode);
                }
            }

            handleLoginFailed(msg);
        }

        @Override
        public void onCancel() {
            handleLoginFailed(getString(R.string.user_canceled));
        }
    };
}
