package com.hyphenate.easeim.section.login.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.hyphenate.easeim.common.net.Resource;
import com.hyphenate.easeim.common.livedatas.SingleSourceLiveData;
import com.hyphenate.easeim.common.repositories.DemoAppRepository;
import com.hyphenate.easeim.common.repositories.EMClientRepository;
import com.hyphenate.util.EMLog;


public class LoginViewModel extends AndroidViewModel {
    private EMClientRepository mRepository;
    private DemoAppRepository appRepository;
    private SingleSourceLiveData<Resource<String>> registerObservable;
    private SingleSourceLiveData<Integer> pageObservable;
    private SingleSourceLiveData<Resource<Boolean>> appKeyObservable;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        mRepository = new EMClientRepository();
        appRepository = new DemoAppRepository();
        registerObservable = new SingleSourceLiveData<>();
        pageObservable = new SingleSourceLiveData<>();
        appKeyObservable = new SingleSourceLiveData<>();
    }

    /**
     * 获取页面跳转的observable
     * @return
     */
    public LiveData<Integer> getPageSelect() {
        return pageObservable;
    }

    /**
     * 设置跳转的页面
     * @param page
     */
    public void setPageSelect(int page) {
        pageObservable.setValue(page);
    }

    /**
     * 注册环信账号
     * @param userName
     * @param pwd
     * @return
     */
    public void register(String userName, String pwd) {
        registerObservable.setSource(mRepository.registerToHx(userName, pwd));
    }

    public LiveData<Resource<String>> getRegisterObservable() {
        return registerObservable;
    }

    /**
     * 清理注册信息
     */
    public void clearRegisterInfo() {
        registerObservable.setValue(null);
    }

    public LiveData<Resource<Boolean>> getAppKeyObservable() {
        return appKeyObservable;
    }

    public void getAppKeyInfo(Context context) {
        appKeyObservable.setSource(appRepository.getAppKeyInfo(context));
    }

}
