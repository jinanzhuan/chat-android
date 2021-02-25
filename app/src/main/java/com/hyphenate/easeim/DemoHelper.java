package com.hyphenate.easeim;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.heytap.msp.push.HeytapPushManager;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMChatRoomManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMContactManager;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMPushManager;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.db.DemoDbHelper;
import com.hyphenate.easeim.common.manager.UserProfileManager;
import com.hyphenate.easeim.common.model.DemoModel;
import com.hyphenate.easeim.common.model.EmojiconExampleGroupData;
import com.hyphenate.easeim.common.receiver.HeadsetReceiver;
import com.hyphenate.easeim.common.utils.PreferenceManager;
import com.hyphenate.easeim.section.chat.ChatPresenter;
import com.hyphenate.easeim.section.chat.delegates.ChatConferenceInviteAdapterDelegate;
import com.hyphenate.easeim.section.chat.delegates.ChatLiveInviteAdapterDelegate;
import com.hyphenate.easeim.section.chat.delegates.ChatNotificationAdapterDelegate;
import com.hyphenate.easeim.section.chat.delegates.ChatRecallAdapterDelegate;
import com.hyphenate.easeim.section.chat.delegates.ChatVideoCallAdapterDelegate;
import com.hyphenate.easeim.section.chat.delegates.ChatVoiceCallAdapterDelegate;
import com.hyphenate.easeim.section.conference.ConferenceInviteActivity;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.delegate.EaseCustomAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseExpressionAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseFileAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseImageAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseLocationAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseTextAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseVideoAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseVoiceAdapterDelegate;
import com.hyphenate.easeui.domain.EaseAvatarOptions;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.manager.EaseMessageTypeSetManager;
import com.hyphenate.easeui.model.EaseNotifier;
import com.hyphenate.easeui.provider.EaseEmojiconInfoProvider;
import com.hyphenate.easeui.provider.EaseSettingsProvider;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.push.EMPushConfig;
import com.hyphenate.push.EMPushHelper;
import com.hyphenate.push.EMPushType;
import com.hyphenate.push.PushListener;
import com.hyphenate.util.EMLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import easemob.hyphenate.calluikit.EaseCallUIKit;
import easemob.hyphenate.calluikit.base.EaseCallKitConfig;
import easemob.hyphenate.calluikit.base.EaseCallKitTokenCallback;
import easemob.hyphenate.calluikit.base.EaseCallUserInfo;
import easemob.hyphenate.calluikit.base.EaseCallEndReason;
import easemob.hyphenate.calluikit.base.EaseCallKitListener;
import easemob.hyphenate.calluikit.base.EaseCallType;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * 作为hyphenate-sdk的入口控制类，获取sdk下的基础类均通过此类
 */
public class DemoHelper {
    private static final String TAG = DemoHelper.class.getSimpleName();

    public boolean isSDKInit;//SDK是否初始化
    private static DemoHelper mInstance;
    private DemoModel demoModel = null;
    private Map<String, EaseUser> contactList;
    private UserProfileManager userProManager;

    private EaseCallKitListener callKitListener;
    private Context mianContext;
    private String tokenUrl = "http://a1-hsb.easemob.com/token/rtcToken?";

    private DemoHelper() {}

