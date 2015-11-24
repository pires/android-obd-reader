package com.github.pires.obd.reader.trips;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Some code taken from https://github.com/wdkapps/FillUp
 */
public class TripLog {

    /// the database version number
    public static final int DATABASE_VERSION = 1;
    /// the name of the database
    public static final String DATABASE_NAME = "tripslog.db";
    /// a tag string for debug logging (the name of this class)
    private static final String TAG = TripLog.class.getName();
    /// database table names
    private static final String RECORDS_TABLE = "Records";
    /// SQL commands to delete the database
    public static final String[] DATABASE_DELETE = new String[]{
            "drop table if exists " + RECORDS_TABLE + ";",
    };
    /// column names for RECORDS_TABLE
    private static final String RECORD_ID = "id";
    private static final String RECORD_START_DATE = "startDate";
    private static final String RECORD_END_DATE = "endDate";
    private static final String RECORD_RPM_MAX = "rmpMax";
    private static final String RECORD_SPEED_MAX = "speedMax";
    private static final String RECORD_ENGINE_RUNTIME = "engineRuntime";
    /// SQL commands to create the database
    public static final String[] DATABASE_CREATE = new String[]{
            "create table " + RECORDS_TABLE + " ( " +
                    RECORD_ID + " integer primary key autoincrement, " +
                    RECORD_START_DATE + " integer not null, " +
                    RECORD_END_DATE + " integer, " +
                    RECORD_SPEED_MAX + " integer, " +
                    RECORD_RPM_MAX + " integer, " +
                    RECORD_ENGINE_RUNTIME + " text" +
                    ");"
    };
    /// array of all column names for RECORDS_TABLE
    private static final String[] RECORDS_TABLE_COLUMNS = new String[]{
            RECORD_ID,
            RECORD_START_DATE,
            RECORD_END_DATE,
            RECORD_SPEED_MAX,
            RECORD_ENGINE_RUNTIME,
            RECORD_RPM_MAX
    };
    /// singleton instance
    private static TripLog instance;
    /// context of the instance creator
    private final Context context;
    /// a helper instance used to open and close the database
    private final TripLogOpenHelper helper;
    /// the database
    private final SQLiteDatabase db;

    private TripLog(Context context) {
        this.context = context;
        this.helper = new TripLogOpenHelper(this.context);
        this.db = helper.getWritableDatabase();
    }

    /**
     * DESCRIPTION:
     * Returns a single instance, creating it if necessary.
     *
     * @return GasLog - singleton instance.
     */
    public static TripLog getInstance(Context context) {
        if (instance == null) {
            instance = new TripLog(context);
        }
        return instance;
    }

    /**
     * DESCRIPTION:
     * Convenience method to test assertion.
     *
     * @param assertion - an asserted boolean condition.
     * @param tag       - a tag String identifying the calling method.
     * @param msg       - an error message to display/log.
     * @throws RuntimeException if the assertion is false
     */
    private void ASSERT(boolean assertion, String tag, String msg) {
        if (!assertion) {
            String assert_msg = "ASSERT failed: " + msg;
            Log.e(tag, assert_msg);
            throw new RuntimeException(assert_msg);
        }
    }

    public TripRecord startTrip() {
        final String tag = TAG + ".createRecord()";

        try {
            TripRecord record = new TripRecord();
            long rowID = db.insertOrThrow(RECORDS_TABLE, null, getContentValues(record));
            record.setID((int) rowID);
            return record;
        } catch (SQLiteConstraintException e) {
            Log.e(tag, "SQLiteConstraintException: " + e.getMessage());
        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
        }
        return null;
    }

    /**
     * DESCRIPTION:
     * Updates a trip record in the log.
     *
     * @param record - the TripRecord to update.
     * @return boolean flag indicating success/failure (true=success)
     */
    public boolean updateRecord(TripRecord record) {
        final String tag = TAG + ".updateRecord()";
        ASSERT((record.getID() != null), tag, "record id cannot be null");
        boolean success = false;
        try {
            ContentValues values = getContentValues(record);
            values.remove(RECORD_ID);
            String whereClause = RECORD_ID + "=" + record.getID();
            int count = db.update(RECORDS_TABLE, values, whereClause, null);
            success = (count > 0);
        } catch (SQLiteConstraintException e) {
            Log.e(tag, "SQLiteConstraintException: " + e.getMessage());
        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
        }
        return success;
    }

