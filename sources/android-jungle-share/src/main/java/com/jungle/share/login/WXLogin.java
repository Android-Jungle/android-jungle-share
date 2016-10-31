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

package com.jungle.share.login;

import android.content.Context;
import android.text.TextUtils;
import com.jungle.share.ShareUtils;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;

public class WXLogin {

    private static final String WX_PERMISSION_SCOPE = "snsapi_userinfo";

    private IWXAPI mWXApi;
    private String mRequestState;


    public void login(Context context) {
        mWXApi = ShareUtils.createNewWXApi(context, true);
        mRequestState = String.valueOf(System.currentTimeMillis());

        if (!ShareUtils.ensureWXApi(context, mWXApi)) {
            return;
        }

        SendAuth.Req req = new SendAuth.Req();
        req.scope = WX_PERMISSION_SCOPE;
        req.state = mRequestState;

        mWXApi.sendReq(req);
    }

    public boolean checkState(String state) {
        return TextUtils.equals(mRequestState, state);
    }
}
