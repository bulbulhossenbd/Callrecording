package com.pgizka.simplecallrecorder.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Pawe� on 2015-07-10.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "callRecorder.db";
    public static final int DB_VERSION = 8;


    public DBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String contact = "CREATE TABLE " + RecorderContract.ContactEntry.TABLE_NAME + " ( " +
                RecorderContract.ContactEntry._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER + " TEXT NOT NULL, " +
                RecorderContract.ContactEntry.COLUMN_RECORDED + " INTEGER, " +
                RecorderContract.ContactEntry.COLUMN_IGNORED + " INTEGER, " +
                RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME + " TEXT, " +
                RecorderContract.ContactEntry.COLUMN_CONTACT_ID + " TEXT, " +
                RecorderContract.ContactEntry.COLUMN_RECORD_NUMBER + " INTEGER )";

        String record = "CREATE TABLE " + RecorderContract.RecordEntry.TABLE_NAME + " ( " +
                RecorderContract.RecordEntry._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                RecorderContract.RecordEntry.COLUMN_CONTACT_KEY + " INTEGER NOT NULL, " +
                RecorderContract.RecordEntry.COLUMN_PATH + " TEXT, " +
                RecorderContract.RecordEntry.COLUMN_LENGTH + " INTEGER, " +
                RecorderContract.RecordEntry.COLUMN_DATE + " LONG, " +
                RecorderContract.RecordEntry.COLUMN_TYPE + " INTEGER, " +
                RecorderContract.RecordEntry.COLUMN_NOTES + " TEXT, " +
                RecorderContract.RecordEntry.COLUMN_SOURCE + " INTEGER, " +
                RecorderContract.RecordEntry.COLUMN_SOURCE_ERROR + " INTEGER, " +
                " FOREIGN KEY (" + RecorderContract.RecordEntry.COLUMN_CONTACT_KEY + ") REFERENCES " +
                RecorderContract.ContactEntry.TABLE_NAME + " (" + RecorderContract.ContactEntry._ID + "))";



        db.execSQL(contact);
        db.execSQL(record);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS " + RecorderContract.RecordEntry.TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS " + RecorderContract.ContactEntry.TABLE_NAME);

        db.execSQL("ALTER TABLE " + RecorderContract.RecordEntry.TABLE_NAME + " ADD COLUMN " +
                RecorderContract.RecordEntry.COLUMN_SOURCE + " INTEGER ");
        db.execSQL("ALTER TABLE " + RecorderContract.RecordEntry.TABLE_NAME + " ADD COLUMN " +
                RecorderContract.RecordEntry.COLUMN_SOURCE_ERROR + " INTEGER ");
        //db.execSQL("ALTER TABLE " + RecorderContract.ContactEntry.TABLE_NAME + " ADD COLUMN " +
        //        RecorderContract.ContactEntry.COLUMN_CONTACT_ID + " TEXT ");
        //db.execSQL("ALTER TABLE " + RecorderContract.ContactEntry.TABLE_NAME + " ADD COLUMN " +
        //        RecorderContract.ContactEntry.COLUMN_RECORD_NUMBER + " INTEGER ");

        //onCreate(db);
    }

}
