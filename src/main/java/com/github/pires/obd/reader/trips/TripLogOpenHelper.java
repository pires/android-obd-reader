package com.github.pires.obd.reader.trips;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by elagin on 13.03.15.
 */
public class TripLogOpenHelper extends SQLiteOpenHelper {
    /// tag for logging
    private static final String TAG = TripLogOpenHelper.class.getName();

    public TripLogOpenHelper(Context context) {
        super(context, TripLog.DATABASE_NAME, null, TripLog.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        execSQL(db, TripLog.DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void execSQL(SQLiteDatabase db, String[] statements) {
        final String tag = TAG + ".execSQL()";
        for (String sql : statements) {
            Log.d(tag, sql);
            db.execSQL(sql);
        }
    }
}
