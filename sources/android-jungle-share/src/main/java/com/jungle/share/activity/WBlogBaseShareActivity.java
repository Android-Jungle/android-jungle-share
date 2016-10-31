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
import android.widget.Toast;
import com.jungle.share.R;
import com.jungle.share.ShareHelper;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.constant.WBConstants;

public class WBlogBaseShareActivity extends Activity implements IWeiboHandler.Response {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            IWeiboShareAPI api = ShareHelper.getInstance().getWeiboShareAPI();
            if (api != null) {
                api.handleWeiboResponse(intent, this);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        IWeiboShareAPI api = ShareHelper.getInstance().getWeiboShareAPI();
        if (api != null) {
            api.handleWeiboResponse(intent, this);
        }
    }

    @Override
    public void onResponse(BaseResponse baseResp) {
        if (baseResp != null) {
            String message = getString(R.string.share_failed);
            ShareHelper.ShareReturnType returnType = ShareHelper.ShareReturnType.FAILED;

            switch (baseResp.errCode) {
                case WBConstants.ErrorCode.ERR_OK:
                    returnType = ShareHelper.ShareReturnType.SUCCESS;
                    message = getString(R.string.share_succeeded);
                    break;
                case WBConstants.ErrorCode.ERR_CANCEL:
                    returnType = ShareHelper.ShareReturnType.CANCELED;
                    message = getString(R.string.user_canceled);
                    break;
                case WBConstants.ErrorCode.ERR_FAIL:
                    returnType = ShareHelper.ShareReturnType.FAILED;
                    message = getString(R.string.share_failed);
                    break;
            }

            ShareHelper.getInstance().notifyWblogShareResult(
                    baseResp.transaction, returnType);

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
