package plugin.huaweiAuthService;

public class Constants {
    static final String eventName = "AuthService";
    static final String pluginName = "AuthService";

    static final String TAG = "HMS Auth Service";
    static final int listenerIndex2 = 2;
    static final int listenerIndex3 = 3;
    static final String signIn = "signIn";
    static final String getSupportedAuthList = "getSupportedAuthList";
    static final String signInAnonymously = "signInAnonymously";
    static final String deleteUser = "deleteUser";
    static final String signOut = "signOut";
    static final String getCurrentUser = "getCurrentUser";
    static final String addTokenListener = "addTokenListener";
    static final String removeTokenListener = "removeTokenListener";
    static final String createUser = "createUser";
    static final String resetPassword = "resetPassword";
    static final String requestVerifyCode = "requestVerifyCode";
    static final String updatePhone = "updatePhone";
    static final String link = "link";
    static final String unlink = "unlink";
    static final String reauthenticate = "reauthenticate";

    // Auth Modes
    static final String MobileNumber = "MobileNumber";
    static final String EmailAdress = "EmailAdress";
    static final String HuaweiID = "HuaweiID";
    static final String WeChat = "WeChat";
    static final String Facebook = "Facebook";
    static final String Twitter = "Twitter";
    static final String Weibo = "Weibo";
    static final String QQ = "QQ";
    static final String Google = "Google";
    static final String GooglePlay = "GooglePlay";
    static final String SelfBuild = "SelfBuild";
    static final String Anonymous = "Anonymous";

    // VerifyCodeSettings
    static final String ACTION_REGISTER_LOGIN = "ACTION_REGISTER_LOGIN";
    static final String ACTION_RESET_PASSWORD = "ACTION_RESET_PASSWORD";
}
