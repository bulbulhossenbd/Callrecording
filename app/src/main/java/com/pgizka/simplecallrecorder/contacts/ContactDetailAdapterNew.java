package com.pgizka.simplecallrecorder.contacts;

import android.content.Context;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Paweł on 2015-07-28.
 */
public class ContactDetailAdapterNew extends CursorAdapter {

    SparseBooleanArray selected;
    ArrayList<Long> selectedIds = new ArrayList<>();

    public ContactDetailAdapterNew(Context context, Cursor c) {
        super(context, c);
        selected = new SparseBooleanArray(c.getCount());
    }

    public class ViewHolder{
        public ImageView callImage;
        public TextView timeText;
        public TextView durationText;
        public TextView separatorText;
        public RelativeLayout separator;
        public RelativeLayout mainLayout;

        public ViewHolder(View view){
            timeText = (TextView) view.findViewById(R.id.contact_detail_item_date_text);
            durationText = (TextView) view.findViewById(R.id.contact_detail_item_duration_text);
            callImage = (ImageView) view.findViewById(R.id.contact_detail_item_call_image);
            separator = (RelativeLayout) view.findViewById(R.id.contact_item_separator);
            separatorText = (TextView) view.findViewById(R.id.contact_item_separator_text);
            mainLayout = (RelativeLayout) view.findViewById(R.id.contact_item_main_layout);
        }

    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_detail_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.separator.setVisibility(View.GONE);

        int duration = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_LENGTH));
        int type = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_TYPE));
        String path = cursor.getString(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_PATH));
        long date = cursor.getLong(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_DATE));

        viewHolder.timeText.setText(Utils.formatTime(date));
        viewHolder.durationText.setText(Utils.formatDuration(duration));

        if(type == RecorderContract.RecordEntry.TYPE_INCOMING){
            viewHolder.callImage.setImageResource(R.drawable.call_arrow_incoming);
        } else {
            viewHolder.callImage.setImageResource(R.drawable.call_arrow_outgoing);
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
