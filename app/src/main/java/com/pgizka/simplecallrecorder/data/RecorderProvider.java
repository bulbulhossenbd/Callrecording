package com.pgizka.simplecallrecorder.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class RecorderProvider extends ContentProvider {
    static final String TAG = RecorderProvider.class.getSimpleName();
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DBHelper dbHelper;

    static final int CONTACT_DIR = 100;
    static final int CONTACT_ITEM = 101;
    static final int RECORD_DIR = 200;
    static final int RECORD_ITEM = 201;

    static final int RECORD_WITH_CONTACT_ITEM = 300;
    static final int RECORD_WITH_CONTACT_DIR = 301;

    static final int CONTACT_COUNT_RECORDS = 400;

    //static final int RECORD_WITH_CONTACT_WITH_SEPARATORS_DIR = 302;

    //This is an inner join which looks like
    //record INNER JOIN contact ON record.column_contact_key = contact._id
    static final String recordWithContactJoin =
            RecorderContract.RecordEntry.TABLE_NAME + " INNER JOIN " +
                    RecorderContract.ContactEntry.TABLE_NAME +
                    " ON " + RecorderContract.RecordEntry.TABLE_NAME +
                    "." + RecorderContract.RecordEntry.COLUMN_CONTACT_KEY +
                    " = " + RecorderContract.ContactEntry.TABLE_NAME +
                    "." + RecorderContract.ContactEntry._ID;


    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RecorderContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, RecorderContract.PATH_CONTACT, CONTACT_DIR);
        matcher.addURI(authority, RecorderContract.PATH_CONTACT + "/#", CONTACT_ITEM);
        matcher.addURI(authority, RecorderContract.PATH_RECORD, RECORD_DIR);
        matcher.addURI(authority, RecorderContract.PATH_RECORD + "/#", RECORD_ITEM);
        matcher.addURI(authority, RecorderContract.PATH_RECORD_WITH_CONTACT, RECORD_WITH_CONTACT_DIR);
        matcher.addURI(authority, RecorderContract.PATH_RECORD_WITH_CONTACT + "/#", RECORD_WITH_CONTACT_ITEM);
        matcher.addURI(authority, RecorderContract.PATH_CONTACT_COUNT_RECORDS, CONTACT_COUNT_RECORDS);
        //matcher.addURI(authority, RecorderContract.PATH_RECORD_WITH_CONTACT_WITH_SEPARATORS,
        //											RECORD_WITH_CONTACT_WITH_SEPARATORS_DIR);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());

        /*
        ContentValues contactContectValues = new ContentValues();
        contactContectValues.put(RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER, 793773212);
        long id = dbHelper.getWritableDatabase().insert(
                RecorderContract.ContactEntry.TABLE_NAME, null, contactContectValues);
        ContentValues recordContentValues = new ContentValues();
        recordContentValues.put(RecorderContract.RecordEntry.COLUMN_CONTACT_KEY, id);
        recordContentValues.put(RecorderContract.RecordEntry.COLUMN_PATH, "nic");
        recordContentValues.put(RecorderContract.RecordEntry.COLUMN_DATE, System.currentTimeMillis());
        dbHelper.getWritableDatabase().insert(
                RecorderContract.RecordEntry.TABLE_NAME, null, recordContentValues);

        query(RecorderContract.getContentUri(RecorderContract.PATH_RECORD_WITH_CONTACT), null, null, null, null); */
        return false;
    }

    @Override
    public String getType(Uri uri) {
        return "nothing";
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String where;
        long id;
        int ret;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int code = sUriMatcher.match(uri);
        switch (code) {
            case CONTACT_DIR:
                where = (selection == null) ? "1" : selection;
                ret = db.delete(RecorderContract.ContactEntry.TABLE_NAME, where, selectionArgs);
                break;
            case CONTACT_ITEM:
                id = ContentUris.parseId(uri);
                where = RecorderContract.ContactEntry._ID +
                        " = " +
                        id +
                        (TextUtils.isEmpty(selection) ? "" : " and ( " + selection + " )");
                ret = db.delete(RecorderContract.ContactEntry.TABLE_NAME, where, selectionArgs);
                break;
            case RECORD_DIR:
                where = (selection == null) ? "1" : selection;
                ret = db.delete(RecorderContract.RecordEntry.TABLE_NAME, where, selectionArgs);
                break;
            case RECORD_ITEM:
                id = ContentUris.parseId(uri);
                where = RecorderContract.RecordEntry._ID +
                        " = " +
                        id +
                        (TextUtils.isEmpty(selection) ? "" : " and ( " + selection + " )");
                ret = db.delete(RecorderContract.RecordEntry.TABLE_NAME, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Illegal uri: " + uri);
        }

        if(ret>0) {
            // Notify that data for this uri has changed
            getContext().getContentResolver().notifyChange(uri, null);
        }
        Log.d(TAG, "deleted records: " + ret);

        return ret;
    }



    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int code = sUriMatcher.match(uri);
        Uri ret;
        long rowId;

        switch (code) {
            case CONTACT_DIR: {
                rowId = db.insert(RecorderContract.ContactEntry.TABLE_NAME, null, values);
                break;
            }
            case RECORD_DIR: {
                rowId = db.insert(RecorderContract.RecordEntry.TABLE_NAME, null, values);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Was insert successful?
        if (rowId != -1) {
            ret = ContentUris.withAppendedId(uri, rowId);
            Log.d(TAG, "inserted id: " + Long.toString(rowId));

            // Notify that data for this uri has changed
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }

        return ret;
    }



    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String groupBy = null, having = null;
        Cursor cursor;
        int code = sUriMatcher.match(uri);
        switch (code) {
            case CONTACT_DIR:
                queryBuilder.setTables(RecorderContract.ContactEntry.TABLE_NAME);
                break;
            case CONTACT_ITEM:
                queryBuilder.setTables(RecorderContract.ContactEntry.TABLE_NAME);
                queryBuilder.appendWhere(RecorderContract.ContactEntry._ID + " = " +
                        uri.getLastPathSegment());
                break;
            case RECORD_DIR:
                queryBuilder.setTables(RecorderContract.RecordEntry.TABLE_NAME);
                break;
            case RECORD_ITEM:
                queryBuilder.setTables(RecorderContract.RecordEntry.TABLE_NAME);
                queryBuilder.appendWhere(RecorderContract.RecordEntry._ID + " = " +
                        uri.getLastPathSegment());
                break;
            case RECORD_WITH_CONTACT_DIR:
                queryBuilder.setTables(recordWithContactJoin);
                break;
            case RECORD_WITH_CONTACT_ITEM:
                queryBuilder.setTables(recordWithContactJoin);
                queryBuilder.appendWhere(RecorderContract.RecordEntry.TABLE_NAME + "." + RecorderContract.RecordEntry._ID + " = " +
                        uri.getLastPathSegment());
                break;
            case CONTACT_COUNT_RECORDS:
                queryBuilder.setTables(recordWithContactJoin);
                String[] newProjection = {"COUNT(" + RecorderContract.RecordEntry.COLUMN_CONTACT_KEY + ")"};
                projection = newProjection;
                groupBy = RecorderContract.RecordEntry.COLUMN_CONTACT_KEY;
                break;
            //case RECORD_WITH_CONTACT_WITH_SEPARATORS_DIR:
            //	queryBuilder = recordWithContactQueryBuilder;
            //	break;
            default:
                break;
        }

        if(code == RECORD_WITH_CONTACT_DIR || code == RECORD_WITH_CONTACT_ITEM
				/*|| code == RECORD_WITH_CONTACT_WITH_SEPARATORS_DIR*/){
            String [] newProjection = {RecorderContract.RecordEntry.TABLE_NAME +
                    "." + RecorderContract.RecordEntry._ID,
                    RecorderContract.RecordEntry.COLUMN_CONTACT_KEY,
                    RecorderContract.RecordEntry.COLUMN_PATH,
                    RecorderContract.RecordEntry.COLUMN_LENGTH,
                    RecorderContract.RecordEntry.COLUMN_DATE,
                    RecorderContract.RecordEntry.COLUMN_TYPE,
                    RecorderContract.RecordEntry.COLUMN_NOTES,
                    RecorderContract.RecordEntry.COLUMN_SOURCE,
                    RecorderContract.RecordEntry.COLUMN_SOURCE_ERROR,
                    RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER,
                    RecorderContract.ContactEntry.COLUMN_RECORDED,
                    RecorderContract.ContactEntry.COLUMN_IGNORED,
                    RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME,
                    RecorderContract.ContactEntry.COLUMN_CONTACT_ID,};
            projection = newProjection;
        }

        cursor = queryBuilder.query(
                dbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                groupBy,
                having,
                sortOrder);


		/*
		if(code != RECORD_DIR && code != RECORD_ITEM){
			String string = new String();
			for(String current : cursor.getColumnNames()){
				string += current + " ";
			}

			Log.d("RecorderProvider", string);
			Log.d("RecorderProvider", "id column index is: " +
					cursor.getColumnIndex(RecorderContract.RecordEntry._ID));
		}

		if(code == RECORD_WITH_CONTACT_DIR || code == RECORD_WITH_CONTACT_ITEM
				|| code == RECORD_WITH_CONTACT_WITH_SEPARATORS_DIR){
			MatrixCursor matrixCursor = new MatrixCursor(columnNames)


		}*/

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        String where;
        long id;
        int ret;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int code = sUriMatcher.match(uri);
        switch (code) {
            case CONTACT_DIR:
                where = selection;
                ret = db.update(RecorderContract.ContactEntry.TABLE_NAME, values, where, selectionArgs);
                break;
            case CONTACT_ITEM:
                id = ContentUris.parseId(uri);
                where = RecorderContract.ContactEntry._ID +
                        " = " +
                        id +
                        (TextUtils.isEmpty(selection) ? "" : " and ( " + selection + " )");
                ret = db.update(RecorderContract.ContactEntry.TABLE_NAME, values, where, selectionArgs);
                break;
            case RECORD_DIR:
                where = (selection == null) ? "1" : selection;
                ret = db.update(RecorderContract.RecordEntry.TABLE_NAME, values, where, selectionArgs);
                break;
            case RECORD_ITEM:
                id = ContentUris.parseId(uri);
                where = RecorderContract.RecordEntry._ID +
                        " = " +
                        id +
                        (TextUtils.isEmpty(selection) ? "" : " and ( " + selection + " )");
                ret = db.update(RecorderContract.RecordEntry.TABLE_NAME, values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Illegal uri: " + uri);
        }

        if (ret > 0) {
            // Notify that data for this uri has changed
            getContext().getContentResolver().notifyChange(uri, null);
            Log.d(TAG, "updated records: " + ret);
        }

        return ret;
    }
}