    /**
     * DESCRIPTION:
     * Convenience method to convert a TripRecord instance to a set of key/value
     * pairs in a ContentValues instance utilized by SQLite access methods.
     *
     * @param record - the GasRecord to convert.
     * @return a ContentValues instance representing the specified GasRecord.
     */
    private ContentValues getContentValues(TripRecord record) {
        ContentValues values = new ContentValues();
        values.put(RECORD_ID, record.getID());
        values.put(RECORD_START_DATE, record.getStartDate().getTime());
        if (record.getEndDate() != null)
            values.put(RECORD_END_DATE, record.getEndDate().getTime());
        values.put(RECORD_RPM_MAX, record.getEngineRpmMax());
        values.put(RECORD_SPEED_MAX, record.getSpeedMax());
        if (record.getEngineRuntime() != null)
            values.put(RECORD_ENGINE_RUNTIME, record.getEngineRuntime());
        return values;
    }

    private void update() {
        String sql = "ALTER TABLE " + RECORDS_TABLE + " ADD COLUMN " + RECORD_ENGINE_RUNTIME + " integer;";
        db.execSQL(sql);
    }

    public List<TripRecord> readAllRecords() {

        //update();

        final String tag = TAG + ".readAllRecords()";
        List<TripRecord> list = new ArrayList<>();
        Cursor cursor = null;

        try {
            String orderBy = RECORD_START_DATE;
            cursor = db.query(
                    RECORDS_TABLE,
                    RECORDS_TABLE_COLUMNS,
                    null,
                    null, null, null,
                    orderBy,
                    null
            );

            // create a list of TripRecords from the data
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        TripRecord record = getRecordFromCursor(cursor);
                        list.add(record);
                    } while (cursor.moveToNext());
                }
            }

        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
            list.clear();
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * DESCRIPTION:
     * Deletes a specified trip record from the log.
     *
     * @param id - the TripRecord to delete.
     * @return boolean flag indicating success/failure (true=success)
     */
    public boolean deleteTrip(long id) {

        final String tag = TAG + ".deleteRecord()";

        boolean success = false;

        try {
            String whereClause = RECORD_ID + "=" + id;
            String[] whereArgs = null;
            int count = db.delete(RECORDS_TABLE, whereClause, whereArgs);
            success = (count == 1);
        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
        }

        return success;
    }

    /**
     * DESCRIPTION:
     * Convenience method to create a TripRecord instance from values read
     * from the database.
     *
     * @param c - a Cursor containing results of a database query.
     * @return a GasRecord instance (null if no data).
     */
    private TripRecord getRecordFromCursor(Cursor c) {
        final String tag = TAG + ".getRecordFromCursor()";
        TripRecord record = null;
        if (c != null) {
            record = new TripRecord();
            int id = c.getInt(c.getColumnIndex(RECORD_ID));
            long startDate = c.getLong(c.getColumnIndex(RECORD_START_DATE));
            long endTime = c.getLong(c.getColumnIndex(RECORD_END_DATE));
            int engineRpmMax = c.getInt(c.getColumnIndex(RECORD_RPM_MAX));
            int speedMax = c.getInt(c.getColumnIndex(RECORD_SPEED_MAX));
            record.setID(id);
            record.setStartDate(new Date(startDate));
            record.setEndDate(new Date(endTime));
            record.setEngineRpmMax(engineRpmMax);
            record.setSpeedMax(speedMax);
            if (!c.isNull(c.getColumnIndex(RECORD_ENGINE_RUNTIME)))
                record.setEngineRuntime(c.getString(c.getColumnIndex(RECORD_ENGINE_RUNTIME)));
        }
        return record;
    }
}
