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

import oxhammar.nicklas.run.Activities.MainActivity;

import static android.content.ContentValues.TAG;
import static oxhammar.nicklas.run.DBHandler.DBHelperItem.COLUMN_ID;
import static oxhammar.nicklas.run.DBHandler.DBHelperItem.COLUMN_JSON_STRING;
import static oxhammar.nicklas.run.DBHandler.DBHelperItem.TABLE_NAME;

/**
 * Created by Nick on 2018-03-22.
 */

public class DBHandler extends SQLiteOpenHelper{

    private static final String LOG_TAG = "DBHandler";

    public static final String DATABASE_NAME = "finished_runs.db";
    private static final int DATABASE_VERSION = 1;

    public static abstract class DBHelperItem implements BaseColumns {
        public static final String TABLE_NAME = "finished_runs";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_JSON_STRING = "json_string";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY " + COMMA_SEP +
                    COLUMN_JSON_STRING + TEXT_TYPE + ")";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public String getRun(long id){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + COLUMN_ID + " = " + id, null);

        cursor.moveToFirst();

        return cursor.getString(cursor.getColumnIndex(COLUMN_JSON_STRING));

    }

    public void deleteRun (long id) {

        Log.d(TAG, String.valueOf(numberOfRows()));
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=" + id , null);

    }

    public long addRun(String jsonString) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_JSON_STRING, jsonString);

        return db.insert(TABLE_NAME, null, cv);
    }


    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        return numRows;
    }

    public ArrayList<String> getAllRuns() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(COLUMN_JSON_STRING)));
            res.moveToNext();
        }
        return array_list;
    }

    public boolean updateRun(String jsonString, FinishedRun run){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_JSON_STRING, jsonString);
        db.update(TABLE_NAME, cv, COLUMN_ID + "=" + run.getId(), null );

        return true;

    }


}
