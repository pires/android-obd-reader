package com.github.pires.obd.reader.result;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.pires.obd.reader.R;

import java.util.ArrayList;

public class ResultListAdapter extends ArrayAdapter<ResultRecord> {
  /// the Android Activity owning the ListView
  private final Activity activity;
  private final ArrayList<ResultRecord> records;

  public ResultListAdapter(Activity activity, ArrayList<ResultRecord> records) {
    super(activity, R.layout.result_view, records);
    this.activity = activity;
    this.records = records;
  }

  @Override
  public View getView(int position, View view, ViewGroup parent) {

    // create a view if it doesn't already exist
    if (view == null) {
      LayoutInflater inflater = activity.getLayoutInflater();
      view = inflater.inflate(R.layout.result_view, null);
    }

    // get widgets from the view
    TextView Description = (TextView) view.findViewById(R.id.rowDescription);
    TextView Result = (TextView) view.findViewById(R.id.rowResult);
    TextView Unit = (TextView) view.findViewById(R.id.rowUnit);

    // populate row widgets from record data
    ResultRecord record = records.get(position);

    Description.setText(record.name);
    Result.setText(record.result);
    Unit.setText(record.unit);

    return view;
  }

  @Override
  public void notifyDataSetChanged() {
    super.notifyDataSetChanged();
  }
}
