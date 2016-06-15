package com.pgizka.simplecallrecorder.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.pgizka.simplecallrecorder.data.RecorderContract;

/**
 * Created by Paweł on 2015-07-20.
 */
public class UpdateUserData extends AsyncTask<Void, Void, Void> {
    Context context;

    public UpdateUserData(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String[] projection = {RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER,
                                RecorderContract.ContactEntry._ID};
        Cursor cursor = context.getContentResolver().query(RecorderContract.getContentUri(
                RecorderContract.PATH_CONTACT), projection, null, null, null);

        while (cursor.moveToNext()){
            String phoneNumber = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER));
            int id = cursor.getInt(cursor.getColumnIndex(RecorderContract.ContactEntry._ID));
            Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber));
            Cursor contactCursor = context.getContentResolver().query(
                    contactUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                            ContactsContract.PhoneLookup._ID}, null, null, null);
            if(contactCursor.moveToFirst()){
                String displayName = contactCursor.getString(contactCursor.getColumnIndex(
                        ContactsContract.PhoneLookup.DISPLAY_NAME));
                String contactId = contactCursor.getString(contactCursor.getColumnIndex(
                        ContactsContract.PhoneLookup._ID));

                ContentValues contentValues = new ContentValues();
                if(displayName != null) {
                    contentValues.put(RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME, displayName);
                }
                if(contactId != null) {
                    contentValues.put(RecorderContract.ContactEntry.COLUMN_CONTACT_ID, contactId);
                }
                Uri uri = RecorderContract.buildRecordItem(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT), id);
                context.getContentResolver().update(uri, contentValues, null, null);
            }
        }

        return null;
    }

}
