package com.pgizka.simplecallrecorder.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;

/**
 * Created by Paweł on 2015-07-20.
 */
public class NavigationDrawerListAdapter extends BaseAdapter {
    Context context;

    public NavigationDrawerListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 7;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.navigation_draver_item, parent, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.navigation_drawer_item_image);
        TextView textView = (TextView) view.findViewById(R.id.navigation_drawer_item_text);

        switch (position) {
            case 0:
                imageView.setImageResource(R.drawable.ic_action_record);
                textView.setText(R.string.title_section_recordings);
                break;
            case 1:
                imageView.setImageResource(R.drawable.ic_action_options);
                textView.setText(R.string.title_section_options);
                break;
            case 2:
                imageView.setImageResource(R.drawable.ic_action_contacts);
                textView.setText(R.string.title_section_contacts);
                break;
            case 3:
                view.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                break;
            case 4:
                view.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                break;
            case 5:
                view.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                break;
            case 6:
                imageView.setImageResource(R.drawable.ic_action_settings);
                textView.setText(R.string.title_section_settings);
                break;

        }
        return view;
    }

}
