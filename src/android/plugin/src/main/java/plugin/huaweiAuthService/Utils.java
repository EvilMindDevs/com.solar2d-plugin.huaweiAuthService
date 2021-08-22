package plugin.huaweiAuthService;

import android.util.Log;
import com.huawei.agconnect.auth.AGConnectUser;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class Utils {

    static JSONArray listIntegerToJsonArray(List<Integer> data) {
        JSONArray jsonArray = new JSONArray();
        for (Integer _data : data) {
            jsonArray.put(_data);
        }
        return jsonArray;
    }

    static JSONObject AGConnectUserToJsonObject(AGConnectUser user) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("isAnonymous", user.isAnonymous());
            jsonObject.put("getUid", user.getUid());
            jsonObject.put("getEmail", user.getEmail());
            jsonObject.put("getPhone", user.getPhone());
            jsonObject.put("getDisplayName", user.getDisplayName());
            jsonObject.put("getPhotoUrl", user.getPhotoUrl());
            jsonObject.put("getProviderId", user.getProviderId());
            jsonObject.put("getEmailVerified", user.getEmailVerified());
            jsonObject.put("getPasswordSetted", user.getPasswordSetted());
            jsonObject.put("getUserExtra", user.getUserExtra());
        } catch (Exception e) {
            Log.d(Constants.TAG, "AGConnectUserToJsonObject Error => " + e.getMessage());
        }
        return jsonObject;
    }

}
