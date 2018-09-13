package oxhammar.nicklas.run;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static oxhammar.nicklas.run.DBHandler.DBHelperItem.COLUMN_ID;
import static oxhammar.nicklas.run.DBHandler.DBHelperItem.COLUMN_JSON_STRING;
import static oxhammar.nicklas.run.DBHandler.DBHelperItem.TABLE_NAME;

/**
 * Created by Nick on 2018-03-22.
 */

public class DBHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "finished_runs.db";
    private static final int DATABASE_VERSION = 1;

    static abstract class DBHelperItem implements BaseColumns {
        static final String TABLE_NAME = "finished_runs";

        static final String COLUMN_ID = "id";
        static final String COLUMN_JSON_STRING = "json_string";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY " + COMMA_SEP +
                    COLUMN_JSON_STRING + TEXT_TYPE + ")";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public String getRun(long id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + COLUMN_ID + " = " + id, null);
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(COLUMN_JSON_STRING));

        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }

    }

    public void deleteRun(long id) {

        Log.d(TAG, String.valueOf(numberOfRows()));
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=" + id, null);

    }

    public long addRun() {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_JSON_STRING, "temporary string");

        return db.insert(TABLE_NAME, null, cv);
    }


    private int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
    }

    public ArrayList<String> getAllRuns() {
        ArrayList<String> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME, null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                array_list.add(cursor.getString(cursor.getColumnIndex(COLUMN_JSON_STRING)));
                cursor.moveToNext();
            }
            return array_list;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean updateRun(String jsonString, FinishedRun run) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_JSON_STRING, jsonString);
        db.update(TABLE_NAME, cv, COLUMN_ID + "=" + run.getId(), null);

        return true;

    }


}
