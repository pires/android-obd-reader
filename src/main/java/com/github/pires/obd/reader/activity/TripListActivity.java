package com.github.pires.obd.reader.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.pires.obd.reader.R;
import com.github.pires.obd.reader.trips.TripListAdapter;
import com.github.pires.obd.reader.trips.TripLog;
import com.github.pires.obd.reader.trips.TripRecord;

import java.util.List;

import roboguice.activity.RoboActivity;

import static com.github.pires.obd.reader.activity.ConfirmDialog.createDialog;

/**
 * Some code taken from https://github.com/wdkapps/FillUp
 */

public class TripListActivity
        extends RoboActivity
        implements ConfirmDialog.Listener {

    private List<TripRecord> records;
    private TripLog triplog = null;
    private TripListAdapter adapter = null;

    /// the currently selected row from the list of records
    private int selectedRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_list);

        ListView lv = (ListView) findViewById(R.id.tripList);

        triplog = TripLog.getInstance(this.getApplicationContext());
        records = triplog.readAllRecords();
        adapter = new TripListAdapter(this, records);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_trips_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        // create the menu
        getMenuInflater().inflate(R.menu.context_trip_list, menu);

        // get index of currently selected row
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedRow = (int) info.id;

        // get record that is currently selected
        TripRecord record = records.get(selectedRow);
    }

    private void deleteTrip() {
        // get the record to delete from our list of records
        TripRecord record = records.get(selectedRow);

        // attempt to remove the record from the log
        if (triplog.deleteTrip(record.getID())) {

            // remove the record from our list of records
            records.remove(selectedRow);

            // update the list view
            adapter.notifyDataSetChanged();
        } else {
            //Utilities.toast(this,getString(R.string.toast_delete_failed));
        }
    }

    public boolean onContextItemSelected(MenuItem item) {

        // get index of currently selected row
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        selectedRow = (int) info.id;

        switch (item.getItemId()) {
            case R.id.itemDelete:
                showDialog(ConfirmDialog.DIALOG_CONFIRM_DELETE_ID);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        return createDialog(id, this, this);
    }

    /**
     * DESCRIPTION:
     * Called when the user has selected a gasoline record to delete
     * from the log and has confirmed deletion.
     */
    protected void deleteRow() {

        // get the record to delete from our list of records
        TripRecord record = records.get(selectedRow);

        // attempt to remove the record from the log
        if (triplog.deleteTrip(record.getID())) {
            records.remove(selectedRow);
            adapter.notifyDataSetChanged();
        } else {
            //Utilities.toast(this,getString(R.string.toast_delete_failed));
        }
    }

    @Override
    public void onConfirmationDialogResponse(int id, boolean confirmed) {
        removeDialog(id);
        if (!confirmed) return;

        switch (id) {
            case ConfirmDialog.DIALOG_CONFIRM_DELETE_ID:
                deleteRow();
                break;

            default:
                //Utilities.toast(this,"Invalid dialog id.");
        }

    }
}
