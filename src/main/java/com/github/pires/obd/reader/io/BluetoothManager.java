package com.github.pires.obd.reader.io;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothManager {
	
    private static final String TAG = BluetoothManager.class.getName();
    /*
     * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
     * #createRfcommSocketToServiceRecord(java.util.UUID)
     *
     * "Hint: If you are connecting to a Bluetooth serial board then try using the
     * well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if you
     * are connecting to an Android peer then please generate your own unique
     * UUID."
     */
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * Instantiates a BluetoothSocket for the remote device and connects it.
     * <p/>
     * See http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701#18786701
     *
     * @param dev The remote device to connect to
     * @return The BluetoothSocket
     * @throws IOException
     */
    public static BluetoothSocket connect(BluetoothDevice dev) throws IOException {
    	BluetoothSocket sock = null;
        BluetoothSocket sockFallback = null;

        Log.d(TAG, "Starting Bluetooth connection..");
    	try {
    		sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
    		sock.connect();
        } catch (Exception e1) {
            Log.e(TAG, "There was an error while establishing Bluetooth connection. Falling back..", e1);
            Class<?> clazz = sock.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
            try {
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                sockFallback = (BluetoothSocket) m.invoke(sock.getRemoteDevice(), params);
                sockFallback.connect();
                sock = sockFallback;
            } catch (Exception e2) {
                Log.e(TAG, "Couldn't fallback while establishing Bluetooth connection.", e2);
                throw new IOException(e2.getMessage());
            }
        }
    	return sock;
    }
}