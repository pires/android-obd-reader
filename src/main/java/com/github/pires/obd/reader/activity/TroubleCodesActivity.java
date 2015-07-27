package com.github.pires.obd.reader.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.pires.obd.commands.control.TroubleCodesObdCommand;
import com.github.pires.obd.commands.protocol.EchoOffObdCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffObdCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.ResetTroubleCodes;
import com.github.pires.obd.commands.protocol.SelectProtocolObdCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.MisunderstoodCommandException;
import com.github.pires.obd.exceptions.UnableToConnectException;
import com.github.pires.obd.reader.R;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TroubleCodesActivity extends Activity {

    private static final String TAG = TroubleCodesActivity.class.getName();
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int NO_BLUETOOTH_DEVICE_SELECTED = 0;
    private static final int CANNOT_CONNECT_TO_DEVICE = 1;
    private static final int OBD_COMMAND_FAILURE = 2;
    private static final int NO_DATA = 3;
    private static final int DATA_OK = 4;
    private static final int CLEAR_DTC = 5;
    @Inject
    SharedPreferences prefs;
    private ProgressDialog progressDialog;
    private String remoteDevice;
    private GetTroubleCodesTask gtct;
    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Message received on handler");
            switch (msg.what) {
                case NO_BLUETOOTH_DEVICE_SELECTED:
                    makeToast(getString(R.string.text_bluetooth_nodevice));
                    finish();
                    break;
                case CANNOT_CONNECT_TO_DEVICE:
                    makeToast(getString(R.string.text_bluetooth_error_connecting));
                    finish();
                    break;
                case OBD_COMMAND_FAILURE:
                    makeToast(getString(R.string.text_obd_command_failure));
                    finish();
                    break;
                case NO_DATA:
                    makeToast(getString(R.string.text_dtc_no_data));
                    finish();
                    break;
                case DATA_OK:
                    dataOk((String) msg.obj);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        remoteDevice = prefs.getString(ConfigActivity.BLUETOOTH_LIST_KEY, null);
        if (remoteDevice == null || "".equals(remoteDevice)) {
            Log.e(TAG, "No Bluetooth device has been selected.");
            mHandler.obtainMessage(NO_BLUETOOTH_DEVICE_SELECTED).sendToTarget();
        } else {
            gtct = new GetTroubleCodesTask();
            gtct.execute(remoteDevice);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trouble_codes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_clear_codes:
                try {
                    sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
                    sock.connect();

                } catch (Exception e) {
                    Log.e(
                            TAG,
                            "There was an error while establishing connection. -> "
                                    + e.getMessage()
                    );
                    Log.d(TAG, "Message received on handler here");
                    mHandler.obtainMessage(CANNOT_CONNECT_TO_DEVICE).sendToTarget();
                    return true;
                }
                try {

                    Log.d("TESTRESET", "Trying reset");
                    //new ObdResetCommand().run(sock.getInputStream(), sock.getOutputStream());
                    ResetTroubleCodes clear = new ResetTroubleCodes();
                    clear.run(sock.getInputStream(), sock.getOutputStream());
                    String result = clear.getFormattedResult();
                    Log.d("TESTRESET", "Trying reset result: " + result);
                } catch (Exception e) {
                    Log.e(
                            TAG,
                            "There was an error while establishing connection. -> "
                                    + e.getMessage()
                    );
                }
                gtct.closeSocket(sock);
                // Refresh main activity upon close of dialog box
                Intent refresh = new Intent(this, TroubleCodesActivity.class);
                startActivity(refresh);
                this.finish(); //
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    Map<String, String> getDict(int keyId, int valId) {
        String[] keys = getResources().getStringArray(keyId);
        String[] vals = getResources().getStringArray(valId);

        Map<String, String> dict = new HashMap<String, String>();
        for (int i = 0, l = keys.length; i < l; i++) {
            dict.put(keys[i], vals[i]);
        }

        return dict;
    }

    public void makeToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void dataOk(String res) {
        ListView lv = (ListView) findViewById(R.id.listView);
        Map<String, String> dtcVals = getDict(R.array.dtc_keys, R.array.dtc_values);
        //TODO replace below codes (res) with aboce dtcVals
        //String tmpVal = dtcVals.get(res.split("\n"));
        //String[] dtcCodes = new String[]{};
        ArrayList<String> dtcCodes = new ArrayList<String>();
        //int i =1;
        if (!res.isEmpty()) {
            for (String dtcCode : res.split("\n")) {
                dtcCodes.add(dtcCode + " : " + dtcVals.get(dtcCode));
                Log.d("TEST", dtcCode + " : " + dtcVals.get(dtcCode));
            }
        } else {
            dtcCodes.add("There are no errors");
        }
        ArrayAdapter<String> myarrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dtcCodes);
        lv.setAdapter(myarrayAdapter);
        lv.setTextFilterEnabled(true);
    }


    public class ModifiedTroubleCodesObdCommand extends TroubleCodesObdCommand {
        @Override
        public String getResult() {
            // remove unwanted response from output since this results in erroneous error codes
            return rawData.replace("SEARCHING...", "");
        }
    }

    public class ClearDTC extends ResetTroubleCodes {
        @Override
        public String getResult() {
            return rawData;
        }
    }


    private class GetTroubleCodesTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            //Create a new progress dialog
            progressDialog = new ProgressDialog(TroubleCodesActivity.this);
            //Set the progress dialog to display a horizontal progress bar
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //Set the dialog title to 'Loading...'
            progressDialog.setTitle(getString(R.string.dialog_loading_title));
            //Set the dialog message to 'Loading application View, please wait...'
            progressDialog.setMessage(getString(R.string.dialog_loading_body));
            //This dialog can't be canceled by pressing the back key
            progressDialog.setCancelable(false);
            //This dialog isn't indeterminate
            progressDialog.setIndeterminate(false);
            //The maximum number of items is 100
            progressDialog.setMax(5);
            //Set the current progress to zero
            progressDialog.setProgress(0);
            //Display the progress dialog
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";

            //Get the current thread's token
            synchronized (this) {
                Log.d(TAG, "Starting service..");
                // get the remote Bluetooth device

                final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                dev = btAdapter.getRemoteDevice(params[0]);

                Log.d(TAG, "Stopping Bluetooth discovery.");
                btAdapter.cancelDiscovery();

                Log.d(TAG, "Starting OBD connection..");

                // Instantiate a BluetoothSocket for the remote device and connect it.
                try {
                    sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
                    sock.connect();

                } catch (Exception e) {
                    Log.e(
                            TAG,
                            "There was an error while establishing connection. -> "
                                    + e.getMessage()
                    );
                    Log.d(TAG, "Message received on handler here");
                    mHandler.obtainMessage(CANNOT_CONNECT_TO_DEVICE).sendToTarget();
                    return null;
                }

                try {
                    // Let's configure the connection.
                    Log.d(TAG, "Queueing jobs for connection configuration..");

                    onProgressUpdate(1);

                    new ObdResetCommand().run(sock.getInputStream(), sock.getOutputStream());


                    onProgressUpdate(2);

                    new EchoOffObdCommand().run(sock.getInputStream(), sock.getOutputStream());

                    onProgressUpdate(3);

                    new LineFeedOffObdCommand().run(sock.getInputStream(), sock.getOutputStream());

                    onProgressUpdate(4);

                    new SelectProtocolObdCommand(ObdProtocols.AUTO).run(sock.getInputStream(), sock.getOutputStream());

                    onProgressUpdate(5);

                    ModifiedTroubleCodesObdCommand tcoc = new ModifiedTroubleCodesObdCommand();
                    tcoc.run(sock.getInputStream(), sock.getOutputStream());
                    result = tcoc.getFormattedResult();

                    onProgressUpdate(6);

                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE).sendToTarget();
                    return null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE).sendToTarget();
                    return null;
                } catch (UnableToConnectException e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE).sendToTarget();
                    return null;
                } catch (MisunderstoodCommandException e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE).sendToTarget();
                    return null;
                } catch (Exception e) {
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE).sendToTarget();
                } finally {

                    // close socket
                    closeSocket(sock);
                }

            }

            return result;
        }

        public void closeSocket(BluetoothSocket sock) {
            if (sock != null)
                // close socket
                try {
                    sock.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result == null) {
                return;
            }
            if (result.contains("NODATA")) {
                mHandler.obtainMessage(NO_DATA, result).sendToTarget();
            } else {
                mHandler.obtainMessage(DATA_OK, result).sendToTarget();
                setContentView(R.layout.trouble_codes);
            }
        }
    }

}
