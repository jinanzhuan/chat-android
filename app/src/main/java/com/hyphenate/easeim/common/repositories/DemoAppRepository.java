package com.hyphenate.easeim.common.repositories;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.hyphenate.easeim.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack;
import com.hyphenate.easeim.common.net.Resource;
import com.hyphenate.util.EMLog;

public class DemoAppRepository extends BaseEMRepository {
    public static final String TAG = DemoAppRepository.class.getSimpleName();

    public LiveData<Resource<Boolean>> getAppKeyInfo(Context context) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                String appPackageName = context.getPackageName();
                ApplicationInfo ai = null;
                try {
                    ai = context.getPackageManager().getApplicationInfo(appPackageName, PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
                    String infoError = context.getString(R.string.demo_error_not_find_application_info);
                    EMLog.e(TAG,infoError);
                    callBack.onError(-1, infoError);
                }
                if(ai != null) {
                    Bundle metaData = ai.metaData;
                    if (metaData == null) {
                        String metaError = context.getString(R.string.demo_error_not_find_meta);
                        EMLog.w(TAG, metaError);
                        callBack.onError(-1, metaError);
                        return;
                    }
                    // read appkey
                    String appKeyFromConfig = metaData.getString(DemoConstant.EASEMOB_APPKEY);
                    if(!TextUtils.isEmpty(appKeyFromConfig) && !TextUtils.equals(appKeyFromConfig, "******")) {
                        callBack.onSuccess(createLiveData(true));
                    }else {
                        String notSetAppkey = context.getString(R.string.em_login_not_set_appkey);
                        callBack.onError(-1, notSetAppkey);
                    }
                }
            }
        }.asLiveData();
    }
}

