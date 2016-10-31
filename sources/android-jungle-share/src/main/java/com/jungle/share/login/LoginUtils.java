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
import com.jungle.share.activity.QQLoginAgentActivity;

public class LoginUtils {

    private static final String TAG = "Login";
    private static final String LAST_LOGIN_UID = "last_uid";
    private static final String LAST_LOGIN_TICKET = "last_ticket";


    private static WXLogin mWXLogin;


    public static void loginByQQ(Context context, Class<? extends QQLoginAgentActivity> clazz) {
        QQLoginAgentActivity.loginByQQ(context, clazz);
    }

    public static void loginByWX(Context context) {
        if (mWXLogin != null) {
            return;
        }

        mWXLogin = new WXLogin();
        mWXLogin.login(context);
    }
}