    public static DemoHelper getInstance() {
        if(mInstance == null) {
            synchronized (DemoHelper.class) {
                if(mInstance == null) {
                    mInstance = new DemoHelper();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        demoModel = new DemoModel(context);
        //初始化IM SDK
        if(initSDK(context)) {
            // debug mode, you'd better set it to false, if you want release your App officially.
            EMClient.getInstance().setDebugMode(true);
            // set Call options
            setCallOptions(context);
            //初始化推送
            initPush(context);
            //注册call Receiver
            //initReceiver(context);
            //初始化ease ui相关
            initEaseUI(context);
            //注册对话类型
            registerConversationType();

            //初始化 calluikit
            EaseCallKitConfig callKitConfig = new EaseCallKitConfig();
            //设置默认头像
//            String headImage = EaseFileUtils.getModelFilePath(context,"watermark.png");
//            callKitConfig.setDefaultHeadImage(headImage);
//            //设置振铃文件
//            String ringFile = EaseFileUtils.getModelFilePath(context,"huahai.mp3");
//            callKitConfig.setRingFile(ringFile);
            //设置呼叫超时时间
            callKitConfig.setCallTimeOut(30 * 1000);
            //设置声网AgoraAppIdappId
            callKitConfig.setAgoraAppId("15cb0d28b87b425ea613fc46f7c9f974");
            Map<String, EaseCallUserInfo> userInfoMap = new HashMap<>();
            userInfoMap.put("lijian66",new EaseCallUserInfo("环信66",null));
            userInfoMap.put("lijian88",new EaseCallUserInfo("环信88",null));
            callKitConfig.setUserInfoMap(userInfoMap);
            EaseCallUIKit.getInstance().init(context,callKitConfig);
            addCallkitListener();
        }

    }

    /**
     * 初始化SDK
     * @param context
     * @return
     */
    private boolean initSDK(Context context) {
        // 根据项目需求对SDK进行配置
        EMOptions options = initChatOptions(context);
        //配置自定义的rest server和im server
        //options.setRestServer("a1-hsb.easemob.com");
        //options.setIMServer("116.85.43.118");
        //options.setImPort(6717);
        // 初始化SDK
        isSDKInit = EaseIM.getInstance().init(context, options);
        mianContext = context;
        return isSDKInit();
    }


    /**
     *注册对话类型
     */
    private void registerConversationType() {
        EaseMessageTypeSetManager.getInstance()
                .addMessageType(EaseExpressionAdapterDelegate.class)       //自定义表情
                .addMessageType(EaseFileAdapterDelegate.class)             //文件
                .addMessageType(EaseImageAdapterDelegate.class)            //图片
                .addMessageType(EaseLocationAdapterDelegate.class)         //定位
                .addMessageType(EaseVideoAdapterDelegate.class)            //视频
                .addMessageType(EaseVoiceAdapterDelegate.class)            //声音
                .addMessageType(ChatConferenceInviteAdapterDelegate.class) //会议邀请
                .addMessageType(ChatLiveInviteAdapterDelegate.class)       //语音邀请
                .addMessageType(ChatRecallAdapterDelegate.class)           //消息撤回
                .addMessageType(ChatVideoCallAdapterDelegate.class)        //视频通话
                .addMessageType(ChatVoiceCallAdapterDelegate.class)        //语音通话
                .addMessageType(EaseCustomAdapterDelegate.class)           //自定义消息
                .addMessageType(ChatNotificationAdapterDelegate.class)     //入群等通知消息
                .setDefaultMessageType(EaseTextAdapterDelegate.class);       //文本
    }

    /**
     * 判断是否之前登录过
     * @return
     */
    public boolean isLoggedIn() {
        return getEMClient().isLoggedInBefore();
    }

    /**
     * 获取IM SDK的入口类
     * @return
     */
    public EMClient getEMClient() {
        return EMClient.getInstance();
    }

    /**
     * 获取contact manager
     * @return
     */
    public EMContactManager getContactManager() {
        return getEMClient().contactManager();
    }

    /**
     * 获取group manager
     * @return
     */
    public EMGroupManager getGroupManager() {
        return getEMClient().groupManager();
    }

    /**
     * 获取chatroom manager
     * @return
     */
    public EMChatRoomManager getChatroomManager() {
        return getEMClient().chatroomManager();
    }


    /**
     * get EMChatManager
     * @return
     */
    public EMChatManager getChatManager() {
        return getEMClient().chatManager();
    }

    /**
     * get push manager
     * @return
     */
    public EMPushManager getPushManager() {
        return getEMClient().pushManager();
    }

    /**
     * get conversation
     * @param username
     * @param type
     * @param createIfNotExists
     * @return
     */
    public EMConversation getConversation(String username, EMConversation.EMConversationType type, boolean createIfNotExists) {
        return getChatManager().getConversation(username, type, createIfNotExists);
    }

    public String getCurrentUser() {
        return getEMClient().getCurrentUser();
    }

    /**
     * ChatPresenter中添加了网络连接状态监听，多端登录监听，群组监听，联系人监听，聊天室监听
     * @param context
     */
    private void initEaseUI(Context context) {
        //添加ChatPresenter,ChatPresenter中添加了网络连接状态监听，
        EaseIM.getInstance().addChatPresenter(ChatPresenter.getInstance());
        EaseIM.getInstance()
                .setSettingsProvider(new EaseSettingsProvider() {
                    @Override
                    public boolean isMsgNotifyAllowed(EMMessage message) {
                        if(message == null){
                            return demoModel.getSettingMsgNotification();
                        }
                        if(!demoModel.getSettingMsgNotification()){
                            return false;
                        }else{
                            String chatUsename = null;
                            List<String> notNotifyIds = null;
                            // get user or group id which was blocked to show message notifications
                            if (message.getChatType() == EMMessage.ChatType.Chat) {
                                chatUsename = message.getFrom();
                                notNotifyIds = demoModel.getDisabledIds();
                            } else {
                                chatUsename = message.getTo();
                                notNotifyIds = demoModel.getDisabledGroups();
                            }

                            if (notNotifyIds == null || !notNotifyIds.contains(chatUsename)) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }

                    @Override
                    public boolean isMsgSoundAllowed(EMMessage message) {
                        return demoModel.getSettingMsgSound();
                    }

                    @Override
                    public boolean isMsgVibrateAllowed(EMMessage message) {
                        return demoModel.getSettingMsgVibrate();
                    }

                    @Override
                    public boolean isSpeakerOpened() {
                        return demoModel.getSettingMsgSpeaker();
                    }
                })
                .setEmojiconInfoProvider(new EaseEmojiconInfoProvider() {
                    @Override
                    public EaseEmojicon getEmojiconInfo(String emojiconIdentityCode) {
                        EaseEmojiconGroupEntity data = EmojiconExampleGroupData.getData();
                        for(EaseEmojicon emojicon : data.getEmojiconList()){
                            if(emojicon.getIdentityCode().equals(emojiconIdentityCode)){
                                return emojicon;
                            }
                        }
                        return null;
                    }

                    @Override
                    public Map<String, Object> getTextEmojiconMapping() {
                        return null;
                    }
                })
                .setAvatarOptions(getAvatarOptions())
                .setUserProvider(new EaseUserProfileProvider() {
                    @Override
                    public EaseUser getUser(String username) {
                        return getUserInfo(username);
                    }

                });
    }

    /**
     * 统一配置头像
     * @return
     */
    private EaseAvatarOptions getAvatarOptions() {
        EaseAvatarOptions avatarOptions = new EaseAvatarOptions();
        avatarOptions.setAvatarShape(1);
        return avatarOptions;
    }

    private EaseUser getUserInfo(String username) {
        // To get instance of EaseUser, here we get it from the user list in memory
        // You'd better cache it if you get it from your server
        EaseUser user = null;
        if(username.equals(EMClient.getInstance().getCurrentUser()))
            return getUserProfileManager().getCurrentUserInfo();
        user = getContactList().get(username);
        return user;
    }


    /**
     * 根据自己的需要进行配置
     * @param context
     * @return
     */
    private EMOptions initChatOptions(Context context){
        Log.d(TAG, "init HuanXin Options");

        EMOptions options = new EMOptions();
        // 设置是否自动接受加好友邀请,默认是true
        options.setAcceptInvitationAlways(false);
        // 设置是否需要接受方已读确认
        options.setRequireAck(true);
        // 设置是否需要接受方送达确认,默认false
        options.setRequireDeliveryAck(false);

        options.setUseRtcConfig(true);

        // 设置是否使用 fcm，有些华为设备本身带有 google 服务，
        options.setUseFCM(demoModel.isUseFCM());

        /**
         * NOTE:你需要设置自己申请的账号来使用三方推送功能，详见集成文档
         */
        EMPushConfig.Builder builder = new EMPushConfig.Builder(context);

        builder.enableVivoPush() // 需要在AndroidManifest.xml中配置appId和appKey
                .enableMeiZuPush("134952", "f00e7e8499a549e09731a60a4da399e3")
                .enableMiPush("2882303761517426801", "5381742660801")
                .enableOppoPush("0bb597c5e9234f3ab9f821adbeceecdb",
                        "cd93056d03e1418eaa6c3faf10fd7537")
                .enableHWPush() // 需要在AndroidManifest.xml中配置appId
                .enableFCM("782795210914");
        options.setPushConfig(builder.build());

        //set custom servers, commonly used in private deployment
        if(demoModel.isCustomSetEnable()) {
            if(demoModel.isCustomServerEnable() && demoModel.getRestServer() != null && demoModel.getIMServer() != null) {
                // 设置rest server地址
                options.setRestServer(demoModel.getRestServer());
                // 设置im server地址
                options.setIMServer(demoModel.getIMServer());
                //如果im server地址中包含端口号
                if(demoModel.getIMServer().contains(":")) {
                    options.setIMServer(demoModel.getIMServer().split(":")[0]);
                    // 设置im server 端口号，默认443
                    options.setImPort(Integer.valueOf(demoModel.getIMServer().split(":")[1]));
                }else {
                    //如果不包含端口号
                    if(demoModel.getIMServerPort() != 0) {
                        options.setImPort(demoModel.getIMServerPort());
                    }
                }
            }
        }
        if (demoModel.isCustomAppkeyEnabled() && !TextUtils.isEmpty(demoModel.getCutomAppkey())) {
            // 设置appkey
            options.setAppKey(demoModel.getCutomAppkey());
        }

        String imServer = options.getImServer();
        String restServer = options.getRestServer();

        // 设置是否允许聊天室owner离开并删除会话记录，意味着owner再不会受到任何消息
        options.allowChatroomOwnerLeave(demoModel.isChatroomOwnerLeaveAllowed());
        // 设置退出(主动和被动退出)群组时是否删除聊天消息
        options.setDeleteMessagesAsExitGroup(demoModel.isDeleteMessagesAsExitGroup());
        // 设置是否自动接受加群邀请
        options.setAutoAcceptGroupInvitation(demoModel.isAutoAcceptGroupInvitation());
        // 是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载
        options.setAutoTransferMessageAttachments(demoModel.isSetTransferFileByUser());
        // 是否自动下载缩略图，默认是true为自动下载
        options.setAutoDownloadThumbnail(demoModel.isSetAutodownloadThumbnail());
        return options;
    }

    private void setCallOptions(Context context) {
        HeadsetReceiver headsetReceiver = new HeadsetReceiver();
        IntentFilter headsetFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        context.registerReceiver(headsetReceiver, headsetFilter);
    }

    public void initPush(Context context) {
        if(EaseIM.getInstance().isMainProcess(context)) {
            //OPPO SDK升级到2.1.0后需要进行初始化
            HeytapPushManager.init(context, true);
            //HMSPushHelper.getInstance().initHMSAgent(DemoApplication.getInstance());
            EMPushHelper.getInstance().setPushListener(new PushListener() {
                @Override
                public void onError(EMPushType pushType, long errorCode) {
                    // TODO: 返回的errorCode仅9xx为环信内部错误，可从EMError中查询，其他错误请根据pushType去相应第三方推送网站查询。
                    EMLog.e("PushClient", "Push client occur a error: " + pushType + " - " + errorCode);
                }
            });
        }
    }

    /**
     * logout
     *
     * @param unbindDeviceToken
     *            whether you need unbind your device token
     * @param callback
     *            callback
     */
    public void logout(boolean unbindDeviceToken, final EMCallBack callback) {
        Log.d(TAG, "logout: " + unbindDeviceToken);
        EMClient.getInstance().logout(unbindDeviceToken, new EMCallBack() {

            @Override
            public void onSuccess() {
                logoutSuccess();
                //reset();
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            @Override
            public void onProgress(int progress, String status) {
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }

            @Override
            public void onError(int code, String error) {
                Log.d(TAG, "logout: onSuccess");
                //reset();
                if (callback != null) {
                    callback.onError(code, error);
                }
            }
        });
    }

    /**
     * 关闭当前进程
     */
    public void killApp() {
        List<Activity> activities = DemoApplication.getInstance().getLifecycleCallbacks().getActivityList();
        if(activities != null && !activities.isEmpty()) {
            for(Activity activity : activities) {
                activity.finish();
            }
        }
        Process.killProcess(Process.myPid());
        System.exit(0);
    }



    /**
     * 退出登录后，需要处理的业务逻辑
     */
    public void logoutSuccess() {
        Log.d(TAG, "logout: onSuccess");
        setAutoLogin(false);
        DemoDbHelper.getInstance(DemoApplication.getInstance()).closeDb();
    }

    public EaseAvatarOptions getEaseAvatarOptions() {
        return EaseIM.getInstance().getAvatarOptions();
    }

    public DemoModel getModel(){
        if(demoModel == null) {
            demoModel = new DemoModel(DemoApplication.getInstance());
        }
        return demoModel;
    }

    public String getCurrentLoginUser() {
        return getModel().getCurrentUsername();
    }

    /**
     * get instance of EaseNotifier
     * @return
     */
    public EaseNotifier getNotifier(){
        return EaseIM.getInstance().getNotifier();
    }

    /**
     * 设置本地标记，是否自动登录
     * @param autoLogin
     */
    public void setAutoLogin(boolean autoLogin) {
        PreferenceManager.getInstance().setAutoLogin(autoLogin);
    }

    /**
     * 获取本地标记，是否自动登录
     * @return
     */
    public boolean getAutoLogin() {
        return PreferenceManager.getInstance().getAutoLogin();
    }

    /**
     * 设置SDK是否初始化
     * @param init
     */
    public void setSDKInit(boolean init) {
        isSDKInit = init;
    }

    public boolean isSDKInit() {
        return isSDKInit;
    }

    /**
     * 向数据库中插入数据
     * @param object
     */
    public void insert(Object object) {
        demoModel.insert(object);
    }

    /**
     * update
     * @param object
     */
    public void update(Object object) {
        demoModel.update(object);
    }

    /**
     * get contact list
     *
     * @return
     */
    public Map<String, EaseUser> getContactList() {
        if (isLoggedIn() && contactList == null) {
            contactList = demoModel.getContactList();
        }

        // return a empty non-null object to avoid app crash
        if(contactList == null){
            return new Hashtable<String, EaseUser>();
        }

        return contactList;
    }

    /**
     * update contact list
     */
    public void updateContactList() {
        if(isLoggedIn()) {
            contactList = demoModel.getContactList();
        }
    }

    public UserProfileManager getUserProfileManager() {
        if (userProManager == null) {
            userProManager = new UserProfileManager();
        }
        return userProManager;
    }

    /**
     * 展示通知设置页面
     */
    public void showNotificationPermissionDialog() {
        EMPushType pushType = EMPushHelper.getInstance().getPushType();
        // oppo
        if(pushType == EMPushType.OPPOPUSH && HeytapPushManager.isSupportPush()) {
            HeytapPushManager.requestNotificationPermission();
        }
    }

    /**
     * 删除联系人
     * @param username
     * @return
     */
    public synchronized int deleteContact(String username) {
        if(TextUtils.isEmpty(username)) {
            return 0;
        }
        DemoDbHelper helper = DemoDbHelper.getInstance(DemoApplication.getInstance());
        if(helper.getUserDao() == null) {
            return 0;
        }
        int num = helper.getUserDao().deleteUser(username);
        if(helper.getInviteMessageDao() != null) {
            helper.getInviteMessageDao().deleteByFrom(username);
        }
        EMClient.getInstance().chatManager().deleteConversation(username, false);
        getModel().deleteUsername(username, false);
        Log.e(TAG, "delete num = "+num);
        return num;
    }

    /**
     * 检查是否是第一次安装登录
     * 默认值是true, 需要在用api拉取完会话列表后，就其置为false.
     * @return
     */
    public boolean isFirstInstall() {
        return getModel().isFirstInstall();
    }

    /**
     * 将状态置为非第一次安装，在调用获取会话列表的api后调用
     * 并将会话列表是否来自服务器置为true
     */
    public void makeNotFirstInstall() {
        getModel().makeNotFirstInstall();
    }

    /**
     * 检查会话列表是否从服务器返回数据
     * @return
     */
    public boolean isConComeFromServer() {
        return getModel().isConComeFromServer();
    }


    /**
     * 增加EaseCallkit监听
     *
     */
    public void addCallkitListener(){
        callKitListener = new EaseCallKitListener() {
            @Override
            public void onInviteUsers(Context context,String userId[],JSONObject ext) {
                Intent intent = new Intent(context, ConferenceInviteActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK);
                String groupId = null;
                if(ext != null && ext.length() > 0){
                    try {
                        groupId = ext.getString("groupId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                intent.putExtra(DemoConstant.EXTRA_CONFERENCE_GROUP_ID, groupId);
                intent.putExtra(DemoConstant.EXTRA_CONFERENCE_GROUP_EXIST_MEMBERS, userId);
                context.startActivity(intent);
            }

            @Override
            public void onEndCallWithReason(EaseCallType callType, String channelName, EaseCallEndReason reason, long callTime) {
                EMLog.d(TAG,"onEndCallWithReason" + callType.name() + " reason:" + reason + " time:"+ callTime);
                SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                String callString = "通话时长 ";
                callString += formatter.format(callTime);

                Toast.makeText(mianContext,callString,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGenerateToken(String userId, String channelName, String appKey, EaseCallKitTokenCallback callback){
                EMLog.d(TAG,"onGenerateToken userId:" + userId + " channelName:" + channelName + " appKey:"+ appKey);
                String url = tokenUrl;
                url += "userAccount=";
                url += userId;
                url += "&channelName=";
                url += channelName;
                url += "&appkey=";
                url +=  appKey;
                getRtcToken(url,callback);
            }

            @Override
            public void onRevivedCall(EaseCallType callType, String fromUserId,JSONObject ext) {
                //收到接听电话
                EMLog.d(TAG,"onRecivedCall" + callType.name() + " fromUserId:" + fromUserId);
            }
            @Override
            public  void onCallError(EaseCallUIKit.EaseCallError type, int errorCode, String description){

            }
        };
        EaseCallUIKit.getInstance().setCallKitListener(callKitListener);
    }


    /**
     * 获取声网Token
     *
     */
    private void getRtcToken(String tokenUrl,EaseCallKitTokenCallback callback){
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String resStr = null;
                try {
                    String url = params[0];
                    URL HttpURL = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) HttpURL.openConnection();
                    conn.setRequestProperty("Authorization", "Bearer " + EMClient.getInstance().getAccessToken());

                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    StringBuilder sb = new StringBuilder();
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    while((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    resStr = sb.toString();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return resStr;
            }
            @Override
            protected void onPostExecute(String resStr) {
                if(resStr != null) {
                    try {
                        JSONObject object = new JSONObject(resStr);
                        String code  = object.getString("code");
                        if(code.equals("RES_0K")){
                            String token = object.getString("accessToken");
                            int expireTime = object.getInt("expireTime");
                            callback.onSetToken(token);
                        }else{
                            callback.onSetToken(null);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    callback.onSetToken(null);
                }
            }
        }.execute(tokenUrl);
    }


    /**
     * data sync listener
     */
    public interface DataSyncListener {
        /**
         * sync complete
         * @param success true：data sync successful，false: failed to sync data
         */
        void onSyncComplete(boolean success);
    }
}
