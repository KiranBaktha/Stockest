package edu.uchicago.cs.stockest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database_Adapter {

    //Column Names
    public static final String COL_ID = "_id";
    public static final String COL_STOCK_SYMBOL = "symbol";
    public static final String COL_STOCK_NAME = "name";
    // For the second table
    public static final String WATCH_ID = "_id";
    public static final String WATCH_STOCK_SYMBOL = "symbol";
    public static final String WATCH_STOCK_NAME = "name";
    public static final String WATCH_STOCK_TARGET = "target"; // We will let the stock price target be a String and convert it later in the code.


    //Corresponding Indices
    public static final int INDEX_ID = 0;
    public static final int INDEX_STOCK_SYMBOL = 1;
    public static final int INDEX_STOCK_NAME = 2;

    // For the second table
    public static final int INDEX_STOCK_TARGET = 3;

    //used for logging
    private static final String TAG = "Stockest_DbAdapter";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "db_holder";
    private static final String TABLE_NAME = "stock_table";
    private static final String TABLE_NAME2= "watchlist_table";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    //SQL statement used to create the database
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY autoincrement, " +
                    COL_STOCK_SYMBOL + " TEXT, " +
                    COL_STOCK_NAME + " TEXT); ";

    // For the second table
    private static final String DATABASE_CREATE2 =
            "CREATE TABLE if not exists " + TABLE_NAME2 + " ( " +
                    WATCH_ID + " INTEGER PRIMARY KEY autoincrement, " +
                    WATCH_STOCK_SYMBOL + " TEXT, " +
                    WATCH_STOCK_NAME + " TEXT, " +
                    WATCH_STOCK_TARGET + " TEXT); ";


    public Database_Adapter(Context ctx) {
        this.mCtx = ctx;
    }

    //open
    public void open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
    }
    //close
    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    // Check if database table has been populated
    public boolean checkEmpty(){
        int NoOfRows = (int) DatabaseUtils.queryNumEntries(mDb,TABLE_NAME);
        return NoOfRows == 0 ;
    }

    //create an entry
    public void createEntry(String symbol, String name) {
        ContentValues values = new ContentValues();
        values.put(COL_STOCK_SYMBOL, symbol);
        values.put(COL_STOCK_NAME, name);
        mDb.insert(TABLE_NAME, null, values);
    }

    //create an entry for watchlist
    public void createEntryWatch(String symbol, String name, String target) {
        ContentValues values = new ContentValues();
        values.put(WATCH_STOCK_SYMBOL, symbol);
        values.put(WATCH_STOCK_NAME, name);
        values.put(WATCH_STOCK_TARGET,target);
        mDb.insert(TABLE_NAME2, null, values);
    }

    // read
    public Cursor fetchAll() {
        Cursor mCursor = mDb.query(TABLE_NAME, new String[]{COL_ID,
                        COL_STOCK_SYMBOL, COL_STOCK_NAME },
                null, null, null, null, null
        );
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    // read for watchlist
    public Cursor fetchAllWatch() {
        Cursor mCursor = mDb.query(TABLE_NAME2, new String[]{WATCH_ID,
                        WATCH_STOCK_SYMBOL, WATCH_STOCK_NAME, WATCH_STOCK_TARGET },
                null, null, null, null, null
        );
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    // detele a stock being watched
    public void deleteWatch(String symbol) {
        mDb.delete(TABLE_NAME2, WATCH_STOCK_SYMBOL + "=?", new String[]{symbol});
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            Log.w(TAG, DATABASE_CREATE2);
            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE2);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
            onCreate(db);
        }
    }

}
