package com.pgizka.simplecallrecorder.contacts;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;


public class ContactsFragment extends Fragment {
    ListView contactsList;
    TextView emptyText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactsList = (ListView) view.findViewById(R.id.contacts_list);
        emptyText = (TextView) view.findViewById(R.id.contacts_empty_text);
        contactsList.setEmptyView(emptyText);

        String order = RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME + ", " +
                        RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER;
        final Cursor mainCursor = getActivity().getContentResolver().query(
                RecorderContract.getContentUri(RecorderContract.PATH_RECORD_WITH_CONTACT),
                null, RecorderContract.ConactWithOneRecordWhere, null, order);
        Cursor recordCountCursor = getActivity().getContentResolver().query(
                RecorderContract.getContentUri(RecorderContract.PATH_CONTACT_COUNT_RECORDS),
                null, null, null, order);
        ContactsAdapter contactsAdapter = new ContactsAdapter(getActivity(), mainCursor, recordCountCursor);
        contactsList.setAdapter(contactsAdapter);

        contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mainCursor.moveToPosition(position);
                int ids = mainCursor.getInt(mainCursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_CONTACT_KEY));
                Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
                intent.setData(RecorderContract.buildRecordItem(
                        RecorderContract.getContentUri(RecorderContract.PATH_CONTACT), ids));
                getActivity().startActivity(intent);
            }
        });


        return view;
    }


}
