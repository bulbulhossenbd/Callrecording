package com.pgizka.simplecallrecorder.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Pawe� on 2015-07-10.
 */
public class RecorderContract {

    public static final String CONTENT_AUTHORITY = "com.pgizka.simplecallrecorder";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_CONTACT = "contact";
    public static final String PATH_RECORD = "record";
    public static final String PATH_RECORD_WITH_CONTACT = "recordWithContact";
    public static final String PATH_CONTACT_COUNT_RECORDS = "contactCountRecords";
    //public static final String PATH_RECORD_WITH_CONTACT_WITH_SEPARATORS = "recordWithContactWithSeparators";

    public static final class ContactEntry implements BaseColumns {

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACT;

        // Table name
        public static final String TABLE_NAME = "contact";

        public static final String COLUMN_PHONE_NUMBER = "phone_number";
        public static final String COLUMN_RECORDED = "recorded";
        public static final String COLUMN_IGNORED = "ignored";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_CONTACT_ID = "contact_id";
        public static final String COLUMN_RECORD_NUMBER = "record_number";
    }

    public static final class RecordEntry implements BaseColumns{

        public static int TYPE_OUTGOING = 0;
        public static int TYPE_INCOMING = 1;

        public static int SOURCE_VOICE_CALL = 0;
        public static int SOURCE_DOWNLINK = 1;
        public static int SOURCE_UPLINK = 2;
        public static int SOURCE_MIC = 3;

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECORD;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECORD;


        public static final String TABLE_NAME = "record";

        public static final String COLUMN_CONTACT_KEY = "contact_id_fk";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_LENGTH = "length";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_SOURCE = "source";
        public static final String COLUMN_SOURCE_ERROR = "source_error";
    }

    public static final Uri getContentUri(String path){
        return BASE_CONTENT_URI.buildUpon().appendPath(path).build();
    }

    public static Uri buildRecordItem(Uri uri, long id) {
        return ContentUris.withAppendedId(uri, id);
    }

    public static final String ConactWithOneRecordWhere =
            RecorderContract.RecordEntry.TABLE_NAME +
            "." + RecorderContract.RecordEntry.COLUMN_DATE +
            " = " + "(SELECT MAX(" + RecorderContract.RecordEntry.COLUMN_DATE + ")" +
            " FROM " + RecorderContract.RecordEntry.TABLE_NAME +
            " WHERE " + RecorderContract.RecordEntry.TABLE_NAME +
            "." + RecorderContract.RecordEntry.COLUMN_CONTACT_KEY +
            " = " + RecorderContract.ContactEntry.TABLE_NAME +
            "." + RecorderContract.ContactEntry._ID + ")";

}
