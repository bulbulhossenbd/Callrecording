package com.pgizka.simplecallrecorder.options;

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
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;

import java.io.InputStream;

/**
 * Created by Paweł on 2015-07-20.
 */
public class OptionsListAdapter extends CursorAdapter {
    static final String TAG = OptionsListAdapter.class.getSimpleName();
    OnDeleteButtonClickListener clickListener;
    ListView listView;

    public OptionsListAdapter(Context context, Cursor c, OnDeleteButtonClickListener listener, ListView listView) {
        super(context, c);
        clickListener = listener;
        this.listView = listView;
    }

    static class ViewHolder{
        ImageView mainImage;
        TextView displayNameText, phoneText;
        Button deleteButton;

        public ViewHolder(View view){
            mainImage = (ImageView) view.findViewById(R.id.options_item_main_image);
            displayNameText = (TextView) view.findViewById(R.id.options_item_display_name_text);
            phoneText = (TextView) view.findViewById(R.id.options_item_phone_text);
            deleteButton = (Button) view.findViewById(R.id.options_item_delete_button);
        }

    }

    @Override
    public View newView(Context context, final Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.options_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    cursor.moveToPosition(listView.getPositionForView(viewHolder.deleteButton));
                    int id = cursor.getInt(cursor.getColumnIndex(RecorderContract.ContactEntry._ID));
                    clickListener.onClick(id);
                }
            }
        });

        String phoneNumber = cursor.getString(cursor.getColumnIndex(
                RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER));
        String displayName = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME));
        String contactId = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_CONTACT_ID));

        if (displayName != null) {
            viewHolder.displayNameText.setText(displayName);
            viewHolder.phoneText.setVisibility(View.VISIBLE);
            viewHolder.phoneText.setText(phoneNumber);
        } else {
            viewHolder.displayNameText.setText(phoneNumber);
            viewHolder.phoneText.setVisibility(View.GONE);
        }

        if (contactId != null && !TextUtils.isEmpty(contactId)) {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
            if (input != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                viewHolder.mainImage.setImageBitmap(bitmap);
            } else {
                viewHolder.mainImage.setImageResource(R.drawable.defult_contact_image);
            }
        } else {
            viewHolder.mainImage.setImageResource(R.drawable.defult_contact_image);


        }
    }

    public static interface OnDeleteButtonClickListener{
        public void onClick(int id);
    }

}
