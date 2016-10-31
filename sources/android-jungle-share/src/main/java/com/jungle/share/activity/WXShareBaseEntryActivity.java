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

import android.text.TextUtils;
import android.widget.Toast;
import com.jungle.share.R;
import com.jungle.share.ShareHelper;
import com.jungle.share.ShareUtils;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import org.json.JSONException;
import org.json.JSONObject;

public class WXShareBaseEntryActivity extends WXBaseEntryActivity {

    private static final String GET_USER_INFO_CGI =
            "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";


    @Override
    protected void handleResponse(BaseResp baseResp) {
        super.handleResponse(baseResp);

        int rspType = baseResp.getType();
        if (rspType == ConstantsAPI.COMMAND_SENDAUTH) {
            handleAuthRsp(baseResp);
        } else if (rspType == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
            handleShareMsgRsp(baseResp);
        }
    }

    private void handleAuthRsp(BaseResp baseResp) {
        SendAuth.Resp resp = (SendAuth.Resp) baseResp;
        if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
            getWXAccessToken(resp);
            return;
        }

        String message = baseResp.errStr;
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                message = getString(R.string.failed_permission_denied);
                break;

            case BaseResp.ErrCode.ERR_USER_CANCEL:
                message = getString(R.string.user_canceled);
                break;

            default:
                break;
        }

        if (TextUtils.isEmpty(message)) {
            message = String.format(getString(R.string.login_failed_error),
                    baseResp.errCode);
        }

        notifyLoginByWXFailed(message);
    }

    private void handleShareMsgRsp(BaseResp baseResp) {
        String message = getString(R.string.share_failed);
        ShareHelper.ShareReturnType returnType = ShareHelper.ShareReturnType.FAILED;

        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                returnType = ShareHelper.ShareReturnType.SUCCESS;
                message = getString(R.string.share_succeeded);
                break;

            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                returnType = ShareHelper.ShareReturnType.FAILED;
                message = getString(R.string.failed_permission_denied);
                break;

            case BaseResp.ErrCode.ERR_USER_CANCEL:
                returnType = ShareHelper.ShareReturnType.CANCELED;
                message = getString(R.string.user_canceled);
                break;

            default:
                break;
        }

        ShareHelper.getInstance().notifyWXShareResult(
                baseResp.transaction, returnType);

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void getWXAccessToken(final SendAuth.Resp resp) {
        ShareUtils.getWXAccessToken(
                this, resp.code,
                new ShareUtils.OnGetAccessTokenListener() {
                    @Override
                    public void onSuccess(String openId, String accessToken) {
                        prepareLogin(resp.state, openId, accessToken);
                    }

                    @Override
                    public void onFailed() {
                        notifyLoginByWXFailed(null);
                    }
                });
    }

    private void prepareLogin(
            final String state, final String openId, final String accessToken) {
        String url = String.format(GET_USER_INFO_CGI, accessToken, openId);
        String content = ShareUtils.getHttpUrlContent(url);
        if (TextUtils.isEmpty(content)) {
            notifyLoginByWXFailed(null);
            return;
        }

        String nickName = null;
        String portraitUrl = null;

        try {
            JSONObject json = new JSONObject(content);
            nickName = ShareUtils.safeGetString(json, "nickname");
            portraitUrl = ShareUtils.safeGetString(json, "headimgurl");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        notifyLoginByWXSuccess(nickName, portraitUrl);
    }

    protected void notifyLoginByWXSuccess(String nickName, String portraitUrl) {
    }

    protected void notifyLoginByWXFailed(final String message) {
    }
}
