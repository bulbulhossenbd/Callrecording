package com.pgizka.simplecallrecorder.contacts;


import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.Utils;

import java.io.InputStream;
import java.text.SimpleDateFormat;

/**
 * Created by Paweł on 2015-07-21.
 */
public class ContactsAdapter extends CursorAdapter {
    static final String TAG = ContactsAdapter.class.getSimpleName();
    Cursor recordCountCursor;

    public ContactsAdapter(Context context, Cursor mainCursor, Cursor recordCountCursor) {
        super(context, mainCursor);
        this.recordCountCursor = recordCountCursor;
    }

    public static class ViewHolder{
        ImageView mainImage;
        TextView displayNameText, phoneText, numberOfRecordsText, lastRecordDateText, separatorText;

        public ViewHolder(View view){
            mainImage = (ImageView) view.findViewById(R.id.contacts_item_main_image);
            displayNameText = (TextView) view.findViewById(R.id.contacts_item_display_name_text);
            phoneText = (TextView) view.findViewById(R.id.contacts_item_phone_text);
            numberOfRecordsText = (TextView) view.findViewById(R.id.contact_item_records_number_text);
            lastRecordDateText = (TextView) view.findViewById(R.id.contact_item_last_record_text);
            separatorText = (TextView) view.findViewById(R.id.contacts_item_separator_text);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.contacts_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String phoneNumber = cursor.getString(cursor.getColumnIndex(
                RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER));
        long date = cursor.getLong(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_DATE));
        String displayName = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME));
        String contactId = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_CONTACT_ID));

        if(displayName != null){
            viewHolder.displayNameText.setText(displayName);
            viewHolder.phoneText.setVisibility(View.VISIBLE);
            viewHolder.phoneText.setText(phoneNumber);
        } else {
            viewHolder.displayNameText.setText(phoneNumber);
            viewHolder.phoneText.setVisibility(View.GONE);
        }

        if(contactId != null && !TextUtils.isEmpty(contactId)){
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
            if(input != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                viewHolder.mainImage.setImageBitmap(bitmap);
            } else {
                viewHolder.mainImage.setImageResource(R.drawable.defult_contact_image);
            }
        } else {
            viewHolder.mainImage.setImageResource(R.drawable.defult_contact_image);
        }

        viewHolder.lastRecordDateText.setText(new SimpleDateFormat("kk:mm EEE dd MMM").format(date));

        recordCountCursor.moveToPosition(cursor.getPosition());
        int recordNumber = recordCountCursor.getInt(0);
        viewHolder.numberOfRecordsText.setText(String.format(context.getResources().
                getQuantityString(R.plurals.contact_recording_number, recordNumber, recordNumber)));


        if(displayName != null) {
            String separatorText = null;
            if(cursor.getPosition() == 0){
                separatorText = String.valueOf(displayName.charAt(0));
                viewHolder.separatorText.setVisibility(View.VISIBLE);
                viewHolder.separatorText.setText(separatorText);
            } else {
                cursor.moveToPosition(cursor.getPosition() - 1);
                String prevDisplayName = cursor.getString(cursor.getColumnIndex(
                        RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME));
                if (prevDisplayName == null || prevDisplayName.charAt(0) != displayName.charAt(0)) {
                    separatorText = String.valueOf(displayName.charAt(0));
                    viewHolder.separatorText.setVisibility(View.VISIBLE);
                    viewHolder.separatorText.setText(separatorText);
                } else {
                    viewHolder.separatorText.setVisibility(View.GONE);
                }
            }
        } else {
            viewHolder.separatorText.setVisibility(View.GONE);
        }
    }


}
