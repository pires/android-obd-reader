package pt.lighthouselabs.obd.reader.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.UUID;

/**
 * Created by admin on 12/24/14
 */
public class Device
{
    private static final String TAG_PREFS_NAME = "spStorage";
    private static final String TAG_DEVICE_UUID = "deviceUUID";

    /**
     * Generates a variant 2, version 4 (randomly generated number) UUID as per
     * http://www.ietf.org/rfc/rfc4122.txt
     *
     * @param context App context
     * @return Immutable representation of a 128-bit universally unique identifier (UUID).
     */
    public static UUID getDeviceUUID(Context context)
    {
        String myUUID = getDeviceUUIDFromSP(context);
        if (TextUtils.isEmpty(myUUID))
        {
            UUID uuid = UUID.randomUUID();
            myUUID = uuid.toString();
            seDeviceUUIDToSP(context, myUUID);
        }

        return UUID.fromString(myUUID);
    }


    private static void seDeviceUUIDToSP(Context context, String token) {
        SharedPreferences settings = context.getSharedPreferences(TAG_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(TAG_DEVICE_UUID, token);
        editor.apply();
    }

    private static String getDeviceUUIDFromSP(Context context) {
        SharedPreferences settings = context.getSharedPreferences(TAG_PREFS_NAME, 0);
        return settings.getString(TAG_DEVICE_UUID, "");
    }
}
