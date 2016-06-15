package com.pgizka.simplecallrecorder.contacts;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.Utils;

/**
 * Created by Paweł on 2015-07-21.
 */
public class ContactDetailAdapter extends CursorAdapter {

    public ContactDetailAdapter(Context context, Cursor c) {
        super(context, c);
    }

    public class ViewHolder{
        TextView dateText, durationText;
        ImageView callImage;

        public ViewHolder(View view){
            dateText = (TextView) view.findViewById(R.id.contact_detail_item_date_text);
            durationText = (TextView) view.findViewById(R.id.contact_detail_item_duration_text);
            callImage = (ImageView) view.findViewById(R.id.contact_detail_item_call_image);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_detail_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        long date = cursor.getLong(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_DATE));
        int duration = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_LENGTH));
        int type = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_TYPE));

        viewHolder.dateText.setText(Utils.formatTime(date) + " " + Utils.formatDate(date));
        viewHolder.durationText.setText(Utils.formatDuration(duration));

        if(type == RecorderContract.RecordEntry.TYPE_OUTGOING){
            viewHolder.callImage.setImageResource(R.drawable.call_arrow_outgoing);
        } else {
            viewHolder.callImage.setImageResource(R.drawable.call_arrow_incoming);
        }

    }

}
