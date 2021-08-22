package plugin.huaweiAuthService;

import android.content.Context;
import android.util.Log;

import com.huawei.agconnect.config.LazyInputStream;

import java.io.InputStream;

public class HmsLazyInputStream extends LazyInputStream {
    HmsLazyInputStream(Context context) {
        super(context);
    }

    @Override
    public InputStream get(Context context) {
        try {
            Log.i(Constants.TAG, "reading agconnect-services.json file");
            return context.getAssets().open("agconnect-services.json");
        } catch (Exception e) {
            Log.e(Constants.TAG, "can not read agconnect-services.json file => " + e.getMessage() + " / " + e.getCause());
            return null;
        }
    }

}
