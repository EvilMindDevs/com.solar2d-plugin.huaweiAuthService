package plugin.huaweiAuthService;

import android.util.Log;

import com.ansca.corona.CoronaEnvironment;
import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;
import com.ansca.corona.CoronaRuntimeTask;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.EmailUser;
import com.huawei.agconnect.auth.FacebookAuthProvider;
import com.huawei.agconnect.auth.GoogleAuthProvider;
import com.huawei.agconnect.auth.GoogleGameAuthProvider;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.PhoneUser;
import com.huawei.agconnect.auth.QQAuthProvider;
import com.huawei.agconnect.auth.SelfBuildProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.TwitterAuthProvider;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.agconnect.auth.WeiboAuthProvider;
import com.huawei.agconnect.auth.WeixinAuthProvider;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.agconnect.core.service.auth.OnTokenListener;
import com.huawei.agconnect.core.service.auth.TokenSnapshot;
import com.huawei.hmf.tasks.Task;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaType;
import com.naef.jnlua.NamedJavaFunction;

import java.util.Locale;

@SuppressWarnings({"WeakerAccess", "unused"})
public class LuaLoader implements JavaFunction, CoronaRuntimeListener {

    private static int fListener;
    public static CoronaRuntimeTaskDispatcher fDispatcher = null;

    private static final String EVENT_NAME = "pluginLibraryEvent";

    private static AGConnectAuth agConnectAuth;
    private static AGConnectUser user;
    private static OnTokenListener onTokenListener;

    private static void initAGConnectAuth() {
        if (agConnectAuth == null) {
            agConnectAuth = AGConnectAuth.getInstance();
        }
    }

    @SuppressWarnings("unused")
    public LuaLoader() {
        fListener = CoronaLua.REFNIL;
        CoronaEnvironment.addRuntimeListener(this);
    }

    @Override
    public int invoke(LuaState L) {
        NamedJavaFunction[] luaFunctions = new NamedJavaFunction[]{
                new init(),
                new signIn(),
                new getSupportedAuthList(),
                new signInAnonymously(),
                new deleteUser(),
                new signOut(),
                new getCurrentUser(),
                new addTokenListener(),
                new removeTokenListener(),
                new createUser(),
                new resetPassword(),
                new requestVerifyCode(),
                new updatePhone(),
                new link(),
                new unlink(),
                new reauthenticate()
        };
        String libName = L.toString(1);
        L.register(libName, luaFunctions);
        return 1;
    }

    @Override
    public void onLoaded(CoronaRuntime runtime) {
    }

    @Override
    public void onStarted(CoronaRuntime runtime) {
    }

    @Override
    public void onSuspended(CoronaRuntime runtime) {
    }

    @Override
    public void onResumed(CoronaRuntime runtime) {
    }

    @Override
    public void onExiting(CoronaRuntime runtime) {
        // Remove the Lua listener reference.
        CoronaLua.deleteRef(runtime.getLuaState(), fListener);
        fListener = CoronaLua.REFNIL;
    }

    @SuppressWarnings("unused")
    public static void dispatchEvent(final String message) {
        CoronaEnvironment.getCoronaActivity().getRuntimeTaskDispatcher().send(new CoronaRuntimeTask() {
            @Override
            public void executeUsing(CoronaRuntime runtime) {
                LuaState L = runtime.getLuaState();

                CoronaLua.newEvent(L, EVENT_NAME);

                L.pushString(message);
                L.setField(-2, "message");

                try {
                    CoronaLua.dispatchEvent(L, fListener, 0);
                } catch (Exception ignored) {
                }
            }
        });
    }

    @SuppressWarnings("unused")
    public static void dispatchEvent(final Boolean isError, final String message, final String type, final String provider) {
        CoronaEnvironment.getCoronaActivity().getRuntimeTaskDispatcher().send(new CoronaRuntimeTask() {
            @Override
            public void executeUsing(CoronaRuntime runtime) {
                LuaState L = runtime.getLuaState();

                CoronaLua.newEvent(L, EVENT_NAME);

                L.pushString(message);
                L.setField(-2, "message");

                L.pushBoolean(isError);
                L.setField(-2, "isError");

                L.pushString(type);
                L.setField(-2, "type");

                L.pushString(provider);
                L.setField(-2, "provider");

                try {
                    CoronaLua.dispatchEvent(L, fListener, 0);
                } catch (Exception ignored) {
                }
            }
        });
    }

    private static class init implements NamedJavaFunction {
        @Override
        public String getName() {
            return "init";
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }
            AGConnectServicesConfig config = AGConnectServicesConfig.fromContext(CoronaEnvironment.getApplicationContext());
            config.overlayWith(new HmsLazyInputStream(CoronaEnvironment.getApplicationContext()).get(CoronaEnvironment.getApplicationContext()));

            initAGConnectAuth();

