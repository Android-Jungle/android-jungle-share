/**
 * Android Jungle-Share-Demo framework project.
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

package com.jungle.share.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.jungle.share.ShareHelper;
import com.jungle.share.ShareInfo;

public class MainActivity extends AppCompatActivity implements ShareHelper.OnShareListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.share_to_wx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareHelper.getInstance().shareToWXFriend(getActivity(), getShareInfo(), getListener());
            }
        });

        findViewById(R.id.share_to_wx_friends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareHelper.getInstance().shareToWXFriendsGroup(getActivity(), getShareInfo(), getListener());
            }
        });

        findViewById(R.id.share_to_qq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareHelper.getInstance().shareToQQ(getActivity(), getShareInfo(), getListener());
            }
        });

        findViewById(R.id.share_to_qzone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareHelper.getInstance().shareToQZone(getActivity(), getShareInfo(), getListener());
            }
        });

        findViewById(R.id.share_to_wblog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareHelper.getInstance().shareToSinaWblog(getActivity(), getShareInfo(), getListener());
            }
        });
    }

    @Override
    public void onSuccess() {
        showToast("Share successfully!");
    }

    @Override
    public void onCancel() {
        showToast("User canceled share.");
    }

    @Override
    public void onFailed(String message) {
        showToast("Share FAILED: " + message);
    }

    private Activity getActivity() {
        return this;
    }

    private ShareHelper.OnShareListener getListener() {
        return this;
    }

    private ShareInfo getShareInfo() {
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.mTitle = "Android Jungle Share Library";
        shareInfo.mSummary = "Thirdparty share components for Android. Supports WeChat / QZone / Weibo etc.";

        return shareInfo;
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
