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
import android.content.Intent;
import android.os.Bundle;
import com.jungle.share.ShareUtils;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

public abstract class WXBaseEntryActivity extends Activity implements IWXAPIEventHandler {

    protected static final String TAG = "WXLogin";


    protected IWXAPI mWXApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWXApi = ShareUtils.createNewWXApi(this, false);
        mWXApi.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        mWXApi.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        handleRequest(baseReq);
    }

    @Override
    public void onResp(BaseResp baseResp) {
        handleResponse(baseResp);

        finish();
    }

    protected void handleRequest(BaseReq baseReq) {
        finish();
    }

    protected void handleResponse(BaseResp baseResp) {
    }
}