            fDispatcher = new CoronaRuntimeTaskDispatcher(L);
            int listenerIndex = 1;
            if (CoronaLua.isListener(L, listenerIndex, EVENT_NAME)) {
                fListener = CoronaLua.newRef(L, listenerIndex);
            }
            return 0;
        }
    }

    private static class signIn implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.signIn;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            AGConnectAuthCredential credential = null;
            final int listener = CoronaLua.isListener(L, Constants.listenerIndex3, Constants.eventName) ?
                    CoronaLua.newRef(L, Constants.listenerIndex3) : CoronaLua.REFNIL;

            String token, openId, secret, idToken, serverAuthCode;
            String countryCode, phoneNumber, password, verifyCode, email;

            if (L.type(1) == LuaType.STRING || L.type(2) == LuaType.TABLE) {
                switch (L.toString(1)) {
                    case Constants.MobileNumber:
                        L.getField(2, "countryCode");
                        if (L.isString(-1)) {
                            countryCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass countryCode(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "phoneNumber");
                        if (L.isString(-1)) {
                            phoneNumber = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass phoneNumber(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "password");
                        if (L.isString(-1)) {
                            password = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass password(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "verifyCode");
                        if (L.isString(-1)) {
                            verifyCode = L.toString(-1);
                            L.pop(1);
                            credential = PhoneAuthProvider.credentialWithVerifyCode(countryCode, phoneNumber, password, verifyCode);
                        } else {
                            credential = PhoneAuthProvider.credentialWithPassword(countryCode, phoneNumber, password);
                        }
                        break;
                    case Constants.EmailAdress:
                        L.getField(2, "email");
                        if (L.isString(-1)) {
                            email = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass email(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "password");
                        if (L.isString(-1)) {
                            password = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass password(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "verifyCode");
                        if (L.isString(-1)) {
                            verifyCode = L.toString(-1);
                            L.pop(1);
                            credential = EmailAuthProvider.credentialWithVerifyCode(email, password, verifyCode);
                        } else {
                            credential = EmailAuthProvider.credentialWithPassword(email, password);
                        }
                        break;
                    case Constants.HuaweiID:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = HwIdAuthProvider.credentialWithToken(token);
                        } else {
                            credential = HwIdAuthProvider.credentialWithToken(token, L.toBoolean(-1));
                        }
                        break;
                    case Constants.WeChat:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "openId");
                        if (L.isString(-1)) {
                            openId = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass openId(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = WeixinAuthProvider.credentialWithToken(token, openId);
                        } else {
                            credential = WeixinAuthProvider.credentialWithToken(token, openId, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Facebook:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = FacebookAuthProvider.credentialWithToken(token);
                        } else {
                            credential = FacebookAuthProvider.credentialWithToken(token, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Twitter:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "secret");
                        if (L.isString(-1)) {
                            secret = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass secret(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = TwitterAuthProvider.credentialWithToken(token, secret);
                        } else {
                            credential = TwitterAuthProvider.credentialWithToken(token, secret, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Weibo:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "openId");
                        if (L.isString(-1)) {
                            openId = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass openId(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = WeiboAuthProvider.credentialWithToken(token, openId);
                        } else {
                            credential = WeiboAuthProvider.credentialWithToken(token, openId, L.toBoolean(-1));
                        }

                        break;
                    case Constants.QQ:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "openId");
                        if (L.isString(-1)) {
                            openId = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass openId(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = QQAuthProvider.credentialWithToken(token, openId);
                        } else {
                            credential = QQAuthProvider.credentialWithToken(token, openId, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Google:
                        L.getField(2, "idToken");
                        if (L.isString(-1)) {
                            idToken = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass idToken(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = GoogleAuthProvider.credentialWithToken(idToken);
                        } else {
                            credential = GoogleAuthProvider.credentialWithToken(idToken, L.toBoolean(-1));
                        }
                        break;
                    case Constants.GooglePlay:
                        L.getField(2, "serverAuthCode");
                        if (L.isString(-1)) {
                            serverAuthCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass serverAuthCode(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = GoogleGameAuthProvider.credentialWithToken(serverAuthCode);
                        } else {
                            credential = GoogleGameAuthProvider.credentialWithToken(serverAuthCode, L.toBoolean(-1));
                        }
                        break;
                    case Constants.SelfBuild:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.signIn, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = SelfBuildProvider.credentialWithToken(token);
                        } else {
                            credential = SelfBuildProvider.credentialWithToken(token, L.toBoolean(-1));
                        }
                        break;
                    default:
                        sendDispatcher(listener, true, "You need to pass signIn Type (String)", Constants.signIn, Constants.pluginName);
                        return 0;
                }
            } else {
                sendDispatcher(listener, true, "You need to pass (`Twitter`, {properties}, listener)", Constants.signIn, Constants.pluginName);
                return 0;
            }

            AGConnectAuth.getInstance().signIn(credential)
                    .addOnSuccessListener(signInResult -> {
                        sendDispatcher(listener, false, "",
                                Constants.signIn, Constants.pluginName, Utils.AGConnectUserToJsonObject(signInResult.getUser()).toString());
                    })
                    .addOnFailureListener(e -> {
                        sendDispatcher(listener, true, "Error => " + e.getMessage(),
                                Constants.signIn, Constants.pluginName);
                    });

            return 0;
        }
    }

    private static class getSupportedAuthList implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.getSupportedAuthList;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();
            L.pushString(Utils.listIntegerToJsonArray(agConnectAuth.getSupportedAuthList()).toString());
            return 1;
        }
    }

    private static class signInAnonymously implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.signInAnonymously;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            final int listener = CoronaLua.isListener(L, 1, Constants.eventName) ? CoronaLua.newRef(L, 1) : CoronaLua.REFNIL;

            agConnectAuth.signInAnonymously()
                    .addOnSuccessListener(signInResult -> {
                        user = signInResult.getUser();
                        sendDispatcher(listener, false, "", Constants.signInAnonymously, Constants.pluginName,
                                Utils.AGConnectUserToJsonObject(signInResult.getUser()).toString());
                    })
                    .addOnFailureListener(e -> {
                        sendDispatcher(listener, true, "Error => " + e.getMessage(), Constants.signInAnonymously, Constants.pluginName);
                    });
            return 0;
        }
    }

    private static class deleteUser implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.deleteUser;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            try {
                agConnectAuth.deleteUser();
            } catch (Exception e) {
                Log.e(Constants.TAG, "deleteUser => " + e.getMessage());
            }
            return 0;
        }
    }

    private static class signOut implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.signOut;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            try {
                agConnectAuth.signOut();
            } catch (Exception e) {
                Log.e(Constants.TAG, "singOut => " + e.getMessage());
            }
            return 0;
        }
    }

    private static class getCurrentUser implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.getCurrentUser;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            try {
                user = agConnectAuth.getCurrentUser();
                L.pushString(Utils.AGConnectUserToJsonObject(user).toString());
            } catch (Exception e) {
                Log.e(Constants.TAG, "getCurrentUser => " + e.getMessage());
            }

            return 1;
        }
    }

    private static class addTokenListener implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.addTokenListener;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            onTokenListener = tokenSnapshot -> {
                TokenSnapshot.State state = tokenSnapshot.getState();
                if (state == TokenSnapshot.State.TOKEN_UPDATED) {
                    String token = tokenSnapshot.getToken();
                    dispatchEvent(false, token, Constants.addTokenListener, Constants.eventName);
                } else {
                    dispatchEvent(true, "TokenSnapshot is empty", Constants.addTokenListener, Constants.eventName);
                }
            };

            try {
                agConnectAuth.addTokenListener(onTokenListener);
            } catch (Exception e) {
                Log.e(Constants.TAG, "addTokenListener Error => " + e.getMessage());
            }
            return 0;
        }
    }

    private static class removeTokenListener implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.removeTokenListener;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            try {
                agConnectAuth.removeTokenListener(onTokenListener);
            } catch (Exception e) {
                Log.e(Constants.TAG, "removeTokenListener Error => " + e.getMessage());
            }

            return 0;
        }
    }

    private static class createUser implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.createUser;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            final int listener = CoronaLua.isListener(L, Constants.listenerIndex3, Constants.eventName) ?
                    CoronaLua.newRef(L, Constants.listenerIndex3) : CoronaLua.REFNIL;

            boolean isEmail = false;
            boolean isPassword = false;
            String email = "", password = "", verifyCode = "", phoneNumber = "", countryCode = "";

            if (L.type(1) == LuaType.STRING || L.type(2) == LuaType.TABLE) {
                switch (L.toString(1)) {
                    case Constants.EmailAdress:
                        isEmail = true;
                        L.getField(2, "password");
                        if (L.isString(-1)) {
                            password = L.toString(-1);
                            L.pop(1);
                            isPassword = true;
                        }

                        L.getField(2, "email");
                        if (L.isString(-1)) {
                            email = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass email(String)", Constants.createUser, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "verifyCode");
                        if (L.isString(-1)) {
                            verifyCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass verifyCode(String)", Constants.createUser, Constants.pluginName);
                            return 0;
                        }
                        break;
                    case Constants.MobileNumber:
                        L.getField(2, "password");
                        if (L.isString(-1)) {
                            password = L.toString(-1);
                            L.pop(1);
                            isPassword = true;
                        }

                        L.getField(2, "countryCode");
                        if (L.isString(-1)) {
                            countryCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass countryCode(String)", Constants.createUser, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "phoneNumber");
                        if (L.isString(-1)) {
                            phoneNumber = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass phoneNumber(String)", Constants.createUser, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "verifyCode");
                        if (L.isString(-1)) {
                            verifyCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass verifyCode(String)", Constants.createUser, Constants.pluginName);
                            return 0;
                        }
                        break;
                    default:
                        sendDispatcher(listener, true, "You need to pass EmailAdress or MobileNumber", Constants.createUser, Constants.pluginName);
                        return 0;
                }
            } else {
                sendDispatcher(listener, true, "You need to pass (`EmailAdress`, {properties}, listener)", Constants.createUser, Constants.pluginName);
                return 0;
            }

            Task<SignInResult> task;

            if (isEmail) {
                EmailUser emailUser;
                if (isPassword) {
                    emailUser = new EmailUser.Builder()
                            .setEmail(email)
                            .setVerifyCode(verifyCode)
                            .setPassword(password)
                            .build();

                } else {
                    emailUser = new EmailUser.Builder()
                            .setEmail(email)
                            .setVerifyCode(verifyCode)
                            .build();
                }
                task = agConnectAuth.createUser(emailUser);
            } else {
                PhoneUser phoneUser;
                if (isPassword) {
                    phoneUser = new PhoneUser.Builder()
                            .setCountryCode(countryCode)
                            .setPhoneNumber(phoneNumber)
                            .setVerifyCode(verifyCode)
                            .setPassword(password)
                            .build();

                } else {
                    phoneUser = new PhoneUser.Builder()
                            .setCountryCode(countryCode)
                            .setPhoneNumber(phoneNumber)
                            .setVerifyCode(verifyCode)
                            .build();
                }
                task = agConnectAuth.createUser(phoneUser);
            }

            task.addOnSuccessListener(signInResult -> {
                sendDispatcher(listener, false, "", Constants.createUser, Constants.pluginName,
                        Utils.AGConnectUserToJsonObject(signInResult.getUser()).toString());
            }).addOnFailureListener(e -> {
                sendDispatcher(listener, true, "createUser error => " + e.getMessage(), Constants.createUser, Constants.pluginName);
            });

            return 0;
        }
    }

    private static class requestVerifyCode implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.requestVerifyCode;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            final int listener = CoronaLua.isListener(L, 5, Constants.eventName) ?
                    CoronaLua.newRef(L, 5) : CoronaLua.REFNIL;

            boolean isEmail = false;
            boolean isPassword = false;
            String email = "", password = "", phoneNumber = "", countryCode = "";
            int interval = 30, action;

            if (L.type(1) == LuaType.STRING && L.type(2) == LuaType.STRING
                    && L.type(3) == LuaType.NUMBER && L.type(4) == LuaType.TABLE) {

                action = L.toString(2).equals(Constants.ACTION_REGISTER_LOGIN) ?
                        VerifyCodeSettings.ACTION_REGISTER_LOGIN : VerifyCodeSettings.ACTION_RESET_PASSWORD;

                L.getField(3, "interval");
                if (L.isNumber(-1)) {
                    interval = L.toInteger(-1);
                    L.pop(1);
                } else {
                    sendDispatcher(listener, true, "You need to pass interval(int)", Constants.createUser, Constants.pluginName);
                    return 0;
                }

                switch (L.toString(1)) {
                    case Constants.EmailAdress:
                        isEmail = true;
                        L.getField(4, "email");
                        if (L.isString(-1)) {
                            email = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass email(String)", Constants.createUser, Constants.pluginName);
                            return 0;
                        }
                        break;
                    case Constants.MobileNumber:
                        L.getField(4, "countryCode");
                        if (L.isString(-1)) {
                            countryCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass countryCode(String)", Constants.createUser, Constants.pluginName);
                            return 0;
                        }

                        L.getField(4, "phoneNumber");
                        if (L.isString(-1)) {
                            phoneNumber = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass phoneNumber(String)", Constants.createUser, Constants.pluginName);
                            return 0;
                        }
                        break;
                    default:
                        sendDispatcher(listener, true, "You need to pass parameters correctly", Constants.createUser, Constants.pluginName);
                        return 0;
                }
            } else {
                sendDispatcher(listener, true, "You need to pass parameters correctly", Constants.createUser, Constants.pluginName);
                return 0;
            }


            VerifyCodeSettings settings = new VerifyCodeSettings.Builder()
                    .action(action)
                    .sendInterval(interval)
                    .locale(Locale.CHINA)
                    .build();

            Task<VerifyCodeResult> task;

            if (isEmail) {
                task = agConnectAuth.requestVerifyCode(email, settings);
            } else {
                task = agConnectAuth.requestVerifyCode(countryCode, phoneNumber, settings);
            }

            task.addOnSuccessListener(verifyCodeResult -> {
                sendDispatcher(listener, false, "", Constants.requestVerifyCode, Constants.pluginName);
            }).addOnFailureListener(e -> {
                sendDispatcher(listener, true, "createUser error => " + e.getMessage(), Constants.requestVerifyCode, Constants.pluginName);
            });
            return 0;
        }
    }

    private static class updatePhone implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.updatePhone;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            final int listener = CoronaLua.isListener(L, Constants.listenerIndex2, Constants.eventName) ?
                    CoronaLua.newRef(L, Constants.listenerIndex2) : CoronaLua.REFNIL;

            String verifyCode = "", phoneNumber = "", countryCode = "";

            if (L.type(1) == LuaType.TABLE) {
                L.getField(2, "countryCode");
                if (L.isString(-1)) {
                    countryCode = L.toString(-1);
                    L.pop(1);
                } else {
                    sendDispatcher(listener, true, "You need to pass countryCode(String)", Constants.updatePhone, Constants.pluginName);
                    return 0;
                }

                L.getField(2, "phoneNumber");
                if (L.isString(-1)) {
                    phoneNumber = L.toString(-1);
                    L.pop(1);
                } else {
                    sendDispatcher(listener, true, "You need to pass phoneNumber(String)", Constants.updatePhone, Constants.pluginName);
                    return 0;
                }

                L.getField(2, "verifyCode");
                if (L.isString(-1)) {
                    verifyCode = L.toString(-1);
                    L.pop(1);
                } else {
                    sendDispatcher(listener, true, "You need to pass verifyCode(String)", Constants.updatePhone, Constants.pluginName);
                    return 0;
                }
            } else {
                sendDispatcher(listener, true, "You need to pass parameters.", Constants.updatePhone, Constants.pluginName);
                return 0;
            }

            user = agConnectAuth.getCurrentUser();

            if (user != null) {
                user.updatePhone(countryCode, phoneNumber, verifyCode).addOnSuccessListener(result -> {
                    sendDispatcher(listener, false, "", Constants.updatePhone, Constants.pluginName);
                }).addOnFailureListener(e -> {
                    sendDispatcher(listener, true, "updatePhone error => " + e.getMessage(), Constants.updatePhone, Constants.pluginName);
                });
            } else {
                sendDispatcher(listener, true, "First you need to call getCurrentUser method," +
                        "And you need to be singed in", Constants.link, Constants.pluginName);
            }

            return 0;
        }
    }

    private static class resetPassword implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.resetPassword;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            final int listener = CoronaLua.isListener(L, Constants.listenerIndex3, Constants.eventName)
                    ? CoronaLua.newRef(L, Constants.listenerIndex3) : CoronaLua.REFNIL;

            boolean isEmail = false;
            String email = "", newPassword = "", verifyCode = "", phoneNumber = "", countryCode = "";

            if (L.type(1) == LuaType.STRING || L.type(2) == LuaType.TABLE) {
                switch (L.toString(1)) {
                    case Constants.EmailAdress:
                        isEmail = true;
                        L.getField(2, "newPassword");
                        if (L.isString(-1)) {
                            newPassword = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass newPassword(String)", Constants.resetPassword, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "email");
                        if (L.isString(-1)) {
                            email = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass email(String)", Constants.resetPassword, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "verifyCode");
                        if (L.isString(-1)) {
                            verifyCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass verifyCode(String)", Constants.resetPassword, Constants.pluginName);
                            return 0;
                        }
                        break;
                    case Constants.MobileNumber:
                        L.getField(2, "newPassword");
                        if (L.isString(-1)) {
                            newPassword = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass newPassword(String)", Constants.resetPassword, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "countryCode");
                        if (L.isString(-1)) {
                            countryCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass countryCode(String)", Constants.resetPassword, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "phoneNumber");
                        if (L.isString(-1)) {
                            phoneNumber = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass phoneNumber(String)", Constants.resetPassword, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "verifyCode");
                        if (L.isString(-1)) {
                            verifyCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass verifyCode(String)", Constants.resetPassword, Constants.pluginName);
                            return 0;
                        }
                        break;
                    default:
                        sendDispatcher(listener, true, "You need to pass parameters.", Constants.resetPassword, Constants.pluginName);
                        return 0;
                }
            } else {
                sendDispatcher(listener, true, "You need to pass parameters.", Constants.resetPassword, Constants.pluginName);
                return 0;
            }

            Task<Void> task;

            if (isEmail) {
                task = agConnectAuth.resetPassword(email, newPassword, verifyCode);
            } else {
                task = agConnectAuth.resetPassword(countryCode, phoneNumber, newPassword, verifyCode);
            }

            task.addOnSuccessListener(result -> {
                sendDispatcher(listener, false, "", Constants.createUser, Constants.pluginName);
            }).addOnFailureListener(e -> {
                sendDispatcher(listener, true, "resetPassword error => " + e.getMessage(), Constants.createUser, Constants.pluginName);
            });
            return 0;
        }
    }

    private static class link implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.link;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            AGConnectAuthCredential credential = null;
            final int listener = CoronaLua.isListener(L, Constants.listenerIndex3, Constants.eventName)
                    ? CoronaLua.newRef(L, Constants.listenerIndex3) : CoronaLua.REFNIL;

            String token, openId, secret, idToken, serverAuthCode;
            String countryCode, phoneNumber, password, verifyCode, email;

            if (L.type(1) == LuaType.STRING || L.type(2) == LuaType.TABLE) {
                switch (L.toString(1)) {
                    case Constants.MobileNumber:
                        L.getField(2, "countryCode");
                        if (L.isString(-1)) {
                            countryCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass countryCode(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "phoneNumber");
                        if (L.isString(-1)) {
                            phoneNumber = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass phoneNumber(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "password");
                        if (L.isString(-1)) {
                            password = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass password(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "verifyCode");
                        if (L.isString(-1)) {
                            verifyCode = L.toString(-1);
                            L.pop(1);
                            credential = PhoneAuthProvider.credentialWithVerifyCode(countryCode, phoneNumber, password, verifyCode);
                        } else {
                            credential = PhoneAuthProvider.credentialWithPassword(countryCode, phoneNumber, password);
                        }
                        break;
                    case Constants.EmailAdress:
                        L.getField(2, "email");
                        if (L.isString(-1)) {
                            email = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass email(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "password");
                        if (L.isString(-1)) {
                            password = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass password(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "verifyCode");
                        if (L.isString(-1)) {
                            verifyCode = L.toString(-1);
                            L.pop(1);
                            credential = EmailAuthProvider.credentialWithVerifyCode(email, password, verifyCode);
                        } else {
                            credential = EmailAuthProvider.credentialWithPassword(email, password);
                        }
                        break;
                    case Constants.HuaweiID:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = HwIdAuthProvider.credentialWithToken(token);
                        } else {
                            credential = HwIdAuthProvider.credentialWithToken(token, L.toBoolean(-1));
                        }
                        break;
                    case Constants.WeChat:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "openId");
                        if (L.isString(-1)) {
                            openId = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass openId(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = WeixinAuthProvider.credentialWithToken(token, openId);
                        } else {
                            credential = WeixinAuthProvider.credentialWithToken(token, openId, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Facebook:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = FacebookAuthProvider.credentialWithToken(token);
                        } else {
                            credential = FacebookAuthProvider.credentialWithToken(token, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Twitter:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "secret");
                        if (L.isString(-1)) {
                            secret = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass secret(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = TwitterAuthProvider.credentialWithToken(token, secret);
                        } else {
                            credential = TwitterAuthProvider.credentialWithToken(token, secret, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Weibo:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "openId");
                        if (L.isString(-1)) {
                            openId = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass openId(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = WeiboAuthProvider.credentialWithToken(token, openId);
                        } else {
                            credential = WeiboAuthProvider.credentialWithToken(token, openId, L.toBoolean(-1));
                        }

                        break;
                    case Constants.QQ:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "openId");
                        if (L.isString(-1)) {
                            openId = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass openId(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = QQAuthProvider.credentialWithToken(token, openId);
                        } else {
                            credential = QQAuthProvider.credentialWithToken(token, openId, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Google:
                        L.getField(2, "idToken");
                        if (L.isString(-1)) {
                            idToken = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass idToken(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = GoogleAuthProvider.credentialWithToken(idToken);
                        } else {
                            credential = GoogleAuthProvider.credentialWithToken(idToken, L.toBoolean(-1));
                        }
                        break;
                    case Constants.GooglePlay:
                        L.getField(2, "serverAuthCode");
                        if (L.isString(-1)) {
                            serverAuthCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass serverAuthCode(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = GoogleGameAuthProvider.credentialWithToken(serverAuthCode);
                        } else {
                            credential = GoogleGameAuthProvider.credentialWithToken(serverAuthCode, L.toBoolean(-1));
                        }
                        break;
                    case Constants.SelfBuild:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.link, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = SelfBuildProvider.credentialWithToken(token);
                        } else {
                            credential = SelfBuildProvider.credentialWithToken(token, L.toBoolean(-1));
                        }
                        break;
                    default:
                        sendDispatcher(listener, true, "You need to pass correctly parameters", Constants.link, Constants.pluginName);
                        return 0;
                }
            } else {
                sendDispatcher(listener, true, "You need to pass correctly parameters", Constants.link, Constants.pluginName);
                return 0;
            }

            user = agConnectAuth.getCurrentUser();
            if (user != null) {
                user.link(credential)
                        .addOnSuccessListener(signInResult -> {
                            user = signInResult.getUser();
                            sendDispatcher(listener, false, "", Constants.link, Constants.pluginName, Utils.AGConnectUserToJsonObject(user).toString());

                        })
                        .addOnFailureListener(e -> {
                            sendDispatcher(listener, true, "Error => " + e.getMessage(), Constants.link, Constants.pluginName);
                        });
            } else {
                sendDispatcher(listener, true, "First you need to call getCurrentUser method," +
                        "And you need to be singed in", Constants.link, Constants.pluginName);
            }


            return 0;
        }
    }

    private static class unlink implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.unlink;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            final int listener = CoronaLua.isListener(L, Constants.listenerIndex2, Constants.eventName) ? CoronaLua.newRef(L, Constants.listenerIndex2) : CoronaLua.REFNIL;

            int provider;
            if (L.type(1) == LuaType.STRING) {
                switch (L.toString(1)) {
                    case Constants.MobileNumber:
                        provider = AGConnectAuthCredential.Phone_Provider;
                        break;
                    case Constants.EmailAdress:
                        provider = AGConnectAuthCredential.Email_Provider;
                        break;
                    case Constants.HuaweiID:
                        provider = AGConnectAuthCredential.HMS_Provider;
                        break;
                    case Constants.WeChat:
                        provider = AGConnectAuthCredential.WeiXin_Provider;
                        break;
                    case Constants.Facebook:
                        provider = AGConnectAuthCredential.Facebook_Provider;
                        break;
                    case Constants.Twitter:
                        provider = AGConnectAuthCredential.Twitter_Provider;
                        break;
                    case Constants.Weibo:
                        provider = AGConnectAuthCredential.WeiBo_Provider;
                        break;
                    case Constants.QQ:
                        provider = AGConnectAuthCredential.QQ_Provider;
                        break;
                    case Constants.Google:
                        provider = AGConnectAuthCredential.Google_Provider;
                        break;
                    case Constants.GooglePlay:
                        provider = AGConnectAuthCredential.GoogleGame_Provider;
                        break;
                    case Constants.SelfBuild:
                        provider = AGConnectAuthCredential.SelfBuild_Provider;
                        break;
                    case Constants.Anonymous:
                        provider = AGConnectAuthCredential.Anonymous;
                        break;
                    default:
                        sendDispatcher(listener, true, "You need to pass correctly parameters", Constants.unlink, Constants.pluginName);
                        return 0;
                }
            } else {
                sendDispatcher(listener, true, "You need to pass correctly parameters", Constants.unlink, Constants.pluginName);
                return 0;
            }

            user = agConnectAuth.getCurrentUser();
            if (user != null) {
                user.unlink(provider);
                sendDispatcher(listener, false, "", Constants.unlink, Constants.pluginName);
            } else {
                sendDispatcher(listener, true, "First you need to call getCurrentUser method," +
                        "And you need to be singed in", Constants.unlink, Constants.pluginName);
            }


            return 0;
        }
    }

    private static class reauthenticate implements NamedJavaFunction {
        @Override
        public String getName() {
            return Constants.reauthenticate;
        }

        @Override
        public int invoke(LuaState L) {
            if (CoronaEnvironment.getCoronaActivity() == null) {
                return 0;
            }

            initAGConnectAuth();

            AGConnectAuthCredential credential = null;
            final int listener = CoronaLua.isListener(L, Constants.listenerIndex3, Constants.eventName)
                    ? CoronaLua.newRef(L, Constants.listenerIndex3) : CoronaLua.REFNIL;

            String token, openId, secret, idToken, serverAuthCode;
            String countryCode, phoneNumber, password, verifyCode, email;

            if (L.type(1) == LuaType.STRING || L.type(2) == LuaType.TABLE) {
                switch (L.toString(1)) {
                    case Constants.MobileNumber:
                        L.getField(2, "countryCode");
                        if (L.isString(-1)) {
                            countryCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass countryCode(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "phoneNumber");
                        if (L.isString(-1)) {
                            phoneNumber = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass phoneNumber(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "password");
                        if (L.isString(-1)) {
                            password = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass password(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "verifyCode");
                        if (L.isString(-1)) {
                            verifyCode = L.toString(-1);
                            L.pop(1);
                            credential = PhoneAuthProvider.credentialWithVerifyCode(countryCode, phoneNumber, password, verifyCode);
                        } else {
                            credential = PhoneAuthProvider.credentialWithPassword(countryCode, phoneNumber, password);
                        }
                        break;
                    case Constants.EmailAdress:
                        L.getField(2, "email");
                        if (L.isString(-1)) {
                            email = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass email(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "password");
                        if (L.isString(-1)) {
                            password = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass password(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "verifyCode");
                        if (L.isString(-1)) {
                            verifyCode = L.toString(-1);
                            L.pop(1);
                            credential = EmailAuthProvider.credentialWithVerifyCode(email, password, verifyCode);
                        } else {
                            credential = EmailAuthProvider.credentialWithPassword(email, password);
                        }
                        break;
                    case Constants.HuaweiID:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = HwIdAuthProvider.credentialWithToken(token);
                        } else {
                            credential = HwIdAuthProvider.credentialWithToken(token, L.toBoolean(-1));
                        }
                        break;
                    case Constants.WeChat:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "openId");
                        if (L.isString(-1)) {
                            openId = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass openId(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = WeixinAuthProvider.credentialWithToken(token, openId);
                        } else {
                            credential = WeixinAuthProvider.credentialWithToken(token, openId, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Facebook:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = FacebookAuthProvider.credentialWithToken(token);
                        } else {
                            credential = FacebookAuthProvider.credentialWithToken(token, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Twitter:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "secret");
                        if (L.isString(-1)) {
                            secret = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass secret(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = TwitterAuthProvider.credentialWithToken(token, secret);
                        } else {
                            credential = TwitterAuthProvider.credentialWithToken(token, secret, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Weibo:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "openId");
                        if (L.isString(-1)) {
                            openId = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass openId(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = WeiboAuthProvider.credentialWithToken(token, openId);
                        } else {
                            credential = WeiboAuthProvider.credentialWithToken(token, openId, L.toBoolean(-1));
                        }

                        break;
                    case Constants.QQ:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "openId");
                        if (L.isString(-1)) {
                            openId = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass openId(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = QQAuthProvider.credentialWithToken(token, openId);
                        } else {
                            credential = QQAuthProvider.credentialWithToken(token, openId, L.toBoolean(-1));
                        }
                        break;
                    case Constants.Google:
                        L.getField(2, "idToken");
                        if (L.isString(-1)) {
                            idToken = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass idToken(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = GoogleAuthProvider.credentialWithToken(idToken);
                        } else {
                            credential = GoogleAuthProvider.credentialWithToken(idToken, L.toBoolean(-1));
                        }
                        break;
                    case Constants.GooglePlay:
                        L.getField(2, "serverAuthCode");
                        if (L.isString(-1)) {
                            serverAuthCode = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass serverAuthCode(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = GoogleGameAuthProvider.credentialWithToken(serverAuthCode);
                        } else {
                            credential = GoogleGameAuthProvider.credentialWithToken(serverAuthCode, L.toBoolean(-1));
                        }
                        break;
                    case Constants.SelfBuild:
                        L.getField(2, "token");
                        if (L.isString(-1)) {
                            token = L.toString(-1);
                            L.pop(1);
                        } else {
                            sendDispatcher(listener, true, "You need to pass token(String)", Constants.reauthenticate, Constants.pluginName);
                            return 0;
                        }

                        L.getField(2, "autoCreateUser");
                        if (L.isBoolean(-1)) {
                            L.pop(1);
                            credential = SelfBuildProvider.credentialWithToken(token);
                        } else {
                            credential = SelfBuildProvider.credentialWithToken(token, L.toBoolean(-1));
                        }
                        break;
                    default:
                        sendDispatcher(listener, true, "You need to pass parameters", Constants.reauthenticate, Constants.pluginName);
                        return 0;
                }
            } else {
                sendDispatcher(listener, true, "You need to pass parameters", Constants.reauthenticate, Constants.pluginName);
                return 0;
            }

            user = agConnectAuth.getCurrentUser();
            if (user != null) {
                user.reauthenticate(credential)
                        .addOnSuccessListener(result -> {
                            user = result.getUser();
                            sendDispatcher(listener, false, "You need to pass token(String)",
                                    Constants.reauthenticate, Constants.pluginName, Utils.AGConnectUserToJsonObject(user).toString());
                        })
                        .addOnFailureListener(e -> {
                            sendDispatcher(listener, true, "Reauthenticate Error => " + e.getMessage(), Constants.reauthenticate, Constants.pluginName);
                        });
            } else {
                sendDispatcher(listener, true, "First, you need to call getCurrentUser method", Constants.reauthenticate, Constants.pluginName);
            }

            return 0;
        }
    }

    public static void sendDispatcher(final int listener, final boolean isError, final String message, final String type, final String provider, final String data) {
        fDispatcher.send(coronaRuntime -> {
            if (listener != CoronaLua.REFNIL) {
                LuaState L = coronaRuntime.getLuaState();
                try {
                    CoronaLua.newEvent(L, Constants.eventName);

                    L.pushString(message);
                    L.setField(-2, "message");

                    L.pushBoolean(isError);
                    L.setField(-2, "isError");

                    L.pushString(type);
                    L.setField(-2, "type");

                    L.pushString(provider);
                    L.setField(-2, "provider");

                    L.pushString(data);
                    L.setField(-2, "data");

                    CoronaLua.dispatchEvent(L, listener, 0);
                } catch (Exception ex) {
                    Log.i(Constants.TAG, "Corona Error:", ex);
                } finally {
                    CoronaLua.deleteRef(L, listener);
                }
            }
        });
    }

    public static void sendDispatcher(final int listener, final boolean isError, final String message, final String type, final String provider) {
        fDispatcher.send(coronaRuntime -> {
            if (listener != CoronaLua.REFNIL) {
                LuaState L = coronaRuntime.getLuaState();
                try {
                    CoronaLua.newEvent(L, Constants.eventName);

                    L.pushString(message);
                    L.setField(-2, "message");

                    L.pushBoolean(isError);
                    L.setField(-2, "isError");

                    L.pushString(type);
                    L.setField(-2, "type");

                    L.pushString(provider);
                    L.setField(-2, "provider");

                    CoronaLua.dispatchEvent(L, listener, 0);
                } catch (Exception ex) {
                    Log.i(Constants.TAG, "Corona Error:", ex);
                } finally {
                    CoronaLua.deleteRef(L, listener);
                }
            }
        });
    }

}
