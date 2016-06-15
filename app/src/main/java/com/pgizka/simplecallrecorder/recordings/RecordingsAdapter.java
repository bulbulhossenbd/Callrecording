package com.pgizka.simplecallrecorder.recordings;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.Utils;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Pawe� on 2015-07-11.
 */
public class RecordingsAdapter extends CursorAdapter {
    static final String TAG = RecordingsAdapter.class.getSimpleName();

    SparseBooleanArray selected;
    ArrayList<Long> selectedIds = new ArrayList<>();

    public RecordingsAdapter(Context context, Cursor c) {
        super(context, c);
        selected = new SparseBooleanArray(c.getCount());
    }

    public static class ViewHolder{
        ImageView contactImage;
        ImageView callImage;
        TextView displayNameText;
        TextView phoneText;
        TextView timeText;
        TextView durationText;
        TextView separatorText;
        RelativeLayout separator;
        RelativeLayout mainLayout;

        public ViewHolder(View view){
            contactImage = (ImageView) view.findViewById(R.id.recording_item_contact_image);
            callImage = (ImageView) view.findViewById(R.id.recording_item_call_image);
            displayNameText = (TextView) view.findViewById(R.id.recording_item_display_name_text);
            phoneText = (TextView) view.findViewById(R.id.recording_item_phone_text);
            timeText = (TextView) view.findViewById(R.id.recording_item_time_text);
            durationText = (TextView) view.findViewById(R.id.recording_item_duration_text);
            separatorText = (TextView) view.findViewById(R.id.recordins_item_separator_text);
            separator = (RelativeLayout) view.findViewById(R.id.recordings_item_separator);
            mainLayout = (RelativeLayout) view.findViewById(R.id.recording_item_main_layout);
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.recordings_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.separator.setVisibility(View.GONE);

        String phoneNumber = cursor.getString(cursor.getColumnIndex(
                RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER));
        int duration = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_LENGTH));
        int type = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_TYPE));
        String path = cursor.getString(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_PATH));
        long date = cursor.getLong(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_DATE));
        String displayName = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME));
        String contactId = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_CONTACT_ID));

        viewHolder.timeText.setText(Utils.formatTime(date));
        viewHolder.durationText.setText(Utils.formatDuration(duration));

        if(type == RecorderContract.RecordEntry.TYPE_INCOMING){
            viewHolder.callImage.setImageResource(R.drawable.call_arrow_incoming);
        } else {
            viewHolder.callImage.setImageResource(R.drawable.call_arrow_outgoing);
        }

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
                viewHolder.contactImage.setImageBitmap(bitmap);
            } else {
                viewHolder.contactImage.setImageResource(R.drawable.defult_contact_image);
            }
        } else {
            viewHolder.contactImage.setImageResource(R.drawable.defult_contact_image);
        }

        if(cursor.getPosition() == 0){
            viewHolder.separator.setVisibility(View.VISIBLE);
            int currentDay = new Date(date).getDay();
            int todayDay = new Date(System.currentTimeMillis()).getDay();
            if(currentDay == todayDay){
                viewHolder.separatorText.setText("Dzisiaj");
            } else if((currentDay+1) == todayDay){
                viewHolder.separatorText.setText("Wczoraj");
            } else {
                viewHolder.separatorText.setText(Utils.formatDate(date));
            }
        } else {
            cursor.moveToPosition(cursor.getPosition() - 1);
            long prevDate = cursor.getLong(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_DATE));
            Date currentDate = new Date(date);
            int currentDay = currentDate.getDay();
            int currentMonth = currentDate.getMonth();
            Date previousDate = new Date(prevDate);
            int previousDay = previousDate.getDay();
            int previousMonth = previousDate.getMonth();

            if(previousMonth != currentMonth || previousDay != currentDay){
                int todayDay = new Date(System.currentTimeMillis()).getDay();
                viewHolder.separator.setVisibility(View.VISIBLE);
                if((currentDay+1) == todayDay){
                    viewHolder.separatorText.setText("Wczoraj");
                } else {
                    viewHolder.separatorText.setText(Utils.formatDate(date));
                }
            }

            cursor.moveToPosition(cursor.getPosition() + 1);
        }

        if(selected.get(cursor.getPosition())){
            viewHolder.mainLayout.setBackgroundResource(R.color.itemSelected);
        } else {
            viewHolder.mainLayout.setBackgroundResource(R.color.itemUnSelected);
        }

    }

    public void setItemSelected(int position, long id, boolean value){
        if(!selected.get(position) && value){
            selectedIds.add(id);
        } else if(selected.get(position) && !value){
            selectedIds.remove(id);
        }
        selected.put(position, value);
        notifyDataSetChanged();
    }

    public boolean getItemSelected(int position){
        return selected.get(position);
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    public ArrayList<Long> getSelectedIds() {
        return selectedIds;
    }

    public void clearSelected(){
        selected.clear();
        selectedIds.clear();
        notifyDataSetChanged();
    }

}
